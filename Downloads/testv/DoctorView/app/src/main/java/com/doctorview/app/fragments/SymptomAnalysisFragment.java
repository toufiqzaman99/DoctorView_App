package com.doctorview.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.doctorview.app.R;
import com.doctorview.app.models.Condition;
import com.doctorview.app.utils.AppUtils;
import com.doctorview.app.utils.SymptomAnalyzer;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Symptom Analysis: the user taps the symptoms they have, and a local
 * rule base (no machine learning) shows the most likely conditions
 * with simple advice. Educational only — always recommends a doctor.
 */
public class SymptomAnalysisFragment extends Fragment {

    // Maximum number of result cards shown
    private static final int MAX_RESULTS = 5;

    private ChipGroup chipGroupSymptoms;
    private ViewGroup resultsContainer;
    private TextView tvResultsTitle;
    private NestedScrollView scrollContainer;
    private TextView tvEmergencyWarning;

    public SymptomAnalysisFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_symptom_analysis, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chipGroupSymptoms = view.findViewById(R.id.chipGroupSymptoms);
        resultsContainer = view.findViewById(R.id.resultsContainer);
        tvResultsTitle = view.findViewById(R.id.tvResultsTitle);
        scrollContainer = view.findViewById(R.id.scrollContainer);
        tvEmergencyWarning = view.findViewById(R.id.tvEmergencyWarning);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        buildSymptomChips();

        view.findViewById(R.id.btnAnalyze).setOnClickListener(v -> analyze());
    }

    /** Creates one selectable chip per symptom in the rule base. */
    private void buildSymptomChips() {
        for (String symptom : SymptomAnalyzer.ALL_SYMPTOMS) {
            chipGroupSymptoms.addView(AppUtils.createFilterChip(requireContext(), symptom));
        }
    }

    /** Collects the checked chips and runs the rule-based analysis. */
    private void analyze() {
        List<String> selected = new ArrayList<>();
        for (int id : chipGroupSymptoms.getCheckedChipIds()) {
            Chip chip = chipGroupSymptoms.findViewById(id);
            if (chip != null) {
                selected.add(chip.getText().toString());
            }
        }

        if (selected.size() < 2) {
            AppUtils.showToast(requireContext(), R.string.select_at_least_two);
            return;
        }

        List<SymptomAnalyzer.Match> matches = SymptomAnalyzer.analyze(selected);
        renderResults(matches);

        // Scroll down so the results are visible
        scrollContainer.post(() -> scrollContainer.smoothScrollTo(0, resultsContainer.getTop()));
    }

    /** Fills the results container with one card per matched condition. */
    private void renderResults(List<SymptomAnalyzer.Match> matches) {
        resultsContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        boolean hasEmergency = false;

        if (matches.isEmpty()) {
            View card = inflater.inflate(R.layout.item_analysis_result, resultsContainer, false);
            ((TextView) card.findViewById(R.id.tvResultName)).setText(R.string.no_match_found);
            card.findViewById(R.id.tvSeverity).setVisibility(View.GONE);
            card.findViewById(R.id.tvMatchPercent).setVisibility(View.GONE);
            card.findViewById(R.id.tvResultAdvice).setVisibility(View.GONE);
            resultsContainer.addView(card);
        } else {
            int count = Math.min(matches.size(), MAX_RESULTS);
            for (int i = 0; i < count; i++) {
                SymptomAnalyzer.Match match = matches.get(i);
                Condition condition = match.getCondition();
                if (Condition.SEVERITY_EMERGENCY.equals(condition.getSeverity())) {
                    hasEmergency = true;
                }

                View card = inflater.inflate(R.layout.item_analysis_result, resultsContainer, false);
                ((TextView) card.findViewById(R.id.tvResultName)).setText(condition.getName());

                TextView tvSeverity = card.findViewById(R.id.tvSeverity);
                tvSeverity.setText(severityLabel(condition.getSeverity()));
                tvSeverity.setBackgroundResource(severityBackground(condition.getSeverity()));
                tvSeverity.setTextColor(getResources().getColor(
                        severityTextColor(condition.getSeverity()), null));

                ((TextView) card.findViewById(R.id.tvMatchPercent)).setText(getString(
                        R.string.symptoms_match, match.getMatchedCount(),
                        condition.getSymptoms().size(), match.getPercent()));
                ((TextView) card.findViewById(R.id.tvResultAdvice)).setText(condition.getAdvice());

                // Emergency conditions get a soft red card
                if (Condition.SEVERITY_EMERGENCY.equals(condition.getSeverity())
                        && card instanceof MaterialCardView) {
                    ((MaterialCardView) card).setCardBackgroundColor(
                            getResources().getColor(R.color.status_cancelled_bg, null));
                    ((MaterialCardView) card).setStrokeColor(
                            getResources().getColor(R.color.error, null));
                }

                resultsContainer.addView(card);
            }
        }

        // Soft red warning banner when an emergency-level condition matched
        tvEmergencyWarning.setVisibility(hasEmergency ? View.VISIBLE : View.GONE);

        tvResultsTitle.setVisibility(View.VISIBLE);
        resultsContainer.setVisibility(View.VISIBLE);
    }

    // ===== Severity → label / colors =====

    private int severityLabel(String severity) {
        switch (severity) {
            case Condition.SEVERITY_MEDIUM:
                return R.string.severity_medium;
            case Condition.SEVERITY_HIGH:
                return R.string.severity_high;
            case Condition.SEVERITY_EMERGENCY:
                return R.string.severity_emergency;
            default:
                return R.string.severity_low;
        }
    }

    private int severityBackground(String severity) {
        switch (severity) {
            case Condition.SEVERITY_MEDIUM:
                return R.drawable.bg_status_pending;
            case Condition.SEVERITY_HIGH:
            case Condition.SEVERITY_EMERGENCY:
                return R.drawable.bg_status_cancelled;
            default:
                return R.drawable.bg_status_confirmed;
        }
    }

    private int severityTextColor(String severity) {
        switch (severity) {
            case Condition.SEVERITY_MEDIUM:
                return R.color.status_pending_text;
            case Condition.SEVERITY_HIGH:
            case Condition.SEVERITY_EMERGENCY:
                return R.color.status_cancelled_text;
            default:
                return R.color.status_confirmed_text;
        }
    }
}

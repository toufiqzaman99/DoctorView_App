package com.doctorview.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.doctorview.app.R;
import com.doctorview.app.firebase.FirebaseHelper;
import com.doctorview.app.models.Disease;
import com.doctorview.app.utils.Constants;

import java.util.List;

/**
 * Disease Details: full information page for one disease —
 * overview, symptoms, causes, prevention and treatment.
 */
public class DiseaseDetailsFragment extends Fragment {

    private View contentContainer;
    private ProgressBar progressBar;
    private TextView tvNotFound;
    private Disease disease;

    public DiseaseDetailsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_disease_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        contentContainer = view.findViewById(R.id.contentContainer);
        progressBar = view.findViewById(R.id.progressBar);
        tvNotFound = view.findViewById(R.id.tvNotFound);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        String diseaseId = getArguments() != null ? getArguments().getString("diseaseId") : null;
        if (diseaseId == null || diseaseId.isEmpty()) {
            showNotFound();
        } else {
            loadDisease(diseaseId);
        }
    }

    /** Reads the disease document from Firestore. */
    private void loadDisease(String diseaseId) {
        showLoading();

        FirebaseHelper.getFirestore()
                .collection(Constants.COLLECTION_DISEASES)
                .document(diseaseId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!isAdded()) {
                        return;
                    }
                    if (doc.exists()) {
                        disease = doc.toObject(Disease.class);
                        if (disease != null) {
                            disease.setId(doc.getId());
                            populateViews();
                            showContent();
                        } else {
                            showNotFound();
                        }
                    } else {
                        showNotFound();
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        showNotFound();
                    }
                });
    }

    /** Fills the screen with the loaded disease's data. */
    private void populateViews() {
        TextView tvName = contentContainer.findViewById(R.id.tvDiseaseName);
        TextView tvCategory = contentContainer.findViewById(R.id.tvDiseaseCategory);
        TextView tvOverview = contentContainer.findViewById(R.id.tvDiseaseOverview);
        TextView tvSymptoms = contentContainer.findViewById(R.id.tvDiseaseSymptoms);
        TextView tvCauses = contentContainer.findViewById(R.id.tvDiseaseCauses);
        TextView tvPrevention = contentContainer.findViewById(R.id.tvDiseasePrevention);
        TextView tvTreatment = contentContainer.findViewById(R.id.tvDiseaseTreatment);

        tvName.setText(disease.getName());
        tvCategory.setText(disease.getCategory());
        tvOverview.setText(disease.getOverview());
        tvSymptoms.setText(bulletList(disease.getSymptoms()));
        tvCauses.setText(bulletList(disease.getCauses()));
        tvPrevention.setText(bulletList(disease.getPrevention()));
        tvTreatment.setText(bulletList(disease.getTreatment()));
    }

    /** Renders a list of strings as "• item\n• item\n…" */
    private String bulletList(List<String> items) {
        if (items == null || items.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (String item : items) {
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append("• ").append(item);
        }
        return builder.toString();
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        contentContainer.setVisibility(View.GONE);
        tvNotFound.setVisibility(View.GONE);
    }

    private void showContent() {
        progressBar.setVisibility(View.GONE);
        contentContainer.setVisibility(View.VISIBLE);
        tvNotFound.setVisibility(View.GONE);
    }

    private void showNotFound() {
        progressBar.setVisibility(View.GONE);
        contentContainer.setVisibility(View.GONE);
        tvNotFound.setVisibility(View.VISIBLE);
    }
}

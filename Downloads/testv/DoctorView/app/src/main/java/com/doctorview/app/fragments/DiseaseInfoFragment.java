package com.doctorview.app.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.doctorview.app.R;
import com.doctorview.app.adapters.DiseaseAdapter;
import com.doctorview.app.firebase.FirebaseHelper;
import com.doctorview.app.models.Disease;
import com.doctorview.app.utils.AppUtils;
import com.doctorview.app.utils.Constants;
import com.doctorview.app.utils.SampleData;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

/**
 * Disease Information: a searchable library of common diseases
 * loaded from Cloud Firestore. One tap seeds sample diseases.
 */
public class DiseaseInfoFragment extends Fragment {

    private final List<Disease> diseases = new ArrayList<>();

    private RecyclerView rvDiseases;
    private TextInputEditText etSearch;
    private ProgressBar progressBar;
    private View emptyContainer;
    private TextView tvEmptyTitle;
    private TextView tvEmptyText;
    private Button btnLoadSamples;
    private Button btnRetry;
    private DiseaseAdapter adapter;

    public DiseaseInfoFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_disease_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvDiseases = view.findViewById(R.id.rvDiseases);
        etSearch = view.findViewById(R.id.etSearch);
        progressBar = view.findViewById(R.id.progressBar);
        emptyContainer = view.findViewById(R.id.emptyContainer);
        tvEmptyTitle = view.findViewById(R.id.tvEmptyTitle);
        tvEmptyText = view.findViewById(R.id.tvEmptyText);
        btnLoadSamples = view.findViewById(R.id.btnLoadSamples);
        btnRetry = view.findViewById(R.id.btnRetry);

        rvDiseases.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new DiseaseAdapter(diseases, disease -> {
            Bundle args = new Bundle();
            args.putString("diseaseId", disease.getId());
            Navigation.findNavController(view)
                    .navigate(R.id.action_diseaseInfoFragment_to_diseaseDetailsFragment, args);
        });
        rvDiseases.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });

        btnLoadSamples.setOnClickListener(v -> seedSampleDiseases());
        btnRetry.setOnClickListener(v -> loadDiseases());

        loadDiseases();
    }

    /** Loads all diseases from Firestore, ordered by name. */
    private void loadDiseases() {
        showLoading();

        FirebaseHelper.getFirestore()
                .collection(Constants.COLLECTION_DISEASES)
                .orderBy("name")
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!isAdded()) {
                        return;
                    }
                    diseases.clear();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Disease disease = doc.toObject(Disease.class);
                        disease.setId(doc.getId());
                        diseases.add(disease);
                    }
                    adapter.setDiseases(diseases);
                    adapter.filter(etSearch.getText().toString());

                    if (diseases.isEmpty()) {
                        showEmptyState(R.string.empty_diseases_title,
                                R.string.empty_diseases_text, true);
                    } else {
                        showList();
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        showEmptyState(R.string.error_load_diseases_title,
                                R.string.error_load_diseases_text, false);
                    }
                });
    }

    /** Writes the sample diseases into Firestore (one tap, for the demo). */
    private void seedSampleDiseases() {
        showLoading();

        WriteBatch batch = FirebaseHelper.getFirestore().batch();
        for (Disease disease : SampleData.getSampleDiseases()) {
            batch.set(FirebaseHelper.getFirestore()
                    .collection(Constants.COLLECTION_DISEASES)
                    .document(), disease);
        }
        batch.commit()
                .addOnSuccessListener(a -> {
                    AppUtils.showToast(requireContext(), R.string.diseases_loaded);
                    loadDiseases();
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        showEmptyState(R.string.error_load_diseases_title,
                                R.string.error_load_diseases_text, false);
                    }
                });
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        rvDiseases.setVisibility(View.GONE);
        emptyContainer.setVisibility(View.GONE);
    }

    private void showList() {
        progressBar.setVisibility(View.GONE);
        rvDiseases.setVisibility(View.VISIBLE);
        emptyContainer.setVisibility(View.GONE);
    }

    private void showEmptyState(int titleRes, int textRes, boolean showSeedButton) {
        progressBar.setVisibility(View.GONE);
        rvDiseases.setVisibility(View.GONE);
        emptyContainer.setVisibility(View.VISIBLE);
        tvEmptyTitle.setText(titleRes);
        tvEmptyText.setText(textRes);
        btnLoadSamples.setVisibility(showSeedButton ? View.VISIBLE : View.GONE);
        btnRetry.setVisibility(showSeedButton ? View.GONE : View.VISIBLE);
    }
}

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
import com.doctorview.app.adapters.DoctorAdapter;
import com.doctorview.app.firebase.FirebaseHelper;
import com.doctorview.app.models.Doctor;
import com.doctorview.app.utils.AppUtils;
import com.doctorview.app.utils.Constants;
import com.doctorview.app.utils.SampleData;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

/**
 * Doctors tab: loads the doctor list from Cloud Firestore with a search box.
 * If the collection is empty, one tap loads 8 sample doctors for the demo.
 */
public class DoctorsFragment extends Fragment {

    private final List<Doctor> doctors = new ArrayList<>();

    private RecyclerView rvDoctors;
    private TextInputEditText etSearch;
    private ProgressBar progressBar;
    private View emptyContainer;
    private TextView tvEmptyTitle;
    private TextView tvEmptyText;
    private Button btnLoadSamples;
    private Button btnRetry;
    private DoctorAdapter adapter;

    public DoctorsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_doctors, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvDoctors = view.findViewById(R.id.rvDoctors);
        etSearch = view.findViewById(R.id.etSearch);
        progressBar = view.findViewById(R.id.progressBar);
        emptyContainer = view.findViewById(R.id.emptyContainer);
        tvEmptyTitle = view.findViewById(R.id.tvEmptyTitle);
        tvEmptyText = view.findViewById(R.id.tvEmptyText);
        btnLoadSamples = view.findViewById(R.id.btnLoadSamples);
        btnRetry = view.findViewById(R.id.btnRetry);

        rvDoctors.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new DoctorAdapter(doctors, new DoctorAdapter.OnDoctorClickListener() {
            @Override
            public void onDoctorClick(Doctor doctor) {
                // Open Doctor Details with the clicked doctor's document id
                Bundle args = new Bundle();
                args.putString("doctorId", doctor.getId());
                Navigation.findNavController(view)
                        .navigate(R.id.action_doctorsFragment_to_doctorDetailsFragment, args);
            }

            @Override
            public void onConsultClick(Doctor doctor) {
                // Consult opens the booking screen directly
                Bundle args = new Bundle();
                args.putString("doctorId", doctor.getId());
                args.putString("doctorName", doctor.getName());
                Navigation.findNavController(view)
                        .navigate(R.id.action_doctorsFragment_to_bookAppointmentFragment, args);
            }
        });
        rvDoctors.setAdapter(adapter);

        // Specialty filter chips
        ChipGroup chipGroupSpecialty = view.findViewById(R.id.chipGroupSpecialty);
        for (int i = 0; i < AppUtils.SPECIALTIES.length; i++) {
            Chip chip = AppUtils.createFilterChip(requireContext(), AppUtils.SPECIALTIES[i]);
            chip.setChecked(i == 0);
            chipGroupSpecialty.addView(chip);
        }
        chipGroupSpecialty.setSingleSelection(true);
        chipGroupSpecialty.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip chip = group.findViewById(checkedIds.get(0));
                if (chip != null) {
                    adapter.setSpecialtyFilter(chip.getText().toString());
                    adapter.filter(etSearch.getText().toString());
                }
            }
        });

        // Live search as the user types
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

        btnLoadSamples.setOnClickListener(v -> seedSampleDoctors());
        btnRetry.setOnClickListener(v -> loadDoctors());

        loadDoctors();
    }

    /** Loads all doctors from Firestore, ordered by name. */
    private void loadDoctors() {
        showLoading();

        FirebaseHelper.getFirestore()
                .collection(Constants.COLLECTION_DOCTORS)
                .orderBy("name")
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!isAdded()) {
                        return;
                    }
                    doctors.clear();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Doctor doctor = doc.toObject(Doctor.class);
                        doctor.setId(doc.getId());
                        doctors.add(doctor);
                    }
                    adapter.setDoctors(doctors);
                    adapter.filter(etSearch.getText().toString()); // keep the current search

                    if (doctors.isEmpty()) {
                        showEmptyState(R.string.empty_doctors_title,
                                R.string.empty_doctors_text, true);
                    } else {
                        showList();
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        showEmptyState(R.string.error_load_doctors_title,
                                R.string.error_load_doctors_text, false);
                    }
                });
    }

    /** Writes the sample doctors into Firestore (one tap, for the demo). */
    private void seedSampleDoctors() {
        showLoading();

        WriteBatch batch = FirebaseHelper.getFirestore().batch();
        for (Doctor doctor : SampleData.getSampleDoctors()) {
            batch.set(FirebaseHelper.getFirestore()
                    .collection(Constants.COLLECTION_DOCTORS)
                    .document(), doctor);
        }
        batch.commit()
                .addOnSuccessListener(a -> {
                    AppUtils.showToast(requireContext(), R.string.samples_loaded);
                    loadDoctors();
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        showEmptyState(R.string.error_load_doctors_title,
                                R.string.error_load_doctors_text, false);
                    }
                });
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        rvDoctors.setVisibility(View.GONE);
        emptyContainer.setVisibility(View.GONE);
    }

    private void showList() {
        progressBar.setVisibility(View.GONE);
        rvDoctors.setVisibility(View.VISIBLE);
        emptyContainer.setVisibility(View.GONE);
    }

    /** Shows the empty/error state; the seed button only for an empty collection. */
    private void showEmptyState(int titleRes, int textRes, boolean showSeedButton) {
        progressBar.setVisibility(View.GONE);
        rvDoctors.setVisibility(View.GONE);
        emptyContainer.setVisibility(View.VISIBLE);
        tvEmptyTitle.setText(titleRes);
        tvEmptyText.setText(textRes);
        btnLoadSamples.setVisibility(showSeedButton ? View.VISIBLE : View.GONE);
        btnRetry.setVisibility(showSeedButton ? View.GONE : View.VISIBLE);
    }
}

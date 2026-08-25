package com.doctorview.app.fragments;

import android.content.Intent;
import android.net.Uri;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.doctorview.app.R;
import com.doctorview.app.adapters.RecordAdapter;
import com.doctorview.app.firebase.FirebaseHelper;
import com.doctorview.app.models.MedicalRecord;
import com.doctorview.app.utils.AppUtils;
import com.doctorview.app.utils.Constants;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Doctor Medical Records: the records of THIS doctor's patients only
 * (patients derived from the doctor's appointments).
 */
public class DoctorRecordsFragment extends Fragment {

    private ProgressBar progressBar;
    private TextView tvEmpty;

    public DoctorRecordsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_doctor_records, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        RecyclerView rv = view.findViewById(R.id.rvRecords);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        RecordAdapter adapter = new RecordAdapter(new RecordAdapter.OnRecordActionListener() {
            @Override
            public void onRecordClick(MedicalRecord record) {
                if (record.getFileUrl() != null && !record.getFileUrl().isEmpty()) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(record.getFileUrl())));
                } else {
                    AppUtils.showToast(requireContext(), R.string.no_file);
                }
            }

            @Override
            public void onRecordDelete(MedicalRecord record) {
                // Doctors do not delete patient records
            }
        });
        adapter.setShowDelete(false);
        rv.setAdapter(adapter);

        loadPatientRecords(adapter);
    }

    /** Collects this doctor's patient ids, then loads their records. */
    private void loadPatientRecords(RecordAdapter adapter) {
        FirebaseUser firebaseUser = FirebaseHelper.getAuth().getCurrentUser();
        if (firebaseUser == null) {
            return;
        }

        FirebaseHelper.getFirestore()
                .collection(Constants.COLLECTION_APPOINTMENTS)
                .whereEqualTo("doctorId", firebaseUser.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {
                    Set<String> patientIds = new HashSet<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        String patientId = doc.getString("userId");
                        if (patientId != null) {
                            patientIds.add(patientId);
                        }
                    }
                    if (patientIds.isEmpty()) {
                        progressBar.setVisibility(View.GONE);
                        tvEmpty.setVisibility(View.VISIBLE);
                        return;
                    }
                    fetchRecords(new ArrayList<>(patientIds), adapter);
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        progressBar.setVisibility(View.GONE);
                        tvEmpty.setVisibility(View.VISIBLE);
                    }
                });
    }

    /** Loads records with whereIn (chunked, max 10 ids per query). */
    private void fetchRecords(List<String> patientIds, RecordAdapter adapter) {
        List<MedicalRecord> records = new ArrayList<>();
        List<com.google.android.gms.tasks.Task<com.google.firebase.firestore.QuerySnapshot>> tasks =
                new ArrayList<>();

        for (int i = 0; i < patientIds.size(); i += 10) {
            List<String> chunk = patientIds.subList(i, Math.min(i + 10, patientIds.size()));
            tasks.add(FirebaseHelper.getFirestore()
                    .collection(Constants.COLLECTION_MEDICAL_RECORDS)
                    .whereIn("userId", chunk)
                    .get());
        }

        com.google.android.gms.tasks.Tasks.whenAllSuccess(tasks)
                .addOnSuccessListener(results -> {
                    if (!isAdded()) {
                        return;
                    }
                    for (Object result : results) {
                        for (QueryDocumentSnapshot doc :
                                (com.google.firebase.firestore.QuerySnapshot) result) {
                            MedicalRecord record = doc.toObject(MedicalRecord.class);
                            record.setId(doc.getId());
                            records.add(record);
                        }
                    }
                    Collections.sort(records, (a, b) -> {
                        String da = a.getDate();
                        String db = b.getDate();
                        if (da == null && db == null) {
                            return 0;
                        }
                        if (da == null) {
                            return 1;
                        }
                        if (db == null) {
                            return -1;
                        }
                        return db.compareTo(da);
                    });
                    progressBar.setVisibility(View.GONE);
                    adapter.setRecords(records);
                    tvEmpty.setVisibility(records.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        progressBar.setVisibility(View.GONE);
                        tvEmpty.setVisibility(View.VISIBLE);
                    }
                });
    }
}

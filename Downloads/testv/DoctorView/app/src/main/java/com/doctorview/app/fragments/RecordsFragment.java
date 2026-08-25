package com.doctorview.app.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Medical Records: the user's health history from Firestore,
 * newest first, with attached files on Firebase Storage.
 */
public class RecordsFragment extends Fragment {

    private final List<MedicalRecord> records = new ArrayList<>();

    private RecyclerView rvRecords;
    private ProgressBar progressBar;
    private View emptyContainer;
    private TextView tvEmptyTitle;
    private TextView tvEmptyText;
    private RecordAdapter adapter;

    public RecordsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_records, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvRecords = view.findViewById(R.id.rvRecords);
        progressBar = view.findViewById(R.id.progressBar);
        emptyContainer = view.findViewById(R.id.emptyContainer);
        tvEmptyTitle = view.findViewById(R.id.tvEmptyTitle);
        tvEmptyText = view.findViewById(R.id.tvEmptyText);

        rvRecords.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new RecordAdapter(new RecordAdapter.OnRecordActionListener() {
            @Override
            public void onRecordClick(MedicalRecord record) {
                openRecord(record);
            }

            @Override
            public void onRecordDelete(MedicalRecord record) {
                deleteRecord(record);
            }
        });
        rvRecords.setAdapter(adapter);

        FloatingActionButton fabAdd = view.findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_recordsFragment_to_addRecordFragment));

        view.findViewById(R.id.btnAddRecord).setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_recordsFragment_to_addRecordFragment));

        loadRecords();
    }

    /** Loads the current user's records from Firestore, newest first. */
    private void loadRecords() {
        showLoading();

        FirebaseUser user = FirebaseHelper.getAuth().getCurrentUser();
        if (user == null) {
            showEmptyState(R.string.empty_records_title, R.string.empty_records_text);
            return;
        }

        FirebaseHelper.getFirestore()
                .collection(Constants.COLLECTION_MEDICAL_RECORDS)
                .whereEqualTo("userId", user.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!isAdded()) {
                        return;
                    }
                    records.clear();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        MedicalRecord record = doc.toObject(MedicalRecord.class);
                        record.setId(doc.getId());
                        records.add(record);
                    }
                    sortNewestFirst();
                    adapter.setRecords(records);

                    if (records.isEmpty()) {
                        showEmptyState(R.string.empty_records_title, R.string.empty_records_text);
                    } else {
                        showList();
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        showEmptyState(R.string.error_load_records_title,
                                R.string.error_load_records_text);
                    }
                });
    }

    /** Opens the attached file in the browser; shows a hint if there is none. */
    private void openRecord(MedicalRecord record) {
        if (record.getFileUrl() != null && !record.getFileUrl().isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(record.getFileUrl()));
            startActivity(intent);
        } else {
            AppUtils.showToast(requireContext(), R.string.no_file);
        }
    }

    /** Deletes the record (and its attached file, if any). */
    private void deleteRecord(MedicalRecord record) {
        FirebaseHelper.getFirestore()
                .collection(Constants.COLLECTION_MEDICAL_RECORDS)
                .document(record.getId())
                .delete()
                .addOnSuccessListener(a -> {
                    // Also remove the file from Storage when one was attached
                    if (record.getFileUrl() != null && !record.getFileUrl().isEmpty()) {
                        FirebaseHelper.getStorage()
                                .getReferenceFromUrl(record.getFileUrl())
                                .delete()
                                .addOnFailureListener(ignored -> {
                                    // File deletion is best-effort
                                });
                    }
                    AppUtils.showToast(requireContext(), R.string.record_deleted);
                    loadRecords();
                })
                .addOnFailureListener(e ->
                        AppUtils.showToast(requireContext(), R.string.delete_error));
    }

    /** Dates are stored as yyyy-MM-dd, so comparing strings sorts correctly. */
    private void sortNewestFirst() {
        Collections.sort(records, (a, b) -> {
            String dateA = a.getDate();
            String dateB = b.getDate();
            if (dateA == null && dateB == null) {
                return 0;
            }
            if (dateA == null) {
                return 1;
            }
            if (dateB == null) {
                return -1;
            }
            return dateB.compareTo(dateA);
        });
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        rvRecords.setVisibility(View.GONE);
        emptyContainer.setVisibility(View.GONE);
    }

    private void showList() {
        progressBar.setVisibility(View.GONE);
        rvRecords.setVisibility(View.VISIBLE);
        emptyContainer.setVisibility(View.GONE);
    }

    private void showEmptyState(int titleRes, int textRes) {
        progressBar.setVisibility(View.GONE);
        rvRecords.setVisibility(View.GONE);
        emptyContainer.setVisibility(View.VISIBLE);
        tvEmptyTitle.setText(titleRes);
        tvEmptyText.setText(textRes);
    }
}

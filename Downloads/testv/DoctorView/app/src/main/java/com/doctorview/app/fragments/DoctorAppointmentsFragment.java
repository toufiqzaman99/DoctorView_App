package com.doctorview.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.doctorview.app.R;
import com.doctorview.app.adapters.DoctorAppointmentAdapter;
import com.doctorview.app.firebase.FirebaseHelper;
import com.doctorview.app.models.Appointment;
import com.doctorview.app.utils.AppUtils;
import com.doctorview.app.utils.Constants;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Doctor Appointments: pending requests (Accept/Reject) and the full
 * appointment list.
 */
public class DoctorAppointmentsFragment extends Fragment {

    private final List<Appointment> allAppointments = new ArrayList<>();

    private DoctorAppointmentAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private String filter = "requests";

    public DoctorAppointmentsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_doctor_appointments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        RecyclerView rv = view.findViewById(R.id.rvAppointments);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new DoctorAppointmentAdapter(new DoctorAppointmentAdapter.OnRequestActionListener() {
            @Override
            public void onAccept(Appointment appointment) {
                setStatus(appointment, Constants.STATUS_CONFIRMED, R.string.request_accepted);
            }

            @Override
            public void onReject(Appointment appointment) {
                setStatus(appointment, Constants.STATUS_REJECTED, R.string.request_rejected);
            }
        });
        rv.setAdapter(adapter);

        ChipGroup chipGroup = view.findViewById(R.id.chipGroupFilter);
        for (int i = 0; i < 2; i++) {
            Chip chip = AppUtils.createFilterChip(requireContext(),
                    i == 0 ? getString(R.string.filter_requests) : getString(R.string.filter_all));
            chip.setChecked(i == 0);
            chipGroup.addView(chip);
        }
        chipGroup.setSingleSelection(true);
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip chip = group.findViewById(checkedIds.get(0));
                filter = chip != null && getString(R.string.filter_all)
                        .equals(chip.getText().toString()) ? "all" : "requests";
                applyFilter();
            }
        });

        loadAppointments();
    }

    private void loadAppointments() {
        progressBar.setVisibility(View.VISIBLE);

        FirebaseUser firebaseUser = FirebaseHelper.getAuth().getCurrentUser();
        if (firebaseUser == null) {
            return;
        }

        FirebaseHelper.getFirestore()
                .collection(Constants.COLLECTION_APPOINTMENTS)
                .whereEqualTo("doctorId", firebaseUser.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!isAdded()) {
                        return;
                    }
                    progressBar.setVisibility(View.GONE);
                    allAppointments.clear();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Appointment appointment = doc.toObject(Appointment.class);
                        appointment.setId(doc.getId());
                        allAppointments.add(appointment);
                    }
                    sortNewestFirst();
                    applyFilter();
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        progressBar.setVisibility(View.GONE);
                        tvEmpty.setVisibility(View.VISIBLE);
                        tvEmpty.setText(R.string.error_load_appointments_title);
                    }
                });
    }

    private void applyFilter() {
        List<Appointment> shown = new ArrayList<>();
        for (Appointment appointment : allAppointments) {
            if ("requests".equals(filter) && !Constants.STATUS_PENDING.equals(appointment.getStatus())) {
                continue;
            }
            shown.add(appointment);
        }
        adapter.setAppointments(shown);
        tvEmpty.setVisibility(shown.isEmpty() ? View.VISIBLE : View.GONE);
        tvEmpty.setText("requests".equals(filter)
                ? R.string.no_requests : R.string.empty_appointments_title);
    }

    /** Accept or reject: updates the status in Firestore, then reloads. */
    private void setStatus(Appointment appointment, String status, int messageRes) {
        FirebaseHelper.getFirestore()
                .collection(Constants.COLLECTION_APPOINTMENTS)
                .document(appointment.getId())
                .update("status", status)
                .addOnSuccessListener(a -> {
                    AppUtils.showToast(requireContext(), messageRes);
                    loadAppointments();
                })
                .addOnFailureListener(e ->
                        AppUtils.showToast(requireContext(), R.string.save_error));
    }

    private void sortNewestFirst() {
        Collections.sort(allAppointments, (a, b) -> {
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
}

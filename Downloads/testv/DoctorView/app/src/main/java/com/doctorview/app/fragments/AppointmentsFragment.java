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
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.doctorview.app.R;
import com.doctorview.app.adapters.AppointmentAdapter;
import com.doctorview.app.firebase.FirebaseHelper;
import com.doctorview.app.models.Appointment;
import com.doctorview.app.utils.AppUtils;
import com.doctorview.app.utils.Constants;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * My Appointments tab: the logged-in user's appointments from Firestore,
 * newest first, with a Cancel button while they are pending/confirmed.
 */
public class AppointmentsFragment extends Fragment {

    private final List<Appointment> appointments = new ArrayList<>();

    private RecyclerView rvAppointments;
    private ProgressBar progressBar;
    private View emptyContainer;
    private TextView tvEmptyTitle;
    private TextView tvEmptyText;
    private AppointmentAdapter adapter;

    public AppointmentsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_appointments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvAppointments = view.findViewById(R.id.rvAppointments);
        progressBar = view.findViewById(R.id.progressBar);
        emptyContainer = view.findViewById(R.id.emptyContainer);
        tvEmptyTitle = view.findViewById(R.id.tvEmptyTitle);
        tvEmptyText = view.findViewById(R.id.tvEmptyText);

        rvAppointments.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AppointmentAdapter(appointments, this::cancelAppointment);
        rvAppointments.setAdapter(adapter);

        view.findViewById(R.id.btnBookNewAppointment).setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_appointmentsFragment_to_bookAppointmentFragment));

        loadAppointments();
    }

    /** Loads the current user's appointments from Firestore. */
    private void loadAppointments() {
        showLoading();

        FirebaseUser user = FirebaseHelper.getAuth().getCurrentUser();
        if (user == null) {
            showEmptyState(R.string.empty_appointments_title,
                    R.string.empty_appointments_text);
            return;
        }

        FirebaseHelper.getFirestore()
                .collection(Constants.COLLECTION_APPOINTMENTS)
                .whereEqualTo("userId", user.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!isAdded()) {
                        return;
                    }
                    appointments.clear();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Appointment appointment = doc.toObject(Appointment.class);
                        appointment.setId(doc.getId());
                        appointments.add(appointment);
                    }
                    sortNewestFirst();
                    adapter.setAppointments(appointments);

                    if (appointments.isEmpty()) {
                        showEmptyState(R.string.empty_appointments_title,
                                R.string.empty_appointments_text);
                    } else {
                        showList();
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        showEmptyState(R.string.error_load_appointments_title,
                                R.string.error_load_appointments_text);
                    }
                });
    }

    /** Dates are stored as yyyy-MM-dd, so comparing strings sorts correctly. */
    private void sortNewestFirst() {
        Collections.sort(appointments, (a, b) -> {
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

    /** Marks an appointment as cancelled in Firestore, then refreshes the list. */
    private void cancelAppointment(Appointment appointment) {
        FirebaseHelper.getFirestore()
                .collection(Constants.COLLECTION_APPOINTMENTS)
                .document(appointment.getId())
                .update("status", Constants.STATUS_CANCELLED)
                .addOnSuccessListener(a -> {
                    AppUtils.showToast(requireContext(), R.string.appointment_cancelled);
                    loadAppointments();
                })
                .addOnFailureListener(e ->
                        AppUtils.showToast(requireContext(), R.string.cancel_error));
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        rvAppointments.setVisibility(View.GONE);
        emptyContainer.setVisibility(View.GONE);
    }

    private void showList() {
        progressBar.setVisibility(View.GONE);
        rvAppointments.setVisibility(View.VISIBLE);
        emptyContainer.setVisibility(View.GONE);
    }

    private void showEmptyState(int titleRes, int textRes) {
        progressBar.setVisibility(View.GONE);
        rvAppointments.setVisibility(View.GONE);
        emptyContainer.setVisibility(View.VISIBLE);
        tvEmptyTitle.setText(titleRes);
        tvEmptyText.setText(textRes);
    }
}

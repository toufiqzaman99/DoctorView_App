package com.doctorview.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.doctorview.app.R;
import com.doctorview.app.firebase.FirebaseHelper;
import com.doctorview.app.models.User;
import com.doctorview.app.utils.AppUtils;
import com.doctorview.app.utils.Constants;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Doctor Dashboard: welcome header, statistics and quick links.
 */
public class DoctorDashboardFragment extends Fragment {

    public DoctorDashboardFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_doctor_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseUser firebaseUser = FirebaseHelper.getAuth().getCurrentUser();
        if (firebaseUser == null) {
            return;
        }

        loadGreeting(view.findViewById(R.id.tvWelcome), firebaseUser.getUid());
        loadStats(firebaseUser.getUid(), view);

        // Hero entrance for the gradient header
        View hero = view.findViewById(R.id.heroHeader);
        hero.startAnimation(
                android.view.animation.AnimationUtils.loadAnimation(requireContext(), R.anim.scale_in));

        view.findViewById(R.id.btnRequests).setOnClickListener(v ->
                goToTab(v, R.id.doctorAppointmentsFragment));
        view.findViewById(R.id.btnAvailability).setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_doctorDashboardFragment_to_doctorAvailabilityFragment));
        view.findViewById(R.id.btnRecords).setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_doctorDashboardFragment_to_doctorRecordsFragment));
        view.findViewById(R.id.btnEditProfile).setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_doctorDashboardFragment_to_doctorEditProfileFragment));

        // Tactile press feedback on the quick links
        AppUtils.applyPressScale(view.findViewById(R.id.btnRequests));
        AppUtils.applyPressScale(view.findViewById(R.id.btnAvailability));
        AppUtils.applyPressScale(view.findViewById(R.id.btnRecords));
        AppUtils.applyPressScale(view.findViewById(R.id.btnEditProfile));
    }

    /** "Welcome, Dr. X" */
    private void loadGreeting(TextView tvWelcome, String uid) {
        FirebaseHelper.getFirestore()
                .collection(Constants.COLLECTION_USERS)
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!isAdded()) {
                        return;
                    }
                    String name = "Doctor";
                    if (doc.exists()) {
                        User user = doc.toObject(User.class);
                        if (user != null && user.getName() != null && !user.getName().isEmpty()) {
                            name = user.getName();
                        }
                    }
                    tvWelcome.setText(getString(R.string.welcome_doctor, name));
                });
    }

    /** Counts today's, pending, confirmed and completed appointments. */
    private void loadStats(String uid, View view) {
        FirebaseHelper.getFirestore()
                .collection(Constants.COLLECTION_APPOINTMENTS)
                .whereEqualTo("doctorId", uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!isAdded()) {
                        return;
                    }
                    String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
                    int todayCount = 0;
                    int pending = 0;
                    int confirmed = 0;
                    int completed = 0;
                    for (QueryDocumentSnapshot doc : snapshot) {
                        String date = doc.getString("date");
                        String status = doc.getString("status");
                        if (today.equals(date)) {
                            todayCount++;
                        }
                        if (Constants.STATUS_PENDING.equals(status)) {
                            pending++;
                        } else if (Constants.STATUS_CONFIRMED.equals(status)) {
                            confirmed++;
                        } else if (Constants.STATUS_COMPLETED.equals(status)) {
                            completed++;
                        }
                    }
                    ((TextView) view.findViewById(R.id.tvStatToday)).setText(String.valueOf(todayCount));
                    ((TextView) view.findViewById(R.id.tvStatPending)).setText(String.valueOf(pending));
                    ((TextView) view.findViewById(R.id.tvStatConfirmed)).setText(String.valueOf(confirmed));
                    ((TextView) view.findViewById(R.id.tvStatCompleted)).setText(String.valueOf(completed));
                })
                .addOnFailureListener(e -> {
                    // Stats stay 0
                });
    }

    private void goToTab(View view, int destination) {
        NavOptions options = new NavOptions.Builder()
                .setPopUpTo(R.id.doctorDashboardFragment, false)
                .build();
        Navigation.findNavController(view).navigate(destination, null, options);
    }
}

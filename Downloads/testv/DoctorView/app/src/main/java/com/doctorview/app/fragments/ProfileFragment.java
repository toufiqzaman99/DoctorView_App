package com.doctorview.app.fragments;

import android.content.Intent;
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
import com.doctorview.app.activities.WelcomeActivity;
import com.doctorview.app.firebase.FirebaseHelper;
import com.doctorview.app.models.User;
import com.doctorview.app.utils.AppUtils;
import com.doctorview.app.utils.Constants;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseUser;

/**
 * Profile tab: the signed-in user's info, links to the account-related
 * sections and a working Logout.
 */
public class ProfileFragment extends Fragment {

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadUserInfo((TextView) view.findViewById(R.id.tvProfileName),
                (TextView) view.findViewById(R.id.tvProfileEmail));

        view.findViewById(R.id.rowRecords).setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_profileFragment_to_recordsFragment));
        view.findViewById(R.id.rowNews).setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_profileFragment_to_newsFragment));
        view.findViewById(R.id.rowSettings).setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_profileFragment_to_settingsFragment));
        view.findViewById(R.id.rowEmergency).setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_profileFragment_to_emergencyFragment));

        view.findViewById(R.id.rowLogout).setOnClickListener(v -> logout());

        // My Appointments (switches to the Appointments tab)
        view.findViewById(R.id.rowAppointments).setOnClickListener(v -> {
            NavOptions options = new NavOptions.Builder()
                    .setPopUpTo(R.id.homeFragment, false)
                    .build();
            Navigation.findNavController(v).navigate(R.id.appointmentsFragment, null, options);
        });

        // About dialog
        view.findViewById(R.id.rowAbout).setOnClickListener(v ->
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.settings_about)
                        .setMessage(R.string.about_message)
                        .setPositiveButton(R.string.btn_ok, null)
                        .show());
    }

    /** Shows the signed-in user's name, email, phone and role. */
    private void loadUserInfo(TextView tvProfileName, TextView tvProfileEmail) {
        FirebaseUser firebaseUser = FirebaseHelper.getAuth().getCurrentUser();
        if (firebaseUser == null) {
            return;
        }
        if (firebaseUser.getEmail() != null) {
            tvProfileEmail.setText(firebaseUser.getEmail());
            tvProfileName.setText(firebaseUser.getEmail().split("@")[0]);
        }

        final TextView tvProfilePhone = getView() != null
                ? getView().findViewById(R.id.tvProfilePhone) : null;
        final TextView tvUserType = getView() != null
                ? getView().findViewById(R.id.tvUserType) : null;

        // The real profile is stored in Firestore when the account was created
        FirebaseHelper.getFirestore()
                .collection(Constants.COLLECTION_USERS)
                .document(firebaseUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (isAdded() && documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null && user.getName() != null && !user.getName().isEmpty()) {
                            tvProfileName.setText(user.getName());
                        }
                        if (tvProfilePhone != null && user != null
                                && user.getPhone() != null && !user.getPhone().isEmpty()) {
                            tvProfilePhone.setText(user.getPhone());
                        }
                        if (tvUserType != null && user != null && user.getRole() != null) {
                            tvUserType.setText(Constants.ROLE_DOCTOR.equals(user.getRole())
                                    ? R.string.role_doctor : R.string.role_patient);
                        }
                    }
                });
    }

    /** Signs the user out and returns to the Login screen. */
    private void logout() {
        FirebaseHelper.getAuth().signOut();
        AppUtils.showToast(requireContext(), R.string.logged_out);

        Intent intent = new Intent(requireContext(), WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}

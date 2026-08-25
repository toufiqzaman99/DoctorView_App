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
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.doctorview.app.R;
import com.doctorview.app.activities.WelcomeActivity;
import com.doctorview.app.firebase.FirebaseHelper;
import com.doctorview.app.models.Doctor;
import com.doctorview.app.models.User;
import com.doctorview.app.utils.AppUtils;
import com.doctorview.app.utils.Constants;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

/**
 * Doctor Profile: the doctor's own professional profile view
 * with Edit Profile, Availability, Settings and Logout.
 */
public class DoctorProfileFragment extends Fragment {

    private User profile;

    public DoctorProfileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_doctor_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseUser firebaseUser = FirebaseHelper.getAuth().getCurrentUser();
        if (firebaseUser == null) {
            return;
        }

        view.findViewById(R.id.btnEditProfile).setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_doctorProfileFragment_to_doctorEditProfileFragment));
        view.findViewById(R.id.btnAvailability).setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_doctorProfileFragment_to_doctorAvailabilityFragment));
        view.findViewById(R.id.btnSettings).setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_doctorProfileFragment_to_doctorSettingsFragment));
        view.findViewById(R.id.btnLogout).setOnClickListener(v -> logout());

        loadProfile(view, firebaseUser.getUid());
    }

    private void loadProfile(View view, String uid) {
        FirebaseHelper.getFirestore()
                .collection(Constants.COLLECTION_USERS)
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!isAdded() || !doc.exists()) {
                        return;
                    }
                    profile = doc.toObject(User.class);
                    if (profile == null) {
                        return;
                    }
                    bindProfile(view);
                    // The photo lives on the doctors collection document
                    loadPhoto(view, uid);
                });
    }

    private void bindProfile(View view) {
        ((TextView) view.findViewById(R.id.tvProfileName)).setText(profile.getName());
        ((TextView) view.findViewById(R.id.tvProfileEmail)).setText(profile.getEmail());
        ((TextView) view.findViewById(R.id.tvProfilePhone)).setText(
                profile.getPhone() != null && !profile.getPhone().isEmpty()
                        ? profile.getPhone() : "-");
        ((TextView) view.findViewById(R.id.tvProfileSpecialty)).setText(
                profile.getSpecialty() != null ? profile.getSpecialty() : "-");
        ((TextView) view.findViewById(R.id.tvProfileHospital)).setText(
                profile.getHospital() != null ? profile.getHospital() : "-");
        ((TextView) view.findViewById(R.id.tvProfileQualification)).setText(
                profile.getQualification() != null ? profile.getQualification() : "-");
        ((TextView) view.findViewById(R.id.tvProfileExperience)).setText(
                getString(R.string.years_value, profile.getExperienceYears()));
        ((TextView) view.findViewById(R.id.tvProfileFee)).setText(
                getString(R.string.fee_per_session,
                        String.format(Locale.getDefault(), "%.0f", profile.getConsultationFee())));
        TextView tvAbout = view.findViewById(R.id.tvProfileAbout);
        if (profile.getAbout() != null && !profile.getAbout().isEmpty()) {
            tvAbout.setVisibility(View.VISIBLE);
            tvAbout.setText(profile.getAbout());
        } else {
            tvAbout.setVisibility(View.GONE);
        }
    }

    private void loadPhoto(View view, String uid) {
        FirebaseHelper.getFirestore()
                .collection(Constants.COLLECTION_DOCTORS)
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!isAdded() || !doc.exists()) {
                        return;
                    }
                    Doctor doctor = doc.toObject(Doctor.class);
                    if (doctor == null) {
                        return;
                    }
                    ShapeableImageView ivPhoto = view.findViewById(R.id.ivProfilePhoto);
                    Glide.with(requireContext())
                            .load(doctor.getImageUrl())
                            .placeholder(R.drawable.ic_doctor_avatar)
                            .error(R.drawable.ic_doctor_avatar)
                            .into(ivPhoto);
                });
    }

    private void logout() {
        FirebaseHelper.getAuth().signOut();
        AppUtils.showToast(requireContext(), R.string.logged_out);
        Intent intent = new Intent(requireContext(), WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}

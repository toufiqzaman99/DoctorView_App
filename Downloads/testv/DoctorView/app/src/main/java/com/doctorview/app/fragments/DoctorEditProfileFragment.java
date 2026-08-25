package com.doctorview.app.fragments;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.doctorview.app.R;
import com.doctorview.app.firebase.FirebaseHelper;
import com.doctorview.app.models.User;
import com.doctorview.app.utils.AppUtils;
import com.doctorview.app.utils.Constants;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

/**
 * Doctor Edit Profile: name, specialty, hospital, qualification,
 * experience, fee, phone, about and an optional profile photo
 * (Firebase Storage). Updates both users/{uid} and doctors/{uid}.
 */
public class DoctorEditProfileFragment extends Fragment {

    private TextInputEditText etName;
    private TextInputEditText etSpecialty;
    private TextInputEditText etHospital;
    private TextInputEditText etQualification;
    private TextInputEditText etExperience;
    private TextInputEditText etFee;
    private TextInputEditText etPhone;
    private TextInputEditText etAbout;
    private TextView tvPhotoName;
    private ProgressBar progressBar;
    private String photoUrl;
    private ActivityResultLauncher<String> photoPickerLauncher;

    public DoctorEditProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(), this::onPhotoPicked);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_doctor_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etName = view.findViewById(R.id.etName);
        etSpecialty = view.findViewById(R.id.etSpecialty);
        etHospital = view.findViewById(R.id.etHospital);
        etQualification = view.findViewById(R.id.etQualification);
        etExperience = view.findViewById(R.id.etExperience);
        etFee = view.findViewById(R.id.etFee);
        etPhone = view.findViewById(R.id.etPhone);
        etAbout = view.findViewById(R.id.etAbout);
        tvPhotoName = view.findViewById(R.id.tvPhotoName);
        progressBar = view.findViewById(R.id.progressBar);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnPickPhoto = view.findViewById(R.id.btnPickPhoto);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        btnPickPhoto.setOnClickListener(v -> photoPickerLauncher.launch("image/*"));
        btnSave.setOnClickListener(v -> saveProfile());

        loadCurrentProfile();
    }

    private void loadCurrentProfile() {
        FirebaseUser firebaseUser = FirebaseHelper.getAuth().getCurrentUser();
        if (firebaseUser == null) {
            return;
        }
        FirebaseHelper.getFirestore()
                .collection(Constants.COLLECTION_USERS)
                .document(firebaseUser.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (!isAdded() || !doc.exists()) {
                        return;
                    }
                    User user = doc.toObject(User.class);
                    if (user == null) {
                        return;
                    }
                    etName.setText(user.getName());
                    etSpecialty.setText(user.getSpecialty());
                    etHospital.setText(user.getHospital());
                    etQualification.setText(user.getQualification());
                    etExperience.setText(String.valueOf(user.getExperienceYears()));
                    etFee.setText(String.valueOf((long) user.getConsultationFee()));
                    etPhone.setText(user.getPhone());
                    etAbout.setText(user.getAbout());
                });
    }

    /** Uploads the picked photo to Firebase Storage. */
    private void onPhotoPicked(Uri uri) {
        if (uri == null) {
            return;
        }
        FirebaseUser firebaseUser = FirebaseHelper.getAuth().getCurrentUser();
        if (firebaseUser == null) {
            return;
        }
        String fileName = queryDisplayName(uri);
        StorageReference ref = FirebaseHelper.getStorage()
                .getReference("doctor_photos")
                .child(firebaseUser.getUid())
                .child(System.currentTimeMillis() + "_" + fileName);

        progressBar.setVisibility(View.VISIBLE);
        ref.putFile(uri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful() && task.getException() != null) {
                        throw task.getException();
                    }
                    return ref.getDownloadUrl();
                })
                .addOnSuccessListener(downloadUri -> {
                    progressBar.setVisibility(View.GONE);
                    photoUrl = downloadUri.toString();
                    tvPhotoName.setVisibility(View.VISIBLE);
                    tvPhotoName.setText(fileName);
                    AppUtils.showToast(requireContext(), R.string.file_attached);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    AppUtils.showToast(requireContext(), R.string.upload_failed);
                });
    }

    private String queryDisplayName(Uri uri) {
        try (Cursor cursor = requireContext().getContentResolver()
                .query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) {
                    String name = cursor.getString(index);
                    if (name != null && !name.isEmpty()) {
                        return name;
                    }
                }
            }
        } catch (Exception ignored) {
            // fall through
        }
        return "photo_" + System.currentTimeMillis();
    }

    /** Writes the profile fields to users/{uid} and doctors/{uid}. */
    private void saveProfile() {
        FirebaseUser firebaseUser = FirebaseHelper.getAuth().getCurrentUser();
        if (firebaseUser == null) {
            return;
        }

        String name = textOf(etName);
        String specialty = textOf(etSpecialty);
        String hospital = textOf(etHospital);
        String qualification = textOf(etQualification);
        String phone = textOf(etPhone);
        String about = textOf(etAbout);
        int experience;
        double fee;
        try {
            experience = Integer.parseInt(textOf(etExperience));
        } catch (NumberFormatException e) {
            experience = 0;
        }
        try {
            fee = Double.parseDouble(textOf(etFee));
        } catch (NumberFormatException e) {
            fee = 0;
        }

        if (name.isEmpty()) {
            AppUtils.showToast(requireContext(), R.string.name_required);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        String uid = firebaseUser.getUid();

        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("name", name);
        userUpdates.put("specialty", specialty);
        userUpdates.put("hospital", hospital);
        userUpdates.put("qualification", qualification);
        userUpdates.put("phone", phone);
        userUpdates.put("experienceYears", experience);
        userUpdates.put("consultationFee", fee);
        userUpdates.put("about", about);

        Map<String, Object> doctorUpdates = new HashMap<>();
        doctorUpdates.put("name", name);
        doctorUpdates.put("specialty", specialty);
        doctorUpdates.put("hospital", hospital);
        doctorUpdates.put("experienceYears", experience);
        doctorUpdates.put("fee", fee);
        doctorUpdates.put("about", about);
        if (photoUrl != null) {
            doctorUpdates.put("imageUrl", photoUrl);
        }

        FirebaseHelper.getFirestore()
                .collection(Constants.COLLECTION_USERS)
                .document(uid)
                .update(userUpdates)
                .addOnCompleteListener(userTask -> {
                    FirebaseHelper.getFirestore()
                            .collection(Constants.COLLECTION_DOCTORS)
                            .document(uid)
                            .update(doctorUpdates)
                            .addOnCompleteListener(doctorTask -> {
                                progressBar.setVisibility(View.GONE);
                                if (userTask.isSuccessful() && doctorTask.isSuccessful()) {
                                    AppUtils.showToast(requireContext(), R.string.profile_updated);
                                    Navigation.findNavController(requireView()).popBackStack();
                                } else {
                                    AppUtils.showToast(requireContext(), R.string.save_error);
                                }
                            });
                });
    }

    private String textOf(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
}

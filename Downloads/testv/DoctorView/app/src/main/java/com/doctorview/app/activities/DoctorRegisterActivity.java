package com.doctorview.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.doctorview.app.R;
import com.doctorview.app.firebase.FirebaseHelper;
import com.doctorview.app.models.Doctor;
import com.doctorview.app.models.User;
import com.doctorview.app.utils.AppUtils;
import com.doctorview.app.utils.Constants;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;

/**
 * Doctor Register — creates the Firebase account, saves the profile
 * (userType = DOCTOR + professional fields) to Firestore "users",
 * and mirrors the doctor into the "doctors" collection so patients
 * can find and book them.
 */
public class DoctorRegisterActivity extends AppCompatActivity {

    private static final String TAG = "DoctorViewDoctorRegister";

    private TextInputLayout tilName;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPhone;
    private TextInputLayout tilPassword;
    private TextInputLayout tilConfirmPassword;
    private TextInputLayout tilSpecialty;
    private TextInputLayout tilHospital;
    private TextInputLayout tilQualification;
    private TextInputLayout tilExperience;
    private TextInputLayout tilFee;
    private TextInputEditText etName;
    private TextInputEditText etEmail;
    private TextInputEditText etPhone;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;
    private TextInputEditText etSpecialty;
    private TextInputEditText etHospital;
    private TextInputEditText etQualification;
    private TextInputEditText etExperience;
    private TextInputEditText etFee;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_register);

        tilName = findViewById(R.id.tilName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPhone = findViewById(R.id.tilPhone);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        tilSpecialty = findViewById(R.id.tilSpecialty);
        tilHospital = findViewById(R.id.tilHospital);
        tilQualification = findViewById(R.id.tilQualification);
        tilExperience = findViewById(R.id.tilExperience);
        tilFee = findViewById(R.id.tilFee);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etSpecialty = findViewById(R.id.etSpecialty);
        etHospital = findViewById(R.id.etHospital);
        etQualification = findViewById(R.id.etQualification);
        etExperience = findViewById(R.id.etExperience);
        etFee = findViewById(R.id.etFee);
        progressBar = findViewById(R.id.progressBar);
        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvGoToLogin = findViewById(R.id.tvGoToLogin);

        btnRegister.setOnClickListener(v -> registerDoctor());
        AppUtils.applyPressScale(btnRegister);
        tvGoToLogin.setOnClickListener(v -> finish());
    }

    private void registerDoctor() {
        tilName.setError(null);
        tilEmail.setError(null);
        tilPhone.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
        tilSpecialty.setError(null);
        tilHospital.setError(null);
        tilExperience.setError(null);
        tilFee.setError(null);

        String name = textOf(etName);
        String email = textOf(etEmail);
        String phone = textOf(etPhone);
        String password = textOf(etPassword);
        String confirmPassword = textOf(etConfirmPassword);
        String specialty = textOf(etSpecialty);
        String hospital = textOf(etHospital);
        String qualification = textOf(etQualification);
        String experienceText = textOf(etExperience);
        String feeText = textOf(etFee);

        if (name.isEmpty()) {
            tilName.setError(getString(R.string.name_required));
            return;
        }
        if (!AppUtils.isValidEmail(email)) {
            tilEmail.setError(getString(R.string.invalid_email));
            return;
        }
        if (!phone.isEmpty() && !AppUtils.isValidPhone(phone)) {
            tilPhone.setError(getString(R.string.invalid_phone));
            return;
        }
        if (password.length() < 6) {
            tilPassword.setError(getString(R.string.invalid_password));
            return;
        }
        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError(getString(R.string.passwords_mismatch));
            return;
        }
        if (specialty.isEmpty()) {
            tilSpecialty.setError(getString(R.string.specialty_required));
            return;
        }
        if (hospital.isEmpty()) {
            tilHospital.setError(getString(R.string.hospital_required));
            return;
        }
        final int experience;
        try {
            experience = Integer.parseInt(experienceText);
        } catch (NumberFormatException ignored) {
            tilExperience.setError(getString(R.string.invalid_number));
            return;
        }
        final double fee;
        try {
            fee = Double.parseDouble(feeText);
        } catch (NumberFormatException ignored) {
            tilFee.setError(getString(R.string.invalid_number));
            return;
        }

        showLoading(true);
        FirebaseHelper.getAuth().createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser == null) {
                        showLoading(false);
                        AppUtils.showToast(this, R.string.register_failed);
                        return;
                    }
                    saveDoctorProfile(firebaseUser, name, email, phone, specialty, hospital,
                            qualification, experience, fee);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "doctor register failed: " + e.getMessage(), e);
                    showLoading(false);
                    AppUtils.showToast(this, AppUtils.authErrorMessage(e));
                });
    }

    /** Writes the DOCTOR profile to users/{uid} and mirrors it into doctors/{uid}. */
    private void saveDoctorProfile(FirebaseUser firebaseUser, String name, String email,
                                   String phone, String specialty, String hospital,
                                   String qualification, int experience, double fee) {
        User user = new User(firebaseUser.getUid(), name, email, phone,
                Constants.USER_TYPE_DOCTOR, specialty, hospital, qualification, experience, fee);

        FirebaseHelper.getFirestore()
                .collection(Constants.COLLECTION_USERS)
                .document(firebaseUser.getUid())
                .set(user)
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    if (!task.isSuccessful()) {
                        AppUtils.showToast(this, R.string.profile_save_failed);
                    } else {
                        AppUtils.showToast(this, R.string.register_success);
                    }

                    // Mirror into the doctors collection so patients can find this doctor
                    Doctor doctor = new Doctor(firebaseUser.getUid(), name, specialty, hospital,
                            experience, fee, 0.0, "", "");
                    FirebaseHelper.getFirestore()
                            .collection(Constants.COLLECTION_DOCTORS)
                            .document(firebaseUser.getUid())
                            .set(doctor);

                    openDashboard();
                });
    }

    private void openDashboard() {
        Intent intent = new Intent(this, DoctorMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        findViewById(R.id.btnRegister).setEnabled(!loading);
    }

    private String textOf(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
}

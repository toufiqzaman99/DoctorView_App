package com.doctorview.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.doctorview.app.R;
import com.doctorview.app.firebase.FirebaseHelper;
import com.doctorview.app.models.User;
import com.doctorview.app.utils.AppUtils;
import com.doctorview.app.utils.Constants;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;

/**
 * Register screen — creates the account with Firebase Authentication
 * and saves the profile in Cloud Firestore.
 */
public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "DoctorViewRegister";

    private TextInputLayout tilName;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPhone;
    private TextInputLayout tilPassword;
    private TextInputLayout tilConfirmPassword;
    private TextInputEditText etName;
    private TextInputEditText etEmail;
    private TextInputEditText etPhone;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;
    private Button btnRegister;
    private TextView tvGoToLogin;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        tilName = findViewById(R.id.tilName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPhone = findViewById(R.id.tilPhone);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);
        progressBar = findViewById(R.id.progressBar);

        btnRegister.setOnClickListener(v -> registerUser());
        AppUtils.applyPressScale(btnRegister);

        tvGoToLogin.setOnClickListener(v -> finish());
    }

    /** Validates the input, creates the Firebase user and saves the profile. */
    private void registerUser() {
        // Clear previous error messages
        tilName.setError(null);
        tilEmail.setError(null);
        tilPhone.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);

        String name = textOf(etName);
        String email = textOf(etEmail);
        String phone = textOf(etPhone);
        String password = textOf(etPassword);
        String confirmPassword = textOf(etConfirmPassword);

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

        // Patient registration always creates a PATIENT profile
        final String userType = Constants.USER_TYPE_PATIENT;

        showLoading(true);

        // createUserWithEmailAndPassword also signs the new user in automatically
        FirebaseHelper.getAuth().createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser == null) {
                        showLoading(false);
                        AppUtils.showToast(this, R.string.register_failed);
                        return;
                    }
                    saveUserProfile(firebaseUser, name, email, phone, userType);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "register failed for " + email + ": " + e.getMessage(), e);
                    showLoading(false);
                    AppUtils.showToast(this, AppUtils.authErrorMessage(e));
                });
    }

    /** Stores the new user in the Firestore "users" collection (document id = uid). */
    private void saveUserProfile(FirebaseUser firebaseUser, String name, String email,
                                 String phone, String userType) {
        User user = new User(firebaseUser.getUid(), name, email, phone,
                Constants.ROLE_PATIENT);
        user.setUserType(userType);
        FirebaseHelper.getFirestore()
                .collection(Constants.COLLECTION_USERS)
                .document(firebaseUser.getUid())
                .set(user)
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        AppUtils.showToast(this, R.string.register_success);
                    } else {
                        // The account exists even if saving the profile failed
                        AppUtils.showToast(this, R.string.profile_save_failed);
                    }
                    openMainScreen();
                });
    }

    /** Opens the main screen and clears the back stack (back button won't return here). */
    private void openMainScreen() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /** Shows/hides the progress indicator while a Firebase call is running. */
    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!loading);
        tvGoToLogin.setEnabled(!loading);
        etName.setEnabled(!loading);
        etEmail.setEnabled(!loading);
        etPhone.setEnabled(!loading);
        etPassword.setEnabled(!loading);
        etConfirmPassword.setEnabled(!loading);
    }

    private String textOf(EditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
}

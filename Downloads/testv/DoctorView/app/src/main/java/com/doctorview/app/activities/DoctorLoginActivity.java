package com.doctorview.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.doctorview.app.R;
import com.doctorview.app.firebase.FirebaseHelper;
import com.doctorview.app.models.User;
import com.doctorview.app.utils.AppUtils;
import com.doctorview.app.utils.Constants;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Doctor Login — signs in with Firebase Authentication and verifies
 * that the Firestore profile really is a DOCTOR before opening the
 * Doctor Dashboard. A Patient account is rejected here.
 */
public class DoctorLoginActivity extends AppCompatActivity {

    private static final String TAG = "DoctorViewDoctorLogin";

    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_login);

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        progressBar = findViewById(R.id.progressBar);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        Button btnGoToRegister = findViewById(R.id.btnGoToRegister);

        btnLogin.setOnClickListener(v -> loginDoctor());
        AppUtils.applyPressScale(btnLogin);
        AppUtils.applyPressScale(btnGoToRegister);

        tvForgotPassword.setOnClickListener(v -> {
            final EditText input = new EditText(this);
            input.setHint(R.string.hint_email);
            FrameLayout container = new FrameLayout(this);
            int padding = (int) (24 * getResources().getDisplayMetrics().density);
            container.setPadding(padding, 0, padding, 0);
            container.addView(input);
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.forgot_password)
                    .setMessage(R.string.forgot_password_message)
                    .setView(container)
                    .setPositiveButton(R.string.btn_send, (dialog, which) -> {
                        String email = input.getText().toString().trim();
                        if (!AppUtils.isValidEmail(email)) {
                            AppUtils.showToast(this, R.string.invalid_email);
                            return;
                        }
                        FirebaseHelper.getAuth().sendPasswordResetEmail(email)
                                .addOnSuccessListener(a ->
                                        AppUtils.showToast(this, R.string.password_reset_sent))
                                .addOnFailureListener(e ->
                                        AppUtils.showToast(this, AppUtils.authErrorMessage(e)));
                    })
                    .setNegativeButton(R.string.btn_cancel, null)
                    .show();
        });

        btnGoToRegister.setOnClickListener(v ->
                startActivity(new Intent(this, DoctorRegisterActivity.class)));
    }

    /** Signs in, then verifies the profile userType is DOCTOR. */
    private void loginDoctor() {
        tilEmail.setError(null);
        tilPassword.setError(null);

        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

        if (!AppUtils.isValidEmail(email)) {
            tilEmail.setError(getString(R.string.invalid_email));
            return;
        }
        if (password.isEmpty()) {
            tilPassword.setError(getString(R.string.password_required));
            return;
        }

        showLoading(true);
        FirebaseHelper.getAuth().signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    if (authResult.getUser() == null) {
                        showLoading(false);
                        return;
                    }
                    verifyDoctorRole(authResult.getUser().getUid());
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "doctor signIn failed: " + e.getMessage(), e);
                    showLoading(false);
                    AppUtils.showToast(this, AppUtils.authErrorMessage(e));
                });
    }

    /** Only a DOCTOR profile may continue — anything else is logged out. */
    private void verifyDoctorRole(String uid) {
        FirebaseHelper.getFirestore()
                .collection(Constants.COLLECTION_USERS)
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    showLoading(false);
                    boolean isDoctor = false;
                    if (doc.exists()) {
                        User user = doc.toObject(User.class);
                        isDoctor = user != null
                                && (Constants.USER_TYPE_DOCTOR.equals(user.getUserType())
                                    || Constants.ROLE_DOCTOR.equals(user.getRole()));
                    }
                    if (isDoctor) {
                        AppUtils.showToast(this, R.string.welcome_back);
                        openDashboard();
                    } else {
                        // Role mismatch: reject and log out
                        FirebaseHelper.getAuth().signOut();
                        AppUtils.showToast(this, R.string.role_mismatch_doctor);
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    FirebaseHelper.getAuth().signOut();
                    AppUtils.showToast(this, R.string.role_verify_failed);
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
        findViewById(R.id.btnLogin).setEnabled(!loading);
        findViewById(R.id.btnGoToRegister).setEnabled(!loading);
        etEmail.setEnabled(!loading);
        etPassword.setEnabled(!loading);
    }
}

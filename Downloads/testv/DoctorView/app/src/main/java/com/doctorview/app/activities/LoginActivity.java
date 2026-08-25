package com.doctorview.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
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
 * Login screen — signs the user in with Firebase Authentication
 * (email + password).
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "DoctorViewLogin";

    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private Button btnLogin;
    private Button btnGoToRegister;
    private TextView tvForgotPassword;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoToRegister = findViewById(R.id.btnGoToRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        progressBar = findViewById(R.id.progressBar);

        btnLogin.setOnClickListener(v -> loginUser());
        AppUtils.applyPressScale(btnLogin);
        AppUtils.applyPressScale(btnGoToRegister);

        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());

        btnGoToRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
    }

    /** Validates the input and signs the user in with Firebase Authentication. */
    private void loginUser() {
        // Clear previous error messages
        tilEmail.setError(null);
        tilPassword.setError(null);

        String email = textOf(etEmail);
        String password = textOf(etPassword);

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
                    // Patient login: verify the profile is really a PATIENT
                    verifyPatientRole(authResult.getUser().getUid());
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "signIn failed for " + email + ": " + e.getMessage(), e);
                    // Log the full cause chain so network problems are easy to diagnose
                    Throwable cause = e;
                    int depth = 0;
                    while (cause != null && depth < 6) {
                        Log.w(TAG, "  cause[" + depth + "] " + cause.getClass().getName()
                                + ": " + cause.getMessage());
                        cause = cause.getCause();
                        depth++;
                    }
                    showLoading(false);
                    AppUtils.showToast(this, AppUtils.authErrorMessage(e));
                });
    }

    /** Only a PATIENT profile may continue — anything else is logged out. */
    private void verifyPatientRole(String uid) {
        FirebaseHelper.getFirestore()
                .collection(Constants.COLLECTION_USERS)
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    showLoading(false);
                    boolean isPatient = false;
                    if (doc.exists()) {
                        User user = doc.toObject(User.class);
                        isPatient = user != null
                                && (Constants.USER_TYPE_PATIENT.equals(user.getUserType())
                                    || Constants.ROLE_PATIENT.equals(user.getRole())
                                    || user.getUserType() == null && user.getRole() == null);
                    }
                    if (isPatient) {
                        AppUtils.showToast(this, R.string.welcome_back);
                        openMainScreen();
                    } else {
                        // Role mismatch: a Doctor account cannot enter the Patient app
                        FirebaseHelper.getAuth().signOut();
                        AppUtils.showToast(this, R.string.role_mismatch_patient);
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    FirebaseHelper.getAuth().signOut();
                    AppUtils.showToast(this, R.string.role_verify_failed);
                });
    }

    /** Shows a dialog that sends a password-reset email to the entered address. */
    private void showForgotPasswordDialog() {
        final EditText input = new EditText(this);
        input.setHint(R.string.hint_email);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        input.setText(textOf(etEmail)); // pre-fill with whatever the user already typed

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
    }

    /** Opens the main screen and clears the back stack (back button won't return here). */
    private void openMainScreen() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /** Shows/hides the progress indicator while a Firebase call is running. */
    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
        btnGoToRegister.setEnabled(!loading);
        tvForgotPassword.setEnabled(!loading);
        etEmail.setEnabled(!loading);
        etPassword.setEnabled(!loading);
    }

    private String textOf(EditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
}

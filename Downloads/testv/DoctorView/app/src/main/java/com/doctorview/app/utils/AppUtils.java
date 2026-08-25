package com.doctorview.app.utils;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.StringRes;

import com.doctorview.app.R;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuthException;

import java.util.regex.Pattern;

/**
 * Small shared helpers so screens don't repeat the same code.
 */
public final class AppUtils {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("[+0-9 -]{7,15}");

    /** Specialty filter labels used on the Home and Doctors screens. */
    public static final String[] SPECIALTIES = {
            "All", "Cardiology", "Dermatology", "Pediatrics", "Neurology",
            "General", "Orthopedics", "Gynecology", "Eye"
    };

    private AppUtils() {
        // No instances
    }

    /** Creates a pill-shaped selectable filter chip in the app's chip style. */
    public static Chip createFilterChip(Context context, String text) {
        Chip chip = new Chip(new ContextThemeWrapper(context, R.style.Widget_DoctorView_Chip), null, 0);
        chip.setText(text);
        chip.setCheckable(true);
        return chip;
    }

    /** Shortens a text to max characters with an ellipsis. */
    public static String truncate(String text, int maxChars) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxChars) {
            return text;
        }
        return text.substring(0, maxChars).trim() + "…";
    }

    /**
     * Adds a tactile press animation: the view shrinks slightly while
     * pressed and springs back on release. Returns false so the normal
     * click/ripple behavior still works.
     */
    public static void applyPressScale(View view) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.96f).scaleY(0.96f).setDuration(90).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1f).scaleY(1f).setDuration(140).start();
                    break;
                default:
                    break;
            }
            return false;
        });
    }

    /** Shows a short toast message. */
    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /** Shows a short toast message from a string resource. */
    public static void showToast(Context context, @StringRes int messageRes) {
        Toast.makeText(context, messageRes, Toast.LENGTH_SHORT).show();
    }

    /** Simple e-mail format check, used by Login / Register validation. */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /** Simple phone check — digits, +, spaces and dashes, 7 to 15 characters. */
    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * Turns a Firebase Authentication error into a friendly message
     * the user can actually understand.
     */
    public static String authErrorMessage(Exception e) {
        if (!(e instanceof FirebaseAuthException)) {
            return "Something went wrong. Please try again.";
        }
        switch (((FirebaseAuthException) e).getErrorCode()) {
            case "ERROR_INVALID_EMAIL":
                return "Please enter a valid email address.";
            case "ERROR_WRONG_PASSWORD":
                return "Incorrect password. Please try again.";
            case "ERROR_USER_NOT_FOUND":
                return "No account found with this email.";
            case "ERROR_USER_DISABLED":
                return "This account has been disabled.";
            case "ERROR_EMAIL_ALREADY_IN_USE":
                return "This email is already registered. Try logging in.";
            case "ERROR_WEAK_PASSWORD":
                return "Password should be at least 6 characters.";
            case "ERROR_NETWORK_REQUEST_FAILED":
                return "Network error. Check your internet connection.";
            case "ERROR_OPERATION_NOT_ALLOWED":
                return "Sign-in is not enabled yet. In the Firebase console, enable Email/Password under Authentication → Sign-in method.";
            case "ERROR_ADMIN_RESTRICTED_OPERATION":
                return "Sign-in is disabled for this app. Enable Email/Password under Authentication → Sign-in method in the Firebase console.";
            case "ERROR_TOO_MANY_REQUESTS":
                return "Too many attempts. Please try again later.";
            default:
                String message = e.getMessage() != null ? e.getMessage() : "";
                if (message.contains("CONFIGURATION_NOT_FOUND")) {
                    return "Email/Password sign-in is not enabled yet. In the Firebase console, go to Authentication → Sign-in method → enable Email/Password.";
                }
                return message.isEmpty() ? "Something went wrong. Please try again." : message;
        }
    }
}

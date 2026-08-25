package com.doctorview.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.doctorview.app.R;
import com.doctorview.app.firebase.FirebaseHelper;
import com.doctorview.app.models.User;
import com.doctorview.app.utils.Constants;
import com.doctorview.app.utils.NetworkDiagnostics;
import com.google.firebase.auth.FirebaseUser;

/**
 * Splash / onboarding hero screen (reference design).
 * "Swipe To Start" proceeds to the role selection screen for logged-out
 * users, or straight to the correct dashboard for logged-in users.
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Temporary: logs how this process sees the network (see logcat DoctorViewDiag)
        NetworkDiagnostics.run("DoctorViewDiag");

        // Hero doctor portrait (remote professional photo)
        ImageView ivHeroDoctor = findViewById(R.id.ivHeroDoctor);
        Glide.with(this)
                .load("https://randomuser.me/api/portraits/women/68.jpg")
                .placeholder(R.drawable.ic_doctor_avatar)
                .error(R.drawable.ic_doctor_avatar)
                .into(ivHeroDoctor);

        // Hero entrance animations
        View heroArea = findViewById(R.id.heroArea);
        heroArea.setAlpha(0f);
        heroArea.animate().alpha(1f).setDuration(600).start();

        View headline = findViewById(R.id.tvSplashLine1);
        headline.setTranslationY(30f);
        headline.setAlpha(0f);
        headline.animate().alpha(1f).translationY(0f)
                .setStartDelay(120).setDuration(500).start();

        ivHeroDoctor.setScaleX(0.9f);
        ivHeroDoctor.setScaleY(0.9f);
        ivHeroDoctor.setAlpha(0f);
        ivHeroDoctor.animate().alpha(1f).scaleX(1f).scaleY(1f)
                .setStartDelay(250).setDuration(550)
                .setInterpolator(new OvershootInterpolator(1.2f)).start();

        // Swipe To Start → role selection (or dashboard when already logged in)
        findViewById(R.id.btnSwipeStart).setOnClickListener(v -> proceed());
    }

    private void proceed() {
        FirebaseUser firebaseUser = FirebaseHelper.getAuth().getCurrentUser();
        if (firebaseUser == null) {
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
            return;
        }
        routeByRole(firebaseUser.getUid());
    }

    /** Reads userType from Firestore and opens the matching dashboard. */
    private void routeByRole(String uid) {
        FirebaseHelper.getFirestore()
                .collection(Constants.COLLECTION_USERS)
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    boolean isDoctor = false;
                    if (doc.exists()) {
                        User user = doc.toObject(User.class);
                        isDoctor = user != null
                                && (Constants.USER_TYPE_DOCTOR.equals(user.getUserType())
                                    || Constants.ROLE_DOCTOR.equals(user.getRole()));
                    }
                    startActivity(new Intent(this,
                            isDoctor ? DoctorMainActivity.class : MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    startActivity(new Intent(this, WelcomeActivity.class));
                    finish();
                });
    }
}

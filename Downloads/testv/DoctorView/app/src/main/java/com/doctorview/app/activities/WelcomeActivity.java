package com.doctorview.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.doctorview.app.R;
import com.doctorview.app.utils.AppUtils;

/**
 * Role selection shown to logged-out users:
 * choose Patient or Doctor before the matching Login screen.
 * The cards enter with a staggered hero animation.
 */
public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        View patientCard = findViewById(R.id.cardPatient);
        View doctorCard = findViewById(R.id.cardDoctor);

        patientCard.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));

        doctorCard.setOnClickListener(v ->
                startActivity(new Intent(this, DoctorLoginActivity.class)));

        // Staggered hero entrance
        patientCard.setAlpha(0f);
        patientCard.setTranslationY(70f);
        doctorCard.setAlpha(0f);
        doctorCard.setTranslationY(70f);

        patientCard.animate().alpha(1f).translationY(0f).setStartDelay(150).setDuration(550).start();
        doctorCard.animate().alpha(1f).translationY(0f).setStartDelay(320).setDuration(550).start();

        // Tactile press feedback on both cards
        AppUtils.applyPressScale(patientCard);
        AppUtils.applyPressScale(doctorCard);
    }
}

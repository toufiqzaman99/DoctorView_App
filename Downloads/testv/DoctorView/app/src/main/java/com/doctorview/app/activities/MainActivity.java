package com.doctorview.app.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.doctorview.app.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Main screen after login.
 * Hosts all the main sections (Home, Doctors, Appointments, Profile)
 * and switches between them with the bottom navigation bar.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.navHostFragment);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            // Connects the bottom navigation bar to the nav graph,
            // so tapping a tab opens the matching fragment.
            NavigationUI.setupWithNavController(bottomNav, navController);
        }
    }
}

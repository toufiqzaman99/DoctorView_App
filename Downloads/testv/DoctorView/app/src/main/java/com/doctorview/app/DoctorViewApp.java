package com.doctorview.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Application class — runs once before any screen.
 *
 * 1. Applies the saved dark-mode choice before the first activity appears.
 * 2. Prefers IPv4 networking for this app process (some networks,
 *    including emulators on IPv4-only hosts, break on IPv6).
 */
public class DoctorViewApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Apply the dark mode choice saved in Settings
        SharedPreferences prefs = getSharedPreferences("doctorview_settings", Context.MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(darkMode
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);

        // Must run before any network call is made
        System.setProperty("java.net.preferIPv4Stack", "true");
    }
}

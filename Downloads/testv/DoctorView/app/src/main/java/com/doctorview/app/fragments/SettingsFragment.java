package com.doctorview.app.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.doctorview.app.R;
import com.doctorview.app.utils.AppUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;

/**
 * Settings: notifications toggle, dark mode switch
 * and the About dialog. Choices are saved in SharedPreferences.
 */
public class SettingsFragment extends Fragment {

    private SharedPreferences prefs;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = requireContext().getSharedPreferences("doctorview_settings", Context.MODE_PRIVATE);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        // Notifications (prototype: the choice is saved for later use)
        MaterialSwitch swNotifications = view.findViewById(R.id.swNotifications);
        swNotifications.setChecked(prefs.getBoolean("notifications", true));
        swNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("notifications", isChecked).apply();
            AppUtils.showToast(requireContext(),
                    isChecked ? R.string.notifications_on : R.string.notifications_off);
        });

        // Dark mode: switches the whole app theme immediately
        MaterialSwitch swDarkMode = view.findViewById(R.id.swDarkMode);
        swDarkMode.setChecked(prefs.getBoolean("dark_mode", false));
        swDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("dark_mode", isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(isChecked
                    ? AppCompatDelegate.MODE_NIGHT_YES
                    : AppCompatDelegate.MODE_NIGHT_NO);
            requireActivity().recreate();
        });

        view.findViewById(R.id.rowAbout).setOnClickListener(v ->
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.settings_about)
                        .setMessage(R.string.about_message)
                        .setPositiveButton(R.string.btn_ok, null)
                        .show());
    }
}

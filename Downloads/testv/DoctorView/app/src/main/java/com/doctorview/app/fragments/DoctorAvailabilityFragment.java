package com.doctorview.app.fragments;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.doctorview.app.R;
import com.doctorview.app.firebase.FirebaseHelper;
import com.doctorview.app.utils.AppUtils;
import com.doctorview.app.utils.Constants;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Doctor Availability: one row per weekday with an Available switch
 * and start/end time pickers. Saved to users/{uid} and doctors/{uid}.
 */
public class DoctorAvailabilityFragment extends Fragment {

    private static final String[] DAY_LABELS = {
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    };

    private final Map<String, MaterialSwitch> switches = new HashMap<>();
    private final Map<String, TextView> startViews = new HashMap<>();
    private final Map<String, TextView> endViews = new HashMap<>();
    private ProgressBar progressBar;

    public DoctorAvailabilityFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_doctor_availability, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.progressBar);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        buildDayRows(view.findViewById(R.id.daysContainer));

        Button btnSave = view.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> saveAvailability());

        loadCurrentAvailability();
    }

    /** Creates the 7 weekday rows. */
    private void buildDayRows(LinearLayout container) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (int i = 0; i < Constants.DAYS.length; i++) {
            String dayKey = Constants.DAYS[i];
            View row = inflater.inflate(R.layout.item_availability_day, container, false);
            ((TextView) row.findViewById(R.id.tvDayName)).setText(DAY_LABELS[i]);
            switches.put(dayKey, row.findViewById(R.id.swAvailable));
            startViews.put(dayKey, row.findViewById(R.id.tvStart));
            endViews.put(dayKey, row.findViewById(R.id.tvEnd));

            final String key = dayKey;
            row.findViewById(R.id.tvStart).setOnClickListener(v ->
                    showTimePicker(startViews.get(key), key));
            row.findViewById(R.id.tvEnd).setOnClickListener(v ->
                    showTimePicker(endViews.get(key), key));

            container.addView(row);
        }
    }

    /** Loads the saved availability and prefills the rows. */
    private void loadCurrentAvailability() {
        FirebaseUser firebaseUser = FirebaseHelper.getAuth().getCurrentUser();
        if (firebaseUser == null) {
            return;
        }
        FirebaseHelper.getFirestore()
                .collection(Constants.COLLECTION_USERS)
                .document(firebaseUser.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (!isAdded() || !doc.exists()) {
                        return;
                    }
                    Object raw = doc.get("availability");
                    if (raw instanceof Map) {
                        Map<String, Object> availability = (Map<String, Object>) raw;
                        for (String day : Constants.DAYS) {
                            Object dayRaw = availability.get(day);
                            if (dayRaw instanceof Map) {
                                Map<String, Object> dayMap = (Map<String, Object>) dayRaw;
                                MaterialSwitch sw = switches.get(day);
                                if (sw != null) {
                                    sw.setChecked(!Boolean.FALSE.equals(dayMap.get("available")));
                                }
                                TextView tvStart = startViews.get(day);
                                if (tvStart != null && dayMap.get("start") != null) {
                                    tvStart.setText(dayMap.get("start").toString());
                                }
                                TextView tvEnd = endViews.get(day);
                                if (tvEnd != null && dayMap.get("end") != null) {
                                    tvEnd.setText(dayMap.get("end").toString());
                                }
                            }
                        }
                    }
                });
    }

    private void showTimePicker(TextView target, String dayKey) {
        int hour = 9;
        int minute = 0;
        String current = target.getText().toString();
        String[] parts = current.split(":");
        if (parts.length == 2) {
            try {
                hour = Integer.parseInt(parts[0]);
                minute = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ignored) {
                // defaults
            }
        }
        new TimePickerDialog(requireContext(), (picker, h, m) -> {
            target.setText(String.format(Locale.US, "%02d:%02d", h, m));
        }, hour, minute, true).show();
    }

    /** Writes the availability map to users/{uid} and doctors/{uid}. */
    private void saveAvailability() {
        FirebaseUser firebaseUser = FirebaseHelper.getAuth().getCurrentUser();
        if (firebaseUser == null) {
            return;
        }

        Map<String, Object> availability = new HashMap<>();
        for (String day : Constants.DAYS) {
            Map<String, Object> dayMap = new HashMap<>();
            MaterialSwitch sw = switches.get(day);
            TextView tvStart = startViews.get(day);
            TextView tvEnd = endViews.get(day);
            dayMap.put("available", sw != null && sw.isChecked());
            dayMap.put("start", tvStart != null ? tvStart.getText().toString() : "09:00");
            dayMap.put("end", tvEnd != null ? tvEnd.getText().toString() : "17:00");
            availability.put(day, dayMap);
        }

        progressBar.setVisibility(View.VISIBLE);
        String uid = firebaseUser.getUid();

        FirebaseHelper.getFirestore()
                .collection(Constants.COLLECTION_USERS)
                .document(uid)
                .update("availability", availability)
                .addOnCompleteListener(userTask -> {
                    FirebaseHelper.getFirestore()
                            .collection(Constants.COLLECTION_DOCTORS)
                            .document(uid)
                            .update("availability", availability)
                            .addOnCompleteListener(doctorTask -> {
                                progressBar.setVisibility(View.GONE);
                                if (userTask.isSuccessful() && doctorTask.isSuccessful()) {
                                    AppUtils.showToast(requireContext(), R.string.availability_saved);
                                    Navigation.findNavController(requireView()).popBackStack();
                                } else {
                                    AppUtils.showToast(requireContext(), R.string.save_error);
                                }
                            });
                });
    }
}

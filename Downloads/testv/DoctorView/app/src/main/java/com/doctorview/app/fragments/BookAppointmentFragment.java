package com.doctorview.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.doctorview.app.R;
import com.doctorview.app.firebase.FirebaseHelper;
import com.doctorview.app.models.Appointment;
import com.doctorview.app.models.Doctor;
import com.doctorview.app.models.User;
import com.doctorview.app.utils.AppUtils;
import com.doctorview.app.utils.Constants;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Appointment Booking: choose a date (calendar dialog) and a time slot,
 * add an optional note, then save the appointment to Cloud Firestore.
 */
public class BookAppointmentFragment extends Fragment {

    // Time slots shown as selectable chips
    private static final String[] TIME_SLOTS = {
            "9:00 AM", "10:00 AM", "11:00 AM", "12:00 PM",
            "3:00 PM", "4:00 PM", "5:00 PM", "6:00 PM"
    };

    private View contentContainer;
    private View browseContainer;
    private ProgressBar progressBar;
    private TextInputLayout tilDate;
    private TextInputEditText etDate;
    private ChipGroup chipGroup;
    private TextInputEditText etNote;
    private Doctor doctor;
    private String doctorId;
    private String selectedDate; // stored as yyyy-MM-dd so lists can sort it
    private String selectedTime;
    private String preselectedDateIso;

    public BookAppointmentFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_book_appointment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        contentContainer = view.findViewById(R.id.contentContainer);
        browseContainer = view.findViewById(R.id.browseContainer);
        progressBar = view.findViewById(R.id.progressBar);
        tilDate = view.findViewById(R.id.tilDate);
        etDate = view.findViewById(R.id.etDate);
        chipGroup = view.findViewById(R.id.chipGroup);
        etNote = view.findViewById(R.id.etNote);
        Button btnConfirm = view.findViewById(R.id.btnConfirm);
        Button btnBrowseDoctors = view.findViewById(R.id.btnBrowseDoctors);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        Bundle args = getArguments();
        doctorId = args != null ? args.getString("doctorId", "") : "";
        preselectedDateIso = args != null ? args.getString("selectedDate", "") : "";

        // Opened without a doctor (e.g. from the Home quick access) → point to Doctors
        if (doctorId == null || doctorId.isEmpty()) {
            showBrowseDoctors();
            btnBrowseDoctors.setOnClickListener(v -> {
                NavOptions options = new NavOptions.Builder()
                        .setPopUpTo(R.id.homeFragment, false)
                        .build();
                Navigation.findNavController(v).navigate(R.id.doctorsFragment, null, options);
            });
            return;
        }

        loadDoctor(doctorId);
        buildTimeSlots();

        // The date field is read-only; tapping the box (or the field itself)
        // opens the calendar dialog
        tilDate.setOnClickListener(v -> showDatePicker());
        etDate.setOnClickListener(v -> showDatePicker());

        btnConfirm.setOnClickListener(v -> confirmBooking());
        AppUtils.applyPressScale(btnConfirm);
    }

    /** Loads the chosen doctor so the card shows name, specialty and fee. */
    private void loadDoctor(String id) {
        showLoading();

        FirebaseHelper.getFirestore()
                .collection(Constants.COLLECTION_DOCTORS)
                .document(id)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!isAdded()) {
                        return;
                    }
                    if (doc.exists()) {
                        doctor = doc.toObject(Doctor.class);
                        if (doctor != null) {
                            doctor.setId(doc.getId());
                        }
                    }
                    if (doctor == null) {
                        showBrowseDoctors();
                        return;
                    }
                    populateDoctorCard();
                    showContent();
                    applyPreselectedDate();
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        showBrowseDoctors();
                    }
                });
    }

    /** Fills the doctor summary card. */
    private void populateDoctorCard() {
        ShapeableImageView ivPhoto = contentContainer.findViewById(R.id.ivDoctorPhoto);
        TextView tvName = contentContainer.findViewById(R.id.tvDoctorName);
        TextView tvInfo = contentContainer.findViewById(R.id.tvDoctorInfo);

        Glide.with(requireContext())
                .load(doctor.getImageUrl())
                .placeholder(R.drawable.ic_doctor_avatar)
                .error(R.drawable.ic_doctor_avatar)
                .into(ivPhoto);

        tvName.setText(doctor.getName());
        tvInfo.setText(doctor.getSpecialty() + " · " + getString(R.string.fee_value,
                String.format(Locale.getDefault(), "%.0f", doctor.getFee())));

        // Appointment summary card
        ((TextView) contentContainer.findViewById(R.id.tvSummaryDoctor)).setText(doctor.getName());
        ((TextView) contentContainer.findViewById(R.id.tvSummaryFee))
                .setText(getString(R.string.fee_per_session,
                        String.format(Locale.getDefault(), "%.0f", doctor.getFee())));
    }

    /** Creates one selectable chip per time slot. */
    private void buildTimeSlots() {
        for (String slot : TIME_SLOTS) {
            Chip chip = AppUtils.createFilterChip(requireContext(), slot);
            chipGroup.addView(chip);
        }
        chipGroup.setSingleSelection(true);
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            selectedTime = checkedIds.isEmpty()
                    ? null
                    : ((Chip) group.findViewById(checkedIds.get(0))).getText().toString();
            TextView tvSummaryTime = contentContainer.findViewById(R.id.tvSummaryTime);
            if (tvSummaryTime != null) {
                tvSummaryTime.setText(selectedTime != null
                        ? selectedTime
                        : getString(R.string.summary_not_selected));
            }
        });
    }

    /** Applies a date picked on the Doctor Details schedule strip. */
    private void applyPreselectedDate() {
        if (preselectedDateIso == null || preselectedDateIso.isEmpty()) {
            return;
        }
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                    .parse(preselectedDateIso);
            SimpleDateFormat prettyFormat =
                    new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());
            selectedDate = preselectedDateIso;
            etDate.setText(prettyFormat.format(date));
            TextView tvSummaryDate = contentContainer.findViewById(R.id.tvSummaryDate);
            if (tvSummaryDate != null) {
                tvSummaryDate.setText(prettyFormat.format(date));
            }
            applyAvailabilitySlots(date);
        } catch (Exception ignored) {
            // keep the default (today)
        }
    }

    /**
     * Rebuilds the time chips from the doctor's saved availability for that
     * weekday. Without availability data, the default slots are used.
     */
    private void applyAvailabilitySlots(Date date) {
        String dayKey = new SimpleDateFormat("EEEE", Locale.ENGLISH)
                .format(date).toLowerCase(Locale.ROOT);

        chipGroup.removeAllViews();

        boolean available = true;
        String start = null;
        String end = null;
        if (doctor.getAvailability() != null
                && doctor.getAvailability().get(dayKey) instanceof Map) {
            Map<String, Object> day = (Map<String, Object>) doctor.getAvailability().get(dayKey);
            available = !Boolean.FALSE.equals(day.get("available"));
            start = day.get("start") != null ? day.get("start").toString() : null;
            end = day.get("end") != null ? day.get("end").toString() : null;
        }

        TextView tvNotice = contentContainer.findViewById(R.id.tvAvailabilityNotice);
        if (!available) {
            if (tvNotice != null) {
                tvNotice.setVisibility(View.VISIBLE);
                tvNotice.setText(R.string.not_available_day);
            }
            return;
        }
        if (tvNotice != null) {
            tvNotice.setVisibility(View.GONE);
        }

        List<String> slots = new ArrayList<>();
        if (start != null && end != null) {
            try {
                SimpleDateFormat parse = new SimpleDateFormat("HH:mm", Locale.US);
                SimpleDateFormat format = new SimpleDateFormat("h:mm a", Locale.getDefault());
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTime(parse.parse(start));
                java.util.Date endTime = parse.parse(end);
                while (cal.getTime().before(endTime)) {
                    slots.add(format.format(cal.getTime()));
                    cal.add(java.util.Calendar.HOUR_OF_DAY, 1);
                }
            } catch (Exception ignored) {
                slots.clear();
            }
        }
        if (slots.isEmpty()) {
            slots.addAll(Arrays.asList(TIME_SLOTS));
        }
        for (String slot : slots) {
            chipGroup.addView(AppUtils.createFilterChip(requireContext(), slot));
        }
    }

    /** Opens the Material calendar dialog (past dates are disabled). */
    private void showDatePicker() {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.date_picker_title)
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(new CalendarConstraints.Builder()
                        .setValidator(DateValidatorPointForward.now())
                        .build())
                .build();
        picker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat prettyFormat =
                    new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());
            Date date = new Date(selection);
            selectedDate = isoFormat.format(date);
            etDate.setText(prettyFormat.format(date));
            TextView tvSummaryDate = contentContainer.findViewById(R.id.tvSummaryDate);
            if (tvSummaryDate != null) {
                tvSummaryDate.setText(prettyFormat.format(date));
            }
            // The time selection resets and the slots follow the doctor's availability
            selectedTime = null;
            TextView tvSummaryTime = contentContainer.findViewById(R.id.tvSummaryTime);
            if (tvSummaryTime != null) {
                tvSummaryTime.setText(R.string.summary_not_selected);
            }
            applyAvailabilitySlots(date);
        });
        picker.show(getParentFragmentManager(), "datePicker");
    }

    /** Validates the selection and saves the appointment to Firestore. */
    private void confirmBooking() {
        if (selectedDate == null) {
            AppUtils.showToast(requireContext(), R.string.please_select_date);
            return;
        }
        if (selectedTime == null) {
            AppUtils.showToast(requireContext(), R.string.please_select_time);
            return;
        }
        FirebaseUser user = FirebaseHelper.getAuth().getCurrentUser();
        if (user == null) {
            return;
        }

        showLoading();

        // Fetch the patient's profile name, then save the appointment
        FirebaseHelper.getFirestore()
                .collection(Constants.COLLECTION_USERS)
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    String patientName = user.getEmail() != null
                            ? user.getEmail().split("@")[0] : "Patient";
                    if (doc.exists()) {
                        User profile = doc.toObject(User.class);
                        if (profile != null && profile.getName() != null
                                && !profile.getName().isEmpty()) {
                            patientName = profile.getName();
                        }
                    }
                    saveAppointment(user, patientName);
                })
                .addOnFailureListener(e -> {
                    showLoading();
                    AppUtils.showToast(requireContext(), R.string.booking_error);
                });
    }

    /** Writes the appointment with the patient name and reason included. */
    private void saveAppointment(FirebaseUser user, String patientName) {
        String reason = textOf(etNote);

        Appointment appointment = new Appointment(null, user.getUid(), doctorId,
                doctor != null ? doctor.getName() : "",
                selectedDate, selectedTime, Constants.STATUS_PENDING);
        if (doctor != null) {
            appointment.setSpecialty(doctor.getSpecialty());
        }
        appointment.setNote(reason);
        appointment.setPatientName(patientName);
        appointment.setReason(reason);

        FirebaseHelper.getFirestore()
                .collection(Constants.COLLECTION_APPOINTMENTS)
                .add(appointment)
                .addOnSuccessListener(documentReference -> {
                    showLoading();
                    AppUtils.showToast(requireContext(), R.string.booking_success);
                    // Jump to the My Appointments tab (clears the back stack)
                    NavOptions options = new NavOptions.Builder()
                            .setPopUpTo(R.id.homeFragment, false)
                            .build();
                    Navigation.findNavController(requireView())
                            .navigate(R.id.appointmentsFragment, null, options);
                })
                .addOnFailureListener(e -> {
                    showLoading();
                    AppUtils.showToast(requireContext(), R.string.booking_error);
                });
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        contentContainer.setVisibility(View.GONE);
        browseContainer.setVisibility(View.GONE);
    }

    private void showContent() {
        progressBar.setVisibility(View.GONE);
        contentContainer.setVisibility(View.VISIBLE);
        browseContainer.setVisibility(View.GONE);
    }

    private void showBrowseDoctors() {
        progressBar.setVisibility(View.GONE);
        contentContainer.setVisibility(View.GONE);
        browseContainer.setVisibility(View.VISIBLE);
    }

    private String textOf(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
}

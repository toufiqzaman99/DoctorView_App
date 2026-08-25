package com.doctorview.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.doctorview.app.R;
import com.doctorview.app.firebase.FirebaseHelper;
import com.doctorview.app.models.Doctor;
import com.doctorview.app.utils.AppUtils;
import com.doctorview.app.utils.Constants;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Doctor Details: full profile of one doctor, loaded from Firestore
 * by the document id passed from the Doctors list.
 */
public class DoctorDetailsFragment extends Fragment {

    private View contentContainer;
    private View rootView;
    private ProgressBar progressBar;
    private TextView tvNotFound;
    private Doctor doctor;
    private String selectedDateIso;

    public DoctorDetailsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_doctor_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rootView = view;
        contentContainer = view.findViewById(R.id.contentContainer);
        progressBar = view.findViewById(R.id.progressBar);
        tvNotFound = view.findViewById(R.id.tvNotFound);

        View btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        // Top bar icons (prototype: informative toasts)
        view.findViewById(R.id.btnMsgIcon).setOnClickListener(v ->
                AppUtils.showToast(requireContext(), R.string.chat_with_doctor_hint));
        view.findViewById(R.id.btnCallIcon).setOnClickListener(v ->
                AppUtils.showToast(requireContext(), R.string.call_doctor_hint));

        // See more / See less toggle for the Details paragraph
        TextView tvSeeMore = view.findViewById(R.id.tvSeeMore);
        TextView tvAbout = view.findViewById(R.id.tvAbout);
        tvSeeMore.setOnClickListener(v -> {
            boolean expanded = tvAbout.getMaxLines() != 2;
            tvAbout.setMaxLines(expanded ? 2 : Integer.MAX_VALUE);
            tvSeeMore.setText(expanded ? R.string.see_more : R.string.see_less);
        });

        buildDateStrip(view);

        String doctorId = getArguments() != null ? getArguments().getString("doctorId") : null;
        if (doctorId == null || doctorId.isEmpty()) {
            showNotFound();
        } else {
            loadDoctor(doctorId);
        }

        // About / Availability / Experience / Education tabs
        View llAbout = view.findViewById(R.id.llTabAbout);
        View llAvailability = view.findViewById(R.id.llTabAvailability);
        View llExperience = view.findViewById(R.id.llTabExperience);
        View llEducation = view.findViewById(R.id.llTabEducation);
        ChipGroup chipGroupTabs = view.findViewById(R.id.chipGroupTabs);
        chipGroupTabs.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                return;
            }
            int id = checkedIds.get(0);
            llAbout.setVisibility(id == R.id.chipTabAbout ? View.VISIBLE : View.GONE);
            llAvailability.setVisibility(id == R.id.chipTabAvailability ? View.VISIBLE : View.GONE);
            llExperience.setVisibility(id == R.id.chipTabExperience ? View.VISIBLE : View.GONE);
            llEducation.setVisibility(id == R.id.chipTabEducation ? View.VISIBLE : View.GONE);
        });
    }

    /** Reads the doctor's document from Firestore. */
    private void loadDoctor(String doctorId) {
        showLoading();

        FirebaseHelper.getFirestore()
                .collection(Constants.COLLECTION_DOCTORS)
                .document(doctorId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!isAdded()) {
                        return;
                    }
                    if (doc.exists()) {
                        doctor = doc.toObject(Doctor.class);
                        if (doctor != null) {
                            doctor.setId(doc.getId());
                            populateViews();
                            showContent();
                        } else {
                            showNotFound();
                        }
                    } else {
                        showNotFound();
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        showNotFound();
                    }
                });
    }

    /** Fills the screen with the loaded doctor's data. */
    private void populateViews() {
        ShapeableImageView ivPhoto = contentContainer.findViewById(R.id.ivDoctorPhoto);
        TextView tvName = contentContainer.findViewById(R.id.tvDoctorName);
        TextView tvSpecialty = contentContainer.findViewById(R.id.tvDoctorSpecialty);
        TextView tvHospital = contentContainer.findViewById(R.id.tvDoctorHospital);
        TextView tvRating = contentContainer.findViewById(R.id.tvRatingValue);
        TextView tvExperience = contentContainer.findViewById(R.id.tvExperienceValue);
        TextView tvFee = contentContainer.findViewById(R.id.tvFeeValue);
        TextView tvAbout = contentContainer.findViewById(R.id.tvAbout);
        // The Book Now button is pinned outside the scroll container
        Button btnBookAppointment = rootView.findViewById(R.id.btnBookAppointment);

        Glide.with(requireContext())
                .load(doctor.getImageUrl())
                .placeholder(R.drawable.ic_doctor_avatar)
                .error(R.drawable.ic_doctor_avatar)
                .into(ivPhoto);

        tvName.setText(doctor.getName());
        tvSpecialty.setText(doctor.getSpecialty());
        tvHospital.setText(doctor.getHospital());
        tvRating.setText(String.format(Locale.getDefault(), "%.1f", doctor.getRating()));
        tvExperience.setText(getString(R.string.years_value, doctor.getExperienceYears()));
        tvFee.setText(String.format(Locale.getDefault(), "%.0f", doctor.getFee()));
        tvAbout.setText(doctor.getAbout());
        TextView tvAboutTabBody = contentContainer.findViewById(R.id.tvAboutTabBody);
        if (tvAboutTabBody != null) {
            tvAboutTabBody.setText(doctor.getAbout());
        }

        // Extra fields of the redesigned screen
        TextView tvDoctorFee = contentContainer.findViewById(R.id.tvDoctorFee);
        tvDoctorFee.setText(getString(R.string.fee_per_session,
                String.format(Locale.getDefault(), "%.0f", doctor.getFee())));

        TextView tvExperienceDetail = contentContainer.findViewById(R.id.tvExperienceDetail);
        tvExperienceDetail.setText(getString(R.string.experience_detail,
                doctor.getExperienceYears(), doctor.getHospital()));

        TextView tvEducationDetail = contentContainer.findViewById(R.id.tvEducationDetail);
        tvEducationDetail.setText(getString(R.string.education_detail, doctor.getHospital()));

        // Hand the doctor (and the picked schedule date, if any) to the booking screen
        btnBookAppointment.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("doctorId", doctor.getId());
            args.putString("doctorName", doctor.getName());
            if (selectedDateIso != null) {
                args.putString("selectedDate", selectedDateIso);
            }
            Navigation.findNavController(v)
                    .navigate(R.id.action_doctorDetailsFragment_to_bookAppointmentFragment, args);
        });
    }

    /** Builds the reference-style horizontal date selector (next 7 days). */
    private void buildDateStrip(View view) {
        LinearLayout dateContainer = view.findViewById(R.id.dateContainer);
        TextView tvMonth = view.findViewById(R.id.tvScheduleMonth);
        SimpleDateFormat dayNameFormat = new SimpleDateFormat("EEE", Locale.ENGLISH);
        SimpleDateFormat dayNumFormat = new SimpleDateFormat("dd", Locale.ENGLISH);
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.ENGLISH);

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        tvMonth.setText(monthFormat.format(calendar.getTime()));

        for (int i = 0; i < 7; i++) {
            final java.util.Date day = calendar.getTime();
            final String iso = isoFormat.format(day);

            View dayView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_date_day, dateContainer, false);
            ((TextView) dayView.findViewById(R.id.tvDayName)).setText(dayNameFormat.format(day));
            ((TextView) dayView.findViewById(R.id.tvDayNum)).setText(dayNumFormat.format(day));

            if (i == 0) {
                highlightDay(dayView, true);
                selectedDateIso = iso;
            }
            dayView.setOnClickListener(v -> {
                // Un-highlight previous, highlight this one
                for (int j = 0; j < dateContainer.getChildCount(); j++) {
                    highlightDay(dateContainer.getChildAt(j), dateContainer.getChildAt(j) == v);
                }
                selectedDateIso = iso;
            });
            dateContainer.addView(dayView);
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1);
        }
    }

    /** Selected day: teal card with white text; otherwise white with border. */
    private void highlightDay(View dayView, boolean selected) {
        com.google.android.material.card.MaterialCardView card =
                dayView.findViewById(R.id.dateCard);
        TextView tvDayName = dayView.findViewById(R.id.tvDayName);
        TextView tvDayNum = dayView.findViewById(R.id.tvDayNum);
        int cardColor = getResources().getColor(
                selected ? R.color.primary : R.color.white, null);
        int textColor = getResources().getColor(
                selected ? R.color.white : R.color.text_primary, null);
        int subColor = getResources().getColor(
                selected ? R.color.white_80 : R.color.text_secondary, null);
        card.setCardBackgroundColor(cardColor);
        tvDayName.setTextColor(subColor);
        tvDayNum.setTextColor(textColor);
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        contentContainer.setVisibility(View.GONE);
        tvNotFound.setVisibility(View.GONE);
    }

    private void showContent() {
        progressBar.setVisibility(View.GONE);
        contentContainer.setVisibility(View.VISIBLE);
        tvNotFound.setVisibility(View.GONE);
    }

    private void showNotFound() {
        progressBar.setVisibility(View.GONE);
        contentContainer.setVisibility(View.GONE);
        tvNotFound.setVisibility(View.VISIBLE);
    }
}

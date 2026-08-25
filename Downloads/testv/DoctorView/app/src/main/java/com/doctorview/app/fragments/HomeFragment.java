package com.doctorview.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.doctorview.app.R;
import com.doctorview.app.adapters.FeatureAdapter;
import com.doctorview.app.adapters.SpecialtyAdapter;
import com.doctorview.app.adapters.TopDoctorAdapter;
import com.doctorview.app.firebase.FirebaseHelper;
import com.doctorview.app.models.Appointment;
import com.doctorview.app.models.Doctor;
import com.doctorview.app.models.Feature;
import com.doctorview.app.models.User;
import com.doctorview.app.utils.AppUtils;
import com.doctorview.app.utils.Constants;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Reference-style Patient Home: gradient header with profile, the
 * upcoming appointment card, circular doctor specialties, the popular
 * doctor card and the quick-access grid.
 */
public class HomeFragment extends Fragment {

    private final List<Doctor> doctors = new ArrayList<>();

    private TopDoctorAdapter topDoctorAdapter;
    private SpecialtyAdapter specialtyAdapter;
    private View featuredCard;
    private String selectedSpecialty = "All";
    private Doctor featuredDoctor;
    private Appointment upcomingAppointment;
    private boolean favoriteToggled = false;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadUserName((TextView) view.findViewById(R.id.tvUserName),
                (TextView) view.findViewById(R.id.tvUserLocation));

        view.findViewById(R.id.btnNotifications).setOnClickListener(v ->
                AppUtils.showToast(requireContext(), R.string.notifications_none));
        AppUtils.applyPressScale(view.findViewById(R.id.btnNotifications));

        featuredCard = view.findViewById(R.id.featuredCard);

        // Circular doctor specialties
        RecyclerView rvSpecialties = view.findViewById(R.id.rvSpecialties);
        rvSpecialties.setLayoutManager(new LinearLayoutManager(requireContext(),
                LinearLayoutManager.HORIZONTAL, false));
        specialtyAdapter = new SpecialtyAdapter(AppUtils.SPECIALTIES, (position, specialty) -> {
            selectedSpecialty = specialty;
            specialtyAdapter.setSelectedIndex(position);
            applySpecialtyFilter();
        });
        rvSpecialties.setAdapter(specialtyAdapter);

        // View Details links
        view.findViewById(R.id.tvViewDetailsSpecialty).setOnClickListener(v -> {
            NavOptions options = new NavOptions.Builder()
                    .setPopUpTo(R.id.homeFragment, false).build();
            Navigation.findNavController(v).navigate(R.id.doctorsFragment, null, options);
        });
        view.findViewById(R.id.tvViewDetailsPopular).setOnClickListener(v -> {
            if (featuredDoctor != null) {
                openDoctor(view, featuredDoctor);
            }
        });

        // Heart / favorite toggle
        ImageView ivFavoriteIcon = view.findViewById(R.id.ivFavoriteIcon);
        view.findViewById(R.id.btnFavorite).setOnClickListener(v -> {
            favoriteToggled = !favoriteToggled;
            ivFavoriteIcon.setColorFilter(getResources().getColor(
                    favoriteToggled ? R.color.error : R.color.primary, null));
            AppUtils.showToast(requireContext(),
                    favoriteToggled ? R.string.added_favorite : R.string.removed_favorite);
        });
        AppUtils.applyPressScale(view.findViewById(R.id.btnFavorite));

        // Top rated doctors (horizontal)
        RecyclerView rvTopDoctors = view.findViewById(R.id.rvTopDoctors);
        rvTopDoctors.setLayoutManager(new LinearLayoutManager(requireContext(),
                LinearLayoutManager.HORIZONTAL, false));
        topDoctorAdapter = new TopDoctorAdapter(new TopDoctorAdapter.OnTopDoctorListener() {
            @Override
            public void onDoctorClick(Doctor doctor) {
                openDoctor(view, doctor);
            }

            @Override
            public void onConsultClick(Doctor doctor) {
                bookDoctor(view, doctor);
            }
        });
        rvTopDoctors.setAdapter(topDoctorAdapter);

        // Quick-access grid (2 columns)
        RecyclerView rvFeatures = view.findViewById(R.id.rvFeatures);
        rvFeatures.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        rvFeatures.setAdapter(new FeatureAdapter(buildFeatures(), feature ->
                Navigation.findNavController(view).navigate(feature.getNavActionRes())));

        loadDoctors();
        loadUpcomingAppointment(view);
    }

    /** Loads the user's name and uses their phone as the location line. */
    private void loadUserName(TextView tvUserName, TextView tvUserLocation) {
        FirebaseUser firebaseUser = FirebaseHelper.getAuth().getCurrentUser();
        if (firebaseUser == null || firebaseUser.getEmail() == null) {
            return;
        }
        tvUserName.setText(firebaseUser.getEmail().split("@")[0]);

        FirebaseHelper.getFirestore()
                .collection(Constants.COLLECTION_USERS)
                .document(firebaseUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded()) {
                        return;
                    }
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            if (user.getName() != null && !user.getName().isEmpty()) {
                                tvUserName.setText(user.getName());
                            }
                            if (user.getPhone() != null && !user.getPhone().isEmpty()) {
                                tvUserLocation.setText(user.getPhone());
                            }
                        }
                    }
                });
    }

    /** Shows the patient's next pending/confirmed appointment in the header card. */
    private void loadUpcomingAppointment(View view) {
        FirebaseUser firebaseUser = FirebaseHelper.getAuth().getCurrentUser();
        if (firebaseUser == null) {
            return;
        }
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());

        FirebaseHelper.getFirestore()
                .collection(Constants.COLLECTION_APPOINTMENTS)
                .whereEqualTo("userId", firebaseUser.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!isAdded()) {
                        return;
                    }
                    List<Appointment> upcoming = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Appointment appointment = doc.toObject(Appointment.class);
                        if (appointment.getDate() == null) {
                            continue;
                        }
                        boolean relevant = Constants.STATUS_PENDING.equals(appointment.getStatus())
                                || Constants.STATUS_CONFIRMED.equals(appointment.getStatus());
                        if (relevant && appointment.getDate().compareTo(today) >= 0) {
                            appointment.setId(doc.getId());
                            upcoming.add(appointment);
                        }
                    }
                    Collections.sort(upcoming, (a, b) -> {
                        int byDate = a.getDate().compareTo(b.getDate());
                        if (byDate != 0) {
                            return byDate;
                        }
                        String ta = a.getTime() == null ? "" : a.getTime();
                        String tb = b.getTime() == null ? "" : b.getTime();
                        return ta.compareTo(tb);
                    });
                    bindUpcomingCard(view, upcoming.isEmpty() ? null : upcoming.get(0));
                })
                .addOnFailureListener(e -> bindUpcomingCard(view, null));
    }

    /** Fills or hides the Upcoming Appointments card. */
    private void bindUpcomingCard(View view, Appointment appointment) {
        upcomingAppointment = appointment;

        TextView tvName = view.findViewById(R.id.tvAptDoctorName);
        TextView tvSpecialty = view.findViewById(R.id.tvAptSpecialty);
        TextView tvEmpty = view.findViewById(R.id.tvAptEmpty);
        TextView tvDate = view.findViewById(R.id.tvAptDate);
        TextView tvTime = view.findViewById(R.id.tvAptTime);
        ShapeableImageView ivPhoto = view.findViewById(R.id.ivAptPhoto);
        LinearLayout rowDateTime = view.findViewById(R.id.rowAptDateTime);
        LinearLayout rowButtons = view.findViewById(R.id.rowAptButtons);
        TextView tvViewDetails = view.findViewById(R.id.tvViewDetailsApt);

        if (appointment == null) {
            tvName.setVisibility(View.GONE);
            tvSpecialty.setVisibility(View.GONE);
            ivPhoto.setVisibility(View.GONE);
            tvViewDetails.setVisibility(View.GONE);
            rowDateTime.setVisibility(View.GONE);
            rowButtons.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }

        tvEmpty.setVisibility(View.GONE);
        tvName.setVisibility(View.VISIBLE);
        tvSpecialty.setVisibility(View.VISIBLE);
        ivPhoto.setVisibility(View.VISIBLE);
        tvViewDetails.setVisibility(View.VISIBLE);
        rowDateTime.setVisibility(View.VISIBLE);
        rowButtons.setVisibility(View.VISIBLE);

        tvName.setText(appointment.getDoctorName());
        tvSpecialty.setText(appointment.getSpecialty());
        tvDate.setText(formatLongDate(appointment.getDate()));
        tvTime.setText(appointment.getTime());

        // Doctor photo for the card
        if (appointment.getDoctorId() != null && !appointment.getDoctorId().isEmpty()) {
            FirebaseHelper.getFirestore()
                    .collection(Constants.COLLECTION_DOCTORS)
                    .document(appointment.getDoctorId())
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (isAdded() && doc.exists()) {
                            Glide.with(requireContext())
                                    .load(doc.getString("imageUrl"))
                                    .placeholder(R.drawable.ic_doctor_avatar)
                                    .error(R.drawable.ic_doctor_avatar)
                                    .into(ivPhoto);
                        }
                    });
        }

        View btnReschedule = view.findViewById(R.id.btnReschedule);
        View btnJoinNow = view.findViewById(R.id.btnJoinNow);
        AppUtils.applyPressScale(btnReschedule);
        AppUtils.applyPressScale(btnJoinNow);

        btnReschedule.setOnClickListener(v -> {
            if (appointment.getDoctorId() != null && !appointment.getDoctorId().isEmpty()) {
                Bundle args = new Bundle();
                args.putString("doctorId", appointment.getDoctorId());
                args.putString("doctorName", appointment.getDoctorName());
                Navigation.findNavController(v)
                        .navigate(R.id.action_homeFragment_to_bookAppointmentFragment, args);
            } else {
                AppUtils.showToast(requireContext(), R.string.no_doctor_selected);
            }
        });

        btnJoinNow.setOnClickListener(v -> {
            if (appointment.getDoctorId() != null && !appointment.getDoctorId().isEmpty()) {
                Bundle args = new Bundle();
                args.putString("doctorId", appointment.getDoctorId());
                args.putString("doctorName", appointment.getDoctorName());
                Navigation.findNavController(v)
                        .navigate(R.id.action_homeFragment_to_chatRoomFragment, args);
            } else {
                AppUtils.showToast(requireContext(), R.string.no_doctor_selected);
            }
        });

        tvViewDetails.setOnClickListener(v -> {
            NavOptions options = new NavOptions.Builder()
                    .setPopUpTo(R.id.homeFragment, false).build();
            Navigation.findNavController(v).navigate(R.id.appointmentsFragment, null, options);
        });
    }

    /** "2026-08-20" → "20 August Thursday" (reference style). */
    private String formatLongDate(String dateIso) {
        if (dateIso == null) {
            return "";
        }
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateIso);
            return new SimpleDateFormat("d MMMM EEEE", Locale.ENGLISH).format(date);
        } catch (Exception ignored) {
            return dateIso;
        }
    }

    /** Loads all doctors from Firestore (featured + top-rated are derived locally). */
    private void loadDoctors() {
        FirebaseHelper.getFirestore()
                .collection(Constants.COLLECTION_DOCTORS)
                .orderBy("name")
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!isAdded()) {
                        return;
                    }
                    doctors.clear();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Doctor doctor = doc.toObject(Doctor.class);
                        doctor.setId(doc.getId());
                        doctors.add(doctor);
                    }
                    applySpecialtyFilter();
                })
                .addOnFailureListener(e -> applySpecialtyFilter());
    }

    /** Applies the selected specialty to the popular card and the top-rated row. */
    private void applySpecialtyFilter() {
        List<Doctor> matching = new ArrayList<>();
        for (Doctor doctor : doctors) {
            if ("All".equals(selectedSpecialty)
                    || (doctor.getSpecialty() != null
                        && doctor.getSpecialty().toLowerCase(Locale.getDefault())
                            .contains(selectedSpecialty.toLowerCase(Locale.getDefault())))) {
                matching.add(doctor);
            }
        }

        if (matching.isEmpty()) {
            featuredDoctor = null;
            featuredCard.setVisibility(View.GONE);
            topDoctorAdapter.setDoctors(new ArrayList<>());
            return;
        }
        featuredDoctor = matching.get(0);
        bindFeatured(featuredDoctor);
        if (featuredCard.getVisibility() != View.VISIBLE) {
            featuredCard.setVisibility(View.VISIBLE);
            featuredCard.startAnimation(
                    AnimationUtils.loadAnimation(requireContext(), R.anim.scale_in));
        }
        featuredCard.setOnClickListener(v -> openDoctor(requireView(), featuredDoctor));
        viewFindBtnBook().setOnClickListener(v -> bookDoctor(requireView(), featuredDoctor));
        AppUtils.applyPressScale(viewFindBtnBook());

        List<Doctor> top = new ArrayList<>(matching.subList(1, matching.size()));
        Collections.sort(top, (a, b) -> Double.compare(b.getRating(), a.getRating()));
        if (top.size() > 8) {
            top = new ArrayList<>(top.subList(0, 8));
        }
        topDoctorAdapter.setDoctors(top);
    }

    /** Fills the popular doctor card. */
    private void bindFeatured(Doctor doctor) {
        ShapeableImageView ivPhoto = featuredCard.findViewById(R.id.ivFeaturedPhoto);
        TextView tvName = featuredCard.findViewById(R.id.tvFeaturedName);
        TextView tvSpecialty = featuredCard.findViewById(R.id.tvFeaturedSpecialty);
        TextView tvExperience = featuredCard.findViewById(R.id.tvFeaturedExperience);
        TextView tvFee = featuredCard.findViewById(R.id.tvFeaturedFee);

        Glide.with(requireContext())
                .load(doctor.getImageUrl())
                .placeholder(R.drawable.ic_doctor_avatar)
                .error(R.drawable.ic_doctor_avatar)
                .into(ivPhoto);
        tvName.setText(doctor.getName());
        tvSpecialty.setText(doctor.getSpecialty());
        tvExperience.setText(getString(R.string.experience_years, doctor.getExperienceYears()));
        tvFee.setText(String.format(Locale.getDefault(), "★ %.1f", doctor.getRating()));
    }

    private View viewFindBtnBook() {
        return featuredCard.findViewById(R.id.btnBookAppointment);
    }

    /** Opens the doctor details screen. */
    private void openDoctor(View view, Doctor doctor) {
        Bundle args = new Bundle();
        args.putString("doctorId", doctor.getId());
        Navigation.findNavController(view)
                .navigate(R.id.action_homeFragment_to_doctorDetailsFragment, args);
    }

    /** Opens the booking screen directly for this doctor. */
    private void bookDoctor(View view, Doctor doctor) {
        Bundle args = new Bundle();
        args.putString("doctorId", doctor.getId());
        args.putString("doctorName", doctor.getName());
        Navigation.findNavController(view)
                .navigate(R.id.action_homeFragment_to_bookAppointmentFragment, args);
    }

    /** The quick-access cards shown on the Home screen. */
    private List<Feature> buildFeatures() {
        List<Feature> features = new ArrayList<>();
        features.add(new Feature(getString(R.string.title_symptom_analysis),
                R.drawable.ic_symptom, R.color.primary,
                R.id.action_homeFragment_to_symptomAnalysisFragment));
        features.add(new Feature(getString(R.string.title_disease_information),
                R.drawable.ic_disease, R.color.primary,
                R.id.action_homeFragment_to_diseaseInfoFragment));
        features.add(new Feature(getString(R.string.title_find_doctor),
                R.drawable.ic_doctors, R.color.primary,
                R.id.action_homeFragment_to_doctorsFragment));
        features.add(new Feature(getString(R.string.title_online_consultation),
                R.drawable.ic_chat, R.color.primary,
                R.id.action_homeFragment_to_chatFragment));
        features.add(new Feature(getString(R.string.title_healthcare_news),
                R.drawable.ic_news, R.color.primary,
                R.id.action_homeFragment_to_newsFragment));
        features.add(new Feature(getString(R.string.title_medical_records),
                R.drawable.ic_records, R.color.primary,
                R.id.action_homeFragment_to_recordsFragment));
        features.add(new Feature(getString(R.string.title_emergency_help),
                R.drawable.ic_emergency, R.color.feature_rose,
                R.id.action_homeFragment_to_emergencyFragment));
        features.add(new Feature(getString(R.string.title_settings),
                R.drawable.ic_settings, R.color.primary,
                R.id.action_homeFragment_to_settingsFragment));
        return features;
    }
}

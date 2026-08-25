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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.doctorview.app.R;
import com.doctorview.app.adapters.DoctorAdapter;
import com.doctorview.app.firebase.FirebaseHelper;
import com.doctorview.app.models.Doctor;
import com.doctorview.app.utils.Constants;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Online Consultation: pick the doctor you want to chat with.
 * The actual chat room is ChatRoomFragment (Realtime Database).
 */
public class ChatFragment extends Fragment {

    private final List<Doctor> doctors = new ArrayList<>();

    private RecyclerView rvDoctors;
    private ProgressBar progressBar;
    private View emptyContainer;
    private TextView tvEmptyTitle;
    private TextView tvEmptyText;
    private DoctorAdapter adapter;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvDoctors = view.findViewById(R.id.rvDoctors);
        progressBar = view.findViewById(R.id.progressBar);
        emptyContainer = view.findViewById(R.id.emptyContainer);
        tvEmptyTitle = view.findViewById(R.id.tvEmptyTitle);
        tvEmptyText = view.findViewById(R.id.tvEmptyText);
        Button btnBrowseDoctors = view.findViewById(R.id.btnBrowseDoctors);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        rvDoctors.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new DoctorAdapter(doctors, new DoctorAdapter.OnDoctorClickListener() {
            @Override
            public void onDoctorClick(Doctor doctor) {
                Bundle args = new Bundle();
                args.putString("doctorId", doctor.getId());
                args.putString("doctorName", doctor.getName());
                Navigation.findNavController(view)
                        .navigate(R.id.action_chatFragment_to_chatRoomFragment, args);
            }

            @Override
            public void onConsultClick(Doctor doctor) {
                // Consult is not used on the chat picker screen
            }
        });
        rvDoctors.setAdapter(adapter);

        // If the Doctors tab is still empty, send the user there to load samples
        btnBrowseDoctors.setOnClickListener(v -> {
            NavOptions options = new NavOptions.Builder()
                    .setPopUpTo(R.id.homeFragment, false)
                    .build();
            Navigation.findNavController(v).navigate(R.id.doctorsFragment, null, options);
        });

        loadDoctors();
    }

    /** Loads the doctor list from Firestore so the user can pick one. */
    private void loadDoctors() {
        showLoading();

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
                    adapter.setDoctors(doctors);
                    if (doctors.isEmpty()) {
                        showEmpty();
                    } else {
                        showList();
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        showEmpty();
                    }
                });
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        rvDoctors.setVisibility(View.GONE);
        emptyContainer.setVisibility(View.GONE);
    }

    private void showList() {
        progressBar.setVisibility(View.GONE);
        rvDoctors.setVisibility(View.VISIBLE);
        emptyContainer.setVisibility(View.GONE);
    }

    private void showEmpty() {
        progressBar.setVisibility(View.GONE);
        rvDoctors.setVisibility(View.GONE);
        emptyContainer.setVisibility(View.VISIBLE);
        tvEmptyTitle.setText(R.string.chat_no_doctors_title);
        tvEmptyText.setText(R.string.chat_no_doctors_text);
    }
}

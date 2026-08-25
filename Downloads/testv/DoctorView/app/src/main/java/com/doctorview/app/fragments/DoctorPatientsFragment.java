package com.doctorview.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.doctorview.app.R;
import com.doctorview.app.adapters.PatientRowAdapter;
import com.doctorview.app.firebase.FirebaseHelper;
import com.doctorview.app.models.PatientRow;
import com.doctorview.app.utils.Constants;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Doctor Patients: the patients who booked this doctor,
 * built from the appointments collection. Tap to open the chat.
 */
public class DoctorPatientsFragment extends Fragment {

    private ProgressBar progressBar;
    private TextView tvEmpty;

    public DoctorPatientsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_doctor_patients, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        RecyclerView rv = view.findViewById(R.id.rvPatients);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        PatientRowAdapter adapter = new PatientRowAdapter(patient -> openChat(patient));
        rv.setAdapter(adapter);

        loadPatients(adapter);
    }

    private void loadPatients(PatientRowAdapter adapter) {
        FirebaseUser firebaseUser = FirebaseHelper.getAuth().getCurrentUser();
        if (firebaseUser == null) {
            return;
        }

        FirebaseHelper.getFirestore()
                .collection(Constants.COLLECTION_APPOINTMENTS)
                .whereEqualTo("doctorId", firebaseUser.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!isAdded()) {
                        return;
                    }
                    progressBar.setVisibility(View.GONE);
                    Map<String, PatientRow> unique = new LinkedHashMap<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        String patientId = doc.getString("userId");
                        if (patientId == null || unique.containsKey(patientId)) {
                            continue;
                        }
                        String name = doc.getString("patientName");
                        unique.put(patientId, new PatientRow(patientId,
                                name != null && !name.isEmpty() ? name : "Patient",
                                getString(R.string.last_visit, doc.getString("date"))));
                    }
                    List<PatientRow> patients = new ArrayList<>(unique.values());
                    adapter.setPatients(patients);
                    tvEmpty.setVisibility(patients.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        progressBar.setVisibility(View.GONE);
                        tvEmpty.setVisibility(View.VISIBLE);
                    }
                });
    }

    /** Opens the shared patient–doctor chat room. */
    private void openChat(PatientRow patient) {
        Bundle args = new Bundle();
        args.putString("patientId", patient.getUserId());
        args.putString("patientName", patient.getName());
        Navigation.findNavController(requireView())
                .navigate(R.id.action_doctorPatientsFragment_to_chatRoomFragment, args);
    }
}

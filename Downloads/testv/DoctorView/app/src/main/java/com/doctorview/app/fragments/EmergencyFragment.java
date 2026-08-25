package com.doctorview.app.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.doctorview.app.R;
import com.doctorview.app.utils.AppUtils;

/**
 * Emergency Help: one-tap dial buttons for emergency numbers
 * and quick first-aid basics.
 */
public class EmergencyFragment extends Fragment {

    public EmergencyFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_emergency, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        Button btnAmbulance = view.findViewById(R.id.btnAmbulance);
        Button btnHelpline = view.findViewById(R.id.btnHelpline);
        Button btnHospital = view.findViewById(R.id.btnHospital);

        btnAmbulance.setOnClickListener(v ->
                dial(getString(R.string.emergency_number_ambulance)));
        btnHelpline.setOnClickListener(v ->
                dial(getString(R.string.emergency_number_helpline)));
        btnHospital.setOnClickListener(v ->
                dial(getString(R.string.emergency_number_hospital)));

        buildFirstAidTips(view.findViewById(R.id.tipsContainer));
    }

    /** Opens the dialer with the number pre-filled (works without permissions). */
    private void dial(String number) {
        try {
            startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number)));
        } catch (Exception e) {
            AppUtils.showToast(requireContext(), R.string.no_dialer);
        }
    }

    /** Inflates one tip card per first-aid topic. */
    private void buildFirstAidTips(LinearLayout tipsContainer) {
        int[][] tips = {
                {R.string.tip_cpr_title, R.string.tip_cpr_text},
                {R.string.tip_bleeding_title, R.string.tip_bleeding_text},
                {R.string.tip_burns_title, R.string.tip_burns_text},
                {R.string.tip_choking_title, R.string.tip_choking_text}
        };
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (int[] tip : tips) {
            View card = inflater.inflate(R.layout.item_first_aid_tip, tipsContainer, false);
            ((TextView) card.findViewById(R.id.tvTipTitle)).setText(tip[0]);
            ((TextView) card.findViewById(R.id.tvTipText)).setText(tip[1]);
            tipsContainer.addView(card);
        }
    }
}

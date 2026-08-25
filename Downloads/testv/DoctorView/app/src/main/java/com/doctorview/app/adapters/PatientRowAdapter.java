package com.doctorview.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.doctorview.app.R;
import com.doctorview.app.models.PatientRow;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple patient row (avatar initial, name, subtitle) for the
 * Doctor Patients and Messages tabs.
 */
public class PatientRowAdapter extends RecyclerView.Adapter<PatientRowAdapter.PatientViewHolder> {

    /** Called when the doctor taps a patient row. */
    public interface OnPatientClickListener {
        void onPatientClick(PatientRow patient);
    }

    private final List<PatientRow> patients = new ArrayList<>();
    private final OnPatientClickListener listener;

    public PatientRowAdapter(OnPatientClickListener listener) {
        this.listener = listener;
    }

    public void setPatients(List<PatientRow> newList) {
        patients.clear();
        patients.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_patient_row, parent, false);
        return new PatientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        PatientRow patient = patients.get(position);

        String name = patient.getName() != null ? patient.getName() : "?";
        holder.tvInitial.setText(name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase());
        holder.tvName.setText(name);
        holder.tvSubtitle.setText(patient.getSubtitle());

        holder.itemView.setOnClickListener(v -> listener.onPatientClick(patient));
    }

    @Override
    public int getItemCount() {
        return patients.size();
    }

    static class PatientViewHolder extends RecyclerView.ViewHolder {
        final TextView tvInitial;
        final TextView tvName;
        final TextView tvSubtitle;

        PatientViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInitial = itemView.findViewById(R.id.tvPatientInitial);
            tvName = itemView.findViewById(R.id.tvPatientName);
            tvSubtitle = itemView.findViewById(R.id.tvPatientSubtitle);
        }
    }
}

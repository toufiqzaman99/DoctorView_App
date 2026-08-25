package com.doctorview.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.doctorview.app.R;
import com.doctorview.app.models.Doctor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Shows doctors as cards in a RecyclerView.
 * Keeps the full list so the search box can filter it.
 */
public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder> {

    /** Called when the user taps a doctor card or the Consult button. */
    public interface OnDoctorClickListener {
        void onDoctorClick(Doctor doctor);

        void onConsultClick(Doctor doctor);
    }

    private final List<Doctor> allDoctors = new ArrayList<>();      // full list
    private final List<Doctor> filteredDoctors = new ArrayList<>(); // what is shown
    private final OnDoctorClickListener listener;
    private String specialtyFilter = "All";

    public DoctorAdapter(List<Doctor> doctors, OnDoctorClickListener listener) {
        this.listener = listener;
        setDoctors(doctors);
    }

    /** Limits the list to doctors of the selected specialty ("All" = no filter). */
    public void setSpecialtyFilter(String specialty) {
        this.specialtyFilter = specialty;
    }

    /** Replaces the whole list (e.g. after loading from Firestore). */
    public void setDoctors(List<Doctor> doctors) {
        allDoctors.clear();
        allDoctors.addAll(doctors);
        filteredDoctors.clear();
        filteredDoctors.addAll(doctors);
        notifyDataSetChanged();
    }

    /** Keeps only doctors matching the search query and the selected specialty. */
    public void filter(String query) {
        String q = query.trim().toLowerCase(Locale.getDefault());
        String specialty = specialtyFilter == null ? "All" : specialtyFilter;
        filteredDoctors.clear();
        for (Doctor doctor : allDoctors) {
            boolean matchesSpecialty = "All".equals(specialty)
                    || (doctor.getSpecialty() != null
                        && doctor.getSpecialty().toLowerCase(Locale.getDefault())
                            .contains(specialty.toLowerCase(Locale.getDefault())));
            if (!matchesSpecialty) {
                continue;
            }
            if (q.isEmpty()
                    || contains(doctor.getName(), q)
                    || contains(doctor.getSpecialty(), q)
                    || contains(doctor.getHospital(), q)) {
                filteredDoctors.add(doctor);
            }
        }
        notifyDataSetChanged();
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.getDefault()).contains(query);
    }

    @NonNull
    @Override
    public DoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_doctor, parent, false);
        return new DoctorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorViewHolder holder, int position) {
        Doctor doctor = filteredDoctors.get(position);

        holder.tvName.setText(doctor.getName());
        holder.tvSpecialty.setText(doctor.getSpecialty());
        holder.tvHospital.setText(doctor.getHospital());
        holder.tvRating.setText(String.format(Locale.getDefault(), "%.1f", doctor.getRating()));
        holder.tvFee.setText(holder.itemView.getContext().getString(
                R.string.fee_value, String.format(Locale.getDefault(), "%.0f", doctor.getFee())));

        // Doctor photo, with a friendly avatar as fallback
        Glide.with(holder.itemView.getContext())
                .load(doctor.getImageUrl())
                .placeholder(R.drawable.ic_doctor_avatar)
                .error(R.drawable.ic_doctor_avatar)
                .into(holder.ivPhoto);

        holder.itemView.setOnClickListener(v -> listener.onDoctorClick(doctor));
        holder.btnConsult.setOnClickListener(v -> listener.onConsultClick(doctor));
    }

    @Override
    public int getItemCount() {
        return filteredDoctors.size();
    }

    static class DoctorViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivPhoto;
        final TextView tvName;
        final TextView tvSpecialty;
        final TextView tvHospital;
        final TextView tvRating;
        final TextView tvFee;
        final View btnConsult;

        DoctorViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivDoctorPhoto);
            tvName = itemView.findViewById(R.id.tvDoctorName);
            tvSpecialty = itemView.findViewById(R.id.tvDoctorSpecialty);
            tvHospital = itemView.findViewById(R.id.tvDoctorHospital);
            tvRating = itemView.findViewById(R.id.tvDoctorRating);
            tvFee = itemView.findViewById(R.id.tvDoctorFee);
            btnConsult = itemView.findViewById(R.id.btnConsult);
        }
    }
}

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
 * Compact horizontal doctor cards for the Home "Top Rated Doctors" row.
 */
public class TopDoctorAdapter extends RecyclerView.Adapter<TopDoctorAdapter.TopDoctorViewHolder> {

    /** Card tap → doctor details; Consult tap → book directly. */
    public interface OnTopDoctorListener {
        void onDoctorClick(Doctor doctor);

        void onConsultClick(Doctor doctor);
    }

    private final List<Doctor> doctors = new ArrayList<>();
    private final OnTopDoctorListener listener;

    public TopDoctorAdapter(OnTopDoctorListener listener) {
        this.listener = listener;
    }

    /** Replaces the whole list. */
    public void setDoctors(List<Doctor> newList) {
        doctors.clear();
        doctors.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TopDoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_top_doctor, parent, false);
        return new TopDoctorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopDoctorViewHolder holder, int position) {
        Doctor doctor = doctors.get(position);

        holder.tvName.setText(doctor.getName());
        holder.tvSpecialty.setText(doctor.getSpecialty());
        holder.tvRating.setText(String.format(Locale.getDefault(), "%.1f", doctor.getRating()));
        holder.tvExperience.setText(holder.itemView.getContext().getString(
                R.string.years_value, doctor.getExperienceYears()));

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
        return doctors.size();
    }

    static class TopDoctorViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivPhoto;
        final TextView tvName;
        final TextView tvSpecialty;
        final TextView tvRating;
        final TextView tvExperience;
        final View btnConsult;

        TopDoctorViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivTopDoctorPhoto);
            tvName = itemView.findViewById(R.id.tvTopDoctorName);
            tvSpecialty = itemView.findViewById(R.id.tvTopDoctorSpecialty);
            tvRating = itemView.findViewById(R.id.tvTopDoctorRating);
            tvExperience = itemView.findViewById(R.id.tvTopDoctorExperience);
            btnConsult = itemView.findViewById(R.id.btnTopConsult);
        }
    }
}

package com.doctorview.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.doctorview.app.R;
import com.doctorview.app.models.Appointment;
import com.doctorview.app.utils.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Shows appointments as cards with a colored status chip.
 * The Cancel button only appears while the status is pending or confirmed.
 */
public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {

    /** Called when the user taps Cancel on an appointment. */
    public interface OnCancelClickListener {
        void onCancelClick(Appointment appointment);
    }

    private final List<Appointment> appointments = new ArrayList<>();
    private final OnCancelClickListener listener;

    public AppointmentAdapter(List<Appointment> appointments, OnCancelClickListener listener) {
        this.listener = listener;
        setAppointments(appointments);
    }

    /** Replaces the whole list (e.g. after loading from Firestore). */
    public void setAppointments(List<Appointment> newList) {
        appointments.clear();
        appointments.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);

        holder.tvDoctorName.setText(appointment.getDoctorName());
        holder.tvSpecialty.setText(appointment.getSpecialty());
        holder.tvDateTime.setText(formatDateTime(appointment.getDate(), appointment.getTime()));

        String status = appointment.getStatus();
        holder.tvStatus.setText(statusLabel(holder, status));
        holder.tvStatus.setBackgroundResource(statusBackground(status));
        holder.tvStatus.setTextColor(
                ContextCompat.getColor(holder.itemView.getContext(), statusTextColor(status)));

        boolean cancellable = Constants.STATUS_PENDING.equals(status)
                || Constants.STATUS_CONFIRMED.equals(status);
        holder.btnCancel.setVisibility(cancellable ? View.VISIBLE : View.GONE);
        holder.btnCancel.setOnClickListener(v -> listener.onCancelClick(appointment));
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    /** "yyyy-MM-dd" + time → "Sun, 24 Aug 2026 · 10:00 AM" */
    private String formatDateTime(String dateIso, String time) {
        String prettyDate = dateIso;
        if (dateIso != null) {
            try {
                Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateIso);
                prettyDate = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(date);
            } catch (ParseException ignored) {
                // keep the raw value if it isn't in the expected format
            }
        }
        return (prettyDate == null ? "" : prettyDate)
                + (time == null || time.isEmpty() ? "" : " · " + time);
    }

    private String statusLabel(AppointmentViewHolder holder, String status) {
        int labelRes;
        switch (status == null ? "" : status) {
            case Constants.STATUS_CONFIRMED:
                labelRes = R.string.status_confirmed;
                break;
            case Constants.STATUS_COMPLETED:
                labelRes = R.string.status_completed;
                break;
            case Constants.STATUS_CANCELLED:
                labelRes = R.string.status_cancelled;
                break;
            case Constants.STATUS_REJECTED:
                labelRes = R.string.status_rejected;
                break;
            default:
                labelRes = R.string.status_pending;
                break;
        }
        return holder.itemView.getContext().getString(labelRes);
    }

    private int statusBackground(String status) {
        switch (status == null ? "" : status) {
            case Constants.STATUS_CONFIRMED:
                return R.drawable.bg_status_confirmed;
            case Constants.STATUS_COMPLETED:
                return R.drawable.bg_status_completed;
            case Constants.STATUS_CANCELLED:
            case Constants.STATUS_REJECTED:
                return R.drawable.bg_status_cancelled;
            default:
                return R.drawable.bg_status_pending;
        }
    }

    private int statusTextColor(String status) {
        switch (status == null ? "" : status) {
            case Constants.STATUS_CONFIRMED:
                return R.color.status_confirmed_text;
            case Constants.STATUS_COMPLETED:
                return R.color.status_completed_text;
            case Constants.STATUS_CANCELLED:
            case Constants.STATUS_REJECTED:
                return R.color.status_cancelled_text;
            default:
                return R.color.status_pending_text;
        }
    }

    static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        final TextView tvDoctorName;
        final TextView tvSpecialty;
        final TextView tvStatus;
        final TextView tvDateTime;
        final Button btnCancel;

        AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDoctorName = itemView.findViewById(R.id.tvDoctorName);
            tvSpecialty = itemView.findViewById(R.id.tvSpecialty);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }
    }
}

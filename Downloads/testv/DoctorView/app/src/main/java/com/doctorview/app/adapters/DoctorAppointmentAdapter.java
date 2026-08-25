package com.doctorview.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
 * Doctor view of appointments: patient name, date/time, reason and
 * status, with Accept/Reject buttons while the request is pending.
 */
public class DoctorAppointmentAdapter extends RecyclerView.Adapter<DoctorAppointmentAdapter.DoctorAppointmentViewHolder> {

    /** Called when the doctor accepts or rejects a request. */
    public interface OnRequestActionListener {
        void onAccept(Appointment appointment);

        void onReject(Appointment appointment);
    }

    private final List<Appointment> appointments = new ArrayList<>();
    private final OnRequestActionListener listener;

    public DoctorAppointmentAdapter(OnRequestActionListener listener) {
        this.listener = listener;
    }

    public void setAppointments(List<Appointment> newList) {
        appointments.clear();
        appointments.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DoctorAppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_doctor_appointment, parent, false);
        return new DoctorAppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorAppointmentViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);

        holder.tvPatientName.setText(appointment.getPatientName() != null
                ? appointment.getPatientName() : "-");
        holder.tvDateTime.setText(formatDateTime(appointment.getDate(), appointment.getTime()));
        holder.tvReason.setText(appointment.getReason() != null && !appointment.getReason().isEmpty()
                ? appointment.getReason()
                : holder.itemView.getContext().getString(R.string.no_reason));

        String status = appointment.getStatus();
        holder.tvStatus.setText(statusLabel(holder, status));
        holder.tvStatus.setBackgroundResource(statusBackground(status));
        holder.tvStatus.setTextColor(
                ContextCompat.getColor(holder.itemView.getContext(), statusTextColor(status)));

        boolean pending = Constants.STATUS_PENDING.equals(status);
        holder.btnAccept.setVisibility(pending ? View.VISIBLE : View.GONE);
        holder.btnReject.setVisibility(pending ? View.VISIBLE : View.GONE);
        holder.btnAccept.setOnClickListener(v -> listener.onAccept(appointment));
        holder.btnReject.setOnClickListener(v -> listener.onReject(appointment));
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    private String formatDateTime(String dateIso, String time) {
        String pretty = dateIso;
        if (dateIso != null) {
            try {
                Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateIso);
                pretty = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(date);
            } catch (ParseException ignored) {
                // keep raw value
            }
        }
        return (pretty == null ? "" : pretty) + (time == null || time.isEmpty() ? "" : " · " + time);
    }

    private String statusLabel(DoctorAppointmentViewHolder holder, String status) {
        int res;
        switch (status == null ? "" : status) {
            case Constants.STATUS_CONFIRMED:
                res = R.string.status_confirmed;
                break;
            case Constants.STATUS_COMPLETED:
                res = R.string.status_completed;
                break;
            case Constants.STATUS_CANCELLED:
                res = R.string.status_cancelled;
                break;
            case Constants.STATUS_REJECTED:
                res = R.string.status_rejected;
                break;
            default:
                res = R.string.status_pending;
                break;
        }
        return holder.itemView.getContext().getString(res);
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

    static class DoctorAppointmentViewHolder extends RecyclerView.ViewHolder {
        final TextView tvPatientName;
        final TextView tvDateTime;
        final TextView tvReason;
        final TextView tvStatus;
        final View btnAccept;
        final View btnReject;

        DoctorAppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvReason = itemView.findViewById(R.id.tvReason);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}

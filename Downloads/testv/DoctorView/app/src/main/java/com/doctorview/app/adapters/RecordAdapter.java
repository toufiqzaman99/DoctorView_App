package com.doctorview.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.doctorview.app.R;
import com.doctorview.app.models.MedicalRecord;
import com.doctorview.app.utils.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Shows the user's medical records as cards with a type chip,
 * date, attached-file row and a delete button.
 */
public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.RecordViewHolder> {

    /** Called when a record is tapped (opens the file) or deleted. */
    public interface OnRecordActionListener {
        void onRecordClick(MedicalRecord record);

        void onRecordDelete(MedicalRecord record);
    }

    private final List<MedicalRecord> records = new ArrayList<>();
    private final OnRecordActionListener listener;
    private boolean showDelete = true;

    public RecordAdapter(OnRecordActionListener listener) {
        this.listener = listener;
    }

    /** Doctors viewing patient records get no delete button. */
    public void setShowDelete(boolean showDelete) {
        this.showDelete = showDelete;
        notifyDataSetChanged();
    }

    /** Replaces the whole list (e.g. after loading from Firestore). */
    public void setRecords(List<MedicalRecord> newList) {
        records.clear();
        records.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_record, parent, false);
        return new RecordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
        MedicalRecord record = records.get(position);

        holder.tvTitle.setText(record.getTitle());

        holder.tvType.setText(record.getType());
        holder.tvType.setBackgroundResource(typeBackground(record.getType()));
        holder.tvType.setTextColor(
                ContextCompat.getColor(holder.itemView.getContext(), typeTextColor(record.getType())));

        holder.tvDate.setText(formatDate(record.getDate()));

        holder.tvFile.setText(record.getFileName() != null && !record.getFileName().isEmpty()
                ? record.getFileName()
                : holder.itemView.getContext().getString(R.string.no_file));

        if (record.getNote() != null && !record.getNote().isEmpty()) {
            holder.tvNote.setVisibility(View.VISIBLE);
            holder.tvNote.setText(record.getNote());
        } else {
            holder.tvNote.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onRecordClick(record));
        holder.btnDelete.setVisibility(showDelete ? View.VISIBLE : View.GONE);
        holder.btnDelete.setOnClickListener(v -> listener.onRecordDelete(record));
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    /** "yyyy-MM-dd" → "15 Aug 2026" */
    private String formatDate(String dateIso) {
        if (dateIso == null) {
            return "";
        }
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateIso);
            return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date);
        } catch (ParseException ignored) {
            return dateIso;
        }
    }

    private int typeBackground(String type) {
        switch (type == null ? "" : type) {
            case Constants.RECORD_TYPE_PRESCRIPTION:
                return R.drawable.bg_status_confirmed;
            case Constants.RECORD_TYPE_VISIT:
                return R.drawable.bg_status_pending;
            case Constants.RECORD_TYPE_VACCINATION:
                return R.drawable.bg_status_cancelled;
            default: // Lab Report
                return R.drawable.bg_status_completed;
        }
    }

    private int typeTextColor(String type) {
        switch (type == null ? "" : type) {
            case Constants.RECORD_TYPE_PRESCRIPTION:
                return R.color.status_confirmed_text;
            case Constants.RECORD_TYPE_VISIT:
                return R.color.status_pending_text;
            case Constants.RECORD_TYPE_VACCINATION:
                return R.color.status_cancelled_text;
            default:
                return R.color.status_completed_text;
        }
    }

    static class RecordViewHolder extends RecyclerView.ViewHolder {
        final TextView tvTitle;
        final TextView tvType;
        final TextView tvDate;
        final TextView tvFile;
        final TextView tvNote;
        final ImageButton btnDelete;

        RecordViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvRecordTitle);
            tvType = itemView.findViewById(R.id.tvRecordType);
            tvDate = itemView.findViewById(R.id.tvRecordDate);
            tvFile = itemView.findViewById(R.id.tvRecordFile);
            tvNote = itemView.findViewById(R.id.tvRecordNote);
            btnDelete = itemView.findViewById(R.id.btnRecordDelete);
        }
    }
}

package com.doctorview.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.doctorview.app.R;
import com.doctorview.app.models.Disease;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Shows diseases as cards in a RecyclerView.
 * Keeps the full list so the search box can filter by name or category.
 */
public class DiseaseAdapter extends RecyclerView.Adapter<DiseaseAdapter.DiseaseViewHolder> {

    /** Called when the user taps a disease card. */
    public interface OnDiseaseClickListener {
        void onDiseaseClick(Disease disease);
    }

    private final List<Disease> allDiseases = new ArrayList<>();
    private final List<Disease> filteredDiseases = new ArrayList<>();
    private final OnDiseaseClickListener listener;

    public DiseaseAdapter(List<Disease> diseases, OnDiseaseClickListener listener) {
        this.listener = listener;
        setDiseases(diseases);
    }

    /** Replaces the whole list (e.g. after loading from Firestore). */
    public void setDiseases(List<Disease> diseases) {
        allDiseases.clear();
        allDiseases.addAll(diseases);
        filteredDiseases.clear();
        filteredDiseases.addAll(diseases);
        notifyDataSetChanged();
    }

    /** Keeps only diseases whose name or category contains the query. */
    public void filter(String query) {
        String q = query.trim().toLowerCase(Locale.getDefault());
        filteredDiseases.clear();
        if (q.isEmpty()) {
            filteredDiseases.addAll(allDiseases);
        } else {
            for (Disease disease : allDiseases) {
                if (contains(disease.getName(), q) || contains(disease.getCategory(), q)) {
                    filteredDiseases.add(disease);
                }
            }
        }
        notifyDataSetChanged();
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.getDefault()).contains(query);
    }

    @NonNull
    @Override
    public DiseaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_disease, parent, false);
        return new DiseaseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiseaseViewHolder holder, int position) {
        Disease disease = filteredDiseases.get(position);

        holder.tvName.setText(disease.getName());
        holder.tvOverview.setText(disease.getOverview());

        holder.tvCategory.setText(disease.getCategory());
        holder.tvCategory.setBackgroundResource(categoryBackground(disease.getCategory()));
        holder.tvCategory.setTextColor(
                ContextCompat.getColor(holder.itemView.getContext(), categoryTextColor(disease.getCategory())));

        holder.itemView.setOnClickListener(v -> listener.onDiseaseClick(disease));
    }

    @Override
    public int getItemCount() {
        return filteredDiseases.size();
    }

    private int categoryBackground(String category) {
        switch (category == null ? "" : category) {
            case "Infectious":
                return R.drawable.bg_status_cancelled;
            case "Chronic":
                return R.drawable.bg_status_pending;
            case "Digestive":
                return R.drawable.bg_status_confirmed;
            default: // Respiratory, Neurological, others
                return R.drawable.bg_status_completed;
        }
    }

    private int categoryTextColor(String category) {
        switch (category == null ? "" : category) {
            case "Infectious":
                return R.color.status_cancelled_text;
            case "Chronic":
                return R.color.status_pending_text;
            case "Digestive":
                return R.color.status_confirmed_text;
            default:
                return R.color.status_completed_text;
        }
    }

    static class DiseaseViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvCategory;
        final TextView tvOverview;

        DiseaseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvDiseaseName);
            tvCategory = itemView.findViewById(R.id.tvDiseaseCategory);
            tvOverview = itemView.findViewById(R.id.tvDiseaseOverview);
        }
    }
}

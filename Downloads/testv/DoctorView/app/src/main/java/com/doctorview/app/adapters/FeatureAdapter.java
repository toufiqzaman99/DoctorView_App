package com.doctorview.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.doctorview.app.R;
import com.doctorview.app.models.Feature;

import java.util.List;

/**
 * Shows the Home screen quick-access cards in a grid.
 * This is the simplest possible RecyclerView adapter —
 * a good template for the DoctorAdapter and other lists later.
 */
public class FeatureAdapter extends RecyclerView.Adapter<FeatureAdapter.FeatureViewHolder> {

    /** Called when the user taps a quick-access card. */
    public interface OnFeatureClickListener {
        void onFeatureClick(Feature feature);
    }

    private final List<Feature> features;
    private final OnFeatureClickListener listener;

    public FeatureAdapter(List<Feature> features, OnFeatureClickListener listener) {
        this.features = features;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FeatureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_feature, parent, false);
        return new FeatureViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeatureViewHolder holder, int position) {
        Feature feature = features.get(position);
        holder.ivIcon.setImageResource(feature.getIconRes());
        holder.ivIcon.setColorFilter(
                ContextCompat.getColor(holder.itemView.getContext(), feature.getColorRes()));
        holder.tvTitle.setText(feature.getTitle());
        holder.itemView.setOnClickListener(v -> listener.onFeatureClick(feature));
    }

    @Override
    public int getItemCount() {
        return features.size();
    }

    static class FeatureViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivIcon;
        final TextView tvTitle;

        FeatureViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivFeatureIcon);
            tvTitle = itemView.findViewById(R.id.tvFeatureTitle);
        }
    }
}

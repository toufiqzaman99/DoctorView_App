package com.doctorview.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.doctorview.app.R;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

/**
 * Circular specialty selector (initial + label) for the Home screen.
 * The selected item gets the teal fill.
 */
public class SpecialtyAdapter extends RecyclerView.Adapter<SpecialtyAdapter.SpecialtyViewHolder> {

    /** Called when the user picks a specialty. */
    public interface OnSpecialtyClickListener {
        void onSpecialtyClick(int position, String specialty);
    }

    private final List<String> specialties = new ArrayList<>();
    private final OnSpecialtyClickListener listener;
    private int selectedIndex = 0;

    public SpecialtyAdapter(String[] specialties, OnSpecialtyClickListener listener) {
        this.listener = listener;
        for (String specialty : specialties) {
            this.specialties.add(specialty);
        }
    }

    public void setSelectedIndex(int index) {
        this.selectedIndex = index;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SpecialtyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_specialty, parent, false);
        return new SpecialtyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SpecialtyViewHolder holder, int position) {
        String specialty = specialties.get(position);
        boolean selected = position == selectedIndex;

        String label = "All".equals(specialty) ? specialty : specialty;
        String initial = label.isEmpty() ? "?" : label.substring(0, 1).toUpperCase();

        holder.tvInitial.setText(initial);
        holder.tvLabel.setText(label);

        int cardColor = holder.itemView.getContext().getResources().getColor(
                selected ? R.color.primary : R.color.white, null);
        int textColor = holder.itemView.getContext().getResources().getColor(
                selected ? R.color.white : R.color.primary, null);
        int labelColor = holder.itemView.getContext().getResources().getColor(
                selected ? R.color.white_80 : R.color.text_secondary, null);

        holder.card.setCardBackgroundColor(cardColor);
        holder.card.setStrokeColor(holder.itemView.getContext().getResources().getColor(
                selected ? R.color.primary : R.color.border, null));
        holder.tvInitial.setTextColor(textColor);
        holder.tvLabel.setTextColor(labelColor);

        holder.itemView.setOnClickListener(v ->
                listener.onSpecialtyClick(position, specialty));
    }

    @Override
    public int getItemCount() {
        return specialties.size();
    }

    static class SpecialtyViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView card;
        final TextView tvInitial;
        final TextView tvLabel;

        SpecialtyViewHolder(@NonNull View itemView) {
            super(itemView);
            card = (MaterialCardView) itemView;
            tvInitial = itemView.findViewById(R.id.tvSpecialtyInitial);
            tvLabel = itemView.findViewById(R.id.tvSpecialtyLabel);
        }
    }
}

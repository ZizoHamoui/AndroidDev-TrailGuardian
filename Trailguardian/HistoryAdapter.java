package com.example.trailguardian;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.TrailViewHolder> {

    private List<Trail> trails;

    public HistoryAdapter(List<Trail> trails) {
        this.trails = trails;
    }

    //Inflates the items of the recycler
    @NonNull
    @Override
    public TrailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
        return new TrailViewHolder(view);
    }

    //binds recycler items and changes background colour based on odd or even list numbers
    @Override
    public void onBindViewHolder(@NonNull TrailViewHolder holder, int position) {
        Trail trail = trails.get(position);
        holder.dateTextView.setText(trail.getDate());
        holder.latitudeTextView.setText(String.valueOf(trail.getLatitude()));
        holder.longitudeTextView.setText(String.valueOf(trail.getLongitude()));

        // Alternate row colors
        if (position % 2 == 0) {
            holder.itemView.setBackgroundColor(Color.parseColor("#E8F5E9")); // Light green color
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    //gets count of trails
    @Override
    public int getItemCount() {
        return trails.size();
    }

    //Holder for the items which are date, lat and lang
    static class TrailViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView, latitudeTextView, longitudeTextView;

        public TrailViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            latitudeTextView = itemView.findViewById(R.id.latitudeTextView);
            longitudeTextView = itemView.findViewById(R.id.longitudeTextView);
        }
    }
}


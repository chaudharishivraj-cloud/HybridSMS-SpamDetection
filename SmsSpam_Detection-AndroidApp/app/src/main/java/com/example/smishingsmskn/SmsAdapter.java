package com.example.smishingsmskn;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SmsAdapter extends RecyclerView.Adapter<SmsAdapter.ViewHolder> {

    private List<SmsResult> smsList;

    public SmsAdapter(List<SmsResult> smsList) {
        this.smsList = smsList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView resultText;
        TextView reasonText;
        ImageView statusIcon;

        public ViewHolder(View view) {
            super(view);
            messageText = view.findViewById(R.id.smsMessage);
            resultText = view.findViewById(R.id.smsStatus);
            reasonText = view.findViewById(R.id.smsReason);
            statusIcon = view.findViewById(R.id.statusIcon);
        }
    }

    @NonNull
    @Override
    public SmsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sms_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SmsResult sms = smsList.get(position);

        if (sms == null) {
            return;
        }

        // Set message text
        holder.messageText.setText(sms.getMessage());

        // Set status text
        holder.resultText.setText(sms.getResult());

        // Apply styling based on result type
        if (sms.isSpam()) {
            // Threat detected - Red theme
            holder.statusIcon.setImageResource(R.drawable.ic_warning);
            holder.statusIcon.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(),
                    android.R.color.holo_red_dark));
            holder.resultText.setTextColor(ContextCompat.getColor(holder.itemView.getContext(),
                    android.R.color.holo_red_dark));

            // Show reason if available
            if (sms.getReason() != null && !sms.getReason().isEmpty()) {
                holder.reasonText.setVisibility(View.VISIBLE);
                holder.reasonText.setText(sms.getReason());
            } else {
                holder.reasonText.setVisibility(View.GONE);
            }

            // Add subtle background tint
            holder.itemView.setBackgroundColor(Color.parseColor("#FFF5F5"));

        } else if (sms.isSecure()) {
            // Secure - Green theme
            holder.statusIcon.setImageResource(R.drawable.ic_check_circle);
            holder.statusIcon.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(),
                    android.R.color.holo_green_dark));
            holder.resultText.setTextColor(ContextCompat.getColor(holder.itemView.getContext(),
                    android.R.color.holo_green_dark));
            holder.reasonText.setVisibility(View.GONE);
            holder.itemView.setBackgroundColor(Color.WHITE);

        } else if (sms.isError()) {
            // Error - Orange theme
            holder.statusIcon.setImageResource(R.drawable.ic_error);
            holder.statusIcon.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(),
                    android.R.color.holo_orange_dark));
            holder.resultText.setTextColor(ContextCompat.getColor(holder.itemView.getContext(),
                    android.R.color.holo_orange_dark));

            if (sms.getReason() != null && !sms.getReason().isEmpty()) {
                holder.reasonText.setVisibility(View.VISIBLE);
                holder.reasonText.setText(sms.getReason());
            } else {
                holder.reasonText.setVisibility(View.GONE);
            }

            holder.itemView.setBackgroundColor(Color.parseColor("#FFFEF5"));
        }
    }

    @Override
    public int getItemCount() {
        return smsList != null ? smsList.size() : 0;
    }

    public void setSmsList(List<SmsResult> newList) {
        this.smsList = newList;
        notifyDataSetChanged();
    }
}
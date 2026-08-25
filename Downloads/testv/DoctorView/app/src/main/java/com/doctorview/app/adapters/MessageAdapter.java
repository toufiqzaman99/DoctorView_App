package com.doctorview.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.doctorview.app.R;
import com.doctorview.app.models.Message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Chat messages: my messages bubble on the right (teal),
 * the other person's on the left (light grey).
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private static final int TYPE_MINE = 0;
    private static final int TYPE_OTHER = 1;

    private final List<Message> messages = new ArrayList<>();
    private final String myUid;

    public MessageAdapter(String myUid) {
        this.myUid = myUid;
    }

    /** Appends one message (newest at the bottom). */
    public void add(Message message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        return myUid != null && myUid.equals(message.getSenderId()) ? TYPE_MINE : TYPE_OTHER;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = viewType == TYPE_MINE
                ? R.layout.item_message_mine
                : R.layout.item_message_other;
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.tvText.setText(message.getText());
        holder.tvTime.setText(new SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(new Date(message.getTimestamp())));
        if (holder.tvSender != null) {
            holder.tvSender.setText(message.getSenderName());
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        final TextView tvText;
        final TextView tvTime;
        final TextView tvSender;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvText = itemView.findViewById(R.id.tvMessageText);
            tvTime = itemView.findViewById(R.id.tvMessageTime);
            tvSender = itemView.findViewById(R.id.tvMessageSender);
        }
    }
}

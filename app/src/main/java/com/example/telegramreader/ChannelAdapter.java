package com.example.telegramreader;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChannelAdapter extends RecyclerView.Adapter<ChannelAdapter.ViewHolder> {

    private List<String> channels;
    private OnChannelClickListener listener;
    private OnChannelDeleteListener deleteListener;

    public interface OnChannelClickListener {
        void onChannelClick(String channel);
    }

    public interface OnChannelDeleteListener {
        void onChannelDelete(String channel);
    }

    public ChannelAdapter(List<String> channels, OnChannelClickListener listener,
                          OnChannelDeleteListener deleteListener) {
        this.channels = channels;
        this.listener = listener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_channel, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String channel = channels.get(position);
        holder.tvName.setText(channel);
        holder.itemView.setOnClickListener(v -> listener.onChannelClick(channel));
        holder.btnDelete.setOnClickListener(v -> deleteListener.onChannelDelete(channel));
    }

    @Override
    public int getItemCount() {
        return channels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageButton btnDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvChannelName);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}

package com.rsin.socketiochat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder>{

    private List<Message> mMessages;
    private int[] mUsernameColors;

    public MessageAdapter(Context context, List<Message> messages) {
        mMessages = messages;
        mUsernameColors = context.getResources().getIntArray(R.array.username_colors);
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = -1;
        switch (viewType) {
            case Message.TYPE_MESSAGE:
                layout = R.layout.item_message;
                break;
            case Message.TYPE_LOG:
                layout = R.layout.item_log;
                break;
            case Message.TYPE_ACTION:
                layout = R.layout.item_action;
                break;
        }
        View v = LayoutInflater
                .from(parent.getContext())
                .inflate(layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {
        Message message = mMessages.get(position);
        holder.setMessage(message.getMessage());
        holder.setUsername(message.getUsername());

    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mUsernameView;
        private TextView mMessageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mUsernameView = itemView.findViewById(R.id.username);
            mMessageView = itemView.findViewById(R.id.message);
        }


        public void setUsername(String username) {
            if (null == mUsernameView) return;
            mUsernameView.setText(username);
//            mUsernameView.setTextColor(getUsernameColor(username));
        }

        public void setMessage(String message) {
            if (null == mMessageView) return;
            mMessageView.setText(message);
        }

//        private int getUsernameColor(String username) {
//            int hash = 7;
//            for (int i = 0, len = username.length(); i < len; i++) {
//                hash = username.codePointAt(i) + (hash << 5) - hash;
//            }
//            int index = Math.abs(hash % mUsernameColors.length);
//            return mUsernameColors[index];
//        }
    }
}

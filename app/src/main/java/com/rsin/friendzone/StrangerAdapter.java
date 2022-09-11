package com.rsin.friendzone;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StrangerAdapter extends RecyclerView.Adapter<StrangerAdapter.ViewHolder> {
    List<StrangeMessage> messageList;
    Context context;

    public StrangerAdapter(List<StrangeMessage> messageList, Context context) {
        this.messageList = messageList;
        this.context = context;
    }
    @NonNull
    @Override
    public StrangerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.stranger_chat_layout, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull StrangerAdapter.ViewHolder holder, int position) {
        if (messageList.get(position).type.equals("IN"))
        {
            holder.incoming.setVisibility(View.GONE);
            holder.my_message.setText(messageList.get(position).getMessage());
            holder.my_name.setText(messageList.get(position).getUsername());
        }
        else {
            holder.outgoing.setVisibility(View.GONE);
            holder.partner_msg.setText(messageList.get(position).getMessage());
            holder.partner_name.setText(messageList.get(position).getUsername());
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout incoming, outgoing;
        TextView partner_msg,partner_name,my_name,my_message;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            incoming = itemView.findViewById(R.id.incoming_msg_layout);
            outgoing = itemView.findViewById(R.id.outgoing_msg_layout);
            partner_msg = itemView.findViewById(R.id.partner_msg);
            partner_name = itemView.findViewById(R.id.partner_username);
            my_name = itemView.findViewById(R.id.my_username);
            my_message = itemView.findViewById(R.id.my_message);
        }
    }
}

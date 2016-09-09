package com.salatart.memeticame.Views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import com.salatart.memeticame.Models.Chat;
import com.salatart.memeticame.Models.Message;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.SessionUtils;

/**
 * Created by sasalatart on 9/4/16.
 */
public class MessagesAdapter extends ArrayAdapter<Message> {
    private Chat mParentChat;
    private ArrayList<Message> mMessages;
    private LayoutInflater mLayoutInflater;

    public MessagesAdapter(Context context, int resource, ArrayList<Message> messages, Chat parentChat) {
        super(context, resource, messages);
        mMessages = messages;
        mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mParentChat = parentChat;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        Message message = mMessages.get(position);

        if (view == null) {
            if (message.getSenderPhone().equals(SessionUtils.getPhoneNumber(getContext()))) {
                view = mLayoutInflater.inflate(R.layout.message_out_list_item, parent, false);
            } else {
                view = mLayoutInflater.inflate(R.layout.message_in_list_item, parent, false);
            }
        }

        TextView senderLabel = (TextView)view.findViewById(R.id.senderLabel);
        TextView timestampLabel = (TextView)view.findViewById(R.id.timestampLabel);
        TextView messageLabel = (TextView)view.findViewById(R.id.messageLabel);

        if (message.getSenderPhone().equals(SessionUtils.getPhoneNumber(getContext()))) {
            senderLabel.setText(R.string.me);
        } else {
            senderLabel.setText(mParentChat.getParticipantsHash().get(message.getSenderPhone()));
        }

        timestampLabel.setText(message.getCreatedAt());
        messageLabel.setText(message.getContent());

        return view;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = getItem(position);

        if (message.getSenderPhone().equals(SessionUtils.getPhoneNumber(getContext()))) {
            return 0;
        } else {
            return 1;
        }
    }
}

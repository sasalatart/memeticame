package com.salatart.memeticame.Views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.salatart.memeticame.Models.Chat;
import com.salatart.memeticame.Models.Message;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.Routes;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

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
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mParentChat = parentChat;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        Message message = mMessages.get(position);

        if (view == null) {
            if (message.isMine(getContext()) && message.getAttachment() == null) {
                view = mLayoutInflater.inflate(R.layout.message_out_text_list_item, parent, false);
            } else if (message.isMine(getContext())) {
                view = mLayoutInflater.inflate(R.layout.message_out_image_list_item, parent, false);
            } else if (!message.isMine(getContext()) && message.getAttachment() == null) {
                view = mLayoutInflater.inflate(R.layout.message_in_text_list_item, parent, false);
            } else if (!message.isMine(getContext())) {
                view = mLayoutInflater.inflate(R.layout.message_in_image_list_item, parent, false);
            }
        }

        TextView senderLabel = (TextView) view.findViewById(R.id.senderLabel);
        if (message.isMine(getContext())) {
            senderLabel.setText(R.string.me);
        } else {
            senderLabel.setText(mParentChat.getParticipantsHash().get(message.getSenderPhone()));
        }

        if (message.getId() == -1) {
            view.findViewById(R.id.messageStatusCheck).setVisibility(View.GONE);
            view.findViewById(R.id.messageStatusWaiting).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.messageStatusWaiting).setVisibility(View.GONE);
            view.findViewById(R.id.messageStatusCheck).setVisibility(View.VISIBLE);
        }

        ((TextView) view.findViewById(R.id.timestampLabel)).setText(message.getCreatedAt());
        ((TextView) view.findViewById(R.id.messageLabel)).setText(message.getContent());

        if (message.getAttachment() != null) {
            ImageView attachedImage = (ImageView) view.findViewById(R.id.attachedImage);
            Picasso.with(getContext())
                    .load(Routes.DOMAIN + "/" + message.getAttachment().getUrl())
                    .resize(480, 480)
                    .into(attachedImage);
        }

        return view;
    }

    @Override
    public int getViewTypeCount() {
        return 4;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = getItem(position);

        if (message.isMine(getContext()) && message.getAttachment() == null) {
            return 0;
        } else if (message.isMine(getContext())) {
            return 1;
        } else if (!message.isMine(getContext()) && message.getAttachment() == null) {
            return 2;
        } else {
            return 3;
        }
    }
}

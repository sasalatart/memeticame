package com.salatart.memeticame.Views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.salatart.memeticame.Models.Attachment;
import com.salatart.memeticame.Models.Chat;
import com.salatart.memeticame.Models.Message;
import com.salatart.memeticame.R;

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
                view = mLayoutInflater.inflate(R.layout.message_out_image_video_list_item, parent, false);
            } else if (!message.isMine(getContext()) && message.getAttachment() == null) {
                view = mLayoutInflater.inflate(R.layout.message_in_text_list_item, parent, false);
            } else if (!message.isMine(getContext())) {
                view = mLayoutInflater.inflate(R.layout.message_in_image_video_list_item, parent, false);
            }
        }

        setTextViews(view, message);
        setAttachment(view, message);

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

    public void setTextViews(View view, Message message) {
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
    }

    public void setAttachment(View view, Message message) {
        Attachment attachment = message.getAttachment();

        if (attachment == null) {
            return;
        }

        String mimeType = attachment.getMimeType();

        int drawablePlaceholder = R.drawable.ic_image_black_24dp;
        if (mimeType.contains("video")) {
            drawablePlaceholder = R.drawable.ic_videocam_black_24dp;
        } else if (mimeType.contains("audio")) {
            drawablePlaceholder = R.drawable.ic_record_voice_over_black_24dp;
        }

        ImageView thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
        if (mimeType.contains("image")) {
            Glide.with(getContext())
                    .load(message.getAttachment().getUri())
                    .placeholder(drawablePlaceholder)
                    .override(Attachment.IMAGE_SIZE, Attachment.IMAGE_SIZE)
                    .into(thumbnail);
        } else if (mimeType.contains("video") || mimeType.contains("audio")) {
            thumbnail.setImageResource(drawablePlaceholder);
        }
    }
}

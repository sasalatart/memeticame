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
import com.salatart.memeticame.Utils.FileUtils;

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
                view = mLayoutInflater.inflate(R.layout.message_out_media_list_item, parent, false);
            } else if (!message.isMine(getContext()) && message.getAttachment() == null) {
                view = mLayoutInflater.inflate(R.layout.message_in_text_list_item, parent, false);
            } else if (!message.isMine(getContext())) {
                view = mLayoutInflater.inflate(R.layout.message_in_media_list_item, parent, false);
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

            ImageView statusImageView = (ImageView) view.findViewById(R.id.messageStatusCheck);
            if (message.getId() == -1) {
                statusImageView.setImageResource(R.drawable.ic_access_time_black_24dp);
            } else {
                statusImageView.setImageResource(R.drawable.ic_check_black_24dp);
            }
        } else {
            senderLabel.setText(mParentChat.getParticipantsHash().get(message.getSenderPhone()));
        }

        ((TextView) view.findViewById(R.id.timestampLabel)).setText(message.getCreatedAt());
        ((TextView) view.findViewById(R.id.messageLabel)).setText(message.getContent());
    }

    public void setAttachment(View view, Message message) {
        Attachment attachment = message.getAttachment();

        if (attachment == null) {
            return;
        }

        boolean fileExists = FileUtils.checkFileExistence(getContext(), attachment.getName());

        if (fileExists) {
            attachment.setUri(FileUtils.getUriFromFileName(getContext(), attachment.getName()).toString());
        }

        String mimeType = attachment.getMimeType();

        ImageView attachmentType = (ImageView) view.findViewById(R.id.attachmentType);
        if (mimeType.contains("image")) {
            attachmentType.setImageResource(R.drawable.ic_image_black_24dp);
        } else if (mimeType.contains("video")) {
            attachmentType.setImageResource(R.drawable.ic_videocam_black_24dp);
        } else {
            attachmentType.setImageResource(R.drawable.ic_record_voice_over_black_24dp);
        }

        ImageView thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
        if (mimeType.contains("image") || (mimeType.contains("video") && fileExists)) {
            Glide.with(getContext())
                    .load(message.getAttachment().getUri())
                    .placeholder(R.drawable.ic_access_time_black_24dp)
                    .crossFade()
                    .override(Attachment.IMAGE_SIZE, Attachment.IMAGE_SIZE)
                    .into(thumbnail);
        } else if (!fileExists) {
            thumbnail.setImageResource(R.drawable.ic_file_download_black_24dp);
        } else if (mimeType.contains("audio")) {
            thumbnail.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
        }

        TextView attachmentName = (TextView) view.findViewById(R.id.attachmentName);
        if (!fileExists && !mimeType.contains("image") || mimeType.contains("audio")) {
            attachmentName.setVisibility(View.VISIBLE);
            attachmentName.setText(attachment.getName());
        } else {
            attachmentName.setVisibility(View.INVISIBLE);
        }
    }
}

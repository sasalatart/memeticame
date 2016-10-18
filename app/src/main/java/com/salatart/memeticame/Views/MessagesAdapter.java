package com.salatart.memeticame.Views;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.salatart.memeticame.Models.Attachment;
import com.salatart.memeticame.Models.Chat;
import com.salatart.memeticame.Models.Message;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.FileUtils;

/**
 * Created by sasalatart on 9/4/16.
 */
public class MessagesAdapter extends ArrayAdapter<Message> {
    private Chat mParentChat;
    private MediaPlayer mMediaPlayer;
    private LayoutInflater mLayoutInflater;

    private ImageButton mPlayButton;
    private ImageButton mPauseButton;
    private ImageButton mStopButton;

    public MessagesAdapter(Context context, int resource, Chat parentChat) {
        super(context, resource, parentChat.getMessages());
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mParentChat = parentChat;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        Message message = mParentChat.getMessages().get(position);

        if (view == null) {
            if (message.isMine(getContext()) && message.getAttachment() == null) {
                view = mLayoutInflater.inflate(R.layout.list_item_message_out_text, parent, false);
            } else if (message.isMine(getContext())) {
                view = mLayoutInflater.inflate(R.layout.list_item_message_out_attachment, parent, false);
            } else if (!message.isMine(getContext()) && message.getAttachment() == null) {
                view = mLayoutInflater.inflate(R.layout.list_item_message_in_text, parent, false);
            } else if (!message.isMine(getContext())) {
                view = mLayoutInflater.inflate(R.layout.list_item_message_in_attachment, parent, false);
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

    private void setTextViews(View view, Message message) {
        TextView senderLabel = (TextView) view.findViewById(R.id.label_sender);
        if (message.isMine(getContext())) {
            senderLabel.setText(R.string.me);

            ImageView statusImageView = (ImageView) view.findViewById(R.id.message_status_check);
            if (message.getId() == -1) {
                statusImageView.setImageResource(R.drawable.ic_access_time_black_24dp);
            } else {
                statusImageView.setImageResource(R.drawable.ic_check_black_24dp);
            }
        } else {
            senderLabel.setText(mParentChat.getParticipantsHash().get(message.getSenderPhone()));
        }

        ((TextView) view.findViewById(R.id.label_timestamp)).setText(message.getCreatedAt());
        ((TextView) view.findViewById(R.id.message)).setText(message.getContent());
    }

    private void setAttachment(View view, Message message) {
        Attachment attachment = message.getAttachment();

        if (attachment == null) {
            return;
        }

        boolean fileExists = FileUtils.checkFileExistence(getContext(), attachment.getName());
        boolean isImage = attachment.isImage();
        boolean isVideo = attachment.isVideo();
        boolean isAudio = attachment.isAudio();
        boolean isMemeaudio = attachment.isMemeaudio();
        boolean isNotMedia = attachment.isNotMedia();

        if (fileExists && !isMemeaudio) {
            attachment.setUri(FileUtils.getUriFromFileName(getContext(), attachment.getName()).toString());
        } else if (fileExists) {
            attachment.setUri(attachment.getMemeaudioPartUri(getContext(), true).toString());
        }

        ImageView attachmentType = (ImageView) view.findViewById(R.id.label_attachment_type);
        if (isImage) {
            attachmentType.setImageResource(R.drawable.ic_image_black_24dp);
        } else if (isVideo) {
            attachmentType.setImageResource(R.drawable.ic_videocam_black_24dp);
        } else if (isAudio) {
            attachmentType.setImageResource(R.drawable.ic_record_voice_over_black_24dp);
        } else if (isMemeaudio) {
            attachmentType.setImageResource(R.drawable.ic_sentiment_very_satisfied_black_24dp);
        } else {
            attachmentType.setImageResource(R.drawable.ic_insert_drive_file_black_24dp);
        }

        ImageView thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
        LinearLayout groupAudioButtons = (LinearLayout) view.findViewById(R.id.group_audio_buttons);

        thumbnail.setVisibility((!fileExists || !isAudio) ? View.VISIBLE : View.GONE);
        groupAudioButtons.setVisibility((isAudio && fileExists) ? View.VISIBLE : View.GONE);
        if (isImage || (fileExists && (isVideo || isMemeaudio))) {
            Glide.with(getContext())
                    .load(attachment.getStringUri())
                    .placeholder(R.drawable.ic_access_time_black_24dp)
                    .crossFade()
                    .override(Attachment.IMAGE_SIZE, Attachment.IMAGE_SIZE)
                    .into(thumbnail);
        } else if (!fileExists) {
            thumbnail.setImageResource(R.drawable.ic_file_download_black_24dp);
        } else if (isAudio) {
            setMediaPlayer(view, Uri.parse(attachment.getStringUri()));
        } else {
            thumbnail.setImageResource(R.drawable.ic_attach_file_black_24dp);
        }

        TextView attachmentName = (TextView) view.findViewById(R.id.label_attachment_name);
        if ((!fileExists && !isImage) || isAudio || isNotMedia) {
            attachmentName.setVisibility(View.VISIBLE);
            attachmentName.setText(attachment.getName());
        } else {
            attachmentName.setVisibility(View.INVISIBLE);
        }

        TextView attachmentSize = (TextView) view.findViewById(R.id.size);
        attachmentSize.setText(attachment.getHumanReadableByteCount(false));

        TextView labelDownloadAvailable = (TextView) view.findViewById(R.id.label_download_available);
        labelDownloadAvailable.setVisibility(fileExists ? View.GONE : View.VISIBLE);
    }

    private void setMediaPlayer(final View view, final Uri audioUri) {
        mPlayButton = ((ImageButton) view.findViewById(R.id.button_play));
        mPauseButton = ((ImageButton) view.findViewById(R.id.button_pause));
        mStopButton = ((ImageButton) view.findViewById(R.id.button_stop));

        mMediaPlayer = MediaPlayer.create(getContext(), audioUri);
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                setMediaPlayer(view, audioUri);
            }
        });

        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlay();
            }
        });

        mPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPause();
            }
        });

        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStop();
                setMediaPlayer(view, audioUri);
            }
        });

        setEnabled(true, false, false);
        setColors(Color.BLACK, Color.BLACK, Color.BLACK);
    }

    public void onPlay() {
        setEnabled(false, true, true);
        setColors(Color.RED, Color.BLACK, Color.BLACK);
        mMediaPlayer.start();
    }

    public void onPause() {
        setEnabled(true, false, true);
        setColors(Color.BLACK, Color.RED, Color.BLACK);
        mMediaPlayer.pause();
    }

    public void onStop() {
        setEnabled(true, false, false);
        mMediaPlayer.stop();
    }

    private void setEnabled(boolean playButtonEnabled, boolean pauseButtonEnabled, boolean stopButtonEnabled) {
        mPlayButton.setEnabled(playButtonEnabled);
        mPauseButton.setEnabled(pauseButtonEnabled);
        mStopButton.setEnabled(stopButtonEnabled);
    }

    private void setColors(int playColor, int pauseColor, int stopColor) {
        mPlayButton.setColorFilter(playColor);
        mPauseButton.setColorFilter(pauseColor);
        mStopButton.setColorFilter(stopColor);
    }
}

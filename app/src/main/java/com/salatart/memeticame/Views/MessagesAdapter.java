package com.salatart.memeticame.Views;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.salatart.memeticame.Models.Attachment;
import com.salatart.memeticame.Models.Chat;
import com.salatart.memeticame.Models.Message;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.FileUtils;
import com.salatart.memeticame.Utils.MessageUtils;

/**
 * Created by sasalatart on 9/4/16.
 */
public class MessagesAdapter extends ArrayAdapter<Message> {
    private Chat mParentChat;
    private LayoutInflater mLayoutInflater;

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
                view = mLayoutInflater.inflate(R.layout.list_item_message_out, parent, false);
            } else if (message.isMine(getContext())) {
                view = mLayoutInflater.inflate(R.layout.list_item_attachment_out, parent, false);
            } else if (!message.isMine(getContext()) && message.getAttachment() == null) {
                view = mLayoutInflater.inflate(R.layout.list_item_message_in, parent, false);
            } else if (!message.isMine(getContext())) {
                view = mLayoutInflater.inflate(R.layout.list_item_attachment_in, parent, false);
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

    private void setAttachment(View view, final Message message) {
        final Attachment attachment = message.getAttachment();

        if (attachment == null) {
            return;
        }

        boolean fileExists = FileUtils.checkFileExistence(getContext(), attachment.getName());
        boolean isImage = attachment.isImage();
        boolean isVideo = attachment.isVideo();
        boolean isAudio = attachment.isAudio();
        boolean isMemeaudio = attachment.isMemeaudio();

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
            thumbnail.setImageResource(R.drawable.ic_audiotrack_black_24dp);
            setMediaPlayer(view, Uri.parse(attachment.getStringUri()));
        } else {
            thumbnail.setImageResource(R.drawable.ic_attach_file_black_24dp);
        }

        TextView attachmentName = (TextView) view.findViewById(R.id.label_attachment_name);
        attachmentName.setText(attachment.getName());

        TextView attachmentMimeType = (TextView) view.findViewById(R.id.label_attachment_mime_type);
        attachmentMimeType.setText(attachment.getMimeType());

        TextView attachmentSize = (TextView) view.findViewById(R.id.label_attachment_size);
        attachmentSize.setText(attachment.getHumanReadableByteCount(false));

        Button openButton = (Button) view.findViewById(R.id.button_open);
        openButton.setText(!fileExists ? "Download" : "Open");
        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!FileUtils.openFile(getContext(), attachment)) {
                    Toast.makeText(getContext(), "Can't open this file.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ImageButton copyButton = ((ImageButton) view.findViewById(R.id.button_copy));
        copyButton.setVisibility(fileExists ? View.VISIBLE : View.GONE);
        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageUtils.copyMessage(getContext(), message);
                Toast.makeText(getContext(), "Message copied", Toast.LENGTH_LONG).show();
            }
        });

        LinearLayout groupAudioButtons = (LinearLayout) view.findViewById(R.id.group_audio_buttons);
        groupAudioButtons.setVisibility((isAudio && fileExists) ? View.VISIBLE : View.GONE);
        openButton.setVisibility((isAudio && fileExists) ? View.GONE : View.VISIBLE);
    }

    private void setMediaPlayer(final View view, final Uri audioUri) {
        final ImageButton playButton = ((ImageButton) view.findViewById(R.id.button_play));
        final ImageButton pauseButton = ((ImageButton) view.findViewById(R.id.button_pause));
        final ImageButton stopButton = ((ImageButton) view.findViewById(R.id.button_stop));

        final MediaPlayer mediaPlayer = MediaPlayer.create(getContext(), audioUri);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                setMediaPlayer(view, audioUri);
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlay(mediaPlayer, playButton, pauseButton, stopButton);
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPause(mediaPlayer, playButton, pauseButton, stopButton);
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStop(mediaPlayer, playButton, pauseButton, stopButton);
                setMediaPlayer(view, audioUri);
            }
        });

        setEnabled(true, false, false, playButton, pauseButton, stopButton);
        setColors(Color.BLACK, Color.BLACK, Color.BLACK, playButton, pauseButton, stopButton);
    }

    public void onPlay(MediaPlayer mediaPlayer, ImageButton playButton, ImageButton pauseButton, ImageButton stopButton) {
        setEnabled(false, true, true, playButton, pauseButton, stopButton);
        setColors(Color.RED, Color.BLACK, Color.BLACK, playButton, pauseButton, stopButton);
        mediaPlayer.start();
    }

    public void onPause(MediaPlayer mediaPlayer, ImageButton playButton, ImageButton pauseButton, ImageButton stopButton) {
        setEnabled(true, false, true, playButton, pauseButton, stopButton);
        setColors(Color.BLACK, Color.RED, Color.BLACK, playButton, pauseButton, stopButton);
        mediaPlayer.pause();
    }

    public void onStop(MediaPlayer mediaPlayer, ImageButton playButton, ImageButton pauseButton, ImageButton stopButton) {
        setEnabled(true, false, false, playButton, pauseButton, stopButton);
        mediaPlayer.stop();
    }

    private void setEnabled(boolean playButtonEnabled, boolean pauseButtonEnabled, boolean stopButtonEnabled, ImageButton playButton, ImageButton pauseButton, ImageButton stopButton) {
        playButton.setEnabled(playButtonEnabled);
        pauseButton.setEnabled(pauseButtonEnabled);
        stopButton.setEnabled(stopButtonEnabled);
    }

    private void setColors(int playColor, int pauseColor, int stopColor, ImageButton playButton, ImageButton pauseButton, ImageButton stopButton) {
        playButton.setColorFilter(playColor);
        pauseButton.setColorFilter(pauseColor);
        stopButton.setColorFilter(stopColor);
    }
}

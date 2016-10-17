package com.salatart.memeticame.Activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.salatart.memeticame.Models.Attachment;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.Touch;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MemeaudioActivity extends AppCompatActivity {

    @BindView(R.id.image_memeaudio) ImageView mImage;
    @BindView(R.id.button_play) ImageButton mPlayButton;
    @BindView(R.id.button_pause) ImageButton mPauseButton;
    @BindView(R.id.button_stop) ImageButton mStopButton;

    private MediaPlayer mMediaPlayer;

    private Attachment mAttachment;
    private Uri mImageUri;
    private Uri mAudioUri;

    public static Intent getIntent(Context context, Attachment attachment) {
        if (!attachment.isMemeaudio()) {
            return null;
        }

        Intent intent = new Intent(context, MemeaudioActivity.class);
        intent.putExtra(Attachment.PARCELABLE_KEY, attachment);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memeaudio);

        ButterKnife.bind(this);

        Bundle data = getIntent().getExtras();
        mAttachment = data.getParcelable(Attachment.PARCELABLE_KEY);
        if (!mAttachment.isMemeaudio()) {
            finish();
        }

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        setTitle("Memeaudio");
        setImage();
        setAudio();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }

        finish();
        return true;
    }

    public void setImage() {
        mImageUri = mAttachment.getMemeaudioPartUri(MemeaudioActivity.this, true);

        Glide.with(MemeaudioActivity.this)
                .load(mImageUri)
                .placeholder(R.drawable.ic_access_time_black_24dp)
                .crossFade()
                .into(mImage);

        mImage.setOnTouchListener(new Touch());
    }

    public void setAudio() {
        mAudioUri = mAttachment.getMemeaudioPartUri(MemeaudioActivity.this, false);
        setMediaPlayer();
    }

    public void setMediaPlayer() {
        mMediaPlayer = MediaPlayer.create(MemeaudioActivity.this, mAudioUri);
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                setMediaPlayer();
            }
        });

        setEnabled(true, false, false);
        setColors(Color.BLACK, Color.BLACK, Color.BLACK);
    }

    public void onPlay(View view) {
        setEnabled(false, true, true);
        setColors(Color.RED, Color.BLACK, Color.BLACK);
        mMediaPlayer.start();
    }

    public void onPause(View view) {
        setEnabled(true, false, true);
        setColors(Color.BLACK, Color.RED, Color.BLACK);
        mMediaPlayer.pause();
    }

    public void onStop(View view) {
        setEnabled(true, false, false);
        mMediaPlayer.stop();
        setMediaPlayer();
    }

    public void setEnabled(boolean playButtonEnabled, boolean pauseButtonEnabled, boolean stopButtonEnabled) {
        mPlayButton.setEnabled(playButtonEnabled);
        mPauseButton.setEnabled(pauseButtonEnabled);
        mStopButton.setEnabled(stopButtonEnabled);
    }

    public void setColors(int playColor, int pauseColor, int stopColor) {
        mPlayButton.setColorFilter(playColor);
        mPauseButton.setColorFilter(pauseColor);
        mStopButton.setColorFilter(stopColor);
    }
}

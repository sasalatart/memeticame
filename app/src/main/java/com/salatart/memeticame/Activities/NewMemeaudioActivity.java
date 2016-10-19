package com.salatart.memeticame.Activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.AudioRecorderManager;
import com.salatart.memeticame.Utils.FileUtils;
import com.salatart.memeticame.Utils.ZipManager;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.salatart.memeticame.Utils.FilterUtils.REQUEST_PICK_FILE;

public class NewMemeaudioActivity extends AppCompatActivity {

    public static final String IMAGE_STATE = "imageState";

    @BindView(R.id.group_step_1) RelativeLayout mGroup1Layout;
    @BindView(R.id.group_step_2) RelativeLayout mGroup2Layout;
    @BindView(R.id.image_memeaudio) ImageView mImageMemeaudio;
    @BindView(R.id.button_record_audio) ImageButton mRecordButton;

    private boolean mCurrentlyRecording;
    private AudioRecorderManager mAudioRecorderManager;

    private Uri mAudioUri;
    private Uri mImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_memeaudio);

        ButterKnife.bind(this);

        if (savedInstanceState != null) {
            Uri uri = savedInstanceState.getParcelable(IMAGE_STATE);
            if (uri != null) {
                startStep2(uri);
            }
        }

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        setTitle("New Memeaudio");

        mAudioRecorderManager = new AudioRecorderManager();
        mCurrentlyRecording = false;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(IMAGE_STATE, mImageUri);
        super.onSaveInstanceState(savedInstanceState);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        setResult(Activity.RESULT_CANCELED, new Intent());
        finish();
        return true;
    }

    public void chooseImage(View view) {
        startActivityForResult(FileUtils.getSelectFileIntent("image/jpeg"), REQUEST_PICK_FILE);
    }

    public void toggleRecording(View view) {
        if (mCurrentlyRecording) {
            File audioFile = mAudioRecorderManager.stopAudioRecording();
            mAudioUri = mAudioRecorderManager.addRecordingToMediaLibrary(NewMemeaudioActivity.this, audioFile);
            mRecordButton.setColorFilter(Color.BLACK);

            try {
                String zipFileName = FileUtils.getName(NewMemeaudioActivity.this, mImageUri) + ZipManager.SEPARATOR + FileUtils.getName(NewMemeaudioActivity.this, mAudioUri) + ".zip";
                String audioPath = audioFile.getAbsolutePath();
                String imagePath = FileUtils.getRealPathFromURI(NewMemeaudioActivity.this, mImageUri);
                Uri memeaudioZipUri = ZipManager.zip(new String[]{audioPath, imagePath}, zipFileName);

                Intent returnIntent = new Intent();
                returnIntent.putExtra(ZipManager.PARCELABLE_KEY, memeaudioZipUri);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            } catch (IOException e) {
                Log.e("ERROR", e.toString());
            }
        } else {
            mAudioRecorderManager.startAudioRecording(NewMemeaudioActivity.this);
            mRecordButton.setColorFilter(Color.RED);
        }
        mCurrentlyRecording = !mCurrentlyRecording;
    }

    public void startStep2(Uri uri) {
        mGroup1Layout.setVisibility(View.GONE);
        mGroup2Layout.setVisibility(View.VISIBLE);

        mImageUri = uri;
        Glide.with(NewMemeaudioActivity.this)
                .load(mImageUri)
                .crossFade()
                .into(mImageMemeaudio);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PICK_FILE && resultCode == RESULT_OK && data != null) {
            startStep2(data.getData());
        }
    }
}
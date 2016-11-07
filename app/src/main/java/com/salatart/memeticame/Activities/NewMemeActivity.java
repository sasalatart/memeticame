package com.salatart.memeticame.Activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.salatart.memeticame.Managers.AudioRecorderManager;
import com.salatart.memeticame.Managers.ZipManager;
import com.salatart.memeticame.Models.Meme;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.FileUtils;
import com.salatart.memeticame.Utils.FilterUtils;
import com.salatart.memeticame.Utils.MemeUtils;
import com.salatart.memeticame.Views.CanvasView;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewMemeActivity extends AppCompatActivity {

    @BindView(R.id.canvas) CanvasView mCanvas;
    @BindView(R.id.input_meme_name) EditText mMemeName;
    @BindView(R.id.button_undo_text) ImageButton mUndoText;
    @BindView(R.id.take_audio) ImageButton mRecordButton;
    @BindView(R.id.button_select_image) ImageButton mSelectImageButton;
    @BindView(R.id.button_play) ImageButton mPlayButton;
    @BindView(R.id.button_pause) ImageButton mPauseButton;
    @BindView(R.id.button_stop) ImageButton mStopButton;

    private boolean mCurrentlyRecording;
    private AudioRecorderManager mAudioRecorderManager;

    private MediaPlayer mMediaPlayer;
    private Uri mAudioUri;
    private File audioFile;
    private String mImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_meme);

        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        setTitle("New Meme");

        mCanvas.setMode(CanvasView.Mode.TEXT);
        mAudioRecorderManager = new AudioRecorderManager();
        registerForContextMenu(mSelectImageButton);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        setResult(Activity.RESULT_CANCELED, new Intent());
        finish();
        return true;
    }

    public void drawBitmap(Bitmap picture) {
        mCanvas.drawBitmap((Bitmap.createScaledBitmap(picture, mCanvas.getWidth(), mCanvas.getHeight(), false)));
    }

    private void createMeme() {
        if (mImagePath == null) {
            Toast.makeText(NewMemeActivity.this, "Create a meme first", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mMemeName.getText().length() == 0) {
            Toast.makeText(NewMemeActivity.this, "Insert a name for the meme", Toast.LENGTH_SHORT).show();
            return;
        }

        String imagePath = FileUtils.getMemeticameMemesDirectory() + "/" + MemeUtils.createName(mMemeName.getText().toString());
        File memeFile = new File(imagePath);
        try {
            memeFile.createNewFile();
            FileUtils.copyFileUsingStream(new File(mImagePath), memeFile);

            Intent returnIntent = new Intent();
            Uri imageUri = Uri.fromFile(memeFile);
            if (mAudioUri != null) {
                String zipFileName = FileUtils.getName(NewMemeActivity.this, imageUri) + ZipManager.SEPARATOR + FileUtils.getName(NewMemeActivity.this, mAudioUri) + ".zip";
                String audioPath = audioFile.getAbsolutePath();
                Uri memeaudioZipUri = ZipManager.zip(new String[]{audioPath, imagePath}, zipFileName);

                returnIntent.putExtra(Meme.URI_KEY, memeaudioZipUri);
            } else {
                returnIntent.putExtra(Meme.URI_KEY, imageUri);
            }

            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void editMemeFromCameraOrGallery() {
        startActivityForResult(MemeEditorActivity.getIntent(NewMemeActivity.this, null), FilterUtils.REQUEST_GET_MEME);
    }

    public void toggleRecording(View view) {
        if (mCurrentlyRecording) {
            audioFile = mAudioRecorderManager.stopAudioRecording();
            mAudioUri = mAudioRecorderManager.addRecordingToMediaLibrary(NewMemeActivity.this, audioFile);
            mRecordButton.setColorFilter(Color.BLACK);
            setMediaPlayer();
        } else {
            mAudioRecorderManager.startAudioRecording(NewMemeActivity.this);
            mRecordButton.setColorFilter(Color.RED);
        }
        mCurrentlyRecording = !mCurrentlyRecording;
    }

    public void setMediaPlayer() {
        mPlayButton.setVisibility(View.VISIBLE);
        mPauseButton.setVisibility(View.VISIBLE);
        mStopButton.setVisibility(View.VISIBLE);

        mMediaPlayer = MediaPlayer.create(NewMemeActivity.this, mAudioUri);
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderIcon(R.drawable.ic_attach_file_black_24dp);
        menu.setHeaderTitle("Select image from...");

        menu.add(Menu.NONE, 0, 0, "Meme Gallery");
        menu.add(Menu.NONE, 1, 1, "Device or Camera");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int menuItemId = item.getItemId();

        if (menuItemId == 0) {
            chooseImageFromPlainGallery();
        } else if (menuItemId == 1) {
            editMemeFromCameraOrGallery();
        }

        return true;
    }

    public void showMemeOptionsMenu(View view) {
        openContextMenu(view);
    }

    public void chooseImageFromPlainGallery() {
        startActivityForResult(new Intent(NewMemeActivity.this, PlainMemeGalleryActivity.class), FilterUtils.REQUEST_PICK_PLAIN_MEME);
    }

    public void onCreateMeme(View view) {
        createMeme();
    }

    public void onUndoText(View view) {
        mCanvas.undoText();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FilterUtils.REQUEST_GET_MEME && resultCode == RESULT_OK) {
            mImagePath = (String) data.getExtras().get(Meme.PATH_KEY);
            drawBitmap(BitmapFactory.decodeFile(mImagePath));
        } else if (requestCode == FilterUtils.REQUEST_PICK_PLAIN_MEME && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getParcelableExtra(Meme.URI_KEY);
            startActivityForResult(MemeEditorActivity.getIntent(NewMemeActivity.this, fileUri), FilterUtils.REQUEST_GET_MEME);
        }
    }
}

package com.salatart.memeticame.Activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.salatart.memeticame.Managers.AudioRecorderManager;
import com.salatart.memeticame.Managers.MediaPlayerManager;
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

    public static final String IMAGE_STATE = "imageState";
    public static final String AUDIO_STATE = "audioState";

    @BindView(R.id.canvas) CanvasView mCanvas;
    @BindView(R.id.take_audio) ImageButton mRecordButton;
    @BindView(R.id.button_play) ImageButton mPlayButton;
    @BindView(R.id.button_pause) ImageButton mPauseButton;
    @BindView(R.id.button_stop) ImageButton mStopButton;
    @BindView(R.id.button_select_image) Button mSelectImageButton;
    @BindView(R.id.button_create) Button mCreateButton;
    @BindView(R.id.button_delete) Button mDeleteButton;

    private boolean mCurrentlyRecording;
    private AudioRecorderManager mAudioRecorderManager;
    private MediaPlayerManager mMediaPlayerManager;

    private Uri mAudioUri;
    private File mAudioFile;
    private String mImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_meme);

        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        setTitle("New Meme");

        if (savedInstanceState != null) {
            mImagePath = savedInstanceState.getString(IMAGE_STATE);
            mAudioUri = savedInstanceState.getParcelable(AUDIO_STATE);

            if (mImagePath != null) {
                mCanvas.post(new Runnable() {
                    @Override
                    public void run() {
                        drawBitmap(BitmapFactory.decodeFile(mImagePath));
                    }
                });
            }

            if (mAudioUri != null) {
                setMediaPlayer();
            }
        }

        mAudioRecorderManager = new AudioRecorderManager();
        registerForContextMenu(mSelectImageButton);

        mCanvas.setMode(CanvasView.Mode.TEXT);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(IMAGE_STATE, mImagePath);
        savedInstanceState.putParcelable(AUDIO_STATE, mAudioUri);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        setResult(Activity.RESULT_CANCELED, new Intent());
        finish();
        return true;
    }

    public void drawBitmap(Bitmap picture) {
        final int maxSize = mCanvas.getHeight();

        int outWidth;
        int outHeight;
        int inWidth = picture.getWidth();
        int inHeight = picture.getHeight();

        if (inWidth > inHeight) {
            outWidth = maxSize;
            outHeight = (inHeight * maxSize) / inWidth;
        } else {
            outHeight = maxSize;
            outWidth = (inWidth * maxSize) / inHeight;
        }

        int centreX = (mCanvas.getWidth() - outWidth) / 2;
        int centreY = (mCanvas.getHeight() - outHeight) / 2;

        mCanvas.drawBitmap(Bitmap.createScaledBitmap(picture, outWidth, outHeight, false), centreX, centreY);

        mDeleteButton.setVisibility(View.VISIBLE);
        mCreateButton.setVisibility(View.VISIBLE);
    }

    private void createMeme(String memeName) {
        if (mImagePath == null) {
            Toast.makeText(NewMemeActivity.this, "Create a meme first", Toast.LENGTH_SHORT).show();
            return;
        }

        String imagePath = FileUtils.getMemeticameMemesDirectory() + "/" + MemeUtils.createName(memeName);
        File memeFile = new File(imagePath);
        try {
            memeFile.createNewFile();
            FileUtils.copyFileUsingStream(new File(mImagePath), memeFile);

            Intent returnIntent = new Intent();
            Uri imageUri = Uri.fromFile(memeFile);
            if (mAudioUri != null) {
                String zipFileName = FileUtils.getName(NewMemeActivity.this, imageUri) + ZipManager.SEPARATOR + FileUtils.getName(NewMemeActivity.this, mAudioUri) + ".zip";
                String audioPath = mAudioFile.getAbsolutePath();
                Uri memeaudioZipUri = ZipManager.zip(new String[]{audioPath, imagePath}, zipFileName);

                returnIntent.putExtra(Meme.URI_KEY, memeaudioZipUri);
            } else {
                returnIntent.putExtra(Meme.URI_KEY, imageUri);
            }

            FileUtils.deleteFile(mImagePath);
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void editMemeFromCameraOrGallery() {
        startActivityForResult(MemeEditorActivity.getIntent(NewMemeActivity.this, null), FilterUtils.REQUEST_GET_MEME);
    }

    public void chooseImageFromPlainGallery() {
        startActivityForResult(new Intent(NewMemeActivity.this, PlainMemeGalleryActivity.class), FilterUtils.REQUEST_PICK_PLAIN_MEME);
    }

    public void toggleRecording(View view) {
        if (mCurrentlyRecording) {
            mAudioFile = mAudioRecorderManager.stopAudioRecording();
            mRecordButton.setColorFilter(Color.BLACK);
            setMediaPlayer();
        } else {
            if (mAudioUri != null) {
                FileUtils.deleteFile(mAudioUri.getPath());
                mMediaPlayerManager = null;
                mAudioUri = null;
            }

            mAudioRecorderManager.startAudioRecording(NewMemeActivity.this);
            mRecordButton.setColorFilter(Color.RED);
        }
        mCurrentlyRecording = !mCurrentlyRecording;
    }

    public void setMediaPlayer() {
        mPlayButton.setVisibility(View.VISIBLE);
        mPauseButton.setVisibility(View.VISIBLE);
        mStopButton.setVisibility(View.VISIBLE);

        if (mAudioUri == null) {
            mAudioUri = mAudioRecorderManager.addRecordingToMediaLibrary(NewMemeActivity.this, mAudioFile);
        }

        mMediaPlayerManager = new MediaPlayerManager(NewMemeActivity.this, mAudioUri, mPlayButton, mPauseButton, mStopButton);
        mDeleteButton.setVisibility(View.VISIBLE);
    }

    public void onPlay(View view) {
        mMediaPlayerManager.onPlay();
    }

    public void onPause(View view) {
        mMediaPlayerManager.onPause();
    }

    public void onStop(View view) {
        mMediaPlayerManager.onStop();
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

    public void onCreateMeme(View view) {
        LayoutInflater layoutInflater = LayoutInflater.from(NewMemeActivity.this);
        View promptView = layoutInflater.inflate(R.layout.prompt_input, null);

        final EditText memeNameInput = (EditText) promptView.findViewById(R.id.input_meme_name);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(NewMemeActivity.this);
        alertDialogBuilder.setView(promptView);

        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String memeName = memeNameInput.getText().toString();
                if (memeName.isEmpty()) {
                    Toast.makeText(NewMemeActivity.this, "You must insert a name.", Toast.LENGTH_SHORT).show();
                    return;
                }

                createMeme(memeName);
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void onDeleteMeme(View view) {
        mPlayButton.setVisibility(View.GONE);
        mPauseButton.setVisibility(View.GONE);
        mStopButton.setVisibility(View.GONE);
        mDeleteButton.setVisibility(View.GONE);
        mCreateButton.setVisibility(View.GONE);

        mCurrentlyRecording = false;

        if (mAudioUri != null) {
            FileUtils.deleteFile(mAudioUri.getPath());
            mMediaPlayerManager = null;
            mAudioUri = null;
        }

        if (mImagePath != null) {
            FileUtils.deleteFile(mImagePath);
            mImagePath = null;
        }

        mCanvas.clear();
        resetCanvas();
    }

    public void resetCanvas() {
        mCanvas = new CanvasView(NewMemeActivity.this);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.ABOVE, R.id.group_meme_general_options);
        mCanvas.setLayoutParams(layoutParams);

        RelativeLayout root = (RelativeLayout) findViewById(R.id.activity_new_meme);
        root.addView(mCanvas);
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

package com.salatart.memeticame.Activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.salatart.memeticame.Models.Memetext;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.AudioRecorderManager;
import com.salatart.memeticame.Utils.FileUtils;
import com.salatart.memeticame.Utils.FilterUtils;
import com.salatart.memeticame.Utils.ZipManager;
import com.salatart.memeticame.Views.CanvasView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.salatart.memeticame.Utils.FilterUtils.REQUEST_PICK_FILE;

public class NewMemetextActivity extends AppCompatActivity {

    @BindView(R.id.canvas) CanvasView mCanvas;
    @BindView(R.id.input_meme_name) EditText mMemeName;
    @BindView(R.id.button_undo_text) ImageButton mUndoText;
    @BindView(R.id.take_audio) ImageButton mRecordButton;
    @BindView(R.id.button_select_image) ImageButton mSelectImageButton;
    @BindView(R.id.button_play) ImageButton mPlayButton;
    @BindView(R.id.button_pause) ImageButton mPauseButton;
    @BindView(R.id.button_stop) ImageButton mStopButton;

    private Uri mCurrentImageUri;

    private boolean mCurrentlyRecording;
    private AudioRecorderManager mAudioRecorderManager;

    private MediaPlayer mMediaPlayer;
    private Uri mAudioUri;
    private File audioFile;

    public static Bitmap RotateBitmap(Bitmap source, float angle) {
        if(angle == 0)
            return source;
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_memetext);

        ButterKnife.bind(this);

        mCanvas.setMode(CanvasView.Mode.TEXT);
        mAudioRecorderManager = new AudioRecorderManager();
        registerForContextMenu(mSelectImageButton);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        setTitle("New Meme");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        setResult(Activity.RESULT_CANCELED, new Intent());
        finish();
        return true;
    }

    public void onUndoText(View view) {
        mCanvas.undoText();
    }

    public void onCreate(View view) {
        createMeme();
    }

    public void setImageFromCurrentUri(float rotateAngle) {
        try {
            Bitmap picture = MediaStore.Images.Media.getBitmap(getContentResolver(), mCurrentImageUri);
            drawBitmap(picture, rotateAngle);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void drawBitmap(Bitmap picture, float rotateAngle) {
        mCanvas.drawBitmap((Bitmap.createScaledBitmap(RotateBitmap(picture,rotateAngle), mCanvas.getWidth(), mCanvas.getHeight(), false)));
    }

    private void createMeme() {
        if (mMemeName.getText().length() == 0) {
            Toast.makeText(NewMemetextActivity.this, "Insert a name for the meme", Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap meme = mCanvas.getBitmap();
        String imagePath = FileUtils.getMemeticameDirectory() + "/" + mMemeName.getText().toString().replace(' ', '_') + ".jpg";
        File memeFile = new File(imagePath);
        try {
            memeFile.createNewFile();
            OutputStream os = new BufferedOutputStream(new FileOutputStream(memeFile));
            meme.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.close();

            Intent returnIntent = new Intent();
            Uri imageUri = Uri.fromFile(memeFile);
            if (mAudioUri != null) {
                String zipFileName = FileUtils.getName(NewMemetextActivity.this, imageUri) + ZipManager.SEPARATOR + FileUtils.getName(NewMemetextActivity.this, mAudioUri) + ".zip";
                String audioPath = audioFile.getAbsolutePath();
                Uri memeaudioZipUri = ZipManager.zip(new String[]{audioPath, imagePath}, zipFileName);

                returnIntent.putExtra(Memetext.PARCELABLE_KEY, memeaudioZipUri);
            } else {
                returnIntent.putExtra(Memetext.PARCELABLE_KEY, imageUri);
            }

            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void toggleRecording(View view) {
        if (mCurrentlyRecording) {
            audioFile = mAudioRecorderManager.stopAudioRecording();
            mAudioUri = mAudioRecorderManager.addRecordingToMediaLibrary(NewMemetextActivity.this, audioFile);
            mRecordButton.setColorFilter(Color.BLACK);
            setMediaPlayer();
        } else {
            mAudioRecorderManager.startAudioRecording(NewMemetextActivity.this);
            mRecordButton.setColorFilter(Color.RED);
        }
        mCurrentlyRecording = !mCurrentlyRecording;
    }

    public void setMediaPlayer() {
        mMediaPlayer = MediaPlayer.create(NewMemetextActivity.this, mAudioUri);
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
        if (mAudioUri == null) {
            Toast.makeText(this, "Please, add an audio", Toast.LENGTH_SHORT).show();
            return;
        }
        setEnabled(false, true, true);
        setColors(Color.RED, Color.BLACK, Color.BLACK);
        mMediaPlayer.start();
    }

    public void onPause(View view) {
        if (mAudioUri == null) {
            Toast.makeText(this, "Please, add an audio", Toast.LENGTH_SHORT).show();
            return;
        }
        setEnabled(true, false, true);
        setColors(Color.BLACK, Color.RED, Color.BLACK);
        mMediaPlayer.pause();
    }

    public void onStop(View view) {
        if (mAudioUri == null) {
            Toast.makeText(this, "Please, add an audio", Toast.LENGTH_SHORT).show();
            return;
        }
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
        menu.add(Menu.NONE, 1, 1, "Device");
        menu.add(Menu.NONE, 2, 2, "Camera");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int menuItemId = item.getItemId();

        if (menuItemId == 0) {
            chooseImageFromPlainGallery();
        } else if (menuItemId == 1) {
            chooseImageFromDevice();
        } else if (menuItemId == 2) {
            chooseImageFromCamera();
        }

        return true;
    }

    public void showMemeOptionsMenu(View view) {
        openContextMenu(view);
    }

    public void chooseImageFromCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = FileUtils.createMediaFile(getApplicationContext(), "jpg", Environment.DIRECTORY_PICTURES);
            if (photoFile != null) {
                mCurrentImageUri = FileProvider.getUriForFile(this, "com.salatart.memeticame.fileprovider", photoFile);
                System.out.println("hola:" + mCurrentImageUri.toString());
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCurrentImageUri);
                startActivityForResult(takePictureIntent, FilterUtils.REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    public void chooseImageFromDevice() {
        startActivityForResult(FileUtils.getSelectFileIntent("image/jpeg"), REQUEST_PICK_FILE);
    }

    public void chooseImageFromPlainGallery() {
        startActivityForResult(new Intent(NewMemetextActivity.this, PlainMemeGalleryActivity.class), FilterUtils.REQUEST_PICK_PLAIN_MEME);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FilterUtils.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            setImageFromCurrentUri(90);
        } else if (requestCode == FilterUtils.REQUEST_PICK_FILE && resultCode == RESULT_OK && data != null) {
            mCurrentImageUri = data.getData();
            setImageFromCurrentUri(0);
        } else if (requestCode == FilterUtils.REQUEST_PICK_PLAIN_MEME && resultCode == RESULT_OK) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String imageUri = (String) data.getExtras().get(Memetext.PARCELABLE_KEY);
                        drawBitmap(FileUtils.getBitmapFromURL(imageUri), 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            thread.start();
        }
    }
}

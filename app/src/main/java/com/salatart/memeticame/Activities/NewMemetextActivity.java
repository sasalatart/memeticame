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
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

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

public class NewMemetextActivity extends AppCompatActivity {

    @BindView(R.id.canvas) CanvasView mCanvas;
    @BindView(R.id.get_from_gallery) Button mGetFromGalleryButton;
    @BindView(R.id.get_from_camera) Button mGetFromCameraButton;
    @BindView(R.id.undo_text) ImageButton mUndoText;
    @BindView(R.id.meme_name) EditText mMemeName;
    @BindView(R.id.take_audio) ImageButton mRecordButton;
    @BindView(R.id.button_play) ImageButton mPlayButton;
    @BindView(R.id.button_pause) ImageButton mPauseButton;
    @BindView(R.id.button_stop) ImageButton mStopButton;

    private Uri mCurrentImageUri;

    private boolean mCurrentlyRecording;
    private AudioRecorderManager mAudioRecorderManager;

    private MediaPlayer mMediaPlayer;
    private Uri mAudioUri;
    private File audioFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_memetext);

        ButterKnife.bind(this);

        mCanvas.setMode(CanvasView.Mode.TEXT);
        mAudioRecorderManager = new AudioRecorderManager();


    }

    public void dispatchTakePictureForMemetextIntent(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = FileUtils.createMediaFile(getApplicationContext(), "jpg", Environment.DIRECTORY_PICTURES);
            if (photoFile != null) {
                mCurrentImageUri = FileProvider.getUriForFile(this, "com.salatart.memeticame.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCurrentImageUri);
                startActivityForResult(takePictureIntent, FilterUtils.REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

         if (requestCode == FilterUtils.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
             Bitmap picture = null;
             try {
                 picture = MediaStore.Images.Media.getBitmap(getContentResolver(), mCurrentImageUri);
             } catch (IOException e) {
                 e.printStackTrace();
             }

             mCanvas.drawBitmap((Bitmap.createScaledBitmap(RotateBitmap(picture,90), mCanvas.getWidth(), mCanvas.getHeight(), false)));
         }
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public void undoText(View view){

        mCanvas.undoText();

    }

    private void createMeme(){

        if(mMemeName.getText().length() == 0){
            Toast.makeText(NewMemetextActivity.this, "Insert a name", Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap meme = mCanvas.getBitmap();
        String imagePath = FileUtils.getMemeticameDirectory() + "/" + mMemeName.getText() + ".jpg";
        File memeFile = new File(imagePath);
        try {
            memeFile.createNewFile();
            OutputStream os =  new BufferedOutputStream(new FileOutputStream(memeFile));
            meme.compress(Bitmap.CompressFormat.JPEG, 100 , os);
            os.close();

            if(mAudioUri != null) {
                Uri mImageUri = Uri.fromFile(memeFile);
                String zipFileName = FileUtils.getName(NewMemetextActivity.this, mImageUri) + ZipManager.SEPARATOR + FileUtils.getName(NewMemetextActivity.this, mAudioUri) + ".zip";
                String audioPath = audioFile.getAbsolutePath();
                Uri memeaudioZipUri = ZipManager.zip(new String[]{audioPath, imagePath}, zipFileName);
            }

            Intent returnIntent = new Intent();
            setResult(Activity.RESULT_OK, returnIntent);
            finish();

        }
        catch (IOException e) {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.create_meme_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_create_meme:
                createMeme();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        if(mAudioUri == null){
            Toast.makeText(this, "Please, add an audio", Toast.LENGTH_SHORT).show();
            return;
        }
        setEnabled(false, true, true);
        setColors(Color.RED, Color.BLACK, Color.BLACK);
        mMediaPlayer.start();
    }

    public void onPause(View view) {
        if(mAudioUri == null){
            Toast.makeText(this, "Please, add an audio", Toast.LENGTH_SHORT).show();
            return;
        }
        setEnabled(true, false, true);
        setColors(Color.BLACK, Color.RED, Color.BLACK);
        mMediaPlayer.pause();
    }

    public void onStop(View view) {
        if(mAudioUri == null){
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






}

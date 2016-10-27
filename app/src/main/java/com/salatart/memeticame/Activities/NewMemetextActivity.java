package com.salatart.memeticame.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
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
import android.widget.ImageButton;

import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.FileUtils;
import com.salatart.memeticame.Utils.FilterUtils;
import com.salatart.memeticame.Views.CanvasView;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewMemetextActivity extends AppCompatActivity {

    @BindView(R.id.canvas) CanvasView mCanvas;
    @BindView(R.id.get_from_gallery) Button mGetFromGalleryButton;
    @BindView(R.id.get_from_camera) Button mGetFromCameraButton;
    @BindView(R.id.undo_text) ImageButton mUndoText;

    private Uri mCurrentImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_memetext);

        ButterKnife.bind(this);

        mCanvas.setMode(CanvasView.Mode.TEXT);


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

        Bitmap meme = mCanvas.getBitmap();

        //mCanvas.drawBitmap((Bitmap.createScaledBitmap(meme, mCanvas.getWidth(), mCanvas.getHeight(), false)));


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







}

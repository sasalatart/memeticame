package com.salatart.memeticame.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.FilterUtils;
import com.salatart.memeticame.Views.CanvasView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewMemetextActivity extends AppCompatActivity {

    @BindView(R.id.canvas) CanvasView mCanvas;
    @BindView(R.id.get_from_gallery) Button mGetFromGalleryButton;
    @BindView(R.id.get_from_camera) Button mGetFromCameraButton;

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
            startActivityForResult(takePictureIntent,  FilterUtils.REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

         if (requestCode == FilterUtils.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
             Bitmap picture = (Bitmap) data.getExtras().get("data");
             mCanvas.drawBitmap((Bitmap.createScaledBitmap(picture, mCanvas.getWidth(), mCanvas.getHeight(), false)));
         }
    }


}

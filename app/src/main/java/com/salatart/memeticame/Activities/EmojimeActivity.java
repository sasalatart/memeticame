package com.salatart.memeticame.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.salatart.memeticame.Models.Meme;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.FilterUtils;
import com.salatart.memeticame.Views.EmojimeView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EmojimeActivity extends AppCompatActivity {

    @BindView(R.id.emojime_view) EmojimeView mEmojimeView;
    @BindView(R.id.button_turn_into_meme) Button mTurnIntoMemeButton;
    @BindView(R.id.button_take_picture) Button mTakePictureButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emojime);

        ButterKnife.bind(this);
    }

    public void takePicture(View view) {
        startActivityForResult(MemeEditorActivity.getIntent(EmojimeActivity.this, null), FilterUtils.REQUEST_IMAGE_CAPTURE);
    }

    public void setImage(Bitmap bitmap) {
        FaceDetector detector = new FaceDetector.Builder(EmojimeActivity.this)
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Face> faces = detector.detect(frame);

        mEmojimeView.setContent(bitmap, faces);

        detector.release();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FilterUtils.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            String mImagePath = (String) data.getExtras().get(Meme.PATH_KEY);
            setImage(BitmapFactory.decodeFile(mImagePath));
            mTurnIntoMemeButton.setVisibility(View.VISIBLE);
            mTakePictureButton.setText("Retake picture");
        }
    }
}

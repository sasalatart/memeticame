package com.salatart.memeticame.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.salatart.memeticame.Listeners.OnRequestIndexListener;
import com.salatart.memeticame.Models.FaceEmotion;
import com.salatart.memeticame.Models.Meme;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.CallbackUtils;
import com.salatart.memeticame.Utils.FaceEmotionsUtils;
import com.salatart.memeticame.Utils.FileUtils;
import com.salatart.memeticame.Utils.FilterUtils;
import com.salatart.memeticame.Utils.Routes;
import com.salatart.memeticame.Views.EmojimeView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Request;

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

    public void setImage(Bitmap bitmap, ArrayList<FaceEmotion> faceEmotions) {
        FaceDetector detector = new FaceDetector.Builder(EmojimeActivity.this)
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Face> faces = detector.detect(frame);

        mEmojimeView.setContent(bitmap, faces, faceEmotions);

        detector.release();
    }

    public void requestEmotions(final String imagePath) {
        final ProgressDialog progressDialog = ProgressDialog.show(EmojimeActivity.this, "Please wait", "Processing faces...", true);
        try {
            Uri uri = Uri.fromFile(new File(imagePath));
            String base64 = FileUtils.encodeToBase64FromUri(EmojimeActivity.this, uri);
            String mimeType = FileUtils.getMimeType(EmojimeActivity.this, uri);

            Request request = Routes.emotionsShow(EmojimeActivity.this, base64, mimeType);
            FaceEmotionsUtils.recognizeRequest(request, new OnRequestIndexListener<FaceEmotion>() {
                @Override
                public void OnSuccess(final ArrayList<FaceEmotion> faceEmotions) {
                    EmojimeActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setImage(BitmapFactory.decodeFile(imagePath), faceEmotions);
                            mTurnIntoMemeButton.setVisibility(View.VISIBLE);
                            mTakePictureButton.setText("Retake picture");
                            progressDialog.dismiss();
                        }
                    });
                }

                @Override
                public void OnFailure(String message) {
                    CallbackUtils.onUnsuccessfulRequest(EmojimeActivity.this, message);
                    progressDialog.dismiss();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FilterUtils.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            requestEmotions((String) data.getExtras().get(Meme.PATH_KEY));
        }
    }
}

package com.salatart.memeticame.Views;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;

import com.google.android.gms.vision.face.Face;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

/**
 * Created by sasalatart on 11/29/16.
 */

public class EmojimeView extends View {
    private Bitmap mBitmap;
    private SparseArray<Face> mFaces;

    public EmojimeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setDrawingCacheEnabled(true);
    }

    /**
     * Sets the bitmap background and the associated face detections.
     */
    public void setContent(Bitmap bitmap, SparseArray<Face> faces) {
        mBitmap = bitmap;
        mFaces = faces;
        invalidate();
    }

    /**
     * Draws the bitmap background and the associated face landmarks.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if ((mBitmap != null) && (mFaces != null)) {
            double scale = drawBitmap(canvas);
            addEmojis(canvas, scale);
            detectFaceCharacteristics(canvas, scale);
        }
    }

    /**
     * Draws the bitmap background, scaled to the device size.  Returns the scale for future use in
     * positioning the facial landmark graphics.
     */
    private double drawBitmap(Canvas canvas) {
        double scale = Math.min(((float) canvas.getWidth()) / mBitmap.getWidth(), ((float) canvas.getHeight()) / mBitmap.getHeight());
        Rect destBounds = new Rect(0, 0, (int) (mBitmap.getWidth() * scale), (int) (mBitmap.getHeight() * scale));
        canvas.drawBitmap(mBitmap, null, destBounds, null);
        return scale;
    }

    /**
     * Adds emojis to each detected face
     */
    private void addEmojis(Canvas canvas, double scale) {
        for (int i = 0; i < mFaces.size(); ++i) {
            Face face = mFaces.valueAt(i);

            float maxSize = (float) Math.max(face.getWidth() * scale, face.getHeight() * scale);

            String emojiName = selectEmoji(face) + ".png";
            Bitmap emoji = getBitmapFromAssets(emojiName);
            Bitmap scaledEmoji = scaleBitmap(emoji, maxSize, true);

            float posX = (float) ((face.getPosition().x * scale) + (face.getWidth() * scale) / 2) - maxSize / 2;
            float posY = (float) ((face.getPosition().y * scale) + (face.getHeight() * scale) / 2) - maxSize / 2;
            canvas.drawBitmap(scaledEmoji, posX, posY, null);
        }
    }

    private String selectEmoji(Face face) {
        Random randomGenerator = new Random();

        boolean leftEyeOpened = face.getIsLeftEyeOpenProbability() > 0.5;
        boolean rightEyeOpened = face.getIsRightEyeOpenProbability() > 0.5;
        boolean bigSmile = face.getIsSmilingProbability() > 0.75;
        boolean normalSmile = face.getIsSmilingProbability() > 0.5;
        boolean neutralSmile = face.getIsSmilingProbability() > 0.25;

        if (bigSmile) {
            if (leftEyeOpened && rightEyeOpened) {
                return "big_smile_both_eyes_opened_" + (randomGenerator.nextInt(3) + 1);
            } else if (leftEyeOpened || rightEyeOpened) {
                return "big_smile_left_eye_closed";
            } else {
                return "big_smile_both_eyes_closed_" + (randomGenerator.nextInt(3) + 1);
            }
        } else if (normalSmile) {
            if (leftEyeOpened && rightEyeOpened) {
                return "normal_smile_both_eyes_opened";
            } else if (leftEyeOpened || rightEyeOpened) {
                return "normal_smile_right_eye_closed";
            } else {
                return "normal_smile_both_eyes_closed";
            }
        } else if (neutralSmile) {
            if (leftEyeOpened && rightEyeOpened) {
                return "neutral_both_eyes_opened";
            } else {
                return "neutral_both_eyes_closed";
            }
        } else {
            return "not_smiling_both_eyes_closed";
        }
    }

    /**
     * Detects characteristics of a face
     */
    private void detectFaceCharacteristics(Canvas canvas, double scale) {
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
        paint.setTextSize(25.0f);

        for (int i = 0; i < mFaces.size(); ++i) {
            Face face = mFaces.valueAt(i);
            float cx = (float) (face.getPosition().x * scale);
            float cy = (float) (face.getPosition().y * scale);
            canvas.drawText(String.valueOf(face.getIsSmilingProbability()), cx, cy + 10.0f, paint);
        }
    }

    public Bitmap getBitmapFromAssets(String filePath) {
        AssetManager assetManager = getContext().getAssets();

        try {
            InputStream is = assetManager.open(filePath);
            return BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Bitmap scaleBitmap(Bitmap realImage, float maxImageSize, boolean filter) {
        float ratio = Math.min(maxImageSize / realImage.getWidth(), maxImageSize / realImage.getHeight());
        int width = Math.round(ratio * realImage.getWidth());
        int height = Math.round(ratio * realImage.getHeight());
        return Bitmap.createScaledBitmap(realImage, width, height, filter);
    }
}

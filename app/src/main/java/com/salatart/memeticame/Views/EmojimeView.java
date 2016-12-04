package com.salatart.memeticame.Views;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;

import com.google.android.gms.vision.face.Face;
import com.salatart.memeticame.Models.FaceEmotion;
import com.salatart.memeticame.Models.FaceEmotion.Emotions;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by sasalatart on 11/29/16.
 */

public class EmojimeView extends View {
    private Bitmap mOriginalBitmap;
    private Bitmap mBitmap;
    private SparseArray<Face> mFaces;
    private ArrayList<FaceEmotion> mFaceEmotions;

    public EmojimeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setDrawingCacheEnabled(true);
    }

    /**
     * Sets the bitmap background and the associated face detections.
     */
    public void setContent(Bitmap bitmap, SparseArray<Face> faces, ArrayList<FaceEmotion> faceEmotions) {
        mOriginalBitmap = bitmap;
        mBitmap = mOriginalBitmap.copy(mOriginalBitmap.getConfig(), true);
        mFaces = faces;
        mFaceEmotions = faceEmotions;
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
        for (FaceEmotion faceEmotion : mFaceEmotions) {

            Face correspondingFace = null;
            for (int i = 0; i < mFaces.size(); i++) {
                int key = mFaces.keyAt(i);
                Face face = mFaces.get(key);

                if (contains(face, faceEmotion)) {
                    correspondingFace = face;
                    break;
                }
            }

            float maxSize = (float) Math.max(faceEmotion.getWidth() * scale, faceEmotion.getHeight() * scale);
            float posX = (float) ((faceEmotion.getLeft() * scale) + (faceEmotion.getWidth() * scale) / 2) - maxSize / 2;
            float posY = (float) ((faceEmotion.getTop() * scale) + (faceEmotion.getHeight() * scale) / 2) - maxSize / 2;
            if (correspondingFace != null) {
                maxSize = (float) Math.max(correspondingFace.getWidth() * scale, correspondingFace.getHeight() * scale);
                posX = (float) ((correspondingFace.getPosition().x * scale) + (correspondingFace.getWidth() * scale) / 2) - maxSize / 2;
                posY = (float) ((correspondingFace.getPosition().y * scale) + (correspondingFace.getHeight() * scale) / 2) - maxSize / 2;
            }

            Emotions emotion = faceEmotion.getEmotion();
            String emojiName = selectEmoji(emotion, correspondingFace) + ".png";
            Bitmap emoji = getBitmapFromAssets(emojiName);
            Bitmap scaledEmoji = scaleBitmap(emoji, maxSize, true);

            canvas.drawBitmap(scaledEmoji, posX, posY, null);
        }
    }

    private String selectEmoji(Emotions emotion, Face faceMatch) {
        Random randomGenerator = new Random();

        boolean leftEyeClosed = (faceMatch != null) && (faceMatch.getIsLeftEyeOpenProbability() < 0.5);
        boolean rightEyeClosed = (faceMatch != null) && (faceMatch.getIsRightEyeOpenProbability() < 0.5);
        boolean eyesClosed = faceMatch != null && leftEyeClosed && rightEyeClosed;
        boolean wink = faceMatch != null && (leftEyeClosed || rightEyeClosed);

        switch (emotion) {
            case ANGER:
                if (eyesClosed) {
                    return "anger_eyes_closed_" + (randomGenerator.nextInt(1) + 1);
                } else {
                    return "anger_" + (randomGenerator.nextInt(2) + 1);
                }
            case CONTEMPT:
                if (eyesClosed) {
                    return "contempt_eyes_closed_" + (randomGenerator.nextInt(1) + 1);
                } else {
                    return "contempt_" + (randomGenerator.nextInt(1) + 1);
                }
            case DISGUST:
                if (eyesClosed) {
                    return "disgust_eyes_closed_" + (randomGenerator.nextInt(1) + 1);
                } else {
                    return "disgust_" + (randomGenerator.nextInt(1) + 1);
                }
            case FEAR:
                return "fear_" + (randomGenerator.nextInt(5) + 1);
            case HAPPINESS:
                if (eyesClosed) {
                    return "happiness_eyes_closed_" + (randomGenerator.nextInt(6) + 1);
                } else if (wink) {
                    return "happiness_wink_" + (randomGenerator.nextInt(2) + 1);
                } else {
                    return "happiness_" + (randomGenerator.nextInt(4) + 1);
                }
            case NEUTRAL:
                if (eyesClosed) {
                    return "neutral_" + (randomGenerator.nextInt(1) + 1);
                } else {
                    return "neutral_" + (randomGenerator.nextInt(1) + 1);
                }
            case SADNESS:
                if (eyesClosed) {
                    return "sadness_eyes_closed_" + (randomGenerator.nextInt(1) + 1);
                } else {
                    return "sadness_" + (randomGenerator.nextInt(1) + 1);
                }
            case SURPRISE:
                if (eyesClosed) {
                    return "surprise_" + (randomGenerator.nextInt(2) + 1);
                } else {
                    return "surprise_" + (randomGenerator.nextInt(4) + 1);
                }
            default:
                return "happiness_" + (randomGenerator.nextInt(4) + 1);
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

    public boolean contains(Face face, FaceEmotion faceEmotion) {
        return (faceEmotion.getLeft() + faceEmotion.getWidth() < face.getPosition().x + face.getWidth()) &&
                faceEmotion.getLeft() > face.getPosition().x &&
                faceEmotion.getTop() + faceEmotion.getHeight() < face.getPosition().y + face.getHeight() &&
                faceEmotion.getTop() > face.getPosition().y;
    }
}

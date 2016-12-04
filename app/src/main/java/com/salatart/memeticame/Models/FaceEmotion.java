package com.salatart.memeticame.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

/**
 * Created by sasalatart on 12/4/16.
 */

public class FaceEmotion implements Parcelable {

    public static final Creator<FaceEmotion> CREATOR = new Creator<FaceEmotion>() {
        @Override
        public FaceEmotion createFromParcel(Parcel in) {
            return new FaceEmotion(in);
        }

        @Override
        public FaceEmotion[] newArray(int size) {
            return new FaceEmotion[size];
        }
    };

    public static String PARCELABLE_KEY = "com.salatart.memeticame.Models.FaceEmotion";
    public static String PARCELABLE_ARRAY_KEY = "com.salatart.memeticame.Models.FaceEmotionArrayList";

    private int mHeight;
    private int mWidth;
    private int mTop;
    private int mLeft;
    private HashMap<Emotions, Float> mScores;

    public FaceEmotion(int height, int width, int top, int left, HashMap<Emotions, Float> scores) {
        mHeight = height;
        mWidth = width;
        mTop = top;
        mLeft = left;
        mScores = scores;
    }

    protected FaceEmotion(Parcel in) {
        mHeight = in.readInt();
        mWidth = in.readInt();
        mTop = in.readInt();
        mLeft = in.readInt();
        mScores = in.readHashMap(HashMap.class.getClassLoader());
    }

    public int getHeight() {
        return mHeight;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getTop() {
        return mTop;
    }

    public int getLeft() {
        return mLeft;
    }

    public HashMap<Emotions, Float> getScores() {
        return mScores;
    }

    public Emotions getEmotion() {
        float maxValue = 0;
        Emotions maxEmotion = Emotions.CONTEMPT;
        for (HashMap.Entry<Emotions, Float> score : mScores.entrySet()) {
            if (score.getValue() > maxValue) {
                maxValue = score.getValue();
                maxEmotion = score.getKey();
            }
        }

        return maxEmotion;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mHeight);
        dest.writeInt(mWidth);
        dest.writeInt(mTop);
        dest.writeInt(mLeft);
        dest.writeMap(mScores);
    }

    public enum Emotions {NEUTRAL, SADNESS, HAPPINESS, CONTEMPT, FEAR, ANGER, DISGUST, SURPRISE}
}

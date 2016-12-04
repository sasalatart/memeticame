package com.salatart.memeticame.Models;

import java.util.HashMap;

/**
 * Created by sasalatart on 12/4/16.
 */

public class FaceEmotion {

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

    public enum Emotions {NEUTRAL, SADNESS, HAPPINESS, CONTEMPT, FEAR, ANGER, DISGUST, SURPRISE}
}

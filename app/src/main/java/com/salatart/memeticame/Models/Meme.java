package com.salatart.memeticame.Models;

import android.graphics.Color;
import android.graphics.Typeface;

/**
 * Created by Sebastian on 26-10-2016.
 */

public class Meme {
    public static String URI_KEY = "memeUri";
    public static String GALLERY_MODE_KEY = "galleryModeKey";
    public static String SEPARATOR = "-meme-";

    private String mText;
    private float mPositionX;
    private float mPositionY;

    private float mFontSize;
    private Typeface mFontFamily;
    private int mPaintColor;

    public Meme(String text, float posX, float posY) {
        mText = text;
        mPositionX = posX;
        mPositionY = posY;
        mFontSize = 256F;
        mFontFamily = Typeface.DEFAULT;
        mPaintColor = Color.BLACK;
    }

    public String getText() {
        return mText;
    }

    public void setText(String mText) {
        this.mText = mText;
    }

    public float getPositionX() {
        return mPositionX;
    }

    public void setPositionX(float mPositionX) {
        this.mPositionX = mPositionX;
    }

    public float getPositionY() {
        return mPositionY;
    }

    public void setPositionY(float mPositionY) {
        this.mPositionY = mPositionY;
    }

    public float getFontSize() {
        return mFontSize;
    }

    public void setFontSize(float mFontSize) {
        this.mFontSize = mFontSize;
    }

    public Typeface getFontFamily() {
        return mFontFamily;
    }

    public void setFontFamily(Typeface mFontFamily) {
        this.mFontFamily = mFontFamily;
    }

    public int getPaintColor() {
        return mPaintColor;
    }

    public void setPaintColor(int mPaintFillColor) {
        this.mPaintColor = mPaintFillColor;
    }
}

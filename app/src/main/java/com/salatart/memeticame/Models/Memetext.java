package com.salatart.memeticame.Models;

/**
 * Created by Sebastian on 26-10-2016.
 */

public class Memetext {

    private String mText;
    private float mPositionX;
    private float mPositionY;

    public Memetext(String text, float posX, float posY){
        mText = text;
        mPositionX = posX;
        mPositionY = posY;
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
}

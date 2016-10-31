package com.salatart.memeticame.Enums;

import android.graphics.Typeface;

/**
 * Created by gio on 30-10-16.
 */

public enum TypefaceConverter {
    DEFAULT,
    DEFAULT_BOLD,
    MONOSPACE,
    SANS_SERIF,
    SERIF;

    public Typeface getTypeface() {
        Typeface value;
        switch (this) {
            case DEFAULT_BOLD:
                value = Typeface.DEFAULT_BOLD;
                break;
            case MONOSPACE:
                value = Typeface.MONOSPACE;
                break;
            case SANS_SERIF:
                value = Typeface.SANS_SERIF;
                break;
            case SERIF:
                value = Typeface.SERIF;
                break;
            default:
                value = Typeface.DEFAULT;
        }
        return value;
    }
}

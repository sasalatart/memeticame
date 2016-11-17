package com.salatart.memeticame.Models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Sebastian on 26-10-2016.
 */

public class Meme implements Parcelable {
    public static final Creator<Meme> CREATOR = new Creator<Meme>() {
        @Override
        public Meme createFromParcel(Parcel in) {
            return new Meme(in);
        }

        @Override
        public Meme[] newArray(int size) {
            return new Meme[size];
        }
    };

    public static String PARCELABLE_KEY = "com.salatart.memeticame.Models.Meme";
    public static String PARCELABLE_ARRAY_KEY = "com.salatart.memeticame.Models.MemeArrayList";

    public static String URI_KEY = "memeUri";
    public static String SEPARATOR = "-meme-";
    public static String PATH_KEY = "memePath";

    private final int mId;
    private String mName;
    private User mOwner;
    private String mThumbUrl;
    private String mOriginalUrl;
    private double mRating;
    private String mCreatedAt;

    public Meme(int id, String name, User owner, String thumbUrl, String originalUrl, double rating, String createdAt) {
        this.mId = id;
        this.mName = name;
        this.mOwner = owner;
        this.mThumbUrl = thumbUrl;
        this.mOriginalUrl = originalUrl;
        this.mRating = rating;
        this.mCreatedAt = createdAt;
    }

    protected Meme(Parcel in) {
        mId = in.readInt();
        mName = in.readString();
        mOwner = in.readParcelable(User.class.getClassLoader());
        mThumbUrl = in.readString();
        mOriginalUrl = in.readString();
        mRating = in.readDouble();
        mCreatedAt = in.readString();
    }

    public int getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public User getOwner() {
        return mOwner;
    }

    public String getThumbUrl() {
        return mThumbUrl;
    }

    public String getOriginalUrl() {
        return mOriginalUrl;
    }

    public double getRating() {
        return mRating;
    }

    public String getCreatedAt() {
        return mCreatedAt;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeString(mName);
        dest.writeParcelable(mOwner, flags);
        dest.writeString(mThumbUrl);
        dest.writeString(mOriginalUrl);
        dest.writeDouble(mRating);
        dest.writeString(mCreatedAt);
    }
}

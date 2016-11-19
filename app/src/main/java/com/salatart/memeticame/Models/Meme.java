package com.salatart.memeticame.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

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
    private int mCategoryId;
    private String mName;
    private User mOwner;
    private String mThumbUrl;
    private String mOriginalUrl;
    private double mRating;
    private ArrayList<String> mTags;
    private String mCreatedAt;

    public Meme(int id, int categoryId, String name, User owner, String thumbUrl, String originalUrl, double rating, ArrayList<String> tags, String createdAt) {
        this.mId = id;
        this.mCategoryId = categoryId;
        this.mName = name;
        this.mOwner = owner;
        this.mThumbUrl = thumbUrl;
        this.mOriginalUrl = originalUrl;
        this.mRating = rating;
        this.mTags = tags;
        this.mCreatedAt = createdAt;
    }

    protected Meme(Parcel in) {
        this.mId = in.readInt();
        this.mCategoryId = in.readInt();
        this.mName = in.readString();
        this.mOwner = in.readParcelable(User.class.getClassLoader());
        this.mThumbUrl = in.readString();
        this.mOriginalUrl = in.readString();
        this.mRating = in.readDouble();

        this.mTags = new ArrayList();
        in.readStringList(this.mTags);

        this.mCreatedAt = in.readString();
    }

    public int getId() {
        return mId;
    }

    public int getCategoryId() {
        return mCategoryId;
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

    public ArrayList<String> getTags() {
        return mTags;
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
        dest.writeInt(mCategoryId);
        dest.writeString(mName);
        dest.writeParcelable(mOwner, flags);
        dest.writeString(mThumbUrl);
        dest.writeString(mOriginalUrl);
        dest.writeDouble(mRating);
        dest.writeStringList(mTags);
        dest.writeString(mCreatedAt);
    }
}

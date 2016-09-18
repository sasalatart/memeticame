package com.salatart.memeticame.Models;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by sasalatart on 9/14/16.
 */
public class Attachment implements Parcelable {
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Attachment createFromParcel(Parcel in) {
            return new Attachment(in);
        }

        public Attachment[] newArray(int size) {
            return new Attachment[size];
        }
    };
    public static int IMAGE_SIZE = 480;
    public static int IMAGE_THUMB_SIZE = 48;
    private String mName;
    private String mMimeType;
    private String mBase64Content;
    private String mUri;

    public Attachment(String name, String mimeType, String base64Content, String uri) {
        this.mName = name;
        this.mMimeType = mimeType;
        this.mBase64Content = base64Content;
        this.mUri = uri;
    }

    public Attachment(Parcel in) {
        this.mName = in.readString();
        this.mMimeType = in.readString();
        this.mBase64Content = in.readString();
        this.mUri = in.readString();
    }

    public static Intent getIntent() {
        Intent intent = new Intent();
        intent.setType("image/* video/* audio/*");
        String[] mimetypes = {"image/*", "video/*", "audio/*"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        return Intent.createChooser(intent, "Select file");
    }

    public Attachment clone() {
        return new Attachment(mName, mMimeType, mBase64Content, mUri);
    }

    public String getName() {
        return mName;
    }

    public String getMimeType() {
        return mMimeType;
    }

    public String getBase64Content() {
        return mBase64Content;
    }

    public String getUri() {
        return mUri;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeString(mMimeType);
        dest.writeString(mBase64Content);
        dest.writeString(mUri);
    }
}

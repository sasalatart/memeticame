package com.salatart.memeticame.Models;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.salatart.memeticame.Activities.MemeaudioActivity;
import com.salatart.memeticame.Utils.FileUtils;
import com.salatart.memeticame.Utils.ZipManager;

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

    public String getStringUri() {
        return mUri;
    }

    public void setUri(String uri) {
        mUri = uri;
    }

    public String getShowableStringUri(Context context) {
        if (isMemeaudio()) {
            return getMemeaudioImageUri(context).toString();
        } else {
            return getStringUri();
        }
    }

    public Uri getMemeaudioImageUri(Context context) {
        if (FileUtils.checkFileExistence(context, mName.split(ZipManager.SEPARATOR)[0])) {
            return FileUtils.getUriFromFileName(context, mName.split(ZipManager.SEPARATOR)[0]);
        } else if (ZipManager.fastUnzip(FileUtils.getMemeticameDirectory() + "/" + mName)) {
            context.sendBroadcast(new Intent(MemeaudioActivity.UNZIP_FILTER));
            return FileUtils.getUriFromFileName(context, mName.split(ZipManager.SEPARATOR)[0]);
        } else {
            return null;
        }
    }

    public Uri getMemeaudioAudioUri(Context context) {
        if (FileUtils.checkFileExistence(context, mName.split(ZipManager.SEPARATOR)[1])) {
            return FileUtils.getUriFromFileName(context, mName.split(ZipManager.SEPARATOR)[1].replace(".zip", ""));
        } else if (ZipManager.fastUnzip(FileUtils.getMemeticameDirectory() + "/" + mName)) {
            return FileUtils.getUriFromFileName(context, mName.split(ZipManager.SEPARATOR)[1].replace(".zip", ""));
        } else {
            return null;
        }
    }

    public boolean isMemeaudio() {
        return mMimeType.contains("memeaudio");
    }

    public boolean isAudio() {
        return mMimeType.contains("audio") && !mMimeType.contains("meme");
    }

    public boolean isVideo() {
        return mMimeType.contains("video");
    }

    public boolean isImage() {
        return mMimeType.contains("image");
    }

    public boolean isNotMedia() {
        return !mMimeType.contains("image") && !mMimeType.contains("video") && !mMimeType.contains("audio");
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

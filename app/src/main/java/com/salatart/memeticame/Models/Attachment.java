package com.salatart.memeticame.Models;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.salatart.memeticame.Utils.FileUtils;
import com.salatart.memeticame.Utils.Routes;
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

    public static String PARCELABLE_KEY = "com.salatart.memeticamea.Models.Attachment";
    public static String IMAGE_URI_KEY = "imageUriKey";
    public static String AUDIO_URI_KEY = "audioUriKey";

    public static int IMAGE_SIZE = 480;
    public static int IMAGE_THUMB_SIZE = 48;

    private String mName;
    private String mMimeType;
    private String mBase64Content;
    private String mUri;
    private long mSize;
    private long mDownloadId;
    private float mProgress;
    private boolean mDirty;

    public Attachment(String name, String mimeType, String base64Content, String uri, long size, float progress, boolean dirty) {
        this.mName = name;
        this.mMimeType = mimeType;
        this.mBase64Content = base64Content;
        this.mUri = uri;
        this.mSize = size;
        this.mProgress = progress;
        this.mDownloadId = 0;
        this.mDirty = dirty;
    }

    public Attachment(Parcel in) {
        this.mName = in.readString();
        this.mMimeType = in.readString();
        this.mBase64Content = in.readString();
        this.mUri = in.readString();
        this.mSize = in.readLong();
        this.mProgress = in.readFloat();
        this.mDownloadId = 0;
        this.mDirty = in.readByte() != 0;
    }

    public Attachment clone() {
        return new Attachment(mName, mMimeType, mBase64Content, mUri, mSize, mProgress, mDirty);
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

    public long getSize() {
        return mSize;
    }

    public int getProgress() {
        return (int) (mProgress * 100);
    }

    public void setProgress(float progress) {
        mDirty = true;
        mProgress = progress;
    }

    public long getDownloadId() {
        return mDownloadId;
    }

    public void setDownloadId(long downloadId) {
        mDownloadId = downloadId;
    }

    public boolean isDirty() {
        return mDirty;
    }

    // Code from: http://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
    public String getHumanReadableByteCount(boolean si) {
        int unit = si ? 1000 : 1024;
        if (mSize < unit) return mSize + " B";
        int exp = (int) (Math.log(mSize) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", mSize / Math.pow(unit, exp), pre);
    }

    public void setUri(String uri) {
        mUri = uri;
    }

    public String getShowableStringUri(Context context) {
        if (isMemeaudio()) {
            return getMemeaudioPartUri(context, true).toString();
        } else {
            return getStringUri();
        }
    }

    public Uri getMemeaudioPartUri(Context context, boolean image) {
        int index = image ? 0 : 1;

        if (!FileUtils.checkFileExistence(context, mName.split(ZipManager.SEPARATOR)[index].replace(".zip", ""))) {
            ZipManager.fastUnzip(FileUtils.getUriFromFileName(context, mName).getPath());
        }

        return FileUtils.getUriFromFileName(context, mName.split(ZipManager.SEPARATOR)[index].replace(".zip", ""));
    }

    public Uri getMemeaudioImagetUrl() {
        return Uri.parse(Routes.UNZIP_ROUTE + "/" + mName + "/" + mName.split(ZipManager.SEPARATOR)[0].replace(".zip", ""));
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

    public boolean exists(Context context) {
        return FileUtils.checkFileExistence(context, mName) && (mProgress == -1 || mProgress == 1);
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
        dest.writeLong(mSize);
        dest.writeFloat(mProgress);
        dest.writeByte((byte) (mDirty ? 1 : 0));
    }
}

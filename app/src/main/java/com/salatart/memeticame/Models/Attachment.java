package com.salatart.memeticame.Models;

/**
 * Created by sasalatart on 9/14/16.
 */
public class Attachment {
    private String mName;
    private String mMimeType;
    private String mBase64Content;
    private String mUrl;

    public Attachment(String name, String mimeType, String base64Content, String url) {
        this.mName = name;
        this.mMimeType = mimeType;
        this.mBase64Content = base64Content;
        this.mUrl = url;
    }

    public Attachment clone() {
        return new Attachment(mName, mMimeType, mBase64Content, mUrl);
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

    public String getUrl() {
        return mUrl;
    }
}

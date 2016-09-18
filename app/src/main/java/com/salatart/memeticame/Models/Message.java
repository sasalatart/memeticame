package com.salatart.memeticame.Models;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.salatart.memeticame.Utils.Routes;
import com.salatart.memeticame.Utils.SessionUtils;
import com.salatart.memeticame.Utils.Time;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by sasalatart on 9/4/16.
 */
public class Message implements Parcelable {
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        public Message[] newArray(int size) {
            return new Message[size];
        }
    };
    public static String PARCELABLE_KEY = "com.salatart.memeticame.Models.Message";
    private final int mId;
    private final String mSenderPhone;
    private final String mContent;
    private final int mChatId;
    private String mCreatedAt;
    private Attachment mAttachment;

    public Message(int mId, String senderPhone, String content, int chatId, String createdAt) {
        this.mId = mId;
        this.mSenderPhone = senderPhone;
        this.mContent = content;
        this.mChatId = chatId;
        this.mCreatedAt = Time.parseISODate(createdAt);
    }

    public Message(Parcel in) {
        this.mId = in.readInt();
        this.mSenderPhone = in.readString();
        this.mContent = in.readString();
        this.mChatId = in.readInt();
        this.mCreatedAt = in.readString();
        this.mAttachment = in.readParcelable(Attachment.class.getClassLoader());
    }

    public static Message fromJson(JSONObject jsonMessage) throws JSONException {
        Message message = new Message(jsonMessage.getInt("id"),
                jsonMessage.getString("sender_phone"),
                jsonMessage.getString("content"),
                jsonMessage.getInt("chat_id"),
                jsonMessage.getString("created_at"));

        JSONObject jsonAttachment = jsonMessage.getJSONObject("attachment_link");
        if (!jsonAttachment.getString("name").equals("null")) {
            message.setAttachment(new Attachment(jsonAttachment.getString("name"),
                    jsonAttachment.getString("mime_type"),
                    null,
                    Routes.DOMAIN + jsonAttachment.getString("url")));
        }

        return message;
    }

    public static ArrayList<Message> fromJsonArray(JSONArray jsonResponse) throws JSONException {
        ArrayList<Message> messages = new ArrayList<>();
        for (int i = 0; i < jsonResponse.length(); i++) {
            messages.add(Message.fromJson(jsonResponse.getJSONObject(i)));
        }

        return messages;
    }

    public static Message fromMap(Map mapMessage) throws JSONException {
        Message message = new Message(Integer.parseInt(mapMessage.get("id").toString()),
                mapMessage.get("sender_phone").toString(),
                mapMessage.get("content").toString(),
                Integer.parseInt(mapMessage.get("chat_id").toString()),
                mapMessage.get("created_at").toString());

        JSONObject jsonAttachment = new JSONObject(mapMessage.get("attachment_link").toString());
        if (!jsonAttachment.getString("name").equals("null")) {
            message.setAttachment(new Attachment(jsonAttachment.getString("name"),
                    jsonAttachment.getString("mime_type"),
                    null,
                    Routes.DOMAIN + jsonAttachment.getString("url")));
        }

        return message;
    }

    public static Message createFake(Context context, String content, int chatId) {
        return new Message(-1, SessionUtils.getPhoneNumber(context), content, chatId, Time.currentISODate());
    }

    public int getId() {
        return mId;
    }

    public String getSenderPhone() {
        return mSenderPhone;
    }

    public String getContent() {
        return mContent;
    }

    public int getChatId() {
        return mChatId;
    }

    public String getCreatedAt() {
        return mCreatedAt;
    }

    public Attachment getAttachment() {
        return mAttachment;
    }

    public void setAttachment(Attachment mAttachment) {
        this.mAttachment = mAttachment;
    }

    public boolean isMine(Context context) {
        return mSenderPhone.equals(SessionUtils.getPhoneNumber(context));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeString(mSenderPhone);
        dest.writeString(mContent);
        dest.writeInt(mChatId);
        dest.writeString(mCreatedAt);
        dest.writeParcelable(mAttachment, flags);
    }
}

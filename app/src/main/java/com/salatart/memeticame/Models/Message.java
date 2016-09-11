package com.salatart.memeticame.Models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by sasalatart on 9/4/16.
 */
public class Message implements Parcelable {
    public static String PARCELABLE_KEY = "com.salatart.memeticame.Models.Message";

    private final int mId;
    private final String mSenderPhone;
    private final String mContent;
    private final int mChatId;
    private String mCreatedAt;

    public Message(int mId, String senderPhone, String content, int chatId, String createdAt) {
        this.mId = mId;
        this.mSenderPhone = senderPhone;
        this.mContent = content;
        this.mChatId = chatId;
        this.mCreatedAt = parseISODate(createdAt);
    }

    public Message(Parcel in) {
        this.mId = in.readInt();
        this.mSenderPhone = in.readString();
        this.mContent = in.readString();
        this.mChatId = in.readInt();
        this.mCreatedAt = in.readString();
    }

    public static ArrayList<Message> fromJsonArray(JSONArray jsonResponse) throws JSONException {
        ArrayList<Message> messages = new ArrayList<>();

        for (int i = 0; i < jsonResponse.length(); i++) {
            JSONObject jsonMessage = jsonResponse.getJSONObject(i);
            messages.add(new Message(jsonMessage.getInt("id"),
                    jsonMessage.getString("sender_phone"),
                    jsonMessage.getString("content"),
                    jsonMessage.getInt("chat_id"),
                    jsonMessage.getString("created_at")));
        }

        return messages;
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

    private String parseISODate(String dateString) {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(dateString);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int hod = calendar.get(Calendar.HOUR_OF_DAY);
            int min = calendar.get(Calendar.MINUTE);
            int dom = calendar.get(Calendar.DAY_OF_MONTH);
            int mon = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            return String.format("%02d:%02d %02d/%02d/%d", hod, min, dom, mon, year);
        } catch (ParseException e) {
            Log.e("ERROR", e.toString());
            return null;
        }
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
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        public Message[] newArray(int size) {
            return new Message[size];
        }
    };
}

package com.salatart.memeticame.Utils;

import android.app.Activity;
import android.view.View;

import com.salatart.memeticame.Activities.ChatActivity;
import com.salatart.memeticame.Listeners.OnRequestShowListener;
import com.salatart.memeticame.Models.Chat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by sasalatart on 10/19/16.
 */

public class ChatUtils {
    public static void showRequest(final Activity activity, final Chat chat, final OnRequestShowListener listener) {
        Request request = Routes.chatShow(activity, chat);
        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                HttpClient.onUnsuccessfulRequest(activity, "Error");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        listener.OnSuccess(ParserUtils.chatFromJson(new JSONObject(response.body().string())));
                    } catch (JSONException e) {
                        HttpClient.onUnsuccessfulRequest(activity, "Error");
                    }
                } else {
                    HttpClient.onUnsuccessfulRequest(activity, "Could not retrieve chat");
                }
                response.body().close();
            }
        });
    }

    public static void createRequest(final Activity activity, Request request, final View submitButton) {
        submitButton.setEnabled(false);
        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                HttpClient.onUnsuccessfulSubmit(activity, "Error", submitButton);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonChat = new JSONObject(response.body().string());
                        activity.startActivity(ChatActivity.getIntent(activity, ParserUtils.chatFromJson(jsonChat)));
                        activity.finish();
                    } catch (JSONException e) {
                        HttpClient.onUnsuccessfulSubmit(activity, "Error", submitButton);
                    }
                } else {
                    HttpClient.onUnsuccessfulSubmit(activity, "Invalid credentials", submitButton);
                }
                response.body().close();
            }
        });
    }
}

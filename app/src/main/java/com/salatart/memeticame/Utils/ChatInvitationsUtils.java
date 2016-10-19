package com.salatart.memeticame.Utils;

import android.app.Activity;
import android.view.View;

import com.salatart.memeticame.Activities.ChatActivity;
import com.salatart.memeticame.Listeners.OnRequestIndexListener;
import com.salatart.memeticame.Models.Chat;
import com.salatart.memeticame.Models.ChatInvitation;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by sasalatart on 10/19/16.
 */

public class ChatInvitationsUtils {
    public static void indexRequest(final Activity activity, Request request, final OnRequestIndexListener<ChatInvitation> listener) {
        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                HttpClient.onUnsuccessfulRequest(activity, "Error");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (activity == null) {
                    response.body().close();
                    return;
                }

                if (response.isSuccessful()) {
                    try {
                        listener.OnSuccess(ParserUtils.chatInvitationsFromJsonArray(new JSONArray(response.body().string())));
                    } catch (JSONException e) {
                        HttpClient.onUnsuccessfulRequest(activity, "Error");
                    }
                } else {
                    HttpClient.onUnsuccessfulRequest(activity, HttpClient.parseErrorMessage(response));
                }

                response.body().close();
            }
        });
    }

    public static void addParticipantsRequest(final Activity activity, Request request, final Chat chat, final View submitButton) {
        submitButton.setEnabled(false);
        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                HttpClient.onUnsuccessfulSubmit(activity, "Error", submitButton);
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (activity == null) {
                    response.body().close();
                    return;
                }

                if (response.isSuccessful()) {
                    activity.startActivity(ChatActivity.getIntent(activity, chat));
                    activity.finish();
                } else {
                    HttpClient.onUnsuccessfulSubmit(activity, HttpClient.parseErrorMessage(response), submitButton);
                }

                response.body().close();
            }
        });
    }
}

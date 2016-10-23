package com.salatart.memeticame.Utils;

import android.app.Activity;
import android.view.View;

import com.salatart.memeticame.Activities.ChatActivity;
import com.salatart.memeticame.Listeners.OnRequestIndexListener;
import com.salatart.memeticame.Listeners.OnRequestListener;
import com.salatart.memeticame.Listeners.OnRequestShowListener;
import com.salatart.memeticame.Models.Chat;

import org.json.JSONArray;
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
    public static void indexRequest(final Activity activity, Request request, final com.wang.avi.AVLoadingIndicatorView loadingIndex, final OnRequestIndexListener<Chat> listener) {
        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                CallbackUtils.onUnsuccessfulRequestWithSpinner(activity, "Error", loadingIndex);
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        listener.OnSuccess(ParserUtils.chatsFromJsonArray(new JSONArray(response.body().string())));

                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadingIndex.hide();
                            }
                        });
                    } catch (JSONException e) {
                        CallbackUtils.onUnsuccessfulRequestWithSpinner(activity, "Error", loadingIndex);
                    }
                } else {
                    CallbackUtils.onUnsuccessfulRequestWithSpinner(activity, HttpClient.parseErrorMessage(response), loadingIndex);
                }
            }
        });
    }

    public static void showRequest(final Activity activity, final Chat chat, final OnRequestShowListener listener) {
        Request request = Routes.chatShow(activity, chat);
        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                CallbackUtils.onUnsuccessfulRequest(activity, "Error");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        listener.OnSuccess(ParserUtils.chatFromJson(new JSONObject(response.body().string())));
                    } catch (JSONException e) {
                        CallbackUtils.onUnsuccessfulRequest(activity, "Error");
                    }
                } else {
                    CallbackUtils.onUnsuccessfulRequest(activity, "Could not retrieve chat");
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
                CallbackUtils.onUnsuccessfulSubmit(activity, "Error", submitButton);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonChat = new JSONObject(response.body().string());
                        activity.startActivity(ChatActivity.getIntent(activity, ParserUtils.chatFromJson(jsonChat)));
                        activity.finish();
                    } catch (JSONException e) {
                        CallbackUtils.onUnsuccessfulSubmit(activity, "Error", submitButton);
                    }
                } else {
                    CallbackUtils.onUnsuccessfulSubmit(activity, HttpClient.parseErrorMessage(response), submitButton);
                }
                response.body().close();
            }
        });
    }

    public static void leaveRequest(final Activity activity, Request request, final OnRequestListener listener) {
        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                CallbackUtils.onUnsuccessfulRequest(activity, "Error");
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (response.isSuccessful()) {
                    listener.OnSuccess();
                } else {
                    CallbackUtils.onUnsuccessfulRequest(activity, HttpClient.parseErrorMessage(response));
                }
                response.body().close();
            }
        });
    }
}

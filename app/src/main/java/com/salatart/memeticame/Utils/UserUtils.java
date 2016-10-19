package com.salatart.memeticame.Utils;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.salatart.memeticame.Activities.MainActivity;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by sasalatart on 10/19/16.
 */

public class UserUtils {
    public static void signup(final Activity activity, Request request, final String phoneNumber, String password, final View submitButton) {
        submitButton.setEnabled(false);
        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                HttpClient.onUnsuccessfulRequest(activity, "Failed to signup");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (activity == null) {
                    response.body().close();
                    return;
                }

                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        SessionUtils.saveToken(jsonResponse.getString("api_key"), activity);
                        SessionUtils.savePhoneNumber(phoneNumber, activity);
                        SessionUtils.registerFCMToken(activity);
                        activity.startActivity(new Intent(activity, MainActivity.class));
                        activity.finish();
                    } catch (Exception e) {
                        HttpClient.onUnsuccessfulSubmit(activity, "Error", submitButton);
                    }
                } else {
                    HttpClient.onUnsuccessfulSubmit(activity, "Error", submitButton);
                }

                response.body().close();
            }
        });
    }
}

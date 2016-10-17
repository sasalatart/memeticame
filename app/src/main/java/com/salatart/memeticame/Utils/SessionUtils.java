package com.salatart.memeticame.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.salatart.memeticame.Activities.LoginActivity;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by sasalatart on 8/27/16.
 */
public class SessionUtils {

    public static String PREFERENCES = "SESSION";

    public static String getToken(Context context) {
        if (context == null) {
            return "";
        }

        return context.getSharedPreferences(PREFERENCES, 0).getString("Token", "");
    }

    public static String getPhoneNumber(Context context) {
        return context.getSharedPreferences(PREFERENCES, 0).getString("PhoneNumber", "");
    }

    public static String getFCMToken(Context context) {
        return context.getSharedPreferences(PREFERENCES, 0).getString("FCMToken", "");
    }

    public static void saveToken(String token, Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCES, 0).edit();
        editor.putString("Token", token);
        editor.commit();
    }

    public static void savePhoneNumber(String phoneNumber, Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCES, 0).edit();
        editor.putString("PhoneNumber", phoneNumber);
        editor.commit();
    }

    public static void saveFCMToken(String fcmToken, Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCES, 0).edit();
        editor.putString("FCMToken", fcmToken);
        editor.commit();
    }

    public static void registerFCMToken(Context context) {
        Request request = Routes.fcmRegisterRequest(context, getFCMToken(context));
        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ERROR", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                response.body().close();
            }
        });
    }

    public static void logout(final Activity activity) {
        Request request = Routes.logoutRequest(activity);
        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ERROR", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (activity == null) {
                    return;
                }

                SharedPreferences.Editor editor = activity.getSharedPreferences(PREFERENCES, 0).edit();
                editor.remove("Token");
                editor.remove("PhoneNumber");
                editor.commit();

                activity.startActivity(new Intent(activity, LoginActivity.class));
                activity.finish();
                response.body().close();
            }
        });
    }
}

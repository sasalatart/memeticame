package com.mecolab.memeticameandroid.Utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by sasalatart on 8/27/16.
 */
public class SessionUtils {

    public static String PREFERENCES = "SESSION";

    public static String getToken(Context context) {
        return context.getSharedPreferences(PREFERENCES, 0).getString("Token", "");
    }

    public static String getPhoneNumber(Context context ) {
        return context.getSharedPreferences(PREFERENCES, 0).getString("PhoneNumber", "");
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
}

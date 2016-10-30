package com.salatart.memeticame.Utils;

import android.app.Activity;
import android.util.Log;

import com.salatart.memeticame.Listeners.OnRequestIndexListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by sasalatart on 10/26/16.
 */

public class PlainMemeUtils {
    public static void indexRequest(final Activity activity, Request request, final com.wang.avi.AVLoadingIndicatorView loadingSpinner, final OnRequestIndexListener<String[]> listener) {
        loadingSpinner.show();
        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                CallbackUtils.onUnsuccessfulRequestWithSpinner(activity, "Error", loadingSpinner);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        listener.OnSuccess(ParserUtils.memesFromJsonArray(new JSONArray(jsonResponse.getString("plain_memes"))));
                    } catch (JSONException e) {
                        Log.e("ERROR", e.toString());
                        CallbackUtils.onUnsuccessfulRequestWithSpinner(activity, "Error", loadingSpinner);
                    }
                } else {
                    CallbackUtils.onUnsuccessfulRequestWithSpinner(activity, HttpClient.parseErrorMessage(response), loadingSpinner);
                }
                response.body().close();
            }
        });
    }
}

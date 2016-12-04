package com.salatart.memeticame.Utils;

import com.salatart.memeticame.Listeners.OnRequestIndexListener;
import com.salatart.memeticame.Models.FaceEmotion;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by sasalatart on 12/3/16.
 */

public class FaceEmotionsUtils {
    public static void recognizeRequest(Request request, final OnRequestIndexListener<FaceEmotion> listener) {
        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.OnFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        listener.OnSuccess(ParserUtils.faceEmotionsFromJsonArray(new JSONArray(response.body().string())));
                    } catch (JSONException e) {
                        listener.OnFailure(e.toString());
                    }
                } else {
                    listener.OnFailure(HttpClient.parseErrorMessage(response));
                }
            }
        });
    }
}

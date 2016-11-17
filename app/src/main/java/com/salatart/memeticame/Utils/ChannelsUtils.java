package com.salatart.memeticame.Utils;

import com.salatart.memeticame.Listeners.OnRequestIndexListener;
import com.salatart.memeticame.Models.Channel;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by sasalatart on 11/13/16.
 */

public class ChannelsUtils {
    public static void indexRequest(Request request, final OnRequestIndexListener<Channel> listener) {
        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.OnFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        listener.OnSuccess(ParserUtils.channelsFromJsonArray(new JSONArray(response.body().string())));
                    } catch (JSONException e) {
                        listener.OnFailure(e.toString());
                    }
                } else {
                    listener.OnFailure(HttpClient.parseErrorMessage(response));
                }

                response.body().close();
            }
        });
    }
}

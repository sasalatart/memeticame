package com.salatart.memeticame.Utils;

import com.salatart.memeticame.Listeners.OnRequestIndexListener;
import com.salatart.memeticame.Listeners.OnRequestShowListener;
import com.salatart.memeticame.Models.Category;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by sasalatart on 11/16/16.
 */

public class CategoriesUtils {
    public static void indexRequest(Request request, final OnRequestIndexListener<Category> listener) {
        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.OnFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        listener.OnSuccess(ParserUtils.categoriesFromJsonArray(new JSONArray(response.body().string())));
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

    public static void showRequest(Request request, final OnRequestShowListener<Category> listener) {
        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.OnFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        listener.OnSuccess(ParserUtils.categoryFromJson(new JSONObject(response.body().string())));
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

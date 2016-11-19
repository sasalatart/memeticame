package com.salatart.memeticame.Utils;

import com.salatart.memeticame.Listeners.OnRequestShowListener;
import com.salatart.memeticame.Models.Meme;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by sasalatart on 10/31/16.
 */

public class MemeUtils {
    public static String getNameFromUrl(String url) {
        int start = url.lastIndexOf("/") + 1;
        int end = url.lastIndexOf("?");
        return start < end ? url.substring(start, end) : url.substring(start);
    }

    public static String createName(String baseName) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return baseName.replace(' ', '_') + Meme.SEPARATOR + timestamp + ".jpg";
    }

    public static String cleanName(String fullName) {
        int indexOfSeparator = fullName.indexOf(Meme.SEPARATOR);
        return fullName.substring(0, indexOfSeparator);
    }

    public static void createRequest(Request request, final OnRequestShowListener<Meme> listener) {
        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.OnFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        listener.OnSuccess(ParserUtils.memeFromJson(new JSONObject(response.body().string())));
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

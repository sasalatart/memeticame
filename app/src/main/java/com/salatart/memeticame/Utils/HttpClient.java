package com.salatart.memeticame.Utils;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * Created by sasalatart on 8/27/16.
 */
public class HttpClient {
    private static OkHttpClient instance = null;

    protected HttpClient() {
        // Exists only to defeat instantiation.
    }

    public static OkHttpClient getInstance() {
        if (instance == null) {
            instance = new OkHttpClient.Builder()
                    .connectTimeout(600, TimeUnit.SECONDS)
                    .writeTimeout(600, TimeUnit.SECONDS)
                    .readTimeout(600, TimeUnit.SECONDS)
                    .build();
        }

        return instance;
    }
}

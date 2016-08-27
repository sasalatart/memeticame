package cl.mecolab.memeticame.Utils;

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
            instance = new OkHttpClient();
        }

        return instance;
    }
}


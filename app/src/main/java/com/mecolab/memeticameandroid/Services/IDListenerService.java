package com.mecolab.memeticameandroid.Services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.mecolab.memeticameandroid.R;
import com.mecolab.memeticameandroid.Utils.HttpClient;
import com.mecolab.memeticameandroid.Utils.Routes;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by sasalatart on 9/5/16.
 */
public class IDListenerService extends IntentService {

    public IDListenerService() {
        super("IDListenerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        InstanceID instanceID = InstanceID.getInstance(getApplicationContext());
        try {
            String token = instanceID.getToken(getApplicationContext().getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            Request request = Routes.buildPushNotificationRegisterRequest(getApplicationContext(), token);
            HttpClient.getInstance().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("ERROR", e.toString());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.i("INFO", response.body().string());
                    response.body().close();
                }
            });
        } catch (IOException e) {
            Log.e("ERROR", e.toString());
        }
    }
}

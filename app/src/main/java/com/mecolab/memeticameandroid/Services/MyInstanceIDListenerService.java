package com.mecolab.memeticameandroid.Services;

import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.android.gms.iid.InstanceIDListenerService;
import com.mecolab.memeticameandroid.R;
import com.mecolab.memeticameandroid.Utils.HttpClient;
import com.mecolab.memeticameandroid.Utils.Routes;

import java.io.IOException;

import okhttp3.Request;

/**
 * Created by sasalatart on 9/5/16.
 */
public class MyInstanceIDListenerService extends InstanceIDListenerService {
    @Override
    public void onTokenRefresh() {
        InstanceID instanceID = InstanceID.getInstance(getApplicationContext());
        try {
            String token = instanceID.getToken(getApplicationContext().getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            Request request = Routes.buildPushNotificationRegisterRequest(getApplicationContext(), token);
            HttpClient.getInstance().newCall(request).execute();
        } catch (IOException e) {
            Log.e("ERROR", e.toString());
        }
    }
}

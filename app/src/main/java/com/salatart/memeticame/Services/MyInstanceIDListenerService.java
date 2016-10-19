package com.salatart.memeticame.Services;

import android.content.pm.PackageInstaller;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.salatart.memeticame.Utils.HttpClient;
import com.salatart.memeticame.Utils.Routes;
import com.salatart.memeticame.Utils.SessionUtils;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by sasalatart on 9/5/16.
 */
public class MyInstanceIDListenerService extends FirebaseInstanceIdService {

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is also called
     * when the InstanceID token is initially generated, so this is where
     * you retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.i("INFO", "Refreshed token: " + refreshedToken);
        SessionUtils.saveFCMToken(refreshedToken, getApplicationContext());

        if (SessionUtils.loggedIn(getApplicationContext())) {
            SessionUtils.registerFCMToken(getApplicationContext());
        }
    }
}

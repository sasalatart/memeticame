package com.salatart.memeticame.Activities;

import android.app.Application;

import io.realm.Realm;

/**
 * Created by sasalatart on 11/2/16.
 */

public class MemeticameApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Realm.init(this);
    }
}

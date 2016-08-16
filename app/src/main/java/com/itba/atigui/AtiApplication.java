package com.itba.atigui;

import android.app.Application;

public class AtiApplication extends Application {

    private static AtiApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static AtiApplication getInstance() {
        return instance;
    }
}

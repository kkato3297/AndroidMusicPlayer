package com.kksystems.musicplayer;

import android.app.Application;
import android.util.Log;

public class MusicPlayerApplication extends Application {
    private static final String TAG = MusicPlayerApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");

        super.onCreate();
    }

    @Override
    public void onTerminate() {
        Log.d(TAG, "onTerminate");

        super.onTerminate();
    }
}

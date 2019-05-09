package com.aerospace.sabena.tc20.loadingpoint.models;

import android.util.Log;

import com.aerospace.sabena.tc20.loadingpoint.Startup;

import java.util.TimerTask;

public class TaskTest extends TimerTask {
    @Override
    public void run() {
        Log.d(Startup.LOG_TAG,"I'm present");
    }
}

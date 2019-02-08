package com.aerospace.sabena.tc20.loadingpoint.datawedge;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SendDataWedge {

    private AppCompatActivity app;

    public SendDataWedge(AppCompatActivity app) {
        this.app = app;
    }

    public void send(String action, String extraKey, Bundle extras){
        Intent dwIntent = new Intent();
        dwIntent.setAction(action);
        dwIntent.putExtra(extraKey, extras);
        app.sendBroadcast(dwIntent);
    }

    public void send(String action, String extraKey, String extraValue){
        Intent dwIntent = new Intent();
        dwIntent.setAction(action);
        dwIntent.putExtra(extraKey, extraValue);
        app.sendBroadcast(dwIntent);
    }
}

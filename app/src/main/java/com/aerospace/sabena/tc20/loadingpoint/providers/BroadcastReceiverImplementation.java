package com.aerospace.sabena.tc20.loadingpoint.providers;

import android.app.AppComponentFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.aerospace.sabena.tc20.loadingpoint.BroadcastReceiverEvent;
import com.aerospace.sabena.tc20.loadingpoint.R;
import com.aerospace.sabena.tc20.loadingpoint.Startup;
import com.aerospace.sabena.tc20.loadingpoint.datawedge.DataWedgeAction;
import com.aerospace.sabena.tc20.loadingpoint.datawedge.DataWedgeExtraData;
import com.aerospace.sabena.tc20.loadingpoint.listeners.BroadcastReceiverListener;

import java.util.ArrayList;
import java.util.List;

public class BroadcastReceiverImplementation extends BroadcastReceiver {

    private AppCompatActivity app;
    private List<BroadcastReceiverListener> listeners = new ArrayList<BroadcastReceiverListener>();

    public BroadcastReceiverImplementation(AppCompatActivity app) {
        this.app = app;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        notifyListeners(context, intent);
    }

    public void register(){
        Log.d(Startup.LOG_TAG, "Register Receiver");
        IntentFilter filter = new IntentFilter();
        filter.addAction(DataWedgeAction.ACTION_RESULT_NOTIFICATION);
        filter.addAction(DataWedgeAction.ACTION_RESULT);
        filter.addCategory(Intent.CATEGORY_DEFAULT);

        filter.addAction(app.getResources().getString(R.string.activity_intent_filter_action));
        filter.addAction(app.getResources().getString(R.string.activity_action_from_service));
        app.registerReceiver(this,filter);

    }

    public void unRegister(){
        app.unregisterReceiver(this);
    }

    private void notifyListeners(Context context, Intent intent){
        for(BroadcastReceiverListener listener : listeners){
            listener.broadcastReceiver(new BroadcastReceiverEvent(this, context, intent));
        }
    }

    public void addBroadcastReceiverListener(BroadcastReceiverListener listener){
        listeners.add(listener);
    }



}

package com.aerospace.sabena.tc20.loadingpoint.listeners;

import com.aerospace.sabena.tc20.loadingpoint.BroadcastReceiverEvent;

import java.util.EventListener;

public interface BroadcastReceiverListener extends EventListener {

    public abstract void broadcastReceiver(BroadcastReceiverEvent evt);

}

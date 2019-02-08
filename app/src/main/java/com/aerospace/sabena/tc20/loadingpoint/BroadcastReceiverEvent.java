package com.aerospace.sabena.tc20.loadingpoint;

import android.content.Context;
import android.content.Intent;

import java.util.EventObject;

public class BroadcastReceiverEvent extends EventObject {

    private Context context;
    private Intent intent;
    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public BroadcastReceiverEvent(Object source, Context context, Intent intent) {
        super(source);
        this.context = context;
        this.intent = intent;
    }

    public Context getContext() {
        return context;
    }

    public Intent getIntent() {
        return intent;
    }
}

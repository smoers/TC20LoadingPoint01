package com.aerospace.sabena.tc20.loadingpoint.listeners;

import android.view.View;

public abstract class OnClickListenerAbstract<T> implements View.OnClickListener {

    protected T controller;

    public OnClickListenerAbstract(T controller) {
        this.controller = controller;
    }

    @Override
    public abstract void onClick(View v);
}

package com.aerospace.sabena.tc20.loadingpoint.models;

import android.util.Log;

import com.aerospace.sabena.tc20.loadingpoint.Startup;
import com.aerospace.sabena.tc20.loadingpoint.controllers.BarcodeScannerController;

import java.util.TimerTask;

public class TaskCounterReset extends TimerTask {

    private BarcodeScannerController barcodeScannerController;

    public TaskCounterReset(BarcodeScannerController barcodeScannerController) {
        this.barcodeScannerController = barcodeScannerController;
    }

    @Override
    public void run() {
        Log.d(Startup.LOG_TAG, "Reset Counter");
        barcodeScannerController.resetCounter();
    }
}

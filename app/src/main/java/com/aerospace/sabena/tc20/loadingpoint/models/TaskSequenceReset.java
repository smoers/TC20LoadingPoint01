package com.aerospace.sabena.tc20.loadingpoint.models;

import com.aerospace.sabena.tc20.loadingpoint.controllers.BarcodeScannerController;

import java.util.TimerTask;

public class TaskSequenceReset extends TimerTask {

    BarcodeScannerController controller;

    public TaskSequenceReset(BarcodeScannerController controller) {
        this.controller = controller;
    }

    @Override
    public void run() {
        controller.resetSequence();
    }
}

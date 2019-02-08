package com.aerospace.sabena.tc20.loadingpoint.models;

import com.aerospace.sabena.tc20.loadingpoint.controllers.BarcodeScannerController;

import java.util.TimerTask;

public class TaskRoleReset extends TimerTask {

    BarcodeScannerController controller;

    public TaskRoleReset(BarcodeScannerController controller) {
        this.controller = controller;
    }

    @Override
    public void run() {
        controller.finish();
    }
}

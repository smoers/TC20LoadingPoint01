package com.aerospace.sabena.tc20.loadingpoint.models;

import android.content.Intent;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.aerospace.sabena.tc20.loadingpoint.Startup;
import com.aerospace.sabena.tc20.loadingpoint.providers.ExternalStore;
import com.aerospace.sabena.tc20.loadingpoint.providers.InternalStore;
import com.aerospace.sabena.tc20.loadingpoint.providers.InternetStore;
import com.aerospace.sabena.tc20.loadingpoint.system.Device;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.util.TimerTask;

public class TaskAutomaticExportData extends TimerTask{

    private AppCompatActivity app;

    public TaskAutomaticExportData(AppCompatActivity app) {
        this.app = app;
    }

    @Override
    public void run() {
        Log.d(Startup.LOG_TAG, "Automatic Export Task invocated");
        InternalStore internalStore = new InternalStore(app);
        Sequences sequences = internalStore.loadSequences();
        //Instance du magasin internet
        InternetStore internetStore = new InternetStore(app);
        //Instance du magasin externe
        ExternalStore externalStore = new ExternalStore(app);
        if (internetStore.isAvailable()){
            //Y-a-t-il des sequences dans le magasin interne
            if (sequences.size() > 0) {
                if (upload(internetStore,sequences)){
                    internalStore.removeSequences();
                }
            }
            externalStore.open();
            if(externalStore.countFiles(Environment.DIRECTORY_DOCUMENTS) > 0){
                Sequences externalSequences = null;
                try {
                    externalSequences = externalStore.read(Environment.DIRECTORY_DOCUMENTS);
                } catch (IOException | ParseException e) {
                    e.printStackTrace();
                }
                if(upload(internetStore, externalSequences)){
                    externalStore.remove(Environment.DIRECTORY_DOCUMENTS);
                }
            }
        } else if (Device.isPlugged(app) && sequences.size() > 0){
            //Le magasin internet n'est pas disponible mais le device est plugged
            //Et il y a des sequences
            if (externalStore.open()){
                //le magasin externe est disponible on effectue l'export
                if (externalStore.write(Environment.DIRECTORY_DOCUMENTS,sequences)){
                    internalStore.removeSequences();
                    app.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(app, "Automatic export file is finished with success", Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    app.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(app, "Automatic export file is in ERROR", Toast.LENGTH_LONG).show();
                        }
                    });

                }
            }
        }
        Intent intent = new Intent();
        intent.setAction("com.aerospace.sabena.tc20.loadingpoint.TASK_AUTOMATIC_INVOCATED");
        intent.putExtra("TASK_AUTOMATIC_INVOCATED", true);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(app);
        localBroadcastManager.sendBroadcast(intent);
    }

    private boolean upload(InternetStore store, Sequences sequences){
        //le magasin internet est disponible on effectue l'upload
        boolean result = store.upload(sequences);
        if (result) {
            app.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(app, "Automatic internet upload is finished with success", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            app.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(app, "Automatic internet upload is in ERROR", Toast.LENGTH_LONG).show();
                }
            });
        }
        return result;
    }
}

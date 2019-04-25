package com.aerospace.sabena.tc20.loadingpoint.providers;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.aerospace.sabena.tc20.loadingpoint.Startup;
import com.aerospace.sabena.tc20.loadingpoint.models.Sequence;
import com.aerospace.sabena.tc20.loadingpoint.models.Sequences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class InternalStore {

    private AppCompatActivity app;

    public InternalStore(AppCompatActivity app) {
        this.app = app;
    }

    /**
     * Permet de sauvegarder la table Sequences
     */
    public void saveSequences(Sequences sequences) {

        Gson gson = new Gson();
        SharedPreferences sharedPreferences = app.getSharedPreferences("LoadingPoint", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = gson.toJson(sequences);
        editor.putString("Sequences", json);
        editor.commit();
        Log.d(Startup.LOG_TAG,"Sequences saved " + String.valueOf(sequences.size()));
    }

    /**
     * Permet de charger la table Sequences
     */
    public Sequences loadSequences() {

        Gson gson = new Gson();
        Sequences sequences = new Sequences();
        SharedPreferences sharedPreferences = app.getSharedPreferences("LoadingPoint", Context.MODE_PRIVATE);;
        if (sharedPreferences.contains("Sequences")) {
            String json = sharedPreferences.getString("Sequences", "");
            sequences = gson.fromJson(json, new TypeToken<Sequences>() {
            }.getType());
            Log.d(Startup.LOG_TAG,"Sequences loaded " + String.valueOf(sequences.size()));
            Log.d(Startup.LOG_TAG,json);
        }
        return sequences;
    }

    /**
     * Supprime la table avec les sequences
     */
    public boolean removeSequences(){
        boolean result = false;
        SharedPreferences sharedPreferences = app.getSharedPreferences("LoadingPoint", Context.MODE_PRIVATE);
        if (sharedPreferences.contains("Sequences")){
            sharedPreferences.edit().remove("Sequences").commit();
            Log.d(Startup.LOG_TAG,"Sequences removed");
            result = true;
        }
        return result;
    }

}

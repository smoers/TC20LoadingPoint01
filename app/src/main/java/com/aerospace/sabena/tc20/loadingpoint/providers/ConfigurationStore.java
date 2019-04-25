package com.aerospace.sabena.tc20.loadingpoint.providers;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.aerospace.sabena.tc20.loadingpoint.R;
import com.aerospace.sabena.tc20.loadingpoint.Startup;
import com.aerospace.sabena.tc20.loadingpoint.models.ConfigurationList;
import com.aerospace.sabena.tc20.loadingpoint.system.network.DownloadCallback;
import com.aerospace.sabena.tc20.loadingpoint.system.network.DownloadTask;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

public class ConfigurationStore implements DownloadCallback<String> {

    private AppCompatActivity app;
    //ID du serveur web
    private final String SERVER_ID;
    //URL du serveur web
    private final String SERVER_URL;
    //URL du serveur pour obtenir la configuration
    private final String SERVER_URL_SETUP;

    public ConfigurationStore(AppCompatActivity app) {
        this.app = app;
        //ID du serveur web
        SERVER_ID = this.app.getResources().getString(R.string.server_id);
        SERVER_URL = this.app.getResources().getString(R.string.server_url);
        SERVER_URL_SETUP = SERVER_URL + "/" + this.app.getResources().getString(R.string.server_url_setup);
    }

    /**
     * Permet de sauvegarder la table ConfigurationList
     */
    public void save(ConfigurationList configurationList) {

        SharedPreferences sharedPreferences = app.getSharedPreferences("LoadingPoint", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("ConfigurationList", configurationList.getJsonString());
        editor.commit();
        Log.d(Startup.LOG_TAG,"ConfigurationList saved ");
    }

    /**
     * Permet de charger la table ConfigurationList
     */
    public ConfigurationList load(){

        ConfigurationList configurationList = null;
        SharedPreferences sharedPreferences = app.getSharedPreferences("LoadingPoint", Context.MODE_PRIVATE);;
        if (sharedPreferences.contains("ConfigurationList")) {
            String json = sharedPreferences.getString("ConfigurationList", "");
            configurationList = ConfigurationList.setJsonString(json);
            Log.d(Startup.LOG_TAG,"ConfigurationList loaded ");
        }
        return configurationList;
    }

    /**
     * récupère la configuration sur le site
     * @return
     */
    public boolean webLoad(){
        boolean loaded = false;
        if (isAvailable()){
            DownloadTask.Result result = null;
            Log.d(Startup.LOG_TAG, "Start connection to download configuration");
            DownloadTask task = new DownloadTask(ConfigurationStore.this);
            try {
                result = task.execute(new String[] {SERVER_URL_SETUP}).get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (CancellationException e){

            }
            if (result != null && result.resultValue != null){
                if (result.resultValue.contains("{") && result.resultValue.contains("}")){
                    ConfigurationList configurationList = ConfigurationList.setJsonString(result.resultValue);
                    remove();
                    save(configurationList);
                    loaded = true;
                }
                Log.d(Startup.LOG_TAG, result.resultValue);
            }
        }
        return loaded;
    }

    /**
     * Supprime la table avec les ConfigurationList
     */
    public boolean remove(){
        boolean result = false;
        SharedPreferences sharedPreferences = app.getSharedPreferences("LoadingPoint", Context.MODE_PRIVATE);
        if (sharedPreferences.contains("ConfigurationList")){
            sharedPreferences.edit().remove("ConfigurationList").commit();
            Log.d(Startup.LOG_TAG,"ConfigurationList removed");
            result = true;
        }
        return result;
    }

    /**
     * La configuration existe-t-elle ?
     * @return
     */
    public boolean isExist(){
        SharedPreferences sharedPreferences = app.getSharedPreferences("LoadingPoint", Context.MODE_PRIVATE);
        return sharedPreferences.contains("ConfigurationList");
    }


    @Override
    public void updateFromDownload(String result) {

    }

    @Override
    public NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager connectivityManager = (ConnectivityManager) app.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo;
    }

    @Override
    public void onProgressUpdate(int progressCode, int percentComplete) {

    }

    @Override
    public void finishDownloading() {

    }

    /**
     * Permet de savoir si le site est accessible
     * @return
     */
    private boolean isAvailable(){
        DownloadTask.Result result = null;
        boolean available = false;
        Log.d(Startup.LOG_TAG, "Start connection to witness site");
        DownloadTask task = new DownloadTask(ConfigurationStore.this);
        try {
            result = task.execute(new String[] {SERVER_URL}).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (CancellationException e){

        }
        if (result != null && result.resultValue != null){
            if (result.resultValue.matches(SERVER_ID))
                available = true;
            Log.d(Startup.LOG_TAG, result.resultValue);
        }
        return available;
    }

}

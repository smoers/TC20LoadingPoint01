package com.aerospace.sabena.tc20.loadingpoint.providers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.aerospace.sabena.tc20.loadingpoint.R;
import com.aerospace.sabena.tc20.loadingpoint.Startup;
import com.aerospace.sabena.tc20.loadingpoint.system.network.DownloadCallback;
import com.aerospace.sabena.tc20.loadingpoint.system.network.DownloadTask;
import com.aerospace.sabena.tc20.loadingpoint.system.network.UploadTask;

import java.io.File;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

public class InternetStore implements DownloadCallback<String> {
    //ID du serveur web
    private final String SERVER_ID;
    //URL du serveur web
    private final String SERVER_URL;
    //page du serveur web utilisée pour l'upload
    private final String SERVER_URL_UPLOAD;
    //Resultat retouner par la tâche
    private String result;

    private AppCompatActivity app;

    public InternetStore(AppCompatActivity app) {
        this.app = app;
        //ID du serveur web
        SERVER_ID = this.app.getResources().getString(R.string.server_id);
        SERVER_URL = this.app.getResources().getString(R.string.server_url);
        SERVER_URL_UPLOAD = SERVER_URL + "/" + this.app.getResources().getString(R.string.server_url_upload);

    }

    public boolean isAvailable(){
        DownloadTask.Result result = null;
        boolean available = false;
        Log.d(Startup.LOG_TAG, "Start connection to witness site");
        DownloadTask task = new DownloadTask(InternetStore.this);
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

    public boolean upload(File file, String fileName){
        boolean isUpload = false;
        UploadTask.Result result = null;
        UploadTask task = new UploadTask(InternetStore.this, file, fileName);
        try {
            result = task.execute(new String[]{SERVER_URL_UPLOAD}).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (result != null && result.resultValue != null) {
            Log.d(Startup.LOG_TAG, result.resultValue);
            if (result.resultValue.matches("200.+OK"))
                isUpload = true;
        } else if (result != null && result.exception != null) {
            Log.d(Startup.LOG_TAG, result.exception.getMessage());
        }
        return isUpload;
    }


    @Override
    public void updateFromDownload(String result) {
        this.result = result;
    }

    @Override
    public NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) app.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo;
    }

    @Override
    public void onProgressUpdate(int progressCode, int percentComplete) {
        Log.d(Startup.LOG_TAG,"Download task on progress ...");
        switch (progressCode){
            case Progress.ERROR:
                break;
            case Progress.CONNECT_SUCCESS:
                Log.d(Startup.LOG_TAG,"Download task CONNECT_SUCCESS");
                break;
            case Progress.GET_INPUT_STREAM_SUCCESS:
                Log.d(Startup.LOG_TAG,"Download task GET_INPUT_STREAM_SUCCESS");
                break;
            case Progress.PROCESS_INPUT_STREAM_IN_PROGRESS:
                Log.d(Startup.LOG_TAG,"Download task PROCESS_INPUT_STREAM_IN_PROGRESS");
                break;
            case Progress.PROCESS_INPUT_STREAM_SUCCESS:
                Log.d(Startup.LOG_TAG,"Download task PROCESS_INPUT_STREAM_SUCCESS");
                break;
        }
    }

    @Override
    public void finishDownloading() {
    }

    public String getResult() {
        return result;
    }
}

package com.aerospace.sabena.tc20.loadingpoint.providers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.aerospace.sabena.tc20.loadingpoint.Startup;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ExternalStore {
    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION = 1;

    private boolean isOpen = false;
    private AppCompatActivity app;

    public ExternalStore(AppCompatActivity app) {
        this.app = app;
    }

    /**
     * S'assure que le stockage externe est mounté et que l'accès est outorisé.
     * @return
     */
    public boolean open(){
        String state = Environment.getExternalStorageState();
        Log.d(Startup.LOG_TAG, "External storage status : " + state);
        if (Environment.MEDIA_MOUNTED.equals(state)){
            int writeExternalStoragePermission = ContextCompat.checkSelfPermission(app.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (writeExternalStoragePermission == PackageManager.PERMISSION_DENIED){
                Log.d(Startup.LOG_TAG, "External storage requested permission");
                ActivityCompat.requestPermissions(app, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION);
            } else {
                isOpen = true;
            }

        }
        return isOpen;
    }

    public boolean isOpen() { return isOpen; }

    /**
     *
     * @param dirType
     * @param fileName
     * @param data
     * @return
     */
    public boolean write(String dirType, String fileName, String data){
        boolean result = false;
        if (isOpen){
            File path = Environment.getExternalStoragePublicDirectory(dirType);
            Log.d(Startup.LOG_TAG,"External storage write path : " + path.getAbsolutePath());
            try{
                File file = new File(path, fileName);
                path.mkdirs();
                file.createNewFile();
                if (file.exists()) {
                    FileWriter fileWriter = new FileWriter(file);
                    fileWriter.write(data);
                    fileWriter.flush();
                    fileWriter.close();
                    result = true;
                }
            } catch (IOException e){}
        }

        return result;
    }

}

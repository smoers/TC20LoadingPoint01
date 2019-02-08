package com.aerospace.sabena.tc20.loadingpoint.system.network;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.aerospace.sabena.tc20.loadingpoint.Startup;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class UploadTask extends AsyncTask<String, Integer, UploadTask.Result> {

    private File file;
    private String fileName;
    private DownloadCallback<String> callback;

    public UploadTask(DownloadCallback<String> callback, File file, String fileName) {
        setCallback(callback);
        setFile(file);
        setFileName(fileName);
    }

    public void setCallback(DownloadCallback<String> callback){
        this.callback = callback;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Wrapper class that serves as a union of a result value and an exception. When the download
     * task has completed, either the result value or exception can be a non-null value.
     * This allows you to pass exceptions to the UI thread that were thrown during doInBackground().
     */
    static public class Result {
        public String resultValue;
        public Exception exception;
        public Result(String resultValue) {
            this.resultValue = resultValue;
        }
        public Result(Exception exception) {
            this.exception = exception;
        }
    }

    @Override
    protected void onPreExecute() {
        Log.d(Startup.LOG_TAG,"Upload task pre execute");
        if (callback != null){
            NetworkInfo networkInfo = callback.getActiveNetworkInfo();
            if (networkInfo == null || !networkInfo.isConnected() || networkInfo.getType() != ConnectivityManager.TYPE_WIFI || file.length() == 0 || fileName == null){
                callback.updateFromDownload(null);
                cancel(true);
                Log.d(Startup.LOG_TAG,"Upload task canceled");
            }
        }
    }

    @Override
    protected UploadTask.Result doInBackground(String... urls) {
        Log.d(Startup.LOG_TAG,"Upload task background process");
        Result result = null;
        if (!isCancelled() && urls != null && urls.length > 0) {
            String urlString = urls[0];
            try {
                URL url = new URL(urlString);
                String resultString = uploadURL(url);
                if (resultString != null){
                    result = new Result(resultString);
                } else {
                    throw new IOException("No response received.");
                }
            } catch (Exception e) {
                result = new Result(e);
            }
        }
        return result;
    }

    @Override
    protected void onPostExecute(Result result) {
        Log.d(Startup.LOG_TAG,"Upload task post execute");
        if (result != null && callback != null) {
            if (result.exception != null) {
                callback.updateFromDownload(result.exception.getMessage());
            } else if (result.resultValue != null) {
                callback.updateFromDownload(result.resultValue);
            }
            callback.finishDownloading();
        }
    }

    private String uploadURL(URL url) throws IOException {
        String result = null;
        DataOutputStream dataOutputStream = null;
        FileInputStream fileInputStream = null;
        HttpURLConnection connection = null;
        String boundary = "*****";
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        byte[] buffer;
        int bytesRead, bytesAvailable, bufferSize;
        int maxBufferSize = 1 * 1024 * 1024;
        try {
            fileInputStream = new FileInputStream(file);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("ENCTYPE", "multipart/form-data");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            connection.setRequestProperty("uploaded_file", fileName);

            dataOutputStream = new DataOutputStream(connection.getOutputStream());


            dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
            dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                    + fileName + "\"" + lineEnd);
            dataOutputStream.writeBytes("Content-Type: text/plain; charset=UTF-8" + lineEnd);

            dataOutputStream.writeBytes(lineEnd);

            bytesAvailable = fileInputStream.available();

            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            bytesRead = fileInputStream.read(buffer,0,bufferSize);
            while (bytesRead > 0 ){
                dataOutputStream.write(buffer,0,bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer,0, bufferSize);
            }

            // send multipart form data necesssary after file data...
            dataOutputStream.writeBytes(lineEnd);
            dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // Responses from the server (code and message)
            int serverResponseCode = connection.getResponseCode();
            String serverResponseMessage = connection.getResponseMessage();
            result = String.valueOf(serverResponseCode) + " : " + serverResponseMessage;

        } finally {
            if(dataOutputStream != null) {
                dataOutputStream.flush();
                dataOutputStream.close();
            }
            if (fileInputStream != null)
                fileInputStream.close();
            if (connection != null)
                connection.disconnect();
        }
        return result;
    }
}

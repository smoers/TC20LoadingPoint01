package com.aerospace.sabena.tc20.loadingpoint.system.network;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.aerospace.sabena.tc20.loadingpoint.Startup;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadTask extends AsyncTask<String, Integer, DownloadTask.Result> {

    private DownloadCallback<String> callback;

    public DownloadTask(DownloadCallback<String> callback) {
        setCallback(callback);
    }

    public void setCallback(DownloadCallback<String> callback){
        this.callback = callback;
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
        Log.d(Startup.LOG_TAG,"Download task pre execute");
        if (callback != null){
            NetworkInfo networkInfo = callback.getActiveNetworkInfo();
            if (networkInfo == null || !networkInfo.isConnected() || networkInfo.getType() != ConnectivityManager.TYPE_WIFI){
                callback.updateFromDownload(null);
                cancel(true);
                Log.d(Startup.LOG_TAG,"Download task canceled");
            }
        }
    }

    @Override
    protected DownloadTask.Result doInBackground(String... urls) {
        Log.d(Startup.LOG_TAG,"Download task background process");
        Result result = null;
        if (!isCancelled() && urls != null && urls.length > 0){
            String urlString = urls[0];
            try {
                URL url = new URL(urlString);
                String resultString = downloadUrl(url);
                if (resultString != null) {
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
        Log.d(Startup.LOG_TAG,"Download task post execute");
        if (result != null && callback != null) {
            if (result.exception != null) {
                callback.updateFromDownload(result.exception.getMessage());
            } else if (result.resultValue != null) {
                callback.updateFromDownload(result.resultValue);
            }
            callback.finishDownloading();
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
//        callback.onProgressUpdate(values[0], values[1]);
    }


    /**
     * Given a URL, sets up a connection and gets the HTTP response body from the server.
     * If the network request is successful, it returns the response body in String form. Otherwise,
     * it will throw an IOException.
     */
    private String downloadUrl(URL url) throws IOException {
        Log.d(Startup.LOG_TAG,"Download task download URL");
        InputStream stream = null;
        HttpURLConnection connection = null;
        String result = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            // Timeout for reading InputStream arbitrarily set to 3000ms.
            connection.setReadTimeout(3000);
            // Timeout for connection.connect() arbitrarily set to 3000ms.
            connection.setConnectTimeout(3000);
            // For this use case, set HTTP method to GET.
            connection.setRequestMethod("GET");
            // Already true by default but setting just in case; needs to be true since this request
            // is carrying an input (response) body.
            connection.setDoInput(true);
            // Open communications link (network traffic occurs here).
            connection.connect();
            publishProgress(DownloadCallback.Progress.CONNECT_SUCCESS);
            callback.onProgressUpdate(DownloadCallback.Progress.CONNECT_SUCCESS,0);
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + responseCode);
            }
            // Retrieve the response body as an InputStream.
            stream = connection.getInputStream();
            publishProgress(DownloadCallback.Progress.GET_INPUT_STREAM_SUCCESS, 0);
            callback.onProgressUpdate(DownloadCallback.Progress.GET_INPUT_STREAM_SUCCESS,0);
            if (stream != null) {
                callback.onProgressUpdate(DownloadCallback.Progress.PROCESS_INPUT_STREAM_IN_PROGRESS,0);
                // Converts Stream to String with max length of 500.
                result = readStream(stream, 500);
            }

        } finally {
            // Close Stream and disconnect HTTPS connection.
            if (stream != null) {
                stream.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
            callback.onProgressUpdate(DownloadCallback.Progress.PROCESS_INPUT_STREAM_SUCCESS,0);
        }
        return result;
    }

    /**
     * Converts the contents of an InputStream to a String.
     */
    public String readStream(InputStream stream, int maxReadSize) throws IOException, UnsupportedEncodingException {
        Log.d(Startup.LOG_TAG,"Download task Read stream");
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] rawBuffer = new char[maxReadSize];
        int readSize;
        StringBuffer buffer = new StringBuffer();
        while (((readSize = reader.read(rawBuffer)) != -1) && maxReadSize > 0) {
            if (readSize > maxReadSize) {
                readSize = maxReadSize;
            }
            buffer.append(rawBuffer, 0, readSize);
            maxReadSize -= readSize;
        }
        return buffer.toString();
    }
}

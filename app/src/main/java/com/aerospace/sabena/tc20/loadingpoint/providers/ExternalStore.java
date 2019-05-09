package com.aerospace.sabena.tc20.loadingpoint.providers;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.aerospace.sabena.tc20.loadingpoint.Startup;
import com.aerospace.sabena.tc20.loadingpoint.models.Barcode;
import com.aerospace.sabena.tc20.loadingpoint.models.ConfigurationList;
import com.aerospace.sabena.tc20.loadingpoint.models.FileNameGenerator;
import com.aerospace.sabena.tc20.loadingpoint.models.Sequence;
import com.aerospace.sabena.tc20.loadingpoint.models.Sequences;
import com.aerospace.sabena.tc20.loadingpoint.models.User;
import com.aerospace.sabena.tc20.loadingpoint.system.BarcodeOrdering;
import com.aerospace.sabena.tc20.loadingpoint.system.FileFilter;
import com.aerospace.sabena.tc20.loadingpoint.system.SequenceFormatter;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Scanner;

public class ExternalStore {
    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION = 1;

    private boolean isOpen = false;
    private AppCompatActivity app;
    private ConfigurationList configurationList;

    public ExternalStore(AppCompatActivity app) {
        this.app = app;
        ConfigurationStore store = new ConfigurationStore(app);
        configurationList = store.load();
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

    /**
     * Cette méthode structure les sequences avant de les sauver dans le fichier
     * @param dirType
     * @param sequences
     * @return
     */
    public boolean write(String dirType, Sequences sequences){
        //Instance de la classe qui va prendre en charge le tri des codes bar
        BarcodeOrdering barcodeOrdering = new BarcodeOrdering(configurationList.getConfiguration("barcode_ordering").getConfigurationValue());
        //Instance de la classe qui va formatter les données de sortie
        SequenceFormatter formatter = new SequenceFormatter(sequences,barcodeOrdering);
        //nom du fichier
        FileNameGenerator fileNameGenerator = new FileNameGenerator(app);
        return write(dirType, fileNameGenerator.getName(), formatter.getString());
    }

    /**
     * Retourne le nombre de fichier dans le repertoire externe passé à la méthode
     * @param dirType
     * @return
     */
    public int countFiles(String dirType){
        int count = 0;
        if (isOpen){
            FilenameFilter filter = new FileFilter(configurationList);
            File files = Environment.getExternalStoragePublicDirectory(dirType);
            Log.d(Startup.LOG_TAG, dirType);
            Log.d(Startup.LOG_TAG,"Length: " + String.valueOf(files.list().length));
            count = files.list(filter).length;
        }
        return count;
    }

    /**
     * Retourne une liste de sequence créée au départ des fichiers en attente de transfert
     * au travers de l'aaplication installée sur un PC
     * @param dirType
     * @return
     * @throws IOException
     * @throws ParseException
     */
    public Sequences read(String dirType) throws IOException, ParseException {
        Sequences sequences = new Sequences();
        if (isOpen){
            FilenameFilter filter = new FileFilter(configurationList);
            File files = Environment.getExternalStoragePublicDirectory(dirType);
            for (File file : files.listFiles(filter)){
                Scanner scanner = new Scanner(file);
                while (scanner.hasNextLine()){
                    String line = scanner.nextLine();
                    String[] items = line.split(";");
                    User user = new User(items[1]);
                    Sequence sequence = new Sequence(Integer.parseInt(items[2]), user);
                    sequence.setDate(new SimpleDateFormat("yyyyMMdd").parse(items[0]));
                    for (int i=3; i < (Integer.parseInt(items[2]) + 3); i++){
                        sequence.addBarcode(new Barcode(items[i]));
                    }
                    sequences.add(sequence);
                }
            }
        }
        return sequences;
    }

    /**
     * Supprimer les fichiers
     * @param dirType
     * @return
     */
    public boolean remove(String dirType){
        boolean result = false;
        if (isOpen){
            FilenameFilter filter = new FileFilter(configurationList);
            File files = Environment.getExternalStoragePublicDirectory(dirType);
            for (File file : files.listFiles(filter)){
                result = file.delete();
            }
        }
        return result;
    }

}

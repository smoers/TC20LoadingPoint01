package com.aerospace.sabena.tc20.loadingpoint.controllers;

import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.aerospace.sabena.tc20.loadingpoint.R;
import com.aerospace.sabena.tc20.loadingpoint.Startup;
import com.aerospace.sabena.tc20.loadingpoint.models.ConfigurationList;
import com.aerospace.sabena.tc20.loadingpoint.models.Sequence;
import com.aerospace.sabena.tc20.loadingpoint.models.Sequences;
import com.aerospace.sabena.tc20.loadingpoint.providers.ConfigurationStore;
import com.aerospace.sabena.tc20.loadingpoint.providers.ExternalStore;
import com.aerospace.sabena.tc20.loadingpoint.providers.InternetStore;
import com.aerospace.sabena.tc20.loadingpoint.providers.InternalStore;
import com.aerospace.sabena.tc20.loadingpoint.system.BarcodeOrdering;
import com.aerospace.sabena.tc20.loadingpoint.system.SequenceFormatter;
import com.aerospace.sabena.tc20.loadingpoint.views.SequenceManagement;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.StringJoiner;

public class SequenceManagementController {

    private InternalStore internalStore;
    private Sequences sequences;
    private SequenceManagement sequenceManagement;
    private ConfigurationStore configurationStore;
    private ConfigurationList configurationList;

    public SequenceManagementController(SequenceManagement sequenceManagement) {
        this.sequenceManagement = sequenceManagement;
        internalStore = new InternalStore(this.sequenceManagement);
        configurationStore = new ConfigurationStore(sequenceManagement);
        configurationList = configurationStore.load();
        loadSequences();
    }

    public Sequences getSequences() {
        return sequences;
    }

    /**
     * Permet de sauvegarder la table Sequences
     */
    public void saveSequences() {
        internalStore.saveSequences(sequences);
    }

    /**
     * Permet de charger la table Sequences
     */
    public void loadSequences() {
        sequences = internalStore.loadSequences();
    }

    /**
     * Supprime la table avec les sequences
     */
    public void removeSequences(){
        if(internalStore.removeSequences())
            sequences = new Sequences();
    }

    /**
     * Permet de supprimer les sequence contenue dans la liste removeSequences
     * @param removeSequences
     */
    public void removeSequence(Sequences removeSequences){
        for (Sequence sequence : removeSequences){
            if (sequences.contains(sequence))
                sequences.remove(sequence);
        }
        saveSequences();
    }

    /**
     * copie les données depuis SequenceFormatter
     * vers l'external storage accessible depuis le pc lorsqu'il est connecté en USB.
     */
    public boolean transfer(){
        boolean result = false;
        if (sequences.size() > 0) {
            ExternalStore store = new ExternalStore(sequenceManagement);
            //Instance de la classe qui va prendre en charge le tri des codes bar
            BarcodeOrdering barcodeOrdering = new BarcodeOrdering(configurationList.getConfiguration("barcode_ordering").getConfigurationValue());
            //Instance de la classe qui va formatter les données de sortie
            SequenceFormatter formatter = new SequenceFormatter(internalStore.loadSequences(), barcodeOrdering);
            //nom du fichier
            String fileName = fileNameGenerator();
            //On s'assure que le store est disponible
            if (store.open()) {
                Log.d(Startup.LOG_TAG, "External storage open");
                if (store.write(Environment.DIRECTORY_DOCUMENTS, fileName, formatter.getString())) {
                    internalStore.removeSequences(); // Vide l'InternalStore
                    Toast.makeText(sequenceManagement, "The data has been saved in the texte file " + fileName, Toast.LENGTH_LONG).show();
                    result = true;
                } else {
                    Toast.makeText(sequenceManagement, "The data CANNOT be saved !!", Toast.LENGTH_LONG).show();
                }
                Log.d(Startup.LOG_TAG, "External storage write");
            }
        } else {
            Toast.makeText(sequenceManagement, "There is not sequences saved !!", Toast.LENGTH_LONG).show();
        }
        return result;
    }


    /**
     * Tranfert les données vers le serveur web
     * @return
     */
    public boolean internetTransfer(){
        //Init result upload
        boolean isUpload = false;
        if (sequences.size() > 0) {
            //Instance InternetStore pour effectuer le transfert
            final InternetStore store = new InternetStore(sequenceManagement);
            Log.d(Startup.LOG_TAG, "InternetStore testing witness");
            //On s'assure que le serveur est joinable, que l'on a du wifi, ...
            boolean available = store.isAvailable();
            Log.d(Startup.LOG_TAG, "InternetStore witness is present : " + String.valueOf(available));
            if (available) {
                sequenceManagement.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(sequenceManagement, "Internet upload started", Toast.LENGTH_LONG).show();
                    }
                });            //Si tout est OK
                //Instance de la classe qui va prendre en charge le tri des codes bar
                BarcodeOrdering barcodeOrdering = new BarcodeOrdering(configurationList.getConfiguration("barcode_ordering").getConfigurationValue());
                //Instance de la classe qui va formatter les données de sortie
                SequenceFormatter formatter = new SequenceFormatter(internalStore.loadSequences(), barcodeOrdering);
                //nom du fichier
                String fileName = fileNameGenerator();
                //création du fichier temporaire car l'upload a besoin d'un objet File
                //le fichier est créer dans le répertoire propre au package de l'application
                //Comme le tranfert est asynchrone le fichier sera détruit par la classe UploadTask
                File directory = sequenceManagement.getDataDir();
                File file = new File(directory, fileName);
                try {
                    //écriture des données
                    FileOutputStream fileOutputStream = new FileOutputStream(file, false);
                    fileOutputStream.write(formatter.getString().getBytes());
                    fileOutputStream.flush();
                    fileOutputStream.close();
                    if (file.exists()) {
                        //si le fichier a bien été créé on effectue le transfert
                        isUpload = store.upload(file, fileName);
                        if (isUpload) {
                            //Upload est terminé avec succès
                            internalStore.removeSequences();
                            sequenceManagement.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(sequenceManagement, "Internet upload is finished with success", Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            sequenceManagement.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(sequenceManagement, "Internet server return error : " + store.getResult(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                        file.delete();
                    }
                } catch (IOException e) {
                    Log.d(Startup.LOG_TAG, e.getMessage());
                }
            } else {
                sequenceManagement.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(sequenceManagement, "Internet web site not available", Toast.LENGTH_LONG).show();
                    }
                });
            }
        } else {
            Toast.makeText(sequenceManagement, "There is not sequences saved !!", Toast.LENGTH_LONG).show();
        }
        return isUpload;
    }

    /**
     * Génère un nom pour le fichier
     * @return
     */
    public String fileNameGenerator(){
        String prefix = configurationList.getConfiguration("filename_prefix").getStringValue();
        String suffix = configurationList.getConfiguration("filename_suffix").getStringValue();
        StringJoiner joiner = new StringJoiner("_",prefix,suffix);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssS");
        return  joiner.add(dateFormat.format(Calendar.getInstance().getTime())).toString();

    }
}

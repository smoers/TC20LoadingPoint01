package com.aerospace.sabena.tc20.loadingpoint.models;

import android.support.v7.app.AppCompatActivity;

import com.aerospace.sabena.tc20.loadingpoint.providers.ConfigurationStore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.StringJoiner;

/**
 * génère une nom de fichier basé sur les prérequis
 */
public class FileNameGenerator {

    private ConfigurationList configurationList;
    private AppCompatActivity app;

    /**
     * Constructeur
     * @param app
     */
    public FileNameGenerator(AppCompatActivity app) {
        ConfigurationStore store = new ConfigurationStore(app);
        configurationList = store.load();
    }

    /**
     * Retourne le nom
     * @return
     */
    public String getName(){
        String prefix = configurationList.getConfiguration("filename_prefix").getStringValue();
        String suffix = configurationList.getConfiguration("filename_suffix").getStringValue();
        StringJoiner joiner = new StringJoiner("_",prefix,suffix);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssS");
        return  joiner.add(dateFormat.format(Calendar.getInstance().getTime())).toString();
    }
}

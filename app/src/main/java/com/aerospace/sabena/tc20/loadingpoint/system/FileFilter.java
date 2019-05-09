package com.aerospace.sabena.tc20.loadingpoint.system;

import android.util.Log;

import com.aerospace.sabena.tc20.loadingpoint.Startup;
import com.aerospace.sabena.tc20.loadingpoint.models.ConfigurationList;

import java.io.File;
import java.io.FilenameFilter;

public class FileFilter implements FilenameFilter {
    private ConfigurationList configurations;

    public FileFilter(ConfigurationList configurations) {
        this.configurations = configurations;
    }

    @Override
    public boolean accept(File dir, String name) {
        String regex = configurations.getConfiguration("filename_prefix").getStringValue() + "\\d{15}" + configurations.getConfiguration("filename_suffix").getStringValue();
        return Utils.findString(regex, name);
    }
}

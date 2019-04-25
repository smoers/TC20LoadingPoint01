package com.aerospace.sabena.tc20.loadingpoint.models;

import android.util.Log;

import com.aerospace.sabena.tc20.loadingpoint.Startup;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Iterator;

public class ConfigurationList extends ArrayList<Configuration> {

    public ConfigurationList(){}

    public Configuration getConfiguration(String key){
        Configuration configuration = null;
        Iterator<Configuration> iterator = this.iterator();
        while (iterator.hasNext()){
            Configuration tmp = iterator.next();
            if (tmp.getKey().equalsIgnoreCase(key)){
                configuration = tmp;
                break;
            }
        }
        return configuration;
    }

    public String getJsonString(){
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static ConfigurationList setJsonString(String json){
        Log.d(Startup.LOG_TAG,json);
        Gson gson = new Gson();
        return gson.fromJson(json, new TypeToken<ConfigurationList>(){}.getType());
    }
}

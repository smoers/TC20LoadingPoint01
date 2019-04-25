package com.aerospace.sabena.tc20.loadingpoint.models;

import java.util.List;
import java.util.StringJoiner;

public class Configuration {

    private String stringValue = null;
    private Integer intValue = null;
    private Boolean booleanValue = null;
    private List<String> listValue = null;
    private Configuration[] configurationValue = null;
    private String key = null;

    public Configuration(String stringValue, String key) {
        this.stringValue = stringValue;
        this.key = key;
    }

    public Configuration(Integer intValue, String key) {
        this.intValue = intValue;
        this.key = key;
    }

    public Configuration(Boolean booleanValue, String key) {
        this.booleanValue = booleanValue;
        this.key = key;
    }

    public Configuration(List<String> listValue, String key) {
        this.listValue = listValue;
        this.key = key;
    }

    public Configuration(Configuration[] configurationValue, String key){
        this.configurationValue = configurationValue;
        this.key = key;
    }

    public String getStringValue() {
        return stringValue;
    }

    public Integer getIntValue() {
        return intValue;
    }

    public Boolean getBooleanValue() {
        return booleanValue;
    }

    public List<String> getListValue() {
        return listValue;
    }

    public Configuration[] getConfigurationValue(){ return configurationValue; }

    public String getKey() {
        return key;
    }

    public String toString(){
        String _return = null;
        if (stringValue != null){
            _return = stringValue;
        } else if (intValue != null){
            _return = String.valueOf(intValue);
        } else if (booleanValue != null){
            _return = String.valueOf(booleanValue);
        } else if (listValue != null){
            StringBuilder joiner = new StringBuilder();
            for (String item : listValue){
                joiner.append(item+"\n\r");
            }
            _return = joiner.toString();
        } else if (configurationValue != null){
            StringBuilder joiner = new StringBuilder();
            for (Configuration configuration : configurationValue){
                joiner.append(configuration.key+": ");
                joiner.append(configuration.toString());
            }
            _return  = joiner.toString();
        }

        return _return;
    }
}

package com.aerospace.sabena.tc20.loadingpoint.models;

import java.util.Calendar;
import java.util.Date;

/**
 * Classe représentant un code bar
 */
public class Barcode {
    private Date date = Calendar.getInstance().getTime();
    private String code = null;

    /**
     * Constructeur
     * @param code
     */
    public Barcode(String code) {
        this.code = code;
    }

    public Date getDate() {
        return date;
    }

    public String getCode() {
        return code;
    }
}

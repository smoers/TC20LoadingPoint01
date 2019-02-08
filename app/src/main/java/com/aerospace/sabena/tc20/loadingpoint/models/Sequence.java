package com.aerospace.sabena.tc20.loadingpoint.models;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Classe représentant une capture d'une séquence de barcode
 */
public class Sequence {
    private Date date;
    private int size = 0;
    private User owner;
    private ArrayList<Barcode> barcodes = new ArrayList<>();

    /**
     * Constructeur
     * @param size
     * @param owner
     */
    public Sequence(int size, User owner) {
        this.size = size;
        this.owner = owner;
        this.date = Calendar.getInstance().getTime();
    }

    public int getSize() {
        return size;
    }

    public User getOwner() {
        return owner;
    }

    public Date getDate() {
        return date;
    }

    public void addBarcode(Barcode barcode){
        barcodes.add(barcode);
    }

    public ArrayList<Barcode> getBarcodes(){
        return barcodes;
    }
}

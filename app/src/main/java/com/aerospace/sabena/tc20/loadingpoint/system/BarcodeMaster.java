package com.aerospace.sabena.tc20.loadingpoint.system;

import android.util.Log;

import com.aerospace.sabena.tc20.loadingpoint.Startup;
import com.aerospace.sabena.tc20.loadingpoint.models.Barcode;
import com.aerospace.sabena.tc20.loadingpoint.models.Sequence;

public class BarcodeMaster {

    private String masterRegex;
    private Barcode masterBarcode = null;
    private boolean force = false;

    public BarcodeMaster(String masterRegex) {
        this.masterRegex = masterRegex;
    }

    public void resetMasterBarcode(){
        masterBarcode = null;
    }

    /**
     * Cette méthode permet de déterminer si le code bar passé en parametre
     * contient le master masterBarcode.
     * Tant que le master masterBarcode n'est pas trouver la méthode return TRUE
     * Lorsque le master masterBarcode est trouvé et chargé dans l'objet BarcodeMaster, tous les barcodes préalablment enregistrés sont controlé.
     * Quand le master masterBarcode existe on controle directement le masterBarcode.
     * @param sequence
     * @param barcode
     * @return
     */
    public boolean isContentMaster(Sequence sequence, Barcode barcode){
        Log.d(Startup.LOG_TAG,"MasterRegex: " + masterRegex);
        //Si l'objet BarcodeMaster a été créé au travers dela méthode setMasterRegex
        if (Utils.findString(masterRegex,barcode.getCode())) {
            //Si le code bar est le BarcodeMaster
            Log.d(Startup.LOG_TAG,"Barcodre Master: " + barcode.getCode());
            masterBarcode = barcode;
            //on parcoure tous les codes bar préalablement enregistré
            if (sequence != null) {
                for (Barcode sbarcode : sequence.getBarcodes()) {
                    if (!Utils.findString(masterBarcode.getCode(), sbarcode.getCode())) {
                        return false;
                    }
                }
            }
        } else if(masterBarcode != null) {
            Log.d(Startup.LOG_TAG,"Barcode Master checked: " + barcode.getCode());
            //On controle directement le masterBarcode
            return Utils.findString(masterBarcode.getCode(),barcode.getCode());
        }
        return true;
    }

    /**
     * Force à true le content master
     * @param force
     */
    public void setForce(boolean force) {
        this.force = force;
    }

    public boolean isForce() {
        return force;
    }
}

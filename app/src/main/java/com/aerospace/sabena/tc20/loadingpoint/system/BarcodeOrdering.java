package com.aerospace.sabena.tc20.loadingpoint.system;

import com.aerospace.sabena.tc20.loadingpoint.models.Barcode;
import com.aerospace.sabena.tc20.loadingpoint.models.Configuration;

import java.util.List;
import java.util.StringJoiner;

/**
 * Cette classe permet de mettre les code bars dans le bon ordre pour l'export.
 */
public class BarcodeOrdering {

    private Configuration[] configurations;

    /**
     * Contructeur
     * @param configurations
     */
    public BarcodeOrdering(Configuration[] configurations) {
        this.configurations = configurations;
    }

    /**
     * Se charge du tri
     * @param joiner
     * @param barcodes
     * @return
     */
    public StringJoiner getOrdering(StringJoiner joiner, List<Barcode> barcodes){

        for (Configuration configuration : configurations){
            //parcours les objet de configuration
            boolean barcodeFind = false;
            for (Barcode barcode : barcodes){
                //parcours les bar codes
                for (String value :configuration.getListValue()){
                    //parcours les expressions régulières
                    if (BarcodeValidator.isValid(value, barcode)){
                        joiner.add(barcode.getCode());
                        barcodeFind = true;
                        break;
                    }
                }
                if (barcodeFind)
                    break;
            }
        }
        return joiner;
    }

}

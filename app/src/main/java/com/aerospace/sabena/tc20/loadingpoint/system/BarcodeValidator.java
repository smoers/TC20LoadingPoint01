package com.aerospace.sabena.tc20.loadingpoint.system;

import android.util.Log;

import com.aerospace.sabena.tc20.loadingpoint.Startup;
import com.aerospace.sabena.tc20.loadingpoint.models.Barcode;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cette classe permet de valider la structure des barcodes
 * Le validateurs doit être une liste d'expression régulières
 */
public class BarcodeValidator {

    /**
     * Liste des expressions régulières
     */
    private List<String> validators;

    /**
     * Contructeur
     * @param validators
     */
    public BarcodeValidator(List<String> validators) {
        this.validators = validators;
    }

    /**
     * Valide le code bar
     * @param barcode
     * @return
     */
    public boolean isValid(Barcode barcode){
        boolean valid = false;
        for (String validator : validators){
            Pattern pattern = Pattern.compile(validator);
            Matcher matcher = pattern.matcher(barcode.getCode());
            if (matcher.find()){
                valid = true;
                break;
            }
        }
        return valid;
    }

    /**
     * Valide le code bar
     * @param validator
     * @param barcode
     * @return
     */
    public static boolean isValid(String validator, Barcode barcode){
        List<String> validators = new ArrayList<>();
        validators.add(validator);
        BarcodeValidator barcodeValidator = new BarcodeValidator(validators);
        return barcodeValidator.isValid(barcode);
    }
}

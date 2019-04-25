package com.aerospace.sabena.tc20.loadingpoint.system;

import com.aerospace.sabena.tc20.loadingpoint.models.Barcode;
import com.aerospace.sabena.tc20.loadingpoint.models.Sequence;
import com.aerospace.sabena.tc20.loadingpoint.models.Sequences;

import java.text.SimpleDateFormat;
import java.util.StringJoiner;

public class SequenceFormatter {

    private Sequences sequences;
    private BarcodeOrdering barcodeOrdering;

    /**
     * Contructeur
     * @param sequences
     * @param barcodeOrdering
     */
    public SequenceFormatter(Sequences sequences, BarcodeOrdering barcodeOrdering) {
        this.sequences = sequences;
        this.barcodeOrdering =barcodeOrdering;
    }

    /**
     *
     * @return
     */
    public String getString(){
        StringBuilder resultString = new StringBuilder();
        String delimiter = ";";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        for (Sequence sequence : sequences){
            StringJoiner joiner = new StringJoiner(delimiter);
            joiner.add(dateFormat.format(sequence.getDate()));
            joiner.add(sequence.getOwner().getRoleNumber());
            joiner.add(String.valueOf(sequence.getSize()));
            barcodeOrdering.getOrdering(joiner,sequence.getBarcodes());
            /*
            for (Barcode barcode : sequence.getBarcodes()){
                joiner.add(barcode.getCode());
            }
            */
            resultString.append(joiner.toString()).append('\r').append('\n');
        }
        return resultString.toString();
    }
}

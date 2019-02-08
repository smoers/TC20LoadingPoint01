package com.aerospace.sabena.tc20.loadingpoint.system;

import com.aerospace.sabena.tc20.loadingpoint.models.Barcode;
import com.aerospace.sabena.tc20.loadingpoint.models.Sequence;
import com.aerospace.sabena.tc20.loadingpoint.models.Sequences;

import java.text.SimpleDateFormat;
import java.util.StringJoiner;

public class SequenceFormatter {

    private Sequences sequences;

    public SequenceFormatter(Sequences sequences) {
        this.sequences = sequences;
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
            for (Barcode barcode : sequence.getBarcodes()){
                joiner.add(barcode.getCode());
            }
            resultString.append(joiner.toString()).append('\r').append('\n');
        }
        return resultString.toString();
    }
}

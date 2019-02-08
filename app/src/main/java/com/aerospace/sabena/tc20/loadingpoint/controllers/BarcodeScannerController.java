package com.aerospace.sabena.tc20.loadingpoint.controllers;

import android.util.Log;
import android.view.View;

import com.aerospace.sabena.tc20.loadingpoint.R;
import com.aerospace.sabena.tc20.loadingpoint.Startup;
import com.aerospace.sabena.tc20.loadingpoint.models.Barcode;
import com.aerospace.sabena.tc20.loadingpoint.models.Sequence;
import com.aerospace.sabena.tc20.loadingpoint.models.Sequences;
import com.aerospace.sabena.tc20.loadingpoint.models.TaskCounterReset;
import com.aerospace.sabena.tc20.loadingpoint.models.TaskRoleReset;
import com.aerospace.sabena.tc20.loadingpoint.models.TaskSequenceReset;
import com.aerospace.sabena.tc20.loadingpoint.models.User;
import com.aerospace.sabena.tc20.loadingpoint.providers.InternalStore;
import com.aerospace.sabena.tc20.loadingpoint.views.BarcodeScanner;

import java.util.Timer;

/**
 * Classe en charge des actions liées à la vue gérée par la classe Barcodescanner
 */
public class BarcodeScannerController {

    private BarcodeScanner barcodeScanner;
    private User user;
    private Sequences sequences = new Sequences();
    private Sequence sequence = null;
    private Timer roleTimer = new Timer(); //Ce timer ce charge de revenir à l'activité Startup si pas d'action pendant les prochaines 1800sec
    private Timer sequenceTimer = new Timer(); //Ce timer ce charge de faire un reset de la sequence si pas d'action pendant les prochaines 120sec
    private Timer counterTimer = new Timer(); //Ce timer ce charge de faire un reset du counter affiché si pas d'action dans les prohaines 60sec
    private int size;
    private InternalStore internalStore;

    /**
     * Constructeur
     * @param barcodeScanner
     * @param user
     */
    public BarcodeScannerController(BarcodeScanner barcodeScanner, User user) {
        this.barcodeScanner = barcodeScanner;
        this.user = user;
        this.size = barcodeScanner.getResources().getInteger(R.integer.barcode_scanner_sequence_size);
        internalStore = new InternalStore(barcodeScanner);
        loadSequences();
    }

    /**
     * Retourne le nombre de Sequences enregistrèes dans
     * les préférence de l'application
     * @return
     */
    public int getSequencesLength(){
        return sequences.size();
    }

    /**
     * Cette méthode est appelée par un event levé par l'instance BroadcastReceiver
     * Ajoute un barcode dans une sequence.
     * Va créer une nouvelle sequende si besoin.
     * @param value
     */
    public void addBarcode(String value){
        //Timer
        roleTimer.cancel();  //ce timer est supprimé dès lors qu'un nouveau scan est exécuté
        counterTimer.cancel(); //ce timer est supprimé dès lors qu'un nouveau scan est exécuté
        sequenceTimer.cancel(); //ce timer est supprimé dès lors qu'un nouveau scan est exécuté
        //Instance barcode
        Barcode barcode = new Barcode(value);
        if (sequence == null){
            //Objet Sequence est null, on le crée
            sequence = new Sequence(size, user);
            Log.d(Startup.LOG_TAG,"New Sequence" + String.valueOf(sequence.getBarcodes().size()));
            //Affiche et int le counter de l'interface
            barcodeScanner.gettCounter().setVisibility(View.VISIBLE);
            barcodeScanner.getiBarcode().setVisibility(View.INVISIBLE);
        }
        //Ajoute le barcode à la sequence
        sequence.addBarcode(barcode);
        barcodeScanner.gettDataScanned().setText(barcode.getCode());
        barcodeScanner.gettCounter().setText(String.valueOf(sequence.getBarcodes().size())+"/"+String.valueOf(size));
        if(sequence != null && sequence.getBarcodes().size() == size){
            //Objet Sequence n'est pas null, mais il a atteint le nombre maximum de barcode
            sequences.add(sequence);
            Log.d(Startup.LOG_TAG,"Sequence added to Sequences" + String.valueOf(sequence.getBarcodes().size()));
            //Sauvegarde de la table des Sequence
            saveSequences();//Sauvegarde la nouvelle sequence
            //Mise à jour du GUI
            barcodeScanner.gettSequenceLength().setText(String.valueOf(sequences.size()));
            sequence = new Sequence(size, user);
            setCounterTimer(); //ce timer est activé à chaque fin de sequence
            sequenceTimer.cancel(); //ce timer est supprimé dès lors qu'une sequence est terminée
        }
        //Timer
        setRoleTimer(); //ce timer est activé à chaque scaan
        if (sequence.getBarcodes().size() < size) {
            setSequenceTimer();  //ce timer est activé entre chaque scan d'une sequence
        }

    }

    /**
     * Permet de sauvegarder la table Sequences
     */
    public void saveSequences() {
        internalStore.saveSequences(sequences);
    }

    /**
     * Permet de charger la table Sequences
     */
    public void loadSequences() {
        sequences = internalStore.loadSequences();
    }

    /**
     * Supprime la table avec les sequences
     */
    public void removeSequences(){
        if(internalStore.removeSequences())
            sequences = new Sequences();
    }

    /**
     * Reset le counter et le champ data
     */
    public void resetCounter(){
        barcodeScanner.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sequence = null; //reset objet sequence
                barcodeScanner.getiBarcode().setVisibility(View.VISIBLE);
                barcodeScanner.gettCounter().setText("0");
                barcodeScanner.gettCounter().setVisibility(View.INVISIBLE);
                barcodeScanner.gettDataScanned().setText("");
            }
        });
    }

    /**
     * Ferme l'activité
     */
    public void finish(){
        barcodeScanner.finish();
    }

    public void resetSequence(){
        sequence = null;
        resetCounter();
    }

    /**
     * Défini un timer pour faire un reset du Role number
     */
    protected void setRoleTimer(){
        roleTimer = new Timer();
        long delay = Long.valueOf(Integer.valueOf(barcodeScanner.getResources().getInteger(R.integer.barcode_scanner_delay_reset_role)).longValue());
        roleTimer.schedule(new TaskRoleReset(BarcodeScannerController.this), delay * 1000);
        Log.d(Startup.LOG_TAG, "Set RoleTimer");
    }

    protected void setSequenceTimer(){
        sequenceTimer = new Timer();
        long delay = Long.valueOf(Integer.valueOf(barcodeScanner.getResources().getInteger(R.integer.barcode_scanner_delay_reset_sequence)).longValue());
        sequenceTimer.schedule(new TaskSequenceReset(BarcodeScannerController.this), delay * 1000);
        Log.d(Startup.LOG_TAG, "Set SequenceTimer");
    }

    protected void setCounterTimer(){
        counterTimer = new Timer();
        long delay = Long.valueOf(Integer.valueOf(barcodeScanner.getResources().getInteger(R.integer.barcode_scanner_delay_reset_counter)).longValue());
        counterTimer.schedule(new TaskCounterReset(BarcodeScannerController.this),delay * 1000);
        Log.d(Startup.LOG_TAG, "Set CounterTimer");
    }


}

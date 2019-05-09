package com.aerospace.sabena.tc20.loadingpoint.controllers;

import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.aerospace.sabena.tc20.loadingpoint.R;
import com.aerospace.sabena.tc20.loadingpoint.Startup;
import com.aerospace.sabena.tc20.loadingpoint.models.Barcode;
import com.aerospace.sabena.tc20.loadingpoint.models.Configuration;
import com.aerospace.sabena.tc20.loadingpoint.models.ConfigurationList;
import com.aerospace.sabena.tc20.loadingpoint.models.Sequence;
import com.aerospace.sabena.tc20.loadingpoint.models.Sequences;
import com.aerospace.sabena.tc20.loadingpoint.models.TaskCounterReset;
import com.aerospace.sabena.tc20.loadingpoint.models.TaskRoleReset;
import com.aerospace.sabena.tc20.loadingpoint.models.TaskSequenceReset;
import com.aerospace.sabena.tc20.loadingpoint.models.User;
import com.aerospace.sabena.tc20.loadingpoint.providers.ConfigurationStore;
import com.aerospace.sabena.tc20.loadingpoint.providers.InternalStore;
import com.aerospace.sabena.tc20.loadingpoint.system.BarcodeMaster;
import com.aerospace.sabena.tc20.loadingpoint.system.BarcodeValidator;
import com.aerospace.sabena.tc20.loadingpoint.views.BarcodeScanner;

import java.util.List;
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
    private ConfigurationStore configurationStore;
    private ConfigurationList configurationList;
    private BarcodeValidator validator;
    private BarcodeMaster master;
    private boolean structureFlag = true;
    private boolean sameUnloadingFlag = true;

    /**
     * Constructeur
     * @param barcodeScanner
     * @param user
     */
    public BarcodeScannerController(BarcodeScanner barcodeScanner, User user) {
        this.barcodeScanner = barcodeScanner;
        this.configurationStore = new ConfigurationStore(this.barcodeScanner);
        this.configurationList = configurationStore.load();

        /** Validator setup**/
        List<String> validationBarcodes = configurationList.getConfiguration("validation_barcode").getListValue();
        this.validator = new BarcodeValidator(validationBarcodes);
        /**Master setup**/
        String masterRegex = validationBarcodes.get(Configuration.getConfiguration(configurationList.getConfiguration("barcode_unloading_point_checked").getConfigurationValue(),"validation_barcode").getIntValue());
        this.master = new BarcodeMaster(masterRegex);

        this.structureFlag = configurationList.getConfiguration("barcode_structure_unique_on_sequence").getBooleanValue();
        this.sameUnloadingFlag = Configuration.getConfiguration(configurationList.getConfiguration("barcode_unloading_point_checked").getConfigurationValue(),"enabled").getBooleanValue();
        this.user = user;
        this.size = configurationList.getConfiguration("barcode_scanner_sequence_size").getIntValue();
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
        if (validator.isValid(barcode)) { //On s'assure que la structure du barcode répond aux critères !!
            if ((!validator.isExist(sequence,barcode) && structureFlag) || !structureFlag) { //On s'assure que la meme structure de barcode n'existe pas dans la sequence
                if (checkedMaster(sequence,barcode)) {
                    Log.d(Startup.LOG_TAG,"Trait barcode");
                    if (sequence == null) {
                        //Objet Sequence est null, on le crée
                        sequence = new Sequence(size, user);
                        Log.d(Startup.LOG_TAG, "New Sequence" + String.valueOf(sequence.getBarcodes().size()));
                        //Affiche et int le counter de l'interface
                        barcodeScanner.gettCounter().setVisibility(View.VISIBLE);
                        barcodeScanner.getiBarcode().setVisibility(View.INVISIBLE);
                    }
                    //Ajoute le barcode à la sequence
                    sequence.addBarcode(barcode);
                    barcodeScanner.gettDataScanned().setText(barcode.getCode());
                    barcodeScanner.gettCounter().setText(String.valueOf(sequence.getBarcodes().size()) + "/" + String.valueOf(size));
                    if (sequence != null && sequence.getBarcodes().size() == size) {
                        //Objet Sequence n'est pas null, mais il a atteint le nombre maximum de barcode
                        sequences.add(sequence);
                        Log.d(Startup.LOG_TAG, "Sequence added to Sequences" + String.valueOf(sequence.getBarcodes().size()));
                        //Sauvegarde de la table des Sequence
                        saveSequences();//Sauvegarde la nouvelle sequence
                        //Mise à jour du GUI
                        barcodeScanner.gettSequenceLength().setText(String.valueOf(sequences.size()));
                        resetSequence();
                        //sequence = new Sequence(size, user);
                        setCounterTimer(); //ce timer est activé à chaque fin de sequence
                        sequenceTimer.cancel(); //ce timer est supprimé dès lors qu'une sequence est terminée
                        master.resetMasterBarcode(); //La sequence est terminée donc on fait un reset du master barcode
                    }
                }
            } else {
                barcodeScanner.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(barcodeScanner, "A barcode with the same structure\n\ralready exist in this sequence !",Toast.LENGTH_LONG).show();
                    }
                });
            }
        } else {
            barcodeScanner.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(barcodeScanner, "This barcode is rejected,\n\rbecause the structure is not valid !",Toast.LENGTH_LONG).show();
                }
            });
        }
        //Timer
        setRoleTimer(); //ce timer est activé à chaque scan
        if (sequence != null && sequence.getBarcodes().size() < size) {
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
        if(internalStore.removeSequences()) {
            sequences = new Sequences();
        }
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
        long delay = Long.valueOf(Integer.valueOf(configurationList.getConfiguration("barcode_scanner_delay_reset_role").getIntValue()).longValue());
        roleTimer.schedule(new TaskRoleReset(BarcodeScannerController.this), delay * 1000);
        Log.d(Startup.LOG_TAG, "Set RoleTimer");
    }

    protected void setSequenceTimer(){
        sequenceTimer = new Timer();
        long delay = Long.valueOf(Integer.valueOf(configurationList.getConfiguration("barcode_scanner_delay_reset_sequence").getIntValue()).longValue());
        sequenceTimer.schedule(new TaskSequenceReset(BarcodeScannerController.this), delay * 1000);
        Log.d(Startup.LOG_TAG, "Set SequenceTimer");
    }

    protected void setCounterTimer(){
        counterTimer = new Timer();
        long delay = Long.valueOf(Integer.valueOf(configurationList.getConfiguration("barcode_scanner_delay_reset_counter").getIntValue()).longValue());
        counterTimer.schedule(new TaskCounterReset(BarcodeScannerController.this),delay * 1000);
        Log.d(Startup.LOG_TAG, "Set CounterTimer");
    }

    protected boolean checkedMaster(Sequence sequence, final Barcode barcode){
        boolean result = (master.isContentMaster(sequence, barcode) && sameUnloadingFlag) || !sameUnloadingFlag || master.isForce();
        Log.d(Startup.LOG_TAG,"Master is forced: " + master.isForce());
        Log.d(Startup.LOG_TAG, "Master result: " + result);
        if(!result){
            barcodeScanner.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(barcodeScanner);
                    builder.setTitle("Warning")
                            .setMessage("The unloading point scanned is not the same as that indicated in the number of goods issue document.\n\rDo you want continue ?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    master.setForce(true);
                                    Log.d(Startup.LOG_TAG,"New presentation of Barcode");
                                    addBarcode(barcode.getCode());
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    resetSequence();
                                }
                            })
                            .create()
                            .show();
                }
            });
        }
        master.setForce(false);
        return result;
    }
}

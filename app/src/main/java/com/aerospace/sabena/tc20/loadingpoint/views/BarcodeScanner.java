package com.aerospace.sabena.tc20.loadingpoint.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.aerospace.sabena.tc20.loadingpoint.BroadcastReceiverEvent;
import com.aerospace.sabena.tc20.loadingpoint.R;
import com.aerospace.sabena.tc20.loadingpoint.Startup;
import com.aerospace.sabena.tc20.loadingpoint.controllers.BarcodeScannerController;
import com.aerospace.sabena.tc20.loadingpoint.controllers.ViewInterface;
import com.aerospace.sabena.tc20.loadingpoint.listeners.BroadcastReceiverListener;
import com.aerospace.sabena.tc20.loadingpoint.models.ConfigurationList;
import com.aerospace.sabena.tc20.loadingpoint.models.TaskAutomaticExportData;
import com.aerospace.sabena.tc20.loadingpoint.models.User;
import com.aerospace.sabena.tc20.loadingpoint.providers.BroadcastReceiverImplementation;
import com.aerospace.sabena.tc20.loadingpoint.providers.ConfigurationStore;

/**
 * Classe en charge de la vue utilisée pour le scan
 * Chaque vue de l'appliocation est liée à un controler en charge des actions
 */
public class BarcodeScanner extends AppCompatActivity implements ViewInterface<BarcodeScannerController> {

    private BarcodeScannerController barcodeScannerController;
    private BroadcastReceiverImplementation broadcast = new BroadcastReceiverImplementation(BarcodeScanner.this);
    private TextView tRoleNumber;
    private TextView tSequenceLength;
    private TextView tSequenceSize;
    private TextView tCounter;
    private TextView tDataScanned;
    private Button bCancel;
    private ImageView iBarcode;
    private Toolbar toolbarScanner;
    private ImageButton bList;
    private ImageButton bRemove;
    private ImageButton bExit;
    private ImageButton bProfile;
    private ImageButton bSetup;
    private MediaPlayer mediaPlayer;

    private ConfigurationStore store;
    private ConfigurationList config;
    private User user;
    private TaskAutomaticExportData taskAutomaticExportData = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scanner);
        initialize();
    }

    /**
     * Initialisation de la View
     */
    protected void initialize(){
        store = new ConfigurationStore(BarcodeScanner.this);
        config = store.load();
        String[] validations = getResources().getStringArray(R.array.validation_barcode);
        for(String validation : validations){
            Log.d(Startup.LOG_TAG, validation);
        }
        //Instance des composants
        tRoleNumber = (TextView) findViewById(R.id.tRoleNumber);
        tSequenceLength = (TextView) findViewById(R.id.tSequencesLength);
        tSequenceSize = (TextView) findViewById(R.id.tSequenceSize);
        tCounter = (TextView) findViewById(R.id.tCounter);
        tDataScanned = (TextView) findViewById(R.id.tDataScanned);
        bCancel = (Button) findViewById(R.id.bCancel);
        iBarcode = (ImageView) findViewById(R.id.iBarcode);
        bList = (ImageButton) findViewById(R.id.bList);
        bRemove = (ImageButton) findViewById(R.id.bRemove);
        bExit = (ImageButton) findViewById(R.id.bExit);
        bProfile = (ImageButton) findViewById(R.id.bProfile);
        toolbarScanner = (Toolbar) findViewById(R.id.toolbar_scanner);
        bSetup = (ImageButton) findViewById(R.id.bSetup);
        tCounter.setVisibility(View.INVISIBLE);
        iBarcode.setVisibility(View.VISIBLE);
        setSupportActionBar(toolbarScanner);        //layout
        mediaPlayer = MediaPlayer.create(BarcodeScanner.this, R.raw.buttonclick);
        //get rolenumber
        Intent intent = getIntent();
        if (intent.hasExtra(getResources().getString(R.string.activity_role_number))){
            String roleNumber = intent.getStringExtra(getResources().getString(R.string.activity_role_number));
            tRoleNumber.setText(roleNumber);
            user = new User(roleNumber);
        } else {
            tRoleNumber.setText("ERROR");
        }
        /* L'icon Remove est disabled dans la bar de manu de l'interface
        *  pour tous les utilisateurs, excepté pour le admin contenu dans la liste
        *  users_admin du fichier de configuration
        */
        bRemove.setEnabled(false);
        bRemove.setImageResource(R.mipmap.deletegray);
        if (config.getConfiguration("users_admin").getListValue().contains(tRoleNumber.getText())){
            bRemove.setEnabled(true);
            bRemove.setImageResource(R.mipmap.delete);
        }
        //Event bouton
        bCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.start();
                barcodeScannerController.resetSequence();
            }
        });
        bList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.start();
                Intent smIntent = new Intent(BarcodeScanner.this, SequenceManagement.class);
                startActivity(smIntent);
            }
        });
        bProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.start();
                Intent pfIntent = new Intent(BarcodeScanner.this, ProfileSetup.class);
                startActivity(pfIntent);
            }
        });
        bRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.start();
                //Création et affichage du message d'alerte
                AlertDialog.Builder builder = new AlertDialog.Builder(BarcodeScanner.this);
                builder.setTitle("Warning")
                        .setMessage("Are you sure you want to delete all Sequences?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                barcodeScannerController.removeSequences();
                                tSequenceLength.setText(barcodeScannerController.getSequencesLength());
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .create()
                        .show();
            }
        });
        bSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.start();
                Intent stIntent = new Intent(BarcodeScanner.this, Setup.class);
                startActivity(stIntent);
            }
        });

        bExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.start();
                finish();
            }
        });

        //Instance du controller de la view
        barcodeScannerController = new BarcodeScannerController(BarcodeScanner.this, user);
        //peuple l'interface utilisateur
        tSequenceSize.setText(String.valueOf(config.getConfiguration("barcode_scanner_sequence_size").getIntValue()));
        //Capture des barcode
        broadcast.addBroadcastReceiverListener(new BroadcastReceiverListener() {
            @Override
            public void broadcastReceiver(BroadcastReceiverEvent evt) {
                Log.d(Startup.LOG_TAG,"Read scanned data");
                Intent intent = evt.getIntent();
                Context context = evt.getContext();
                String action = intent.getAction();
                Log.d(Startup.LOG_TAG, action);
                if(action.equals(context.getResources().getString(R.string.activity_intent_filter_action))){
                    String data = intent.getStringExtra(context.getResources().getString(R.string.datawedge_intent_key_data));
                    Log.d(Startup.LOG_TAG,data);
                    getController().addBarcode(data);
                }
            }
        });
        //Enregistre un receiver afin de savoir quand la tâche d'export automatique a été invoquée.
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase("com.aerospace.sabena.tc20.loadingpoint.TASK_AUTOMATIC_INVOCATED")){
                    if (intent.getExtras().getBoolean("TASK_AUTOMATIC_INVOCATED")){
                        gettSequenceLength().setText(String.valueOf(getController().getSequencesLength()));
                        Log.d(Startup.LOG_TAG,"Broadcast Receiver TASK_AUTOMATIC_INVOCATED");
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.aerospace.sabena.tc20.loadingpoint.TASK_AUTOMATIC_INVOCATED");
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(BarcodeScanner.this);
        localBroadcastManager.registerReceiver(receiver, filter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.miExportSequence:
                Intent smIntent = new Intent(BarcodeScanner.this, SequenceManagement.class);
                startActivity(smIntent);
                return true;
            case R.id.miRemoveSequence:
                AlertDialog.Builder builder = new AlertDialog.Builder(BarcodeScanner.this);
                builder.setTitle("Warning")
                        .setMessage("Are you sure you want to delete all Sequences?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                barcodeScannerController.removeSequences();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .create()
                        .show();

                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public BarcodeScannerController getController() {
        return barcodeScannerController;
    }

    /**
     * Composant
     * @return
     */
    public TextView gettRoleNumber() {
        return tRoleNumber;
    }

    /**
     * Composant
     * @return
     */
    public TextView gettSequenceLength() {
        return tSequenceLength;
    }

    /**
     * Composant
     * @return
     */
    public TextView gettSequenceSize() {
        return tSequenceSize;
    }

    /**
     * Composant
     * @return
     */
    public TextView gettCounter() {
        return tCounter;
    }

    /**
     * Composant
     * @return
     */
    public TextView gettDataScanned() {
        return tDataScanned;
    }

    /**
     * Composant
     * @return
     */
    public Button getbCancel() {
        return bCancel;
    }

    /**
     * Composant
     * @return
     */
    public ImageView getiBarcode() {
        return iBarcode;
    }

    /**
     * Lors de la relance de l'activité on enregistre le broadcast receiver
     */
    @Override
    protected void onResume() {
        super.onResume();
        broadcast.register();
        getController().loadSequences();
        tSequenceLength.setText(String.valueOf(getController().getSequencesLength()));    }


    /**
     * Lors de la mise en pause de l'activité on supprime l'enregistrement du broadcast receiver
     */
    @Override
    protected void onPause() {
        super.onPause();
        broadcast.unRegister();
    }
}

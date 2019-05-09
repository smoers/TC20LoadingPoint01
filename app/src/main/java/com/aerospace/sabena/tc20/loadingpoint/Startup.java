package com.aerospace.sabena.tc20.loadingpoint;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.aerospace.sabena.tc20.loadingpoint.controllers.ProfileSetupController;
import com.aerospace.sabena.tc20.loadingpoint.datawedge.DataWedgeAction;
import com.aerospace.sabena.tc20.loadingpoint.datawedge.DataWedgeExtraData;
import com.aerospace.sabena.tc20.loadingpoint.datawedge.SendDataWedge;
import com.aerospace.sabena.tc20.loadingpoint.listeners.BroadcastReceiverListener;
import com.aerospace.sabena.tc20.loadingpoint.models.Configuration;
import com.aerospace.sabena.tc20.loadingpoint.models.ConfigurationList;
import com.aerospace.sabena.tc20.loadingpoint.models.TaskAutomaticExportData;
import com.aerospace.sabena.tc20.loadingpoint.models.TaskTest;
import com.aerospace.sabena.tc20.loadingpoint.providers.BroadcastReceiverImplementation;
import com.aerospace.sabena.tc20.loadingpoint.providers.ConfigurationStore;
import com.aerospace.sabena.tc20.loadingpoint.views.BarcodeScanner;
import com.aerospace.sabena.tc20.loadingpoint.views.ProfileSetup;
import com.aerospace.sabena.tc20.loadingpoint.views.Setup;

import java.util.ArrayList;
import java.util.Timer;

/**
 * Classe principale de l'application
 * Dans un premier temps celle-ci va s'assurer qu'un profile DataWedge existe pour cette application
 * Si il n'y pas de profile, elle va proposer de le créer et ensuite revenir à l'activité de démarrage.
 */
public class Startup extends AppCompatActivity {

    public static final int REQUEST_CODE_SETUP_PROFILE = 100;
    public static final String LOG_TAG = "LoadingPoint1";
    //Instance du receiver, récupère les données en provenance du DataWedge
    private BroadcastReceiverImplementation broadcast = new BroadcastReceiverImplementation(Startup.this);
    private Timer automaticTask = new Timer();
    private Timer test = new Timer();
    private TaskAutomaticExportData task = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);
        /**
         *  définition du listener pour le contrôle de
         *  l'existance du profil
         */
        broadcast.addBroadcastReceiverListener(new BroadcastReceiverListener() {
            @Override
            public void broadcastReceiver(BroadcastReceiverEvent evt) {
                Intent intent = evt.getIntent();
                if (intent.hasExtra(DataWedgeExtraData.EXTRA_RESULT_GET_PROFILES_LIST)){
                    String[] profiles = intent.getStringArrayExtra(DataWedgeExtraData.EXTRA_RESULT_GET_PROFILES_LIST);
                    Log.d(LOG_TAG, "Profiles" + String.valueOf(profiles.length));
                    //Si le profile n'existe pas lancement de l'activité ProfileSetup
                    if (!isProfileExist(profiles)) {
                        Intent spIntent = new Intent(Startup.this, ProfileSetup.class);
                        startActivityForResult(spIntent, REQUEST_CODE_SETUP_PROFILE);
                    }
                }
            }
        });

        /**
         * Définition du listener pour obtenir le matricule scanné
         */
        broadcast.addBroadcastReceiverListener(new BroadcastReceiverListener() {
            @Override
            public void broadcastReceiver(BroadcastReceiverEvent evt) {
                Log.d(Startup.LOG_TAG,"Start read scanned data");
                Intent intent = evt.getIntent();
                Context context = evt.getContext();
                String action = intent.getAction();
                Log.d(Startup.LOG_TAG, action);
                if(action.equals(context.getResources().getString(R.string.activity_intent_filter_action))){
                    String data = intent.getStringExtra(context.getResources().getString(R.string.datawedge_intent_key_data));
                    Log.d(Startup.LOG_TAG,data);
                    if(isValidRoleNumber(data)){
                        Intent sbIntent = new Intent(context, BarcodeScanner.class);
                        sbIntent.putExtra(context.getResources().getString(R.string.activity_role_number), data);
                        context.startActivity(sbIntent);
                    }
                }
            }
        });

        getResources().getString(R.string.server_id);
        //enregistrement du receiver
        broadcast.register();

        //demande au DataWedge de nous retourner tous les profiles
        SendDataWedge sendDataWedge = new SendDataWedge(Startup.this);
        sendDataWedge.send(DataWedgeAction.ACTION_DATAWEDGE, DataWedgeExtraData.EXTRA_GET_PROFILES_LIST,"");

        //Charge et détermine l'existance d'un setup local
        ConfigurationList configurationList = getConfiguration();
        if (configurationList == null){
            //S'il n'y pas de configurationList local & que le site est indisponible
            //l'application est inutilisable.  Il faut au moins une fois disposer d'un wifi pour charger la config.
            Intent stIntent = new Intent(Startup.this, Setup.class);
            startActivity(stIntent);
        } else {
            Log.d(Startup.LOG_TAG, "Liste des items");
            Log.d(Startup.LOG_TAG, String.valueOf(configurationList.size()));
            //tache automatique d'export
            Configuration[] configurations = configurationList.getConfiguration("automatic_task").getConfigurationValue();
            if(Configuration.getConfiguration(configurations,"enabled").getBooleanValue()){
                task = new TaskAutomaticExportData(Startup.this);
                Long delay = Long.valueOf(Configuration.getConfiguration(configurations,"delay").getIntValue().longValue());
                automaticTask.schedule(task,delay * 1000,delay * 1000);
                //automaticTask.schedule(task,15000,15000);
                Log.d(Startup.LOG_TAG, "Automatic Export Timer is enabled");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        automaticTask.cancel();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        Log.d(LOG_TAG, "onActivityResult");
        if(requestCode == REQUEST_CODE_SETUP_PROFILE){
            if(resultCode == Activity.RESULT_OK){
                AlertDialog.Builder builder = new AlertDialog.Builder(Startup.this);
                builder.setTitle("Info")
                        .setMessage("The profile has been created !")
                        .setCancelable(true)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .create()
                        .show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        broadcast.register();
    }


    @Override
    protected void onPause() {
        super.onPause();
        broadcast.unRegister();
    }

    private boolean isProfileExist(String[] profiles){
        boolean result = false;
        for(int i=0; i<profiles.length;i++) {
            if (profiles[i].equalsIgnoreCase(ProfileSetupController.EXTRA_PROFILENAME)){
                result = true;
            }
        }
        return result;
    }

    private boolean isValidRoleNumber(String roleNumber){
        return roleNumber.matches("00\\d{5}$");
    }

    /**
     * Charge le setup
     * @return
     */
    public ConfigurationList getConfiguration(){
        ConfigurationStore store = new ConfigurationStore(Startup.this);
        if (store.webLoad())
            Log.d(Startup.LOG_TAG,"Setup has been downloaded");
        else
            Log.d(Startup.LOG_TAG,"Setup cannot be downloaded");

        return store.load();
    }

}

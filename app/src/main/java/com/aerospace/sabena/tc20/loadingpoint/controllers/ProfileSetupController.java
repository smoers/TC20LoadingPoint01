package com.aerospace.sabena.tc20.loadingpoint.controllers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.aerospace.sabena.tc20.loadingpoint.datawedge.DataWedgeAction;
import com.aerospace.sabena.tc20.loadingpoint.datawedge.DataWedgeExtraData;
import com.aerospace.sabena.tc20.loadingpoint.datawedge.SendDataWedge;
import com.aerospace.sabena.tc20.loadingpoint.views.ProfileSetup;

public class ProfileSetupController {

    public static final String EXTRA_PROFILENAME = "DWLoadingPoint1";

    private ProfileSetup profileSetup;

    public ProfileSetupController(ProfileSetup profileSetup) {
        this.profileSetup = profileSetup;
        profileSetup.geteProfileName().setText(EXTRA_PROFILENAME);
    }

    public void createProfile(){
        //Instance object sender
        SendDataWedge send = new SendDataWedge(profileSetup);
        //Create profile
        send.send(DataWedgeAction.ACTION_DATAWEDGE, DataWedgeExtraData.EXTRA_CREATE_PROFILE, EXTRA_PROFILENAME);

        // Configure created profile to apply to this app
        Bundle profileConfig = new Bundle();
        profileConfig.putString("PROFILE_NAME", EXTRA_PROFILENAME);
        profileConfig.putString("PROFILE_ENABLED", "true");
        profileConfig.putString("CONFIG_MODE", "CREATE_IF_NOT_EXIST");

        // Configure barcode input plugin
        Bundle barcodeConfig = new Bundle();
        barcodeConfig.putString("PLUGIN_NAME", "BARCODE");
        barcodeConfig.putString("RESET_CONFIG", "true"); //  This is the default
        Bundle barcodeProps = new Bundle();
        barcodeConfig.putBundle("PARAM_LIST", barcodeProps);
        profileConfig.putBundle("PLUGIN_CONFIG", barcodeConfig);

        // Associate profile with this app
        Bundle appConfig = new Bundle();
        appConfig.putString("PACKAGE_NAME", profileSetup.getPackageName());
        appConfig.putStringArray("ACTIVITY_LIST", new String[]{"*"});
        profileConfig.putParcelableArray("APP_LIST", new Bundle[]{appConfig});
        profileConfig.remove("PLUGIN_CONFIG");

        // Apply configs
        // Use SET_CONFIG: http://techdocs.zebra.com/datawedge/latest/guide/api/setconfig/
        send.send(DataWedgeAction.ACTION_DATAWEDGE, DataWedgeExtraData.EXTRA_SET_CONFIG, profileConfig);

        // Configure intent output for captured data to be sent to this app
        Bundle intentConfig = new Bundle();
        intentConfig.putString("PLUGIN_NAME", "INTENT");
        intentConfig.putString("RESET_CONFIG", "true");
        Bundle intentProps = new Bundle();
        intentProps.putString("intent_output_enabled", "true");
        intentProps.putString("intent_action", "com.aerospace.sabena.tc20.loadingpoint.ACTION");
        intentProps.putString("intent_delivery", "2");
        intentConfig.putBundle("PARAM_LIST", intentProps);
        profileConfig.putBundle("PLUGIN_CONFIG", intentConfig);
        send.send(DataWedgeAction.ACTION_DATAWEDGE, DataWedgeExtraData.EXTRA_SET_CONFIG, profileConfig);
        //Reytourne le resultat
        Intent intent = new Intent();
        intent.putExtra("result", true);
        profileSetup.setResult(Activity.RESULT_OK, intent);
        profileSetup.finish();

    }
}

package com.aerospace.sabena.tc20.loadingpoint.views;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.aerospace.sabena.tc20.loadingpoint.R;
import com.aerospace.sabena.tc20.loadingpoint.controllers.ViewInterface;
import com.aerospace.sabena.tc20.loadingpoint.controllers.ProfileSetupController;
import com.aerospace.sabena.tc20.loadingpoint.listeners.OnClickListenerAbstract;

public class ProfileSetup extends AppCompatActivity implements ViewInterface<ProfileSetupController> {

    private EditText eProfileName;
    private Button bCreate;
    private ProfileSetupController profileSetupController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);
        initialize();
    }

    public EditText geteProfileName() {
        return eProfileName;
    }

    public Button getbCreate() {
        return bCreate;
    }

    private void initialize(){

        //Graphique Components
        eProfileName = (EditText) findViewById(R.id.eProfileName);
        bCreate = (Button) findViewById(R.id.bCreate);

        //ViewInterface
        profileSetupController = new ProfileSetupController(ProfileSetup.this);

        bCreate.setOnClickListener(new OnClickListenerAbstract<ProfileSetupController>(profileSetupController) {
            @Override
            public void onClick(View v) {
                controller.createProfile();
            }
        });
    }

    @Override
    public ProfileSetupController getController() {
        return profileSetupController;
    }
}

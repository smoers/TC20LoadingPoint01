package com.aerospace.sabena.tc20.loadingpoint.views;

import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;

import com.aerospace.sabena.tc20.loadingpoint.R;
import com.aerospace.sabena.tc20.loadingpoint.Startup;
import com.aerospace.sabena.tc20.loadingpoint.controllers.ViewInterface;
import com.aerospace.sabena.tc20.loadingpoint.controllers.SequenceManagementController;
import com.aerospace.sabena.tc20.loadingpoint.listeners.OnClickListenerAbstract;
import com.aerospace.sabena.tc20.loadingpoint.models.Sequences;
import com.aerospace.sabena.tc20.loadingpoint.system.Device;

import static com.aerospace.sabena.tc20.loadingpoint.R.mipmap.plug;

/**
 * Cette classe gére la vue avec la liste des séquences
 * Elle permet d'uploaded les séquences vers le serveur web ou de les mettre
 * à disposition d'un PC au travers du cardle
 */
public class SequenceManagement extends AppCompatActivity implements ViewInterface<SequenceManagementController> {

    private SequenceManagementController sequenceManagementController;
    private ListView listSequence;
    private ImageButton bDelete;
    private ImageButton bTransfer;
    private ImageButton bInternetTransfer;
    private Toolbar toolbarSequence;
    private MediaPlayer mediaPlayer;
    private ImageView plug;

    /**
     * Create
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sequence_management);
        initialize();

    }

    /**
     * Initialise la vue
     */
    private void initialize(){
        listSequence = (ListView) findViewById(R.id.listSequence);
        bTransfer = (ImageButton) findViewById(R.id.bTransfer);
        bInternetTransfer = (ImageButton) findViewById(R.id.bInternetTransfer);
        bDelete = (ImageButton) findViewById(R.id.bDelete);
        toolbarSequence = (Toolbar) findViewById(R.id.toolbar_sequence_management);
        setSupportActionBar(toolbarSequence);
        mediaPlayer = MediaPlayer.create(this, R.raw.buttonclick);
        if (Device.isPlugged(SequenceManagement.this)) {
            plug = new ImageView(SequenceManagement.this);
            plug.setImageResource(R.mipmap.plug);
            toolbarSequence.addView(plug);
        }
        sequenceManagementController = new SequenceManagementController(SequenceManagement.this);
        Sequences sequences = sequenceManagementController.getSequences();
        Log.d(Startup.LOG_TAG, "Sequence management : Sequence size " + sequences.size());
        SequenceList sequenceList = new SequenceList(SequenceManagement.this, R.layout.barcode_list, sequences);
        listSequence.setAdapter(sequenceList);
        //Supprime les sequences sélectées
        bDelete.setOnClickListener(new OnClickListenerAbstract<SequenceList>(sequenceList) {
            @Override
            public void onClick(View v) {
                mediaPlayer.start();
                //Création et affichage du message d'alerte
                AlertDialog.Builder builder = new AlertDialog.Builder(SequenceManagement.this);
                builder.setTitle("Warning")
                        .setMessage("Are you sure you want to delete the selected Sequences?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //récupère la liste des sequences à supprimer depuis l'objet SequenceManagementController
                                Sequences removeSequences = controller.getRemoveSequences();
                                //On demande au controller d'effectuer l'operation
                                getController().removeSequence(removeSequences);
                                //On retire les seqduences de l'objet SequenceList
                                controller.removeSequences();
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
        //Transfert les données de InternalStore vers l'ExternalStore afin de les rendre disponible via USB
        bTransfer.setOnClickListener(new OnClickListenerAbstract<SequenceList>(sequenceList){
            @Override
            public void onClick(View v) {
                mediaPlayer.start();
                if (getController().transfer()){
                    controller.clear(); // vide l'interface utilisateur
                }
            }
        });

        /**
         * Transfert les données vers le serveur web
         */
        bInternetTransfer.setOnClickListener(new OnClickListenerAbstract<SequenceList>(sequenceList) {
            @Override
            public void onClick(View v) {
                mediaPlayer.start();
                if (getController().internetTransfer())
                    controller.clear();
            }
        });

    }

    @Override
    public SequenceManagementController getController() {
        return sequenceManagementController;
    }
}

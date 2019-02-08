package com.aerospace.sabena.tc20.loadingpoint.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.aerospace.sabena.tc20.loadingpoint.R;
import com.aerospace.sabena.tc20.loadingpoint.Startup;
import com.aerospace.sabena.tc20.loadingpoint.models.Barcode;
import com.aerospace.sabena.tc20.loadingpoint.models.Sequence;
import com.aerospace.sabena.tc20.loadingpoint.models.Sequences;

import java.util.ArrayList;

/**
 * Cette classe se charge de l'affichage des Sequences sauvegardées
 * Elle permmet également de supprimer une sequence
 */
public class SequenceList extends ArrayAdapter<Sequence> {

    private int resource;
    private Context context;
    /**
     * Contient la liste des sequences qui doivent être supprimées
     * par une action sur le bouton Remove
     */
    private Sequences removeSequences = new Sequences();

    /**
     *
     * @param context
     * @param resource
     * @param sequences
     */
    public SequenceList(@NonNull Context context, int resource, Sequences sequences) {
        super(context, resource, sequences);
        this.resource = resource;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Log.d(Startup.LOG_TAG,"Adapter View " + String.valueOf(position));
        //Récupère la Sequence
        final Sequence sequence = getItem(position);
        //Récupére la vue depuis le l'objet context si le convertView est null
        if( convertView == null){
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(resource, parent, false);
        }
        //Défini la date et le matricule
        TextView tDate = (TextView) convertView.findViewById(R.id.tDate);
        TextView tRoleNumber = (TextView) convertView.findViewById(R.id.tRoleNumber);
        //Défini le switch
        Switch sDelete = (Switch) convertView.findViewById(R.id.sDelete);
        //met en place un listener sur le switch
        sDelete.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(Startup.LOG_TAG, "Switch listener, position " + position);
                Sequence deleteSequence = getItem(position);

                if(isChecked){
                    //Si le switch est checked on place la sequence dans la table des seuences à supprimer
                    removeSequences.add(deleteSequence);
                } else {
                    //Si le switch est unchecked on s'assure que la sequence existe dans la liste
                    if (removeSequences.contains(deleteSequence)){
                        //si elle existe on la supprime
                        removeSequences.remove(deleteSequence);
                    }
                }
                Log.d(Startup.LOG_TAG, "Delete Sequence table size " + String.valueOf(removeSequences.size()));
            }
        });

        //Affiche la date
        tDate.setText(sequence.getDate().toString());
        //affiche le matricule
        tRoleNumber.setText(sequence.getOwner().getRoleNumber());
        //Récupère la liste des barcode de la sequence
        ArrayList<Barcode> barcodes = sequence.getBarcodes();
        //Récupère le layout qui doit contenir les textview avec les barcodes
        LinearLayout layout = convertView.findViewById(R.id.layout_barcode);
        //afin d'eviter les doublons
        layout.removeAllViews();
        //Crée un textview pour chaque barcode de lla sequence encours de traitement
        for (Barcode barcode : barcodes){
            TextView textView = new TextView(getContext());
            textView.setText(barcode.getCode());
            textView.setTextSize(10);
            layout.addView(textView,LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        }

        return convertView;
    }

    /**
     * Retourne la liste des sequences à supprimer
     * @return
     */
    public Sequences getRemoveSequences() {
        return removeSequences;
    }

    /**
     * Supprime les sequences de l'objet SequenceList
     */
    public void removeSequences(){
        for (Sequence sequence : removeSequences){
            remove(sequence);
        }
        notifyDataSetChanged();
    }

}

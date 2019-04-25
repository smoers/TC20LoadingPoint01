package com.aerospace.sabena.tc20.loadingpoint.views;

import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.aerospace.sabena.tc20.loadingpoint.R;
import com.aerospace.sabena.tc20.loadingpoint.Startup;
import com.aerospace.sabena.tc20.loadingpoint.models.Configuration;
import com.aerospace.sabena.tc20.loadingpoint.models.ConfigurationList;
import com.aerospace.sabena.tc20.loadingpoint.providers.ConfigurationStore;

import java.util.Iterator;

public class Setup extends AppCompatActivity {

    private TableLayout tblSetup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        initialize();
    }

    private void initialize(){
        tblSetup = (TableLayout) findViewById(R.id.tblSetup);
        ConfigurationStore store = new ConfigurationStore(Setup.this);
        Iterator<Configuration> iterator = store.load().iterator();
        while (iterator.hasNext()){
            Configuration config = iterator.next();
            TableRow rowkey = new TableRow(Setup.this);
            TextView tKey = new TextView(Setup.this);
            tKey.setTextColor(Color.GRAY);
            tKey.setTextSize(12);
            tKey.setPaintFlags(Paint.FAKE_BOLD_TEXT_FLAG);
            TableRow rowvalue = new TableRow(Setup.this);
            TextView tValue = new TextView(Setup.this);
            tValue.setTextSize(10);
            tValue.setTextColor(Color.RED);
            rowkey.addView(tKey, LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            rowvalue.addView(tValue,LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            tblSetup.addView(rowkey,LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            tblSetup.addView(rowvalue,LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            tKey.setText(config.getKey());
            tValue.setText(config.toString());
        }

    }
}

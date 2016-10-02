package com.basmapp.marshal.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.basmapp.marshal.BaseActivity;
import com.basmapp.marshal.R;

import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

public class WrongClock extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wrong_clock);

        Button adjustDate = (Button) findViewById(R.id.close);
        adjustDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setClock();
            }
        });

        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
        DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getApplicationContext());

        TextView wrongDate = (TextView) findViewById(R.id.clock_wrong_date);
        wrongDate.setText(String.format(getString(R.string.clock_wrong_report_current_date_time),
                dateFormat.format(new Date()) + ", " + timeFormat.format(new Date()),
                (TimeZone.getDefault().getDisplayName())));
    }

    @Override
    public void onBackPressed() {
        setClock();
    }

    public void setClock() {
        startActivity(new Intent(android.provider.Settings.ACTION_DATE_SETTINGS));
        finishAffinity();
    }
}

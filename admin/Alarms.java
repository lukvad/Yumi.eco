package com.lukvad.admin;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;


public class Alarms extends AppCompatActivity {
Button nCancel;
DatabaseReference nAlarms;
TableLayout tableLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarms);
        nCancel = findViewById(R.id.cancel);
        nAlarms = FirebaseDatabase.getInstance().getReference().child("alarms");

        nAlarms.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i = 0;
                tableLayout = findViewById(R.id.tableLayout);
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    final String scooter = childDataSnapshot.getKey();
                    if (scooter.length()<3) {
                        String alarms = "";
                        String timer = "";
                        for (DataSnapshot al : childDataSnapshot.getChildren()) {
                            if(al.getKey().equals("time"))
                                timer = al.getValue(String.class);
                            else if(al.getKey().equals("state")){}
                            else
                                alarms = alarms + " | " + al.getKey();
                        }
                        alarms = scooter + " : " + alarms;
                            TextView alarm = new TextView(Alarms.this);
                            TextView time = new TextView(Alarms.this);
                            time.setTypeface(null, Typeface.BOLD);
                            time.setText(timer);
                            time.setTextSize(12);
                            alarm.setTextSize(17);
                            alarm.setPadding(10, 50, 10, 50);    //left,top,right,bottom
                            time.setPadding(15, 50, 15, 50);    //left,top,right,bottom
                            alarm.setText(alarms);

                             final   TableRow row = new TableRow(Alarms.this);
                            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
                            row.setLayoutParams(lp);
                            row.addView(alarm);
                            row.addView(time);
                            row.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    nAlarms.child(scooter).removeValue();
                                tableLayout.removeView(row);
                                }
                            });
                            tableLayout.addView(row);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        nCancel = findViewById(R.id.cancel);
        nCancel.setText("WRÓĆ");
        nCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}

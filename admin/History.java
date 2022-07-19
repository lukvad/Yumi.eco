package com.lukvad.scooter;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;

import static com.lukvad.scooter.MapsActivity.Uid;


public class History extends AppCompatActivity {
Button nCancel;
LinearLayout nLinearLayout;
DatabaseReference nhistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        nLinearLayout = findViewById(R.id.tableLayout);
        nhistory = FirebaseDatabase.getInstance().getReference("service/history").child(Uid);
        nhistory.orderByChild("timestamp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i = 0;
                TableLayout tableLayout = findViewById(R.id.tableLayout);
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    String fDate = childDataSnapshot.getKey();
                    Long fCharge = (Long) dataSnapshot.child(fDate).child("duration").getValue();
                    String fCost = (String)dataSnapshot.child(fDate).child("cost").getValue();
                    String fName = (String) dataSnapshot.child(fDate).child("scooterName").getValue();
                    double aCharge = (double)fCharge/60000;
                    int iCharge = (int)Math.ceil(aCharge);
                    String nDate[]= fDate.split("-");
                    TextView date = new TextView(History.this);
                    TextView time = new TextView(History.this);
                    TextView sName = new TextView (History.this);
                    date.setTypeface(null, Typeface.BOLD);
                    TextView charge = new TextView(History.this);
                    if (nDate[2].length()==1){
                        nDate[2]= "0"+nDate[2];
                    }
                    date.setPadding(40, 50, 20, 50);    //left,top,right,bottom
                    charge.setPadding(170, 50, 20, 50);    //left,top,right,bottom
                    sName.setPadding(140, 50, 50, 50);    //left,top,right,bottom
                    date.setText(nDate[0]+ "-" +nDate[1] + "-" + nDate[2]);
                    DecimalFormat df = new DecimalFormat("#.##");
                    charge.setText(iCharge +"''" + "    " + fCost + " zł");
                    sName.setText("#"+fName);
                    TableRow row= new TableRow(History.this);
                    TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
                    row.setLayoutParams(lp);
                    row.addView(date);
                    row.addView(sName);
                    row.addView(charge);
                    if(i % 2 == 1) {
                        row.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDarkGrey));
                    }

                    tableLayout.addView(row);
                    i++;
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        nCancel = findViewById(R.id.cancel);
        nCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}

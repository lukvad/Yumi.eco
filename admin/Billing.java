package com.lukvad.scooter;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
import java.util.Locale;

import static com.lukvad.scooter.MapsActivity.Uid;


public class Billing extends AppCompatActivity {
Button nCancel;
DatabaseReference nhistory,nuser;
TextView sBalance, sTime, date, charge, sName, sNumber;
Double balance=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing);
        sBalance = findViewById(R.id.sBalance);
        sTime = findViewById(R.id.time);
        date = findViewById(R.id.date);
        charge = findViewById(R.id.charge);
        sName = findViewById(R.id.sName);
        sNumber = findViewById(R.id.sNumber);
        nuser = FirebaseDatabase.getInstance().getReference("service/users").child(Uid);
        nhistory = FirebaseDatabase.getInstance().getReference("service/history").child(Uid);
        nuser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                balance = dataSnapshot.child("balance").getValue(Double.class);
                sBalance.setText(balance+" zł");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        nhistory.orderByChild("timestamp").limitToFirst(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i = 0;
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    String fDate = childDataSnapshot.getKey();
                    Long fCharge = (Long) dataSnapshot.child(fDate).child("duration").getValue();
                    String fCost = (String)dataSnapshot.child(fDate).child("cost").getValue();
                    String fName = (String) dataSnapshot.child(fDate).child("scooterName").getValue();
                    double aCharge = (double)fCharge/60000;
                    int iCharge = (int)Math.ceil(aCharge);
                    String nDate[]= fDate.split("-");
                    if (nDate[2].length()==1){
                        nDate[2]= "0"+nDate[2];
                    }
                    sTime.setText(nDate[3]+":"+nDate[4]);
                    date.setText(nDate[0]+ "-" +nDate[1] + "-" + nDate[2]);
                    charge.setText(fCost);//TODO
                    sNumber.setText("#"+fName);
                    //TODO
                    sName.setText("GD 524XT");
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
                Intent intent = new Intent(Billing.this, MapsActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}

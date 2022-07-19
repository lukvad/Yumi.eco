package com.lukvad.admin;

import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;

public class Transaction extends AppCompatActivity {
    private DatabaseReference ntransactions;
    private String user;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);
        if(savedInstanceState==null) {
            Bundle register = getIntent().getExtras();
            if (register != null) {
                user = register.getString("user");
            }
            else {
                user= (String) savedInstanceState.getSerializable("user");
            }
        }
        ntransactions = FirebaseDatabase.getInstance().getReference("service/transaction").child(user);
        button = findViewById(R.id.button);
        ntransactions.orderByChild("timestamp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i = 0;
                TableLayout tDate = findViewById(R.id.tDate);
                TableLayout tName = findViewById(R.id.tName);
                TableLayout tCharge = findViewById(R.id.tCharge);
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    String fDate = childDataSnapshot.getKey();
                    String fId = (String)dataSnapshot.child(fDate).child("id").getValue();
                    Double fPayment = dataSnapshot.child(fDate).child("payment").getValue(Double.class);
                    String nDate[]= fDate.split(" ");
                    TextView date = new TextView(Transaction.this);
                    TextView charge = new TextView(Transaction.this);
                    TextView sName = new TextView(Transaction.this);
                    TextView hour = new TextView(Transaction.this);
                    date.setTypeface(null, Typeface.BOLD);

                    date.setPadding(40,50,10,50);
                    charge.setPadding(10,50,40,50);
                    sName.setPadding(10,50,10,50);
                    sName.setGravity(Gravity.CENTER_HORIZONTAL);
                    TableRow rDate = new TableRow(Transaction.this);
                    date.setText(nDate[0]);
                    rDate.addView(date);
                    rDate.addView(hour);
                    DecimalFormat df = new DecimalFormat("#.##");
                    charge.setText(fPayment + " zł");
                    sName.setText(fId);

                    tCharge.addView(charge);
                    tDate.addView(rDate);
                    tName.addView(sName);
                    i++;
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}

package com.lukvad.scooter;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Balance extends AppCompatActivity {
TextView avBalance, topUp;
Button bTopUp;
String payment = "5";
EditText editText;
DatabaseReference nbalance;
static String  name, email, Uid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance);
        avBalance = findViewById(R.id.avBalance);
        bTopUp = findViewById(R.id.payment);
        editText = findViewById(R.id.editText);
        Uid = FirebaseAuth.getInstance().getUid();
        nbalance = FirebaseDatabase.getInstance().getReference().child("service/users").child(Uid);
        nbalance.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserInformation userInformation = dataSnapshot.getValue(UserInformation.class);
                email = userInformation.email;
                name = userInformation.firstname +" "+userInformation.surname;
                int a = (int) Math.floor(userInformation.balance);
                int b = (int)Math.floor(100*(userInformation.balance - a));
                avBalance.setText(String.valueOf(a)+" zł");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        bTopUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                payment = editText.getText().toString();
                Intent intent = new Intent(Balance.this, PaymentTpay.class);
                intent.putExtra("payment",payment);
                intent.putExtra("email", email);
                intent.putExtra("name", name);
                startActivity(intent);
            }
        });
    }
}

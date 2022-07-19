package com.lukvad.admin;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

public class UserProfile extends AppCompatActivity {
private String iUser;
private DatabaseReference nuser;
private Button button,clear;
private TextView tName, tEmail, tAddress, tCharge, tScooter, tBalance, tId,tAlert, tFin, tUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        tName = findViewById(R.id.fullName);
        tEmail = findViewById(R.id.email);
        clear = findViewById(R.id.clear);
        tAddress = findViewById(R.id.address);
        button = findViewById(R.id.button);
        tCharge = findViewById(R.id.charge);
        tScooter = findViewById(R.id.scooter);
        tBalance = findViewById(R.id.balance);
        tId = findViewById(R.id.id);
        tAlert = findViewById(R.id.alert);
        tFin = findViewById(R.id.fIn);
        tUser = findViewById(R.id.user);

        if(savedInstanceState==null) {
            Bundle userProfile = getIntent().getExtras();
            if (userProfile != null) {
                iUser = userProfile.getString("user");
            }
            else {
                iUser= (String) savedInstanceState.getSerializable("user");
            }
        }
        nuser = FirebaseDatabase.getInstance().getReference("service/users").child(iUser);

        tUser.setText(iUser);
        tUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(UserProfile.this, iUser, Toast.LENGTH_SHORT).show();
                setClipboard(UserProfile.this, iUser);
            }
        });
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nuser.child("scooterName").setValue("");
            }
        });
        nuser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final UserInformation userInformation = dataSnapshot.getValue(UserInformation.class);
                    tName.setText("Imię i nazwisko : " + userInformation.firstname + " "+userInformation.surname);
                    tName.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast.makeText(UserProfile.this, userInformation.firstname + " "+ userInformation.surname, Toast.LENGTH_SHORT).show();
                            setClipboard(UserProfile.this, userInformation.firstname + " "+ userInformation.surname);
                        }
                    });
                    tEmail.setText("email : " + userInformation.email);
                    tEmail.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast.makeText(UserProfile.this, userInformation.email, Toast.LENGTH_SHORT).show();
                            setClipboard(UserProfile.this, userInformation.email);
                        }
                    });
                    tAddress.setText("Adres : "+ userInformation.address);
                    tAddress.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast.makeText(UserProfile.this, userInformation.address, Toast.LENGTH_SHORT).show();
                            setClipboard(UserProfile.this, userInformation.address);
                        }
                    });
                    final String phone = dataSnapshot.child("verPhone").getValue(String.class);
                    if(!userInformation.alert.equals(""))
                        tAlert.setText("alert : " + userInformation.alert);
                    else
                        tAlert.setText("alert : brak");
                    if(!userInformation.scooterName.equals(""))
                        tScooter.setText("skuter : " + userInformation.scooterName);
                    else
                        tScooter.setText("skuter : brak");
                    tBalance.setText("konto : " + userInformation.balance.toString());
                    tCharge.setText("Minuty dzisiaj : " + userInformation.charge);
                    tId.setText("Dowód osobisty : "+ userInformation.idno);
                    tId.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast.makeText(UserProfile.this, userInformation.idno, Toast.LENGTH_SHORT).show();
                            setClipboard(UserProfile.this, userInformation.idno);
                        }
                    });
                    tFin.setText("ostatnie wyłączenie : "+ userInformation.fIn);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
                        startActivity(intent);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    private void setClipboard(Context context, String text) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
        clipboard.setPrimaryClip(clip);
    }
}

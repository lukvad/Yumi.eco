package com.lukvad.admin;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.j256.ormlite.field.types.DateTimeType;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ScooterProfile extends AppCompatActivity {
private String scooter, user;
private DatabaseReference nscooter, nadmin, ntime, ncontrol;
private Button bAlarm,bAlarmFin, bFin, bHistory, bClear, bTrunk, bUser;
private TextView tScooter, tHelmet, tCheck, tGps ;
private ValueEventListener eventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_scooter);
        bAlarm = findViewById(R.id.alarm);
        bFin = findViewById(R.id.fin);
        bHistory = findViewById(R.id.history);
        bAlarmFin = findViewById(R.id.alarmOff);
        bClear = findViewById(R.id.Uid);
        bTrunk = findViewById(R.id.trunk);
        bUser = findViewById(R.id.userid);
        tHelmet = findViewById(R.id.helmet);
        tScooter = findViewById(R.id.scooter);
        tCheck = findViewById(R.id.check);
        tGps = findViewById(R.id.gps);

        if(savedInstanceState==null) {
            Bundle userProfile = getIntent().getExtras();
            if (userProfile != null) {
                scooter = userProfile.getString("scooter");
            }
            else {
                scooter= (String) savedInstanceState.getSerializable("scooter");
            }
        }
        nscooter = FirebaseDatabase.getInstance().getReference("service/scooters").child(scooter);
        nadmin = FirebaseDatabase.getInstance().getReference("admin").child(scooter);
        ntime = FirebaseDatabase.getInstance().getReference("time").child(scooter);
        ncontrol = FirebaseDatabase.getInstance().getReference("control").child(scooter);

        ncontrol.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                tCheck.setText("łączenie ze skuterem : " + dataSnapshot.child("engine").getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
         });
        nscooter.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ScooterInformation scooterInformation = dataSnapshot.getValue(ScooterInformation.class);
                tScooter.setText(scooterInformation.name);
                tHelmet.setText("Kufer/kaski : " + scooterInformation.engine);

                user = scooterInformation.userKey;
                tGps.setText("Obecny stan skutera : " + scooterInformation.state);
                if(!user.equals(""))
                    bUser.setText("Użytkownik");
                else
                    bUser.setText("Ostatni Użytkownik");
                    bUser.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(!user.equals("")) {
                                Intent intent = new Intent(ScooterProfile.this, UserProfile.class);
                                intent.putExtra("user", user);
                                startActivity(intent);
                                finish();
                            }else {
                                getHistory();
                            }
                        }
                    });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        bTrunk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nscooter.child("state").setValue("*hE&");
            }
        });
        bClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nscooter.child("userKey").setValue("");
                nscooter.child("check").removeValue();
                nscooter.child("feedback").removeValue();
            }
        });
        bAlarm.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                popUp();
                return false;
            }
        });
        bHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detailPopUp();
            }
        });
        bFin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nscooter.child("temp").setValue(true);
                nscooter.child("state").setValue("*fIn&");
                eventListener = nscooter.child("state").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String state = dataSnapshot.getValue(String.class);
                        Log.e("UWAGA", state);
                        if (state.equals("*oF&")) {
                            nscooter.child("temp").removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
        bAlarmFin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nscooter.child("state").setValue("*aLoF&");
            }
        });
    }


    private void popUp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Kontrola alarmu!")
                .setPositiveButton("Załącz alarm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        nscooter.child("state").setValue("*aLoN&");
                    }
                }).setNeutralButton("Zakończ alarm", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                nscooter.child("state").setValue("*aLoF&");
            }
        });
        builder.create().show();
    }


    private void detailPopUp() {
        final EditText input = new EditText(ScooterProfile.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(input);
        builder.setTitle("Historia")
                .setMessage("Wielokrotność 30 sekund")
                .setPositiveButton("zobacz", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Integer time = Integer.valueOf(input.getText().toString());
                        Intent intent = new Intent(ScooterProfile.this, FullMap2.class);
                        intent.putExtra("scooter", scooter);
                        intent.putExtra("time", time);
                        startActivity(intent);
                    }
                });
        builder.create().show();
    }


    private void getHistory() {

        nadmin.orderByChild("state").equalTo("*oN&").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    user = childDataSnapshot.child("userKey").getValue(String.class);
               }
                if (user != null) {
                    Intent intent = new Intent(ScooterProfile.this, UserProfile.class);
                    intent.putExtra("user", user);
                    startActivity(intent);
                    finish();
                } else
                    Toast.makeText(ScooterProfile.this, "brak usera", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        nscooter.child("state").removeEventListener(eventListener);
    }
}

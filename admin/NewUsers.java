package com.lukvad.admin;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class NewUsers extends AppCompatActivity {
    private DatabaseReference nusers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newusers);
        nusers = FirebaseDatabase.getInstance().getReference("service/users");


        nusers.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                TableLayout tableLayout = findViewById(R.id.tableLayout);
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    final String fUser = childDataSnapshot.getKey();
                    Log.d("UWAGA", fUser);
                    try {
                        String fName = childDataSnapshot.child("firstname").getValue(String.class) + " " + childDataSnapshot.child("surname").getValue(String.class);
                        final String fScooter = childDataSnapshot.child("scooterName").getValue(String.class);
                        TextView user = new TextView(NewUsers.this);
                        TextView name = new TextView(NewUsers.this);
                        TextView scooter = new TextView(NewUsers.this);
                        name.setTypeface(null, Typeface.BOLD);
                        user.setTextSize(8);
                        user.setPadding(10, 50, 10, 50);    //left,top,right,bottom
                        name.setPadding(15, 50, 15, 50);    //left,top,right,bottom
                        scooter.setPadding(15, 50, 15, 50);    //left,top,right,bottom
                        user.setText(fUser);
                        name.setText(fName);
                        scooter.setText(fScooter);
                        TableRow row = new TableRow(NewUsers.this);
                        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
                        row.setLayoutParams(lp);
                        row.addView(user);
                        row.addView(name);
                        row.addView(scooter);
                        row.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(NewUsers.this, Verification.class);
                                intent.putExtra("user", fUser);
                                startActivity(intent);
                            }
                        });
                        tableLayout.addView(row);

                    } catch (Exception io) {

                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}


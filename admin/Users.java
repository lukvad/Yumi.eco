package com.lukvad.admin;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.j256.ormlite.stmt.query.In;

import java.text.DecimalFormat;

public class Users extends AppCompatActivity {
    private DatabaseReference nusers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        nusers = FirebaseDatabase.getInstance().getReference("Users");
        nusers.orderByChild("email").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                    int i = 0;
                    TableLayout tableLayout = findViewById(R.id.tableLayout);
                    for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                        final String fUser = childDataSnapshot.getKey();
                        try {
                            String fEmail  = childDataSnapshot.child("email").getValue(String.class);
                            String url  = childDataSnapshot.child("url").getValue(String.class);

                            TextView email = new TextView(Users.this);
                            TextView photo = new TextView(Users.this);
                            photo.setTypeface(null, Typeface.BOLD);
                            email.setTextSize(8);
                            if(url!=null&&url.length()>20)
                                photo.setText("dodano zdjęcie");
                            email.setPadding(10, 50, 10, 50);    //left,top,right,bottom
                            photo.setPadding(15, 50, 15, 50);    //left,top,right,bottom
                            email.setText(fEmail);
//                            TableRow row = new TableRow(Users.this);
//                            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
//                            row.setLayoutParams(lp);
//                            row.addView(email);


                            //                            email.setTextSize(8);

                            TableRow row = new TableRow(Users.this);

                            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
                            row.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(Users.this, Verification.class);
                                    intent.putExtra("User", fUser);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                            if(childDataSnapshot.child("state").exists()) {
                                String state = childDataSnapshot.child("state").getValue(String.class);
                                if(state.equals("ignore")){
                                    row.setBackgroundColor(getResources().getColor(R.color.colorGrey));
                                }
                            }
                            row.setLayoutParams(lp);
                            row.addView(email);
                            row.addView(photo);
                            tableLayout.addView(row);
                            i++;
                        }catch (Exception io){

                        }
                    }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

}

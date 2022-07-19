package com.lukvad.scooter;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.j256.ormlite.stmt.query.In;

import org.w3c.dom.Text;

public class VerificationActivity extends AppCompatActivity {
    protected TextView email;
    protected Button nBack;
    protected FirebaseAuth nAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_verification);
        email = findViewById(R.id.email);
        nBack = findViewById(R.id.back);
        nBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                        Intent intent = new Intent(VerificationActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                }
        });
    }
    @Override
    protected void onPause() {
        super.onPause();
            nAuth.signOut();
    }

}
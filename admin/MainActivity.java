package com.lukvad.scooter;

import android.*;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

import static com.paypal.android.sdk.cu.s;

public class MainActivity extends AppCompatActivity {
    private EditText nEmail, nPassword;
    private Button nLogin;
    private TextView nRegister, nRecover;
    private FirebaseAuth nAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
//    static Boolean isEmailVerified=false;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nAuth = FirebaseAuth.getInstance();
        nEmail = findViewById(R.id.email);
        nPassword = findViewById(R.id.password);
        nLogin = findViewById(R.id.login);
        nRegister = findViewById(R.id.register);
        nRecover = findViewById(R.id.recover);
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user!=null) {
//                    isEmailVerified = user.isEmailVerified();
//                    if(isEmailVerified) {
                        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                        startActivity(intent);
                        finish();
                        return;
//                    }
//                    else Toast.makeText(MainActivity.this, getResources().getString(R.string.loginNotVerified),Toast.LENGTH_LONG).show();
                }
            }
        };
        nRecover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Email.class);
                startActivity(intent);
                finish();
            }
        });
        nRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });

        nLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = nEmail.getText().toString();
                final String password = nPassword.getText().toString();
                    if((!email.equals(""))&&(!password.equals(""))) {

                        nAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(MainActivity.this, getResources().getString(R.string.loginBad), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    else Toast.makeText(MainActivity.this, getResources().getString(R.string.loginBad), Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        nAuth.addAuthStateListener(firebaseAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        nAuth.removeAuthStateListener(firebaseAuthListener);
//        if(!isEmailVerified){
//            nAuth.signOut();
//            finish();
//        }
    }








}

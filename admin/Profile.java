package com.lukvad.scooter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;



public class Profile extends AppCompatActivity {
Button nBalance, nHistory;
Button changePassword;
FirebaseAuth auth;
View profileLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        profileLayout = findViewById(R.id.profileLayout);
        changePassword = findViewById(R.id.changePassword);
        nBalance = findViewById(R.id.balance);
        nHistory = findViewById(R.id.history);
        auth = FirebaseAuth.getInstance();
        nBalance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Profile.this, Balance.class);
                startActivity(intent);
            }
        });
        nHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Profile.this, History.class);
                startActivity(intent);
            }
        });

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Profile.this, Password.class);
                startActivity(intent);

            }
        });
    }
}

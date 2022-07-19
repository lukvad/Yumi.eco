package eco.yumi;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import eco.yumi.R;


public class Profile extends AppCompatActivity {
ImageButton nBalance, nHistory, nTransactions, nDetails, back, nPhone;
String Uid;
TextView userEmail;
View profileLayout;
FirebaseUser user;
DatabaseReference nuser;
private static Intent payment = new Intent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        user = FirebaseAuth.getInstance().getCurrentUser();
        profileLayout = findViewById(R.id.profileLayout);
        nPhone = findViewById(R.id.phone);
        nBalance = findViewById(R.id.balance);
        nHistory = findViewById(R.id.history);
        userEmail = findViewById(R.id.user_email);
        userEmail.setText(user.getEmail());
        nTransactions = findViewById(R.id.billing);
        back = findViewById(R.id.menu);
        nDetails = findViewById(R.id.details);
        Uid = FirebaseAuth.getInstance().getUid();
        nuser = FirebaseDatabase.getInstance().getReference("service/users").child(Uid);
        nuser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String pay =  dataSnapshot.child("pay").getValue(String.class);
                if(pay.equals("account")){
                    nBalance.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            payment = new Intent(Profile.this, Balance.class);
                            startActivity(payment);
                        }
                    });
                }
                if(pay.equals("card")){
                    nBalance.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            payment = new Intent(Profile.this, Card.class);
                            startActivity(payment);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        nHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Profile.this, History.class);
                startActivity(intent);
            }
        });
        nTransactions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Profile.this, Transactions.class);
                startActivity(intent);
            }
        });
        nDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Profile.this, Details.class);
                startActivity(intent);
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        nPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phone = "+48577711733";
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
                startActivity(intent);
            }
        });

    }
}

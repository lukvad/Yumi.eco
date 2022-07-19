package eco.yumi;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;

import eco.yumi.R;

public class Balance extends AppCompatActivity {
TextView account, topUp;
Button bTopUp, bCard;
ImageButton back, nPhone;
String payment = "10";
SeekBar seekBar;
DatabaseReference nbalance, nuser, fMax;
static String  name, email, Uid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance);
        account = findViewById(R.id.account);
        topUp = findViewById(R.id.topUp);
        nPhone = findViewById(R.id.phone);
        bTopUp = findViewById(R.id.payment);
        back = findViewById(R.id.menu);
        bCard = findViewById(R.id.bCard);
        seekBar = findViewById(R.id.seekBar);
        Uid = FirebaseAuth.getInstance().getUid();
        bCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Balance.this, Card.class);
                startActivity(intent);
                finish();
            }
        });
        nbalance = FirebaseDatabase.getInstance().getReference().child("service/users").child(Uid);
        nbalance.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserInformation userInformation = dataSnapshot.getValue(UserInformation.class);
                email = userInformation.email;
                name = userInformation.firstname + " " + userInformation.surname;
                double a = userInformation.balance;
                DecimalFormat sf = new DecimalFormat("0.00");
                String bal = sf.format(a);
                account.setText(bal+" zł");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int a = i*2;
                if(a<20){
                    a=20;
                }
                payment = String.valueOf(a);
                topUp.setText(payment);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

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
        bTopUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Balance.this, PaymentTpay.class);
                intent.putExtra("payment",payment);
                intent.putExtra("email", email);
                intent.putExtra("name", name);
                startActivity(intent);
            }
        });
    }
}

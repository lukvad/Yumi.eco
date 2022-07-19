package eco.yumi;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;


public class Details extends AppCompatActivity {
ImageButton nCancel, nPhone;
DatabaseReference nDetails;
TextView Firstname, Surname, email, idno, address, phone, password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        String Uid = FirebaseAuth.getInstance().getUid();
        Firstname = findViewById(R.id.firstName);
        Surname = findViewById(R.id.surName);
        email = findViewById(R.id.email);
        idno = findViewById(R.id.idno);
        address = findViewById(R.id.address);
        nPhone = findViewById(R.id.phone);
        nCancel = findViewById(R.id.menu);
        phone = findViewById(R.id.phoneno);
        password = findViewById(R.id.approvePassword);
        nDetails = FirebaseDatabase.getInstance().getReference("service/users").child(Uid);
        nDetails.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                    UserInformation userInformation = dataSnapshot.getValue(UserInformation.class);
                    Firstname.setText(userInformation.firstname);
                    Surname.setText(userInformation.surname);
                    email.setText(userInformation.email);
                    if(dataSnapshot.child("verPhone").exists()){
                        phone.setText(dataSnapshot.child("verPhone").getValue(String.class));
                    }else{
                        phone.setText(userInformation.phone);
                    }
                    address.setText(userInformation.address);
                    idno.setText(userInformation.idno);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Details.this, Password.class);
                startActivity(intent);
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
        nCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}

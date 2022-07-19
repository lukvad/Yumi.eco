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
import android.widget.TableRow;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.text.DecimalFormat;


public class Transactions extends AppCompatActivity {
ImageButton nCancel, nPhone;
DatabaseReference ntransactions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);
        String Uid = FirebaseAuth.getInstance().getUid();
        nPhone = findViewById(R.id.phone);
        ntransactions = FirebaseDatabase.getInstance().getReference("service/transaction").child(Uid);
        ntransactions.orderByChild("timestamp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i = 0;
                TableLayout tDate = findViewById(R.id.tDate);
                TableLayout tName = findViewById(R.id.tName);
                TableLayout tCharge = findViewById(R.id.tCharge);
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    String fDate = childDataSnapshot.getKey();
                    String fId = (String)dataSnapshot.child(fDate).child("id").getValue();
                    Double fPayment = dataSnapshot.child(fDate).child("payment").getValue(Double.class);
                    String nDate[]= fDate.split(" ");
                    TextView date = new TextView(Transactions.this);
                    TextView charge = new TextView(Transactions.this);
                    TextView sName = new TextView(Transactions.this);
                    TextView hour = new TextView(Transactions.this);
                    date.setTypeface(null, Typeface.BOLD);

                    date.setPadding(40,50,10,50);
                    charge.setPadding(10,50,40,50);
                    sName.setPadding(10,50,10,50);
                    sName.setGravity(Gravity.CENTER_HORIZONTAL);
                    TableRow rDate = new TableRow(Transactions.this);
                    date.setText(nDate[0]);
                    rDate.addView(date);
                    rDate.addView(hour);
                    DecimalFormat df = new DecimalFormat("#.##");
                    charge.setText(fPayment + " zł");
                    sName.setText(fId);

                    tCharge.addView(charge);
                    tDate.addView(rDate);
                    tName.addView(sName);
                    i++;
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

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
        nCancel = findViewById(R.id.menu);
        nCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}

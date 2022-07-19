package eco.yumi;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;


public class History extends AppCompatActivity {
ImageButton nCancel, nPhone;
DatabaseReference nhistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        String Uid = FirebaseAuth.getInstance().getUid();
        nPhone = findViewById(R.id.phone);
        nhistory = FirebaseDatabase.getInstance().getReference("service/history").child(Uid);
        nhistory.orderByChild("timestamp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i = 0;
                TableLayout tDate = findViewById(R.id.tDate);
                TableLayout tName = findViewById(R.id.tName);
                TableLayout tCharge = findViewById(R.id.tCharge);
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    String fDate = childDataSnapshot.getKey();
                    String fCost = (String)dataSnapshot.child(fDate).child("cost").getValue();
                    String fName = (String) dataSnapshot.child(fDate).child("scooterName").getValue();
                    String nDate[]= fDate.split("-");
                    TextView date = new TextView(History.this);
                    TextView charge = new TextView(History.this);
                    TextView sName = new TextView(History.this);
                    date.setTypeface(null, Typeface.BOLD);
                    if (nDate[2].length()==1){
                        nDate[2]= "0"+nDate[2];
                    }
                    date.setPadding(20,50,10,50);
                    charge.setPadding(10,50,20,50);
                    sName.setPadding(10,50,10,50);
                    sName.setGravity(Gravity.CENTER_HORIZONTAL);
                    date.setText(nDate[0]+ "-" +nDate[1] + "-" + nDate[2]);
                    DecimalFormat df = new DecimalFormat("#.##");
                    charge.setText(fCost + " zł");
                    sName.setText("#"+fName);

                    tCharge.addView(charge);
                    tDate.addView(date);
                    tName.addView(sName);
                    i++;
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        nCancel = findViewById(R.id.menu);
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

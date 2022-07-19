package eco.yumi;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Contacts;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;

import eco.yumi.R;


public class Billing extends AppCompatActivity {
Button nCancel;
DatabaseReference nhistory,nuser, nrating;
RatingBar ratingBar;
static String scooter;
TextView sBalance, sTime, date, charge, sName, sNumber;
Double balance=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing);
        sBalance = findViewById(R.id.sBalance);
        sTime = findViewById(R.id.time);
        date = findViewById(R.id.date);
        charge = findViewById(R.id.charge);
        ratingBar = findViewById(R.id.ratingBar);
        sName = findViewById(R.id.sName);
        sNumber = findViewById(R.id.sNumber);
        nuser = FirebaseDatabase.getInstance().getReference("service/users").child(MapsActivity.Uid);
        nhistory = FirebaseDatabase.getInstance().getReference("service/history").child(MapsActivity.Uid);
        nrating = FirebaseDatabase.getInstance().getReference("rating");
        ratingBar.setRating(5);
        nuser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                balance = dataSnapshot.child("balance").getValue(Double.class);
                DecimalFormat sf = new DecimalFormat("00.00");
                String s = sf.format(balance);
                sBalance.setText(s+" zł");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        nhistory.orderByChild("timestamp").limitToFirst(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i = 0;
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    String fDate = childDataSnapshot.getKey();
                    Long fCharge = (Long) dataSnapshot.child(fDate).child("duration").getValue();
                    String fCost = (String)dataSnapshot.child(fDate).child("cost").getValue();
                    String fName = (String) dataSnapshot.child(fDate).child("scooterName").getValue();
                    double aCharge = (double)fCharge/60000;
                    int iCharge = (int)Math.ceil(aCharge);
                    String nDate[]= fDate.split("-");
                    if (nDate[2].length()==1){
                        nDate[2]= "0"+nDate[2];
                    }
                    sTime.setText(nDate[3]+":"+nDate[4]);
                    date.setText(nDate[0]+ "-" +nDate[1] + "-" + nDate[2]);
                    charge.setText(fCost);//TODO
                    String[] aName = fName.split(",");
                    scooter = aName[0];
                    scooter = scooter.replace("#","");
                    sNumber.setText(aName[0]);
                    //TODO
                    sName.setText(aName[1]);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        nCancel = findViewById(R.id.cancel);

        nCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Float rating = ratingBar.getRating();
                if(rating<=2f){
                    detailPopUp(scooter, MapsActivity.Uid,rating);
                } else {
                    Intent intent = new Intent(Billing.this, MapsActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }
    private void detailPopUp(final String scooter,final String Uid, final float rating) {
        final EditText input = new EditText(Billing.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(input);

        builder.setTitle(getResources().getString(R.string.scooter))
                .setMessage(getResources().getString(R.string.wouldYou))
                .setNegativeButton(getResources().getString(R.string.NO), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Billing.this, MapsActivity.class);
                        nrating.child(scooter).child(Uid).setValue(rating);
                        startActivity(intent);
                        finish();
                    }
                })
                .setPositiveButton(getResources().getString(R.string.YES), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String comment = input.getText().toString();
                        if(!comment.equals("")) {
                            nrating.child(scooter).child(Uid).setValue(comment);
                        }else
                            nrating.child(scooter).child(Uid).setValue(rating);
                        Intent intent = new Intent(Billing.this, MapsActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
        builder.create().show();
    }
}

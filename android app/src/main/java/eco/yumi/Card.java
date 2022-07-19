package eco.yumi;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;


public class Card extends AppCompatActivity {
private static Button nAddCard;
private static DatabaseReference ncards, nuser;
private static TextView tCard1,tCard2,cCard1,cCard2, link;
private static RadioButton card1,card2;
private static RelativeLayout rCard1, rCard2;
private static ImageView iCard1, iCard2;
private static int c = 0;
private static long count;
private static String Uid, cardName1, cardName2;
ImageButton back, nPhone;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);
        rCard1 = findViewById(R.id.firstCard);
        rCard2 = findViewById(R.id.secondCard);
        nAddCard = findViewById(R.id.addCard);
        tCard1 = findViewById(R.id.card1Text);
        link = findViewById(R.id.link);
        tCard2 = findViewById(R.id.card2Text);
        tCard1 = findViewById(R.id.card1Text);
        tCard2 = findViewById(R.id.card2Text);
        card1 = findViewById(R.id.card1);
        card2 = findViewById(R.id.card2);
        cCard2 = findViewById(R.id.card2delete);
        cCard1 = findViewById(R.id.card1delete);
        iCard1 = findViewById(R.id.card1Image);
        iCard2 = findViewById(R.id.card2Image);
        rCard1.setVisibility(View.INVISIBLE);
        rCard2.setVisibility(View.INVISIBLE);
        card1.setActivated(false);
        card2.setActivated(false);
        nAddCard.setEnabled(false);
        nPhone = findViewById(R.id.phone);
        back = findViewById(R.id.menu);
        Uid = FirebaseAuth.getInstance().getUid();
        card1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                card2.setChecked(false);
                card1.setChecked(true);;
            }
        });
        card2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                card1.setChecked(false);
                card2.setChecked(true);;
            }
        });
        link.setMovementMethod(LinkMovementMethod.getInstance());

        ncards = FirebaseDatabase.getInstance().getReference("service/card").child(Uid);
        nuser = FirebaseDatabase.getInstance().getReference("service/users").child(Uid);
        nuser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                nAddCard.setEnabled(true);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        ncards.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                c=0;
                count = dataSnapshot.getChildrenCount();
                if(count<2) {
                    card1.setChecked(true);
                    nAddCard.setText(getResources().getString(R.string.addCard));
                }else{
                    nAddCard.setText(getResources().getString(R.string.save));
                }
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    c++;
                    final String name = childDataSnapshot.getKey();
                    String type = childDataSnapshot.child("type").getValue(String.class);
                    String control = childDataSnapshot.child("control").getValue(String.class);
                    if(c>1){
                        cardName2 = name;
                        rCard2.setVisibility(View.VISIBLE);
                        tCard2.setText(name);
                        switch(type){
                            case ("visa"):
                                iCard2.setImageResource(R.drawable.visa);
                                break;
                            case ("master"):
                                iCard2.setImageResource(R.drawable.master);
                                break;
                            default:
                                iCard2.setImageResource(R.drawable.card);
                        }
                        if(card1.isChecked())
                            card2.setChecked(false);
                        else
                            card2.setChecked(true);
                        cCard2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                confirm(rCard2, name);
                            }
                        });
                    }
                    if(c==1) {
                        cardName1 = name;
                        rCard1.setVisibility(View.VISIBLE);
                        tCard1.setText(name);
                        switch(type){
                            case ("visa"):
                                iCard1.setImageResource(R.drawable.visa);
                                break;
                            case ("master"):
                                iCard1.setImageResource(R.drawable.master);
                                break;
                            default:
                                iCard1.setImageResource(R.drawable.card);
                        }
                        if(count==1)
                            card1.setChecked(true);
                        else if (control!=null) {
                            if(control.equals("master")){
                                card1.setChecked(true);
                            }else
                                card1.setChecked(false);
                        }
                        cCard1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                confirm(rCard1, name);
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        nAddCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(count<2) {
                    Intent intent = new Intent(Card.this, Payment.class);
                    startActivity(intent);
                }else{
                    if(card1.isChecked()){
                        ncards.child(cardName1).child("control").setValue("master");
                        ncards.child(cardName2).child("control").setValue("slave");
                    }
                    if(card2.isChecked()){
                        ncards.child(cardName2).child("control").setValue("master");
                        ncards.child(cardName1).child("control").setValue("slave");
                    }
                }
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
    private void confirm(final RelativeLayout relativeLayout,final String name) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.erease)
                .setMessage(R.string.AreYouSure)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        relativeLayout.setVisibility(View.INVISIBLE);
                        ncards.child(name).removeValue();
                    }
                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }
}

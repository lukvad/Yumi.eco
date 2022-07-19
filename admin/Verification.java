package com.lukvad.admin;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.Random;

public class Verification extends AppCompatActivity {

    private WebView wv;
    private TextView name, idno, address, phone;
    private Button ok, erase,email, id, ignore, sub, cancel, forceOk, above, next, prev;
    private String sFirstname,sSurname,sEmail,sPhone,sId,sAddress, url,rUrl="", User;
    private Target target;
    DatabaseReference waiting, done;
    RelativeLayout rWindow;
    static ProgressBar progressBar = null;
    private String iState = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);
        rWindow = findViewById(R.id.window);
        progressBar = new ProgressBar(Verification.this,null,android.R.attr.progressBarStyleLarge);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100,100);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        rWindow.addView(progressBar,params);
        progressBar.setVisibility(View.INVISIBLE);
        done = FirebaseDatabase.getInstance().getReference().child("service/users");
        id = findViewById(R.id.id);
        next = findViewById(R.id.next);
        prev = findViewById(R.id.prev);
        name = findViewById(R.id.name);
        cancel = findViewById(R.id.cancel);
        idno = findViewById(R.id.idno);
        address = findViewById(R.id.address);
        above = findViewById(R.id.above);
        forceOk = findViewById(R.id.force);
        sub = findViewById(R.id.sub);
        ignore = findViewById(R.id.ignore);
        ok = findViewById(R.id.ok);
        erase = findViewById(R.id.erase);
        email = findViewById(R.id.email);
        next.setVisibility(View.INVISIBLE);
        prev.setVisibility(View.INVISIBLE);
        wv = (WebView) findViewById(R.id.imageView);


        wv.getSettings().setLoadWithOverviewMode(true);
        wv.getSettings().setUseWideViewPort(true);
        wv.getSettings().setBuiltInZoomControls(true);
        wv.getSettings().setPluginState(WebSettings.PluginState.ON);
        WebSettings mWebSettings = wv.getSettings();
        mWebSettings.setJavaScriptEnabled(true); // Done above
        mWebSettings.setDomStorageEnabled(true); // Try
        mWebSettings.setSupportZoom(false);
        mWebSettings.setAllowFileAccess(true);
        mWebSettings.setAllowContentAccess(true);

        wv.setWebViewClient(new myWebClient());

        if(savedInstanceState==null) {
            Bundle register = getIntent().getExtras();
            if (register != null) {
                User = register.getString("User");

            } else {
                User = (String) savedInstanceState.getSerializable("User");
            }
        }
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Verification.this, Users.class);
                startActivity(intent);
                finish();
            }
        });
        waiting = FirebaseDatabase.getInstance().getReference().child("Users").child(User);
        waiting.child("check").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Toast.makeText(Verification.this, dataSnapshot.getValue(String.class), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        waiting.child("idno").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                idno.setText(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        waiting.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                url = (String) dataSnapshot.child("url").getValue();
                if(dataSnapshot.child("rear").exists())
                    rUrl = (String) dataSnapshot.child("rear").getValue();
                if(!rUrl.equals("")){
                    next.setVisibility(View.VISIBLE);
                    prev.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            wv.loadUrl(url);
                            prev.setVisibility(View.INVISIBLE);
                            next.setVisibility(View.VISIBLE);
                        }
                    });
                    next.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            wv.loadUrl(rUrl);
                            prev.setVisibility(View.VISIBLE);
                            next.setVisibility(View.INVISIBLE);
                        }
                    });
                }
                wv.loadUrl(url);
                sFirstname = (String)dataSnapshot.child("firstname").getValue();
                sSurname =   (String) dataSnapshot.child("surname").getValue();
                sAddress = (String)dataSnapshot.child("address").getValue();
                sEmail = (String)dataSnapshot.child("email").getValue();
                sId = (String)dataSnapshot.child("idno").getValue();
                sPhone = (String)dataSnapshot.child("phone").getValue();

                    name.setText(sFirstname + " " + sSurname);
                    idno.setText(sId);
                    address.setText(sAddress);
                    idno.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast.makeText(Verification.this, sId, Toast.LENGTH_SHORT).show();
                            setClipboard(Verification.this, sId);
                        }
                    });
//                }
                forceOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        popUp("FORCE");
                    }
                });
                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        popUp("OK");
                    }
                });
                ignore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        popUp("IGNORE");
                    }
                });
                id.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        detailPopUp();
                    }
                });
                sub.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        waiting.child("state").setValue("sub");
                    }
                });
                above.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        popUp("ABOVE");
                    }
                });
                erase.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        popUp("DELETE");
                    }
                });
                email.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        popUp("RESEND");
                  }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    private void popUp(final String state) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Czy na pewno")
                .setPositiveButton(state, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        switch(state){
                            case "RESEND":
                                waiting.child("state").setValue("resend");
                                break;
                            case "ABOVE":
                                waiting.child("state").setValue("above");
                                break;
                            case "DELETE":
                                delete();
                                break;
                            case "OK":
                                ok();
                                break;
                            case "IGNORE":
                                waiting.child("state").setValue("ignore");
                                finish();
                                break;
                            case "FORCE" :
                                forceOk();
                                break;
                        }
                    }
                }).setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }
    private void forceOk(){
        progressBar.setVisibility(View.VISIBLE);
        waiting.child("state").setValue("approved");
        if(!url.equals(""))
            FirebaseStorage.getInstance().getReferenceFromUrl(url).delete();
        if(!rUrl.equals(""))
            FirebaseStorage.getInstance().getReferenceFromUrl(rUrl).delete();
        waiting.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.INVISIBLE);
                Intent intent = new Intent(Verification.this, Users.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    private void ok(){
        progressBar.setVisibility(View.VISIBLE);
        waiting.child("state").setValue("approve");
        if(!url.equals(""))
            FirebaseStorage.getInstance().getReferenceFromUrl(url).delete();
        if(!rUrl.equals(""))
            FirebaseStorage.getInstance().getReferenceFromUrl(rUrl).delete();
        waiting.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.INVISIBLE);
                Intent intent = new Intent(Verification.this, Users.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void delete(){
        if(!url.equals(""))
            FirebaseStorage.getInstance().getReferenceFromUrl(url).delete();
        if(!rUrl.equals(""))
            FirebaseStorage.getInstance().getReferenceFromUrl(rUrl).delete();
        waiting.child("state").setValue("delete");
        Intent intent = new Intent(Verification.this, Users.class);
        startActivity(intent);
        finish();
    }

    private void detailPopUp() {
        final EditText input = new EditText(Verification.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(input);

        builder
                .setMessage("Zmiana ID")
                .setNegativeButton("ANULUJ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String idno = input.getText().toString();
                        waiting.child("idno").setValue(idno);
                    }
                });
        builder.create().show();
    }


    public class myWebClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // TODO Auto-generated method stub
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // TODO Auto-generated method stub

            view.loadUrl(url);
            return true;

        }
    }
    private void setClipboard(Context context, String text) {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
            clipboard.setPrimaryClip(clip);
    }
}

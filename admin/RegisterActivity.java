package com.lukvad.scooter;

import android.*;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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
import com.j256.ormlite.stmt.query.In;

import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private EditText nEmail, nPassword1, nPassword2, nFirstname, nSurname ;
    private Button nRegister;
    private TextView textView;
    private static String firstname="", surname="", email="", password="";
    private static final int REQUEST_CODE_ASK_PERMISSIONS=123;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA},
                    REQUEST_CODE_ASK_PERMISSIONS);
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.CAMERA)) {
                showExplanation(getResources().getString(R.string.CameraPermitTitle), getResources().getString(R.string.CameraPermitTitle), android.Manifest.permission.CAMERA, REQUEST_CODE_ASK_PERMISSIONS);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA},
                        REQUEST_CODE_ASK_PERMISSIONS);
            }
        }
        nEmail = findViewById(R.id.email);
        textView = findViewById(R.id.textView);
        nPassword1 = findViewById(R.id.password);
        nPassword2 = findViewById(R.id.confirmPassword);
        nFirstname = findViewById(R.id.firstName);
        nSurname = findViewById(R.id.surName);
        nRegister = findViewById(R.id.register);
        nRegister.setEnabled(false);
        textView.setVisibility(View.INVISIBLE);


        nPassword1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String password1 = nPassword1.getText().toString();
                String password2 = nPassword2.getText().toString();
                if ((!password1.equals(""))&&(password1.equals(password2))){
                    nRegister.setEnabled(true);
                    textView.setVisibility(View.INVISIBLE);
                }
                else {
                    nRegister.setEnabled(false);
                    textView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        nPassword2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                String password1 = nPassword1.getText().toString();
                String password2 = nPassword2.getText().toString();
                if ((!password2.equals(""))&&(password1.equals(password2))){
                    nRegister.setEnabled(true);
                    textView.setVisibility(View.INVISIBLE);
                }
                else {
                    nRegister.setEnabled(false);
                    textView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        nRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                email = nEmail.getText().toString();
                password = nPassword1.getText().toString();
                firstname = nFirstname.getText().toString();
                surname = nSurname.getText().toString();
                if((!email.equals(""))&&(!password.equals(""))&&(!firstname.equals(""))&&(!surname.equals(""))) {
                   if(isDigit(password)&&(password.length()>7)) {
                       if((email.contains("@"))&&(email.contains("."))) {
                           Intent intent = new Intent(RegisterActivity.this, Register2Activity.class);
                           intent.putExtra("email", email);
                           intent.putExtra("password", password);
                           intent.putExtra("firstname", firstname);
                           intent.putExtra("surname", surname);
                           startActivity(intent);
                       }else Toast.makeText(RegisterActivity.this, getResources().getString(R.string.signupEmail),Toast.LENGTH_SHORT).show();
                   }else Toast.makeText(RegisterActivity.this, getResources().getString(R.string.signupError),Toast.LENGTH_SHORT).show();

                }
                else Toast.makeText(RegisterActivity.this, getResources().getString(R.string.signupBad),Toast.LENGTH_SHORT).show();
            }
        });
    }

    public Boolean isDigit(String str) {
        for(int i=str.length()-1; i>=0; i--) {
            if(Character.isDigit(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }
    private void showExplanation(String title,String message,final String permission,final int permissionRequestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        requestPermission(permission, permissionRequestCode);
                    }
                });
        builder.create().show();
    }
    private void requestPermission(String permissionName, int permissionRequestCode) {
        ActivityCompat.requestPermissions(this,
                new String[]{permissionName}, permissionRequestCode);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[],int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, getResources().getString(R.string.PermisionGranted), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, getResources().getString(R.string.PermisionDenied), Toast.LENGTH_SHORT).show();
                }
        }
    }
}
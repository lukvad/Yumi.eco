package eco.yumi;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.apache.http.auth.AUTH;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static eco.yumi.MapsActivity.Uid;

public class VerificationPhoneActivity extends AppCompatActivity {
    protected Button proceed;
    protected FirebaseAuth nAuth;
    private String verificationId;
    protected String  password="", firstname="", surname="", phone="", user;
    private EditText editText;
    private TextView phoneNo, changePhone;
    private DatabaseReference nuser;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_phoneverification);
        editText = findViewById(R.id.code);
        phoneNo = findViewById(R.id.phoneNumber);
        changePhone = findViewById(R.id.changePhone);
        final FirebaseUser auth = FirebaseAuth.getInstance().getCurrentUser();
        user = auth.getUid();
            nuser = FirebaseDatabase.getInstance().getReference("service/users").child(user);
        proceed = findViewById(R.id.back);
        if(savedInstanceState==null) {
            Bundle register = getIntent().getExtras();
            if (register != null) {
                phone = register.getString("phone");
            }
            else {
                phone= (String) savedInstanceState.getSerializable("phone");
                phone = register.getString("phone");
            }
        }
        nAuth = FirebaseAuth.getInstance();
        phoneNo.setText(phone);
        sendCode(phone);
        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                        String code = editText.getText().toString().trim();
                        if(code.length()==6)
                            verifyCode(code);
                        else
                            Toast.makeText(VerificationPhoneActivity.this, getResources().getString(R.string.codeError), Toast.LENGTH_SHORT).show();
                }
        });
        changePhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(VerificationPhoneActivity.this, MapsActivity.class);
                nuser.child("phone").setValue("");
                startActivity(intent);
                finish();
            }
        });
    }
    private void verifyCode(String code){
        try {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
            linkWithCredential(credential);
        }catch (Exception e){
            Toast.makeText(this, "Nieudana weryfikacja", Toast.LENGTH_SHORT).show();
        }
    }
    private void linkWithCredential(PhoneAuthCredential credential){
        nAuth.getCurrentUser().linkWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    startActivity(new Intent(VerificationPhoneActivity.this, MapsActivity.class));
                    nuser.child("verPhone").setValue(phone);
                    finish();
                } else {
                    Toast.makeText(VerificationPhoneActivity.this, getResources().getString(R.string.AuthFailed), Toast.LENGTH_SHORT).show();
                    editText.getText().clear();
                }
            }
        });

    }
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationId = s;
        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if(code != null){
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {

        }
    };
    private void sendCode(String phone) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phone,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks

    }


}
package eco.yumi;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import eco.yumi.R;


public class Email extends AppCompatActivity {
Button changePassword;
EditText email;
FirebaseAuth auth;
TextView cancel;
String sEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);
        email = findViewById(R.id.email);
        cancel = findViewById(R.id.cancel);
        changePassword = findViewById(R.id.changePassword);
        auth = FirebaseAuth.getInstance();

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sEmail = email.getText().toString();
                if(sEmail.contains("@")&&sEmail.contains(".")) {
                    auth.sendPasswordResetEmail(sEmail).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(Email.this, "Reset link sent to your email", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Email.this, "User does not exist", Toast.LENGTH_SHORT).show();
                        }
                    });
                }else
                    Toast.makeText(Email.this, "Błędny mail", Toast.LENGTH_SHORT).show();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

}

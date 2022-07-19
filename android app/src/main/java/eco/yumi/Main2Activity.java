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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Main2Activity extends AppCompatActivity {
    private EditText nEmail, nPassword;
    private Button nLogin;
    private TextView nRecover;
    private FirebaseAuth nAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity2_main);
        nAuth = FirebaseAuth.getInstance();
        nEmail = findViewById(R.id.email);
        nPassword = findViewById(R.id.password);
        nLogin = findViewById(R.id.login);
        nRecover = findViewById(R.id.recover);
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user!=null) {
                    if(!user.isEmailVerified()){
                        Toast.makeText(Main2Activity.this, getResources().getString(R.string.needemail), Toast.LENGTH_SHORT).show();
                        nAuth.signOut();
                        return;
                    }else {
                        Intent intent = new Intent(Main2Activity.this, MapsActivity.class);
                        startActivity(intent);
                        finish();
                        return;
                    }
                }
            }
        };
        nRecover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Main2Activity.this, Email.class);
                startActivity(intent);
            }
        });

        nLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = nEmail.getText().toString();
                final String password = nPassword.getText().toString();
                    if((!email.equals(""))&&(!password.equals(""))) {

                        nAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(Main2Activity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(Main2Activity.this, getResources().getString(R.string.loginBad), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    else Toast.makeText(Main2Activity.this, getResources().getString(R.string.loginBad), Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        nAuth.addAuthStateListener(firebaseAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        nAuth.removeAuthStateListener(firebaseAuthListener);
    }








}

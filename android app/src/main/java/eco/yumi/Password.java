package eco.yumi;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class Password extends AppCompatActivity {
Button nBalance, nHistory, changePassword;
ImageButton back;
FirebaseUser user;
FirebaseAuth auth;
EditText nPassword, nPassword1, nPassword2;
String password, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);
        changePassword = findViewById(R.id.changePassword);
        auth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        nPassword = findViewById(R.id.oldPassword);
        nPassword1 = findViewById(R.id.password);
        nPassword2 = findViewById(R.id.confirmPassword);
        changePassword = findViewById(R.id.changePassword);
        changePassword.setEnabled(false);
        email = user.getEmail();
        back = findViewById(R.id.menu);

        nPassword1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String password1 = nPassword1.getText().toString();
                String password2 = nPassword2.getText().toString();
                if ((!password1.equals(""))&&(password1.equals(password2))){
                    changePassword.setEnabled(true);
                }
                else {
                    changePassword.setEnabled(false);
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
                    changePassword.setEnabled(true);
                }
                else {
                    changePassword.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPassword = nPassword.getText().toString();
                password = nPassword1.getText().toString();
                auth.signInWithEmailAndPassword(email,oldPassword).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        if(!password.equals("")) {
                            if(isDigit(password)&&(password.length()>7)) {
                                user.updatePassword(password);
                                Toast.makeText(Password.this, getResources().getString(R.string.passwordChanged), Toast.LENGTH_SHORT).show();
                                finish();
                            }else Toast.makeText(Password.this, getResources().getString(R.string.signupError),Toast.LENGTH_SHORT).show();

                        }
                        else Toast.makeText(Password.this, getResources().getString(R.string.signupBad),Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Password.this, getResources().getString(R.string.wrongPassword), Toast.LENGTH_SHORT).show();
                    }
                });
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
}

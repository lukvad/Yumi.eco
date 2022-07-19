package eco.yumi;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import eco.yumi.R;

public class VerificationActivity extends AppCompatActivity {
    protected TextView email;
    protected Button nBack;
    protected FirebaseAuth nAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_verification);
        email = findViewById(R.id.email);
        nBack = findViewById(R.id.back);
        nBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VerificationActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
    @Override
    protected void onPause() {
        super.onPause();
        nAuth.signOut();
    }

}
package eco.yumi;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import eco.yumi.R;

import com.google.firebase.database.ServerValue;
import com.tpay.android.library.web.TpayActivity;
import com.tpay.android.library.web.TpayPayment;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


//TODO CARD IO STRIPE
public class PaymentTpay extends AppCompatActivity {
    private TpayPayment.Builder paymentBuilder = null;
    private ImageButton back;
    private String  iEmail, iPayment,iName, id = "32777", random = null, security = "aqA2sDzKj2DcX7Pl";
    private DatabaseReference ref;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(
                TpayActivity.EXTRA_TPAY_PAYMENT,
                paymentBuilder);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_t_pay);
        back = findViewById(R.id.menu);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        ref= FirebaseDatabase.getInstance().getReference().child("service/temporary").child(MapsActivity.Uid).push();
        if (savedInstanceState == null) {
            paymentBuilder = new TpayPayment.Builder();
            // ustawienia parametrów
        } else {
            paymentBuilder = savedInstanceState
                    .getParcelable(TpayActivity.EXTRA_TPAY_PAYMENT);
        }

        if(savedInstanceState==null) {
            Bundle bPay = getIntent().getExtras();
            if (bPay != null) {
                iEmail = bPay.getString("email");
                iPayment= bPay.getString("payment");
                iName= bPay.getString("name");
            }
            else {
                iEmail= (String) savedInstanceState.getSerializable("email");
                iPayment= (String) savedInstanceState.getSerializable("payment");
                iName= (String) savedInstanceState.getSerializable("name");
            }
        }
        String push = ref.getKey();
        String MD5String=id + iPayment + push + security;
        Double payment = Double.valueOf(iPayment);
        MessageDigest md = null;
        String hashtext = null;


        try {
            md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(MD5String.getBytes());
            BigInteger number = new BigInteger(1, messageDigest);
            hashtext = number.toString(16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        ref.setValue(ServerValue.TIMESTAMP);
        if (savedInstanceState == null) {
            paymentBuilder = new TpayPayment.Builder()
                    .setId(id)
                    .setAmount(iPayment)
                    .setCrc(push)
                    .setClientName(iName)
                    .setSecurityCode(security)
                    .setDescription("doladowanie")
                    .setClientEmail(iEmail);
        } else {
            paymentBuilder = savedInstanceState
                    .getParcelable(TpayActivity.EXTRA_TPAY_PAYMENT);
        }
                final Intent payIntent = new Intent(PaymentTpay.this,
                        TpayActivity.class);
                final TpayPayment tpayPayment = paymentBuilder.create();
                payIntent.putExtra(TpayActivity.EXTRA_TPAY_PAYMENT, tpayPayment);

                startActivityForResult(payIntent,
                        TpayActivity.TPAY_PAYMENT_REQUEST);

    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode) {
            case TpayActivity.TPAY_PAYMENT_REQUEST:
                if (resultCode == RESULT_OK) {
// Transakcja poprawna. Poczekaj na powiadomienie.
                } else {
// Użytkownik anulował transakcję lub wystąpił błąd.
                }
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

}

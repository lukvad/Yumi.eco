package com.lukvad.scooter;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tpay.android.library.web.TpayActivity;
import com.tpay.android.library.web.TpayPayment;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.Random;

import static com.lukvad.scooter.MapsActivity.Uid;


//TODO CARD IO STRIPE
public class PaymentTpay extends AppCompatActivity {
    private TpayPayment.Builder paymentBuilder = null;
    private String iName, iEmail, iPayment, id = "32611", random = null, security = "3u3Y920lII8feRf1";
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
        ref= FirebaseDatabase.getInstance().getReference().child("service/transaction").child(Uid).push();
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
                iName = bPay.getString("name");
                iEmail = bPay.getString("email");
                iPayment= bPay.getString("payment");
            }
            else {
                iName= (String) savedInstanceState.getSerializable("name");
                iEmail= (String) savedInstanceState.getSerializable("email");
                iPayment= (String) savedInstanceState.getSerializable("payment");
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
        ref.setValue(true);
        if (savedInstanceState == null) {
            paymentBuilder = new TpayPayment.Builder()
                    .setId(id)
                    .setAmount(iPayment)
                    .setCrc(push)
                    .setSecurityCode(security)
                    .setDescription("doladowanie")
                    .setClientEmail(iEmail)
                    .setClientName(iName);
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

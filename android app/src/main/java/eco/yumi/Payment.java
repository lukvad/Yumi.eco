package eco.yumi;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Contacts;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;


public class Payment extends Activity {
    WebView wv;
    ImageButton back, nPhone;
    String url = "https://lukvad.usermd.net/vendor/tpay-com/tpay-php/tpayLibs/examples/CardGate.php";
    FirebaseAuth auth;
    String Uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        nPhone = findViewById(R.id.phone);
        back = findViewById(R.id.menu);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Payment.this, Card.class);
                startActivity(intent);
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
        wv = (WebView) findViewById(R.id.webview);
        auth = FirebaseAuth.getInstance();
        Uid = auth.getCurrentUser().getUid();
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
        mWebSettings.setPluginState(WebSettings.PluginState.ON);
        mWebSettings.setAppCacheEnabled(true);
        CookieSyncManager.createInstance(Payment.this);
        CookieManager cookieManager = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)
            cookieManager.removeAllCookies(null);
        else
            cookieManager.removeAllCookie();
        String cookieString = "id="+Uid;
        CookieSyncManager.getInstance().sync();
        Map<String, String> abc = new HashMap<String, String>();
        abc.put("Cookie", cookieString);

        wv.setWebViewClient(new myWebClient());
        wv.loadUrl(url, abc);
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
}
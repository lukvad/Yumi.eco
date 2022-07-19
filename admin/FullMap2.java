package com.lukvad.admin;
import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.lang.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class FullMap2 extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,GoogleMap.OnInfoWindowLongClickListener, GoogleMap.OnInfoWindowClickListener {

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    public LocationRequest nLocationRequest;
    public GoogleApiClient nGoogleApiClient;
    private static final String TAG = "FullMap2";



    public enum state {oF, rE, oN, fIn};
    public static String nDistance= null, Uid;
    public static state iState=state.oF;
    private static DatabaseReference nadmin, nbase, nalarms, nservice;
    private static Boolean bReserve=false;
    static List<LatLng> PolyList;
    final String[] childname = {null};
    private LatLng nLastLatLng = new LatLng(0,0);
    public static LatLng scooterLatLng = new LatLng(0,0);
    private static Button nLogout, nAlarms, nUsers;
    private static Button nMenu;
    public PopupWindow mnavig;
    public static RelativeLayout rWindow;
    private int i=0;
    public static TextView Distance;
    private static final int REQUEST_CODE_ASK_PERMISSIONS=123;
    private static Map<String, Marker> markerMap = new HashMap<>();
    private static final int[] COLORS = new int[]{R.color.colorSecondaryDark, R.color.colorSecondary, R.color.colorGrey, R.color.colorAccent, R.color.colorPrimaryDarkGrey, R.color.red};
    private View vMap;
    static ProgressBar progressBar = null;
    static String scooter;
    static Integer time;
    ArrayList<String> directionPoint = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        rWindow = findViewById(R.id.MapWindow);

        if(savedInstanceState==null) {
            Bundle userProfile = getIntent().getExtras();
            if (userProfile != null) {
                scooter = userProfile.getString("scooter");
                time = userProfile.getInt("time");
            }
            else {
                scooter= (String) savedInstanceState.getSerializable("scooter");
                time = (Integer) savedInstanceState.getSerializable("time");

            }
        }


        nMenu = findViewById(R.id.menu);
        Uid = FirebaseAuth.getInstance().getUid();
        nbase = FirebaseDatabase.getInstance().getReference();
        nservice = FirebaseDatabase.getInstance().getReference().child("service");
        nalarms = nbase.child("alarms");
        nadmin = FirebaseDatabase.getInstance().getReference("admin");
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        progressBar = new ProgressBar(FullMap2.this,null,android.R.attr.progressBarStyleLarge);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100,100);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        rWindow.addView(progressBar,params);
        progressBar.setVisibility(View.VISIBLE);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            finish();
            System.exit(0);
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {



        final Context mContext = getBaseContext();

        mMap = googleMap;
        vMap = findViewById(R.id.map);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(54.442,18.569)));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(11));

        nadmin.child(scooter).orderByKey().limitToLast(time).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    String state = (String) childDataSnapshot.child("state").getValue();
                    String latitude = (String) childDataSnapshot.child("latitude").getValue();
                    String longitude = (String) childDataSnapshot.child("longitude").getValue();
                    String battery = (String) childDataSnapshot.child("battery").getValue();
                    String userKey = (String) childDataSnapshot.child("userKey").getValue();
                    String date = childDataSnapshot.getKey();

                    String data = latitude+"/"+longitude+"/"+battery+"/"+state + "/" + date;
                    directionPoint.add(data);
                }

                for(int k = 0; k < directionPoint.size()/2; k++) {
                    PolylineOptions rectLine = new PolylineOptions().width(15);
                    for (int i = k*2; i < (k*2)+2; i++) {
                        Log.d(TAG, i + " / " + k + " : " + directionPoint.get(i));
                        String data[] = directionPoint.get(i).split("/");
                        Log.d(TAG, "state :"+data[3]);
                        LatLng latLng = new LatLng(Double.valueOf(data[0]), Double.valueOf(data[1]));
                        Marker marker = mMap.addMarker(new MarkerOptions().position(latLng));
                        drawMarker(marker, String.valueOf(i) , Integer.valueOf(data[2]));
                        if(data[3].equals("*oF&"))
                            rectLine.color(getResources().getColor(R.color.colorGrey));
                        if(data[3].equals("*oN&"))
                            rectLine.color(getResources().getColor(R.color.colorSecondaryDark));
                        if(data[3].equals("*rE&"))
                            rectLine.color(getResources().getColor(R.color.colorAccent));
                        Log.d(TAG, latLng.toString());
                        rectLine.add(latLng);
                    }
                    Polyline polyline = mMap.addPolyline(rectLine);
                }

                progressBar.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });





        buildGoogleApiClient();
        googleMap.setOnInfoWindowClickListener(this);
        googleMap.setOnInfoWindowLongClickListener(this);


    }

    protected synchronized void buildGoogleApiClient() {
        nGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        nGoogleApiClient.connect();
    }




    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }


    //
    @Override
    public void onInfoWindowClick(Marker marker) {

        for (Map.Entry<String, Marker> entry : markerMap.entrySet()) {
            if (entry.getValue().equals(marker)) {
                childname[0] = entry.getKey();
            }
        }
        String data[] = marker.getSnippet().split("/");
//        if(data[1].equals("*oF&")||data[1].equals("*rE&")||data[1].equals("*fIn&"))
//            enginePopUp("Załącz skuter", childname[0], "$oNn%");

//        if(data[1].equals("*oN&")||data[1].equals("$oNn%"))
//            enginePopUp("Wyłącz skuter", childname[0], "*oF&");


    }

    @Override
    public void onInfoWindowLongClick(Marker marker) {
        String data[]=marker.getSnippet().split("/");
        String child = marker.getTitle();
        if(!data[2].equals("")){
//            detailPopUp(data[3],child);
        }
        else{
            Intent intent = new Intent(FullMap2.this, FullMap.class);
            intent.putExtra("scooter", child);
            startActivity(intent);
        }
    }



    public void drawMarker(final Marker marker, final String number, int battery){
        Paint paint = new Paint();
        if(battery<2)
            paint.setColor(getResources().getColor(R.color.red));
        else
            paint.setColor(getResources().getColor(R.color.colorSecondaryDark));
        paint.setTextSize(35);
        paint.setStyle(Paint.Style.FILL);
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bmp = Bitmap.createBitmap(200, 50, conf);
        Canvas canvas = new Canvas(bmp);
        canvas.drawText(number, 0, 50,paint ); // paint defines the text color, stroke width, size
        marker.setIcon(BitmapDescriptorFactory.fromBitmap(bmp));
        marker.setAnchor(0.5f, 1);

    }

}

package com.lukvad.admin;
import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.lang.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class FullMap extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener,GoogleMap.OnInfoWindowLongClickListener, GoogleMap.OnInfoWindowClickListener, RoutingListener {

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    public LocationRequest nLocationRequest;
    public GoogleApiClient nGoogleApiClient;
    private static final String TAG = "FullMap";



    public enum state {oF, rE, oN, fIn};
    public static Calendar c;
    SimpleDateFormat dateformat = new SimpleDateFormat("HH",Locale.GERMANY);
    public static String nDistance= null, Uid, rating="",accident="" ,datetime="14";
    public static state iState=state.oF;
    private static DatabaseReference nskutery, nusers,nsearch, npoly, nalarms, nservice, naccident, nrating;
    private static Boolean bReserve=false;
    final String[] childname = {null};
    private LatLng nLastLatLng = new LatLng(0,0);
    public static LatLng scooterLatLng = new LatLng(0,0);
    private static Button nLogout, nAlarms, nUsers, nCheck, nSearch;
    private static TextView tOf, tPa, tOn, tRe;
    private static Button nMenu;
    public PopupWindow mnavig;
    public static RelativeLayout rWindow;
    private int i=0, on=0, of=0, pa=0, out=0;
    protected static Integer poly=0;
    private static Polygon polygon;
    private static List<LatLng> PolyList;
    private static PolygonOptions polygonOptions = new PolygonOptions();
    private List<Polyline> polylines = new ArrayList<>();
    private static final int[] COLORS = new int[]{R.color.colorAccent};
    public static TextView Distance;
    private static final int REQUEST_CODE_ASK_PERMISSIONS=123;
    private static Map<String, Marker> markerMap = new HashMap<>();
    private View vMap;
    static ProgressBar progressBar = null;
    static StorageReference storageRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        rWindow = findViewById(R.id.MapWindow);
        nMenu = findViewById(R.id.menu);
        tOf = findViewById(R.id.of);
        tOn = findViewById(R.id.on);
        tPa = findViewById(R.id.pa);
        tRe = findViewById(R.id.re);
        Uid = FirebaseAuth.getInstance().getUid();
        npoly = FirebaseDatabase.getInstance().getReference("poly");
        nusers = FirebaseDatabase.getInstance().getReference().child("Users");
        nsearch = FirebaseDatabase.getInstance().getReference().child("service/users");
        nservice = FirebaseDatabase.getInstance().getReference().child("service");
        nalarms = FirebaseDatabase.getInstance().getReference().child("alarms");
        nrating = FirebaseDatabase.getInstance().getReference().child("rating");
        naccident = FirebaseDatabase.getInstance().getReference().child("accident");
        nskutery = FirebaseDatabase.getInstance().getReference("service/scooters");
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        polylines = new ArrayList<>();
        progressBar = new ProgressBar(FullMap.this,null,android.R.attr.progressBarStyleLarge);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100,100);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        rWindow.addView(progressBar,params);
        progressBar.setVisibility(View.VISIBLE);
        storageRef = FirebaseStorage.getInstance().getReference();
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

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    FullMap.REQUEST_CODE_ASK_PERMISSIONS);
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                showExplanation(getResources().getString(R.string.GPSpermitTitle), getResources().getString(R.string.GPSpermitText), android.Manifest.permission.ACCESS_FINE_LOCATION, FullMap.REQUEST_CODE_ASK_PERMISSIONS);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        FullMap.REQUEST_CODE_ASK_PERMISSIONS);
            }
        }

        mMap = googleMap;
        vMap = findViewById(R.id.map);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(54.442,18.569)));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(11));
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        npoly.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(polygon!=null)
                    polygon.remove();
                polygonOptions = new PolygonOptions();
                polygonOptions.strokeWidth(5)
                        .strokeColor(getResources().getColor(R.color.colorSecondary));
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    String childName = childDataSnapshot.getKey();
                    Polylines polylines = dataSnapshot.child(childName).getValue(Polylines.class);
                    if (!polylines.latitude.equals("0.0")) {
                        polygonOptions.add(new LatLng(Double.valueOf(polylines.latitude), Double.valueOf(polylines.longitude)));
                    }
                }
                polygon = mMap.addPolygon(polygonOptions);
                PolyList = polygon.getPoints();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        int users = 0;
        nusers.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Random random= new Random();
                int id = random.nextInt(100);
                c = Calendar.getInstance();
                datetime = dateformat.format(c.getTime());
                if(Integer.valueOf(datetime)<20&&Integer.valueOf(datetime)>6) {
                    if (!dataSnapshot.child("state").exists() && i == 0) {
                        i++;
                        showNotification(dataSnapshot.getKey(), "Nowy użytkownik!", Users.class, id);
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String User = dataSnapshot.getKey();
                Intent intent = new Intent(FullMap.this, Verification.class);
                intent.putExtra("User", User);
                startActivity(intent);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        nalarms.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String title = dataSnapshot.getKey();
                c = Calendar.getInstance();
                datetime = dateformat.format(c.getTime());
                Log.e("UWAGA", datetime);
                if(Integer.valueOf(datetime)<20&&Integer.valueOf(datetime)>5) {
                    if (title.length() < 4) {
                        showNotification(title, "alarm", Alarms.class, Integer.valueOf(title));
                    }
                }else {
                    if (title.length() < 4) {
                        if (dataSnapshot.child("steal").exists() || dataSnapshot.child("battery").exists() || dataSnapshot.child("hack").exists() || dataSnapshot.child("payment").exists())
                            showNotification(title, "alarm", Alarms.class, Integer.valueOf(title));
                    }
                }
           }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        nskutery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    String childName = childDataSnapshot.getKey();
                    ScooterInformation scooter = dataSnapshot.child(childName).getValue(ScooterInformation.class);
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(Double.parseDouble(scooter.latitude),Double.parseDouble(scooter.longitude)))
                            .anchor(0.5f,0.5f).zIndex(0.4f).title(childName));
                    markerMap.put(childName,marker);
                }
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        nrating.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                        rating = rating + " | " + childDataSnapshot.getKey();
                }
                showNotification(rating, " Oceny", FullMap.class, 1234);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        naccident.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                        accident = accident + " | " + childDataSnapshot.getKey();
                }
                showNotification(accident, " wypadki", FullMap.class, 1243);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        nservice.child("scooters").addValueEventListener((new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    String childName = childDataSnapshot.getKey();
                    ScooterInformation scooter = dataSnapshot.child(childName).getValue(ScooterInformation.class);
                    Marker marker = markerMap.get(childName);
                    Boolean error = false;
                    LatLng location = new LatLng(Double.parseDouble(scooter.latitude), Double.parseDouble(scooter.longitude));
                    marker.setPosition(location);
                    if(scooter.userKey.equals(""))
                        marker.setSnippet(scooter.battery + "/"+ scooter.state +"/"+ scooter.engine);
                    else
                        marker.setSnippet(scooter.battery + "/"+ scooter.state +"/"+ scooter.engine +"/" + scooter.userKey);
                    if((!scooter.userKey.equals("")&&scooter.state.equals("*oF&"))||childDataSnapshot.child("check").exists()||childDataSnapshot.child("feedback").exists()){
                        error = true;
                    }

                    if(scooter.state.equals("*oN&")||scooter.state.equals("$oNn$"))
                        markerBattery(scooter.battery, marker, true, error);
                    else
                        markerBattery(scooter.battery, marker, false, error);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        }));





        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
        googleMap.setOnInfoWindowClickListener(this);
        googleMap.setOnInfoWindowLongClickListener(this);

        nMenu.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                View popUpView = getLayoutInflater().inflate(R.layout.navwindow, null);
                AlphaAnimation mapOff = new AlphaAnimation(1f, 0.5f);
                mapOff.setDuration(400);
                final AlphaAnimation mapOn = new AlphaAnimation(0.5f, 1f);
                mapOn.setDuration(400);
                mnavig = new PopupWindow(popUpView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.FILL_PARENT, true);
                mnavig.setAnimationStyle(R.style.NavAnimation);
                mnavig.setBackgroundDrawable(new BitmapDrawable());
                mnavig.showAtLocation(popUpView, Gravity.LEFT, 0,0);
                mnavig.setFocusable(true);
                mnavig.setOutsideTouchable(true);
                vMap.startAnimation(mapOff);
                animationListener(mapOff, vMap, 0.5f);
                animationListener(mapOn, vMap, 1.0f );
                mnavig.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        vMap.startAnimation(mapOn);
                    }
                });
                nSearch = popUpView.findViewById(R.id.search);
                nLogout = popUpView.findViewById(R.id.logout);
                nAlarms = popUpView.findViewById(R.id.alarms);
                nUsers = popUpView.findViewById(R.id.userList);
                nCheck = popUpView.findViewById(R.id.check);

                nSearch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        detailPopUp();
                    }
                });
                nLogout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(bReserve==false) {
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(FullMap.this, MainActivity.class);
                            startActivity(intent);
                            mnavig.dismiss();
                            finish();
                        }
                        else {
                            Toast.makeText(FullMap.this, getResources().getString(R.string.NotLoggingOut), Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }
                });
                nAlarms.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(FullMap.this, Alarms.class);
                        startActivity(intent);
                        mnavig.dismiss();
                    }
                });
                nUsers.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(FullMap.this, Users.class);
                        startActivity(intent);
                        mnavig.dismiss();
                    }
                });
                nCheck.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        nservice.child("scooters").orderByChild("state").equalTo("*oN&").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                long count = dataSnapshot.getChildrenCount();
                                tOn.setTextSize(20);
                                tOn.setText("ON : " + String.valueOf(count));
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        nservice.child("scooters").orderByChild("state").equalTo("*oF&").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                long count = dataSnapshot.getChildrenCount();
                                tOf.setTextSize(20);
                                tOf.setText("OF : " + String.valueOf(count));
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        nservice.child("scooters").orderByChild("state").equalTo("*pA&").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                long count = dataSnapshot.getChildrenCount();
                                tPa.setTextSize(20);
                                tPa.setText("PA : " + String.valueOf(count));
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        nservice.child("scooters").orderByChild("state").equalTo("*rE&").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                long count = dataSnapshot.getChildrenCount();
                                tRe.setTextSize(20);
                                tRe.setText("RE : " + String.valueOf(count));
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                });
            }
        });
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
    public void onLocationChanged(Location location) {
        nLastLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        if(iState==state.rE) {
            erasePolyline();
            findScooter(nLastLatLng, scooterLatLng);
        }
        for (;i<1;i++){
            mMap.moveCamera(CameraUpdateFactory.newLatLng(nLastLatLng));
            mMap.moveCamera(CameraUpdateFactory.zoomTo(11));
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        nLocationRequest = new LocationRequest();
        nLocationRequest.setInterval(10000);
        nLocationRequest.setFastestInterval(10000);
        nLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        for (Map.Entry<String, Marker> entry : markerMap.entrySet()) {
            if (entry.getValue().equals(marker)) {
                childname[0] = entry.getKey();
            }
        }
        String data[] = marker.getSnippet().split("/");
        if(data[1].equals("*oF&")||data[1].equals("*rE&")||data[1].equals("*fIn&")||data[1].equals("*pA&"))
            enginePopUp("Załącz skuter", childname[0], "$oNn%");

        if(data[1].equals("*oN&")||data[1].equals("$oNn%"))
            enginePopUp("Wyłącz skuter", childname[0], "*oF&");
    }

    @Override
    public void onInfoWindowLongClick(Marker marker) {
        String data[]=marker.getSnippet().split("/");
        String child = marker.getTitle();
        Intent intent = new Intent(FullMap.this, ScooterProfile.class);
        intent.putExtra("scooter", child);
        startActivity(intent);
    }


    private void requestPermission(String permissionName, int permissionRequestCode) {
        ActivityCompat.requestPermissions(this,
                new String[]{permissionName}, permissionRequestCode);
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
    private void enginePopUp(String message, final String childname, final String state) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Czy na pewno ?")
                .setMessage(message)
                .setPositiveButton("Włącz/Wyłącz", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        nskutery.child(childname).child("state").setValue(state);
                    }
                }).setNegativeButton("OTWORZYĆ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        nskutery.child(childname).child("state").setValue("*hEM&");
                    }
                }).setNeutralButton("PARKING", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        nskutery.child(childname).child("state").setValue("*pA&");
                    }
                });
                builder.create().show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[],int[] grantResults) {
        switch (requestCode) {
            case FullMap.REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mapFragment.getMapAsync(this);
                    Toast.makeText(this, getResources().getString(R.string.PermisionGranted), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, getResources().getString(R.string.PermisionDenied), Toast.LENGTH_SHORT).show();
                }
        }
    }

    public Integer markerBattery (String mBattery , Marker marker, Boolean big, Boolean er){
        Integer p=null;
        String number = marker.getTitle();
        Integer battery = Integer.valueOf(mBattery);
        if (battery>4){
            drawMarker(marker,number, Color.BLACK,big );
            p=4;
        }
        if(battery<=20&&battery>10){
            drawMarker(marker,number, Color.MAGENTA, big );
        }
        if(battery<=10)
            drawMarker(marker,number, Color.RED , big);
        else if(er){
            drawMarker(marker,number, Color.YELLOW , true);
        }
        return p;
    }

    public void findScooter (LatLng start, LatLng end) {
        Routing routing = new Routing.Builder()
                .travelMode(Routing.TravelMode.WALKING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(start, end)
                .build();
        routing.execute();
    }

    @Override
    public void onRoutingFailure(RouteException e) {
    }

    @Override
    public void onRoutingStart() {
    }

    @Override
    public void onRoutingSuccess(ArrayList<com.directions.route.Route> route, int shortestRouteIndex) {

        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            if (polyOptions != null) {
                Polyline polyline = mMap.addPolyline(polyOptions);
                polylines.add(polyline);
            }
            nDistance = (String.valueOf(route.get(i).getDistanceValue()))+"  "+getResources().getString(R.string.Metres);
            Distance.setText(nDistance);
        }
    }

    @Override
    public void onRoutingCancelled() {

    }
    public void erasePolyline (){
        for (Polyline line : polylines){
            line.remove();
        }
        polylines.clear();
    }

    public void drawMarker(final Marker marker, final String number, int color, Boolean big){
        Paint paint = new Paint();
        paint.setColor(color);
        if(big)
            paint.setTextSize(45);
        else
            paint.setTextSize(35);
        paint.setStyle(Paint.Style.FILL);
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bmp = Bitmap.createBitmap(200, 50, conf);
        Canvas canvas = new Canvas(bmp);
        canvas.drawText(number, 0, 50,paint ); // paint defines the text color, stroke width, size
        marker.setIcon(BitmapDescriptorFactory.fromBitmap(bmp));
        marker.setAnchor(0.5f, 0.5f);

    }
    private void detailPopUp() {
        final EditText input = new EditText(FullMap.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(input);
        builder.setTitle("Znajdź po :")
                .setPositiveButton("telefonie", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        final String phone = input.getText().toString();
                        nsearch.orderByChild("phone").equalTo(phone).addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                String user =  dataSnapshot.getKey();
                                Intent intent = new Intent(FullMap.this, UserProfile.class);
                                intent.putExtra("user", user);
                                startActivity(intent);
                            }

                            @Override
                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onChildRemoved(DataSnapshot dataSnapshot) {

                            }

                            @Override
                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }).setNeutralButton("mailu", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final String email = input.getText().toString();
                nsearch.orderByChild("email").equalTo(email).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        String user =  dataSnapshot.getKey();
                            Intent intent = new Intent(FullMap.this, UserProfile.class);
                            intent.putExtra("user", user);
                            startActivity(intent);
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
        builder.create().show();
    }

    private void showNotification(String title, String text, Class a, int s) {

        Intent intent =  new Intent(this, a);
        intent.putExtra("time", 20);
        intent.putExtra("scooter", title);
        intent.putExtra("User", title);
        PendingIntent pi = PendingIntent.getActivity(this, s, intent, 0);
        Resources r = getResources();
        Notification notification = new NotificationCompat.Builder(this)
                .setTicker(r.getString(R.string.start))
                .setPriority(Notification.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_VIBRATE|NotificationCompat.DEFAULT_SOUND)
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(s, notification);
    }

    private void animationListener(Animation animation, final View view1, final float setting){
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view1.setAlpha(setting);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

}

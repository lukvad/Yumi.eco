package com.lukvad.scooter;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Location;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.PolyUtil;

import android.location.Geocoder;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, GoogleMap.OnMarkerClickListener, RoutingListener {
    private static final String TAG = "MapsActivity";  // TAG do logów

    // GOOGLE MAPS

    private GoogleMap mMap; // Serwis Google Maps
    private SupportMapFragment mapFragment; // Fragment Google Maps
    private LocationRequest nLocationRequest; // Serwis lokalizacji GPS telefonu
    private GoogleApiClient nGoogleApiClient; // Google API
    // FIREBASE
    private static ValueEventListener eventListener, valueEventListener1, valueEventListener2, valueEventListener3;
    private static ChildEventListener childEventListener;
    private static DatabaseReference nscooters, nuser, nbase, nhistory; //Scooters - zakładka skuterów , User - zakładka użytkowników

    // Inicjacja zmiennej dystansu między użytkownikiem a skuterem ; Uid - klucz użytkownika Firebase
    // reservedScooter - zarezerwowany skuter pobierany z firebase'a : users/$Uid/scooterName
    private static String nDistance = null, addressLine, reservedScooter;
    protected static String Uid;
    protected Double balance;
    private static TextView Distance;
    private static TableLayout nLinearLayout;
    //TODO base - zakładka zawierająca scooters i users, do zmiany.

    /* Pomocniczy stan użytkownika :
     state.oF - w trakcie poszukiwania skutera
     state.rE - zarezerwowany skuter
     state.oN - włączony skuter
     state.fIn - oddanie skutera i oczekiwanie
     state.pA - naliczany postój
      */
    private enum state {
        oF, rE, oN, fIn, pA
    }

    ;
    private static state iState = state.oF;  // inicjacja stanu początkowego użytkownika


    /* FLAGI :
    reserve - użytkownik ma rezerwację. Nie może : nacisnąć innego markera, wyłączyć okna kontroli skutera.
    taken - przed naciśnięciem przycisku rezerwacja sprawdzamy czy nie został akurat zarezerwowany przez kogoś innego przed chwilą.
    recover - stan podczas włączenia aplikacji i odzyskiwania poprzedniego stanu innego niż state.oF.
    wait - stan oczekiwania na odpowiedź skutera. Po 20 sekundach nieudanej komendy zostanie wykasowana z Firebase'a
     */
    private static Boolean bReserve = false, bTaken = false, recover = false, bWait = false, bVerified=false;

    /* Lista pozycji, które rysują zielony poligon, strefę dostępności serwisu.
   TODO Ładowana w OnCreate, a powinno być przez bazę danych */
    private static List<LatLng> PolyList;
    final String[]  fAlert = {null}, battery = {null}, childname = {null};
    protected String fFirstname;
    /* ostatnia pozycja użytkownika. Google Location API ma opóźnienie przy starcie aplikacji,
     a routing(w stanie rezerwacji) przy włączeniu potrafi być szybszy i wymagać tej zmiennej - dlatego LatLng(0,0)
     */
    private static LatLng nLastLatLng = new LatLng(0, 0), scooterLatLng = new LatLng(0, 0);
    ;


    //LAYOUT

    /*
    Logout, profil i help(do zrobienia) - SIDE BAR. Start - przycisk rezerwacji, startu lub wyłączenia skutera.
    Out - wyłączenie kontrolnego okna lub rezerwacji, Helmet - otworzenie kufra, Parking - pauza usługi. Menu - side bar
    Walk - image do dystansu od skutera - znika poza stanem rezerwacji.
    */
    private static Button nLogout, nProfile, nHelp, nStart, nCancel;
    private static ImageButton nOut, nHelmet, nParking, nMenu;
    private static ImageView nWalk;

    public PopupWindow sideBar; // SIDEBAR
    private int once = 0; // Location Update tylko raz po włączeniu aplikacji - OnLocationChanged

    // Relativelayout - okno kontroli skutera, rWindow - odniesienie się do głównego okna aby wyświetlać progressbar
    public static RelativeLayout reserveWindow, rWindow, billing;
    private View vMap; // widok samej mapy


    // GPS Persmission Request
    private static final int REQUEST_CODE_ASK_PERMISSIONS = 123;

    //Lista polylines dla Routingu
    private List<Polyline> polylines = new ArrayList<>();
    //Kolor dla Routingu
    private static final int[] COLORS = new int[]{R.color.colorSecondaryDark};

    //Mapa skuterów
    private static Map<String, Marker> markerMap = new HashMap<>();
    private static String markerMemory = "";

    //Progressbar
    static ProgressBar progressBar = null;

    /*
        time - czas ostatniej komendy rezerwacji lub startu skutera pobrany z firebase'a
        textClock - timer wyświatlany podczas rezerwacji lub startu.
        wait - count down timer 20 s oczekujący na feedback skutera
        reserve - count down timer 15 minut odliczający dozwolony czas rezerwacji
        start - chronometer liczący czas usługi.
         */
    private static Long time;
    private TextView textClock, sName, sTime, charge, date, sBalance;
    private CountDownTimer wait, reserve;
    private Chronometer cStart;

    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        user = FirebaseAuth.getInstance().getCurrentUser();
        reserveWindow = findViewById(R.id.reserveWindow); // Okno kontroli skutera
        rWindow = findViewById(R.id.MapWindow); // Fragment mapy
        billing = findViewById(R.id.billing);
        nStart = (Button) findViewById(R.id.start); // przycisk kontroli zapłonu skutera
        nOut = (ImageButton) findViewById(R.id.out); // wyjście z okna kontroli skutera
        nHelmet = (ImageButton) findViewById(R.id.helmet); // przycisk do otwierania kufra z kaskiem
        nParking = (ImageButton) findViewById(R.id.parking); // przycisk pauzy usługi
        nWalk = (ImageView) findViewById(R.id.walk); // Walk - image do dystansu od skutera - znika poza stanem rezerwacji.
        cStart = findViewById(R.id.chronometer); //start - chronometer liczący czas usługi.
        Distance = reserveWindow.findViewById(R.id.distance);
        textClock = (TextView) reserveWindow.findViewById(R.id.clock); // Wyświetlacz count down timera
        nMenu = findViewById(R.id.menu); // SIDE BAR
        textClock.setText(null);
        billing.setVisibility(View.INVISIBLE);
        cStart.setVisibility(View.INVISIBLE);
        reserveWindow.setVisibility(View.INVISIBLE);
        date = findViewById(R.id.date);
        sTime = findViewById(R.id.time);
        sName = findViewById(R.id.sName);
        charge = findViewById(R.id.charge);
        sBalance = findViewById(R.id.sBalance);
        /*
        Inicjacja progressbar
         */
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        progressBar = new ProgressBar(MapsActivity.this, null, android.R.attr.progressBarStyleLarge);
        rWindow.addView(progressBar, params);
        progressBar.setVisibility(View.INVISIBLE);

        /*
        Odniesniea do bazy danych Firebase
         */

        Uid = FirebaseAuth.getInstance().getUid();
        nbase = FirebaseDatabase.getInstance().getReference("service");
        nscooters = FirebaseDatabase.getInstance().getReference("service/scooters");
        if (user != null)
            nuser = FirebaseDatabase.getInstance().getReference("service/users").child(Uid);
        nbase.child("users").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("TESTING", dataSnapshot.getKey());
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
                Log.d("TESTING", databaseError.toString());
            }
        });
        //Inicjacja Google Maps
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            System.exit(0);
        }
        return super.onKeyDown(keyCode, event);
    } // Przycisk wstecz wyłącza aplikację

    @Override
    public void onMapReady(GoogleMap googleMap) {

        // Sprawdzenie pozwolenia na funkcję GPS

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MapsActivity.REQUEST_CODE_ASK_PERMISSIONS);
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                showExplanation(getResources().getString(R.string.GPSpermitTitle), getResources().getString(R.string.GPSpermitText), android.Manifest.permission.ACCESS_FINE_LOCATION, MapsActivity.REQUEST_CODE_ASK_PERMISSIONS);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MapsActivity.REQUEST_CODE_ASK_PERMISSIONS);
            }
        }
        markerMemory = "";
        bVerified = false;
        mMap = googleMap;//przypisanie zmiennej mapy
        vMap = findViewById(R.id.map); // przypisanie zmiennej widoku mapy
        /*
        1. Ograniczenie widoku mapy do rejonu woj. pomorskiego.
        2. Granice przesuwania się po mapie w rejonie woj. pomorskiego.
        3. Wypośrodkowanie widoku na trójmiasto.
        4. Domyślny zoom na trójmiasto.
         */
//        mMap.setMinZoomPreference(11);
//        LatLngBounds latLngBounds = new LatLngBounds((new LatLng(54.317246, 18.408935)), (new LatLng(54.709342, 18.970773)));
//        mMap.setLatLngBoundsForCameraTarget(latLngBounds);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(54.442, 18.569)));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(11));


        if (user != null) {
            valueEventListener3 = nuser.addValueEventListener((new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                /*
                nasłuchiwanie zakładki użytkownika
                 */
                    bVerified = dataSnapshot.exists();
                    fFirstname = (String) dataSnapshot.child("firstname").getValue();// Imię do side baru
                    balance =  dataSnapshot.child("balance").getValue(Double.class);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            }));
        }
        /*
        Nasłuchiwanie zakładki "scooters"
        On Child Added - dodaje markery
        On Child Changed - zmienia właściwości markerów
         */
        childEventListener = nscooters.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String childName = dataSnapshot.getKey();
                ScooterInformation scooter = dataSnapshot.getValue(ScooterInformation.class); // Klasa do nasłuchiwania tej zakładki
                String nState = scooter.state; // stan skutera : off , on , reserve , fin , pause
                LatLng location = new LatLng(Double.parseDouble(scooter.latitude), Double.parseDouble(scooter.longitude));  //pozycja skutera
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(location)
                        .icon(BitmapDescriptorFactory
                                .fromResource(R.mipmap.placeholder_red))
                        .anchor(0.5f, 0.5f).zIndex(0.4f)); // Stworzenie markera
                markerBattery(scooter.battery, marker); // Metoda tworzenia markera na podstawie stanu baterii
                markerMap.put(childName, marker); // Przypisanie markera do nazwy.

                    /*
                     sprawdzenie przy odpaleniu czy w bazie mamy jakiś skuter
                     uruchomiony lub zarezerwowany na tego użytkownika.
                     */
                if (scooter.userKey.equals(Uid)) {
                    scooterLatLng = location;
                    marker.setVisible(true);
                    time = new Date().getTime() - new Date(scooter.start).getTime(); //odnośnik czasu do countdowntimera lub chronometra
                    if (scooter.state.equals("*rE&")) { // stan reserve
                        iState = state.rE;
                    }
                    if (scooter.state.equals("*oN&")) { // stan switched on
                        iState = state.oN;
                    }
                    if (scooter.state.equals("*pA&")) { // stan pause
                        iState = state.pA;
                    }
                    if (scooter.state.equals("*fIn&")) { // stan finish
                        iState = state.fIn;
                    }
                    scooterDetails(marker); //metoda otwierania okna kontroli skutera na podstawie markera
                }
                // Przy braku rezerwacji :
                else {
                    if (scooter.state.equals("*oF&")) {
                        marker.setVisible(true); //pokazujemy wolne skutery
                    } else {
                        marker.setVisible(false); // ukrywamy wypożyczone lub uruchomione przez innych użytkowników
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                String childName = dataSnapshot.getKey(); //nazwa skutera
                ScooterInformation scooter = dataSnapshot.getValue(ScooterInformation.class); // Klasa do nasłuchiwania tej zakładki
                Marker marker = markerMap.get(childName); //odczytanie markera z HashMapa za pomocą nazwy
                LatLng location = new LatLng(Double.parseDouble(scooter.latitude), Double.parseDouble(scooter.longitude)); //zmiana pozycji
                marker.setPosition(location); // przypisanie pozycji
                marker.setAnchor(0.5f, 0.5f); // marker jest nieruchomy przy zoomie
                String nState = scooter.state;

                //zmiana widoczności markera
                if (nState.equals("*oF&")) {
                    marker.setVisible(true);
                } else {
                    marker.setVisible(false);
                }

                //zmiana globalnie stanu użytkownika
                if (scooter.userKey.equals(Uid)) {
                    scooterLatLng = location;
                    bReserve = true;
                    marker.setVisible(true);
                    if (nState.equals("*rE&")) {
                        iState = state.rE;
                    }
                    if (nState.equals("*oN&")) {
                        iState = state.oN;
                    }
                    if (nState.equals("*pA&")) { //TODO SWITCH
                        iState = state.pA;
                    }
                }

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


        /*
        Zielona strefa serwisu
         */
        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .add(new LatLng(54.539799, 18.501657), new LatLng(54.528272, 18.530382), new LatLng(54.533967, 18.547440), new LatLng(54.519617, 18.550232), new LatLng(54.519046, 18.559756), new LatLng(54.518009, 18.559742), new LatLng(54.518195, 18.555775), new LatLng(54.517697, 18.555237), new LatLng(54.517933, 18.550704), new LatLng(54.516744, 18.550356), new LatLng(54.516845, 18.548336), new LatLng(54.508311, 18.552151), new LatLng(54.504042, 18.546644), new LatLng(54.489547, 18.556387), new LatLng(54.486519, 18.549496), new LatLng(54.481109, 18.564377), new LatLng(54.475704, 18.561338), new LatLng(54.465033, 18.561787), new LatLng(54.451475, 18.567000), new LatLng(54.441458, 18.576099), new LatLng(54.422598, 18.603486), new LatLng(54.411093, 18.631995), new LatLng(54.402266, 18.659160), new LatLng(54.378632, 18.630551), new LatLng(54.353199, 18.676235), new LatLng(54.343731, 18.665745), new LatLng(54.344409, 18.627942), new LatLng(54.355663, 18.579928), new LatLng(54.374515, 18.565316), new LatLng(54.381658, 18.580450), new LatLng(54.408396, 18.552790), new LatLng(54.447407, 18.555400), new LatLng(54.460571, 18.549613), new LatLng(54.483770, 18.544150), new LatLng(54.483318, 18.540275), new LatLng(54.500596, 18.531403), new LatLng(54.516351, 18.528793), new LatLng(54.527256, 18.504526), new LatLng(54.539799, 18.501657))
                .strokeWidth((float) 0.8).strokeColor(Color.RED)
                .fillColor(Color.argb(75, 1, 152, 43)));
        PolyList = polygon.getPoints();


        //SIDE BAR
        nMenu.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                // Animacja
                View popUpView = getLayoutInflater().inflate(R.layout.navwindow, null);
                AlphaAnimation mapOff = new AlphaAnimation(1f, 0.5f);
                mapOff.setDuration(400);
                final AlphaAnimation mapOn = new AlphaAnimation(0.5f, 1f);
                mapOn.setDuration(400);
                sideBar = new PopupWindow(popUpView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.FILL_PARENT, true);
                sideBar.setAnimationStyle(R.style.NavAnimation);
                sideBar.setBackgroundDrawable(new BitmapDrawable());
                sideBar.showAtLocation(popUpView, Gravity.LEFT, 0, 0);
                sideBar.setFocusable(true);
                sideBar.setOutsideTouchable(true);
                vMap.startAnimation(mapOff);

                if (reserveWindow.getVisibility() == View.VISIBLE) {
                    reserveWindow.startAnimation(mapOff);
                }
                animationListener(mapOff, vMap, reserveWindow, 0.5f); //utrzymanie stanu po animacji
                animationListener(mapOn, vMap, reserveWindow, 1.0f);//utrzymanie stanu po animacji
                sideBar.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        vMap.startAnimation(mapOn);
                        if (reserveWindow.getVisibility() == View.VISIBLE) { // dodatkowa animacja gdy jest włączone okno kontroli skutera
                            reserveWindow.startAnimation(mapOn);
                        }
                    }
                });
                // Inicjacja przycisków menu
                nLogout = popUpView.findViewById(R.id.logout);
                nProfile = popUpView.findViewById(R.id.Profile);
                nHelp = popUpView.findViewById(R.id.Balance);
                final TextView nName = popUpView.findViewById(R.id.user_name);

                Typeface type = Typeface.createFromAsset(getAssets(), "font/astal.ttf");
                nName.setTypeface(type);
                nName.setText(fFirstname);// Imię użytkownika

                nLogout.setOnClickListener(new View.OnClickListener() { // Wylogowanie
                    @Override
                    public void onClick(View view) {
                        if (bReserve == false) {
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(MapsActivity.this, MainActivity.class);
                            nscooters.removeEventListener(childEventListener);
                            nuser.removeEventListener(valueEventListener3);
                            nbase.removeEventListener(valueEventListener1);
                            startActivity(intent);
                            sideBar.dismiss();
                            finish();
                        } else {
                            Toast.makeText(MapsActivity.this, getResources().getString(R.string.NotLoggingOut), Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }
                });
                nProfile.setOnClickListener(new View.OnClickListener() { // Okno profilu
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MapsActivity.this, Profile.class);
                        startActivity(intent);
                        sideBar.dismiss();
                    }
                });
                nHelp.setOnClickListener(new View.OnClickListener() { // opcja pomocy itp.
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MapsActivity.this, Balance.class);
                        startActivity(intent);
                        sideBar.dismiss();
                    }
                });
                if (user == null) {
                    nHelp.setVisibility(View.INVISIBLE);
                    nLogout.setVisibility(View.INVISIBLE);
                    nProfile.setText("Log in");
                    nProfile.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(MapsActivity.this, MainActivity.class);
                            startActivity(intent);
                            sideBar.dismiss();
                            finish();
                        }
                    });
                }
            }
        });
        googleMap.setOnMarkerClickListener(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //Inicjacja usług google
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);

    }

    protected synchronized void buildGoogleApiClient() {
        nGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        nGoogleApiClient.connect();
    } // funkcja lokalizacji

    @Override
    public void onLocationChanged(Location location) {
        nLastLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (iState == state.rE) {
            erasePolyline();
            findScooter(nLastLatLng, scooterLatLng);
        }
        for (; once < 1; once++) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(nLastLatLng));
            mMap.moveCamera(CameraUpdateFactory.zoomTo(11));
        }
        if (iState == state.oN) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(scooterLatLng));
        }
    } //

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        nLocationRequest = new LocationRequest();
        nLocationRequest.setInterval(10000);
        nLocationRequest.setFastestInterval(10000);
        nLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(nGoogleApiClient, nLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //TODO
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {

        scooterDetails(marker);
        return true;
    }

    private void requestPermission(String permissionName, int permissionRequestCode) {
        ActivityCompat.requestPermissions(this,
                new String[]{permissionName}, permissionRequestCode);
    }

    private void showExplanation(String title, String message, final String permission, final int permissionRequestCode) {
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

    private void confirmParking(final String scooter) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.AreYouSure)
                .setMessage(R.string.Switchingoff)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        parkedScooter(scooter);
                    }
                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }// Pop up przy wyłączaniu skutera na pauze

    private void confirmSwitchingOff(final String scooter) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.AreYouSure))
                .setMessage(getResources().getString(R.string.Switchingoff))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        stoppedScooter(scooter);
                    }
                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    } // Pop up przy wyłączaniu skutera

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MapsActivity.REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    buildGoogleApiClient();
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mMap.setMyLocationEnabled(true);
                    Toast.makeText(this, getResources().getString(R.string.PermisionGranted), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, getResources().getString(R.string.PermisionDenied), Toast.LENGTH_SHORT).show();
                }
        }
    }

    private Integer markerBattery (String mBattery , Marker marker){
        Integer p=null;
        Integer battery = Integer.valueOf(mBattery);
        if (battery>30){
            marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.placeholder_green));
            p=4;
        }
        if ((battery<=30)&(battery>=20)){
            marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.placeholder_yellow));
            p=3;
        }
        if ((battery<20)&(battery>=10)){
            marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.placeholder_yellow));
            p=2;
        }
        if (battery<10){
            marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.placeholder_red));
            p=1;
        }
        return p;
    }

    private void findScooter (LatLng start, LatLng end) {
        Routing routing = new Routing.Builder()
                .travelMode(Routing.TravelMode.WALKING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(start, end)
                .build();
        routing.execute();
    } // Routing trasy od uzytkownika do zarezerwowanego skutera

    @Override
    public void onRoutingFailure(RouteException e) {
    }

    @Override
    public void onRoutingStart() {
    }



    @Override
    public void onRoutingSuccess(ArrayList<com.directions.route.Route> route, int shortestRouteIndex) { //Rysowanie lini do trasy routingu

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
            //zapisanie dystansu do zmiennej globalnej i wyświetlenie w reserveWindow
            nDistance = (String.valueOf(route.get(i).getDistanceValue()))+"  "+getResources().getString(R.string.Metres);
            Distance.setText(nDistance);
        }
    }

    @Override
    public void onRoutingCancelled() {

    }
    private void erasePolyline (){
        for (Polyline line : polylines){
            line.remove();
        }
        polylines.clear();
    } //kasowanie wczesniejszej trasy. Aktualizacja co 10 sekund przy każdym OnLocationChanged

    private void scooterDetails(final Marker marker){
        if(!bReserve&&!bWait) { // przy rezerwacji jak i oczekiwaniu na inny skuter nie możemy nacisnąć na inny marker.
            mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()), 300, new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    if(iState==state.oF){ // stan przed rezerwacją
                        nHelmet.setVisibility(View.INVISIBLE); // Nie można otworzyć kufra
                        nParking.setVisibility(View.INVISIBLE);//nie można pauzować w tym stanie
                        nOut.setVisibility(View.INVISIBLE);// można wyjść z tego okna
                        nWalk.setImageResource(R.drawable.distance_faded);// nie widać odległości i routingu
                        nStart.setText(getResources().getString(R.string.reserve)); // przycisk z funkcją rezerwacji
                        nStart.setTextSize(12);
                        Distance.setVisibility(View.INVISIBLE);// nie widać odległości i routingu
                    }
                    if(iState==state.oN){
                        nParking.setVisibility(View.VISIBLE);// Można zapauzować
                        nHelmet.setVisibility(View.VISIBLE);// Można otworzyć kufer
                        Distance.setVisibility(View.INVISIBLE); // Odległości nie pokazuje
                        nOut.setVisibility(View.INVISIBLE); // Nie można wyjść(trzeba nacisnąć "wyłącz")
                        nWalk.setImageResource(R.drawable.distance_faded);
                        nStart.setText(getResources().getString(R.string.finish));// opcja "wyłącz"
                        nStart.setTextSize(12);

                    }
                    if(iState==state.pA){
                        nParking.setVisibility(View.INVISIBLE);// Nie widać
                        nHelmet.setVisibility(View.INVISIBLE);// Nie widać
                        Distance.setVisibility(View.INVISIBLE); // Nie widać
                        nOut.setVisibility(View.INVISIBLE);// Nie widać
                        nWalk.setImageResource(R.drawable.distance_faded);// Nie widać
                        nStart.setText(getResources().getString(R.string.start)); // Można wrócić do usługi i uruchomić skuter tylko
                        nStart.setTextSize(12);

                    }
                    if(iState==state.rE){
                        nHelmet.setVisibility(View.INVISIBLE);// Nie widać
                        nParking.setVisibility(View.INVISIBLE);// Nie widać
                        nOut.setVisibility(View.VISIBLE);// Można wyłączyć rezerwację i wyjść z okna
                        nStart.setText(getResources().getString(R.string.start));// Opcja wystartowania skutera
                        nStart.setTextSize(12);
                        Distance.setVisibility(View.VISIBLE); // Widać dystans od skutera wraz z routingiem
                        nWalk.setImageResource(R.drawable.distance);// widać ikonkę dystansu
                    }
                    /*
                    Przydaje się przy zmienianiu z jednego markera na drugi - pamięć zapamiętuje
                    wcześniejszy marker i zmienia ikonę na stan podstawowy tak żeby tylko wybrany marker
                    miał wyszczególnioną ikonkę np. placeholder_red_big.
                                         */
                    if(markerMemory!=""){
                        Marker tempMarker = markerMap.get(markerMemory);
                        markerBattery( tempMarker.getTitle() , tempMarker);
                        markerMemory="";
                    }else {//jeżeli wcześniej nie był wybrany inny marker i reserveWindow nie było wyświetlone włączamy animację wysuwania okna
                        Animation animationShow = AnimationUtils.loadAnimation(MapsActivity.this, R.anim.popup_show);
                        reserveWindow.startAnimation(animationShow);
                        animationListener(animationShow, reserveWindow, View.VISIBLE);
                    }
                    final Integer[] bat = new Integer[1]; // inicjacja stanu baterii
                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.placeholder_red_big)); // większa ikonka wybranego markera
                    marker.setAnchor(0.5f, 0.5f);

                    //LAYOUT
                    final Animation animationHide = AnimationUtils.loadAnimation(MapsActivity.this, R.anim.popup_hide);
                    final TextView myAddress = (TextView) reserveWindow.findViewById(R.id.address);
                    final TextView Range = (TextView) reserveWindow.findViewById(R.id.range);
                    final TextView Name = (TextView) reserveWindow.findViewById(R.id.name);
                    final TextView Plates = (TextView) reserveWindow.findViewById(R.id.plates);
                    final TextView myCity = (TextView) reserveWindow.findViewById(R.id.city);

                    // GEOLOCATION
                    Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
                    final LatLng latLng = marker.getPosition();
                    List<Address> addresses;
                    try {
                        addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                        if (addresses != null && addresses.size() > 0) {
                            addressLine = addresses.get(0).getAddressLine(0);
                            String address[]= addressLine.split(","); // Usuwamy ostatnie słowo z adresu :"Poland"
                            if(!address[0].equals("Unnamed Road"))
                                myAddress.setText(address[0]); // pierwsza linijka
                            else
                                myAddress.setText(null);// Czasem nie łapie adresu, nie chcemy aby pokazywało Unnamed Road
                            if(address[1].charAt(3)=='-'){//nie pokazujemy kodu pocztowego
                                String[] sCity = address[1].split(" ");
                                myCity.setText(sCity[2]); // miasto pokazane w linijce poniżej
                            }
                            else myCity.setText(address[1]);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    for (Map.Entry<String, Marker> entry : markerMap.entrySet()) {
                        if (entry.getValue().equals(marker)) {
                            childname[0] = entry.getKey();//odczytanie nazwy skutera z mapy za pomocą markera
                        }
                    }
                    final String finalChildname = childname[0];

                    markerMemory = finalChildname; // zapisanie ostatniego markera na wypadek przeskoczenia do innego markera (zmiana ikonki patrz wyżej)

                    valueEventListener1 = new ValueEventListener() {//TODO broadcast receiver, service, async task
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // TODO do uproszczenia
                            /*
                            Klasa zakładki danych skutera
                             */
                            ScooterInformation scooter = dataSnapshot.child("scooters").child(childname[0]).getValue(ScooterInformation.class);

                             /*
                            Klasa zakładki użytkownika
                             */
                                if (bVerified) {
                                    UserInformation userInformation = dataSnapshot.child("users").child(Uid).getValue(UserInformation.class);
                                    reservedScooter = userInformation.scooterName; //zapamiętanie zarezewowanego skutera users/$Uid/scooterName
                                    if (!reservedScooter.equals("")) {
                                        bReserve = true;//użytkownik ma przypisany skuter
                                    } else {
                                        reserveWindow.setVisibility(View.INVISIBLE);//użytkownik nie ma przypisanego skutera
                                        bReserve = false;
                                    }

                                    // ALERTY użytkownika
                                    fAlert[0] = userInformation.alert;
                                    if (fAlert[0].equals("overtime")) { // Po 15 minutach rezerwacji dostaje z serwera komendę, aby wyłączyć rezerwację
                                        nuser.child("alert").setValue("");
                                        Intent intent = new Intent(MapsActivity.this, MapsActivity.class);
                                        startActivity(intent);
                                        Toast.makeText(MapsActivity.this, getResources().getString(R.string.overtime), Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                    if ((iState == state.oN) || (iState == state.pA)) {
                                        bat[0] = Integer.valueOf(scooter.battery);
                                        if (fAlert[0].equals("no battery")) { // wyłączenie skutera przy niskiej baterii TODO ogranicznik
                                            Toast.makeText(MapsActivity.this, "Low Battery !", Toast.LENGTH_SHORT).show();
                                            nuser.child("alert").setValue("");
                                            fAlert[0] = "";
                                        }
                                        if (fAlert[0].equals("low balance")) { // niski stan konta
                                            Toast.makeText(MapsActivity.this, "Niski stan konta !", Toast.LENGTH_SHORT).show();
                                            nuser.child("alert").setValue("");
                                            fAlert[0] = "";
                                        }
                                        if (fAlert[0].equals("no balance")) { // brak pieniędzy
                                            Toast.makeText(MapsActivity.this, "Brak środków na koncie ! Odstaw skuter", Toast.LENGTH_SHORT).show(); //TODO ogranicznik
                                            nuser.child("alert").setValue("");
                                            fAlert[0] = "";
                                        }
                                        if (fAlert[0].equals("full day")) { // powyżej 24 h
                                            Toast.makeText(MapsActivity.this, "Przekroczona doba użytkowania ! Odstaw skuter", Toast.LENGTH_SHORT).show();//TODO ogranicznik
                                            nuser.child("alert").setValue("");
                                            fAlert[0] = "";
                                        }
                                }
                            }
                            time = new Date().getTime() - new Date(scooter.start).getTime(); // timestamp ostatniej komendy rezerwacji lub startu
                            marker.setTitle(scooter.battery); // zapamiętanie stanu baterii w tytule markera

                            battery[0] = scooter.battery;// zmienna globalna stanu baterii
                            scooterLatLng = new LatLng(Double.parseDouble(scooter.latitude), Double.parseDouble(scooter.longitude));
                            if((iState==state.rE)||(iState==state.oN)||(iState==state.pA))
                                marker.setPosition(scooterLatLng);
                            Range.setText(battery[0]+" km");// Wyświetlenie stanu baterii
                            String sName[] = scooter.name.split(",");// Wyświetlenie nazwy i nr rejestracyjnego
                            Name.setText(sName[0]);
                            Plates.setText(sName[1]);
                            if(!scooter.userKey.equals("")){// TODO upewnienie się czy ktoś nie zdążył zarezerwować przed użytkownikiem, może wyłączać okno
                                bTaken = true;
                            }else bTaken=false;
                            // aktualizacja stanu użytkownika
                            if (scooter.state.equals("*rE&")){
                                iState=state.rE;
                            }
                            if (scooter.state.equals("*oF&")){
                                iState=state.oF;
                            }
                            if (scooter.state.equals("*fIn&")){
                                iState=state.fIn;
                            }
                            if (scooter.state.equals("*oN&")){
                                iState=state.oN;
                            }
                            if (scooter.state.equals("*pA&")){
                                iState=state.pA;
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    };
                    nbase.addValueEventListener(valueEventListener1);
                    mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                        @Override
                        public void onMapClick(LatLng latLng) {
                            animationListener(animationHide, reserveWindow, View.INVISIBLE );

                            // działa tylko w stanie state.oF, w innym przypadku trzeba wyłączyć przyciskiem
                            if ((!markerMemory.equals(""))&&(iState == state.oF)) {
                                markerMemory = "";
                                reserveWindow.startAnimation(animationHide);
                                markerBattery(battery[0], marker);
                                if(wait!=null) {// gdy jest oczekiwanie na odpowiedź skutera
                                    wait.cancel();
                                    nscooters.child(finalChildname).child("check").removeValue(); // wycofanie się z komendy
                                    progressBar.setVisibility(View.INVISIBLE);
                                    bWait = false;
                                }
                                if(eventListener !=null) {
                                    nscooters.child(finalChildname).child("feedback").removeEventListener(eventListener);
                                }
                                reserveWindow.setVisibility(View.GONE);
                                nbase.removeEventListener(valueEventListener1);
//                                nbase.addChildEventListener(valueEventListener);
                            }

                        }

                    });
                    nOut.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            markerMemory = "";
                                erasePolyline();
                                nscooters.child(reservedScooter).child("userKey").setValue("");
                                nscooters.child(reservedScooter).child("state").setValue("*oF&");
                                nuser.child("scooterName").setValue("");
                                reserve.cancel();
                                textClock.setText(null);
                                animationListener(animationHide, reserveWindow, View.INVISIBLE );
                                markerBattery(battery[0], marker);
                                reserveWindow.startAnimation(animationHide);
                                reserveWindow.setVisibility(View.GONE);
                                nbase.removeEventListener(valueEventListener1);
//                                nbase.addChildEventListener(valueEventListener);
                                //listener feedback'u ze skutera(patrz wait)
                                nscooters.child(finalChildname).child("feedback").removeEventListener(eventListener);
                                progressBar.setVisibility(View.INVISIBLE);
                                iState = state.oF;
                                bReserve=false;
                                Distance.setText("");
                        }
                    });
                    // Odzyskanie stanu po restarcie :
                    if(iState.equals(state.rE)){
                        recover = true;
                        reservedScooter(finalChildname,latLng, 900000 - time );
                    }
                    if(iState.equals(state.fIn)){
                        recover = true;
                        stoppedScooter(finalChildname);
                    }
                    if(iState.equals(state.oN)){
                        recover = true;
                        startedScooter(finalChildname,latLng);
                    }
                    if(iState.equals(state.pA)){
                        recover = true;
                        parkedScooter(finalChildname);
                    }
                    nStart.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (iState == state.oF) {
                                if (!bTaken) {
                                    reservedScooter(finalChildname, latLng,Long.valueOf(900000) );
                                } else
                                    Toast.makeText(MapsActivity.this, getResources().getString(R.string.ScooterTaken), Toast.LENGTH_LONG).show();

                            }
                            if (iState == state.rE){
                                Toast.makeText(MapsActivity.this, getResources().getString(R.string.Starting), Toast.LENGTH_LONG).show();
                                startedScooter(finalChildname, latLng);
                            }
                            if (iState == state.pA){
                                Toast.makeText(MapsActivity.this, getResources().getString(R.string.Starting), Toast.LENGTH_LONG).show();
                                startedScooter(finalChildname, latLng);
                            }
                            if (iState == state.oN){
                                confirmSwitchingOff(finalChildname); // Trzeba potwierdzić wyłączenie skutera
                                bat[0]=null;
                            }
                        }
                    });
                    nHelmet.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            nscooters.child(reservedScooter).child("state").setValue("*hE&");//TODO Otworzenie kufra
                        }
                    });
                    nParking.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            confirmParking(finalChildname);// Trzeba potwierdzić wyłączenie skutera
                        }
                    });
                }

                @Override
                public void onCancel() {

                }
            });
        }
    } // OKNO KONTROLI SKUTERA

    /*
    Wystartowanie skutera przyciskiem lub przy starcie aplikacji włączenie stanu startu.
     */
    private void startedScooter(final String scooterName,  final LatLng latLng){
        wait = new CountDownTimer(40000,1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                cancelPost(scooterName);
            }
        }; // oczekiwanie na odpowiedź skutera
        nStart.setClickable(false);
        nOut.setClickable(false);
        nscooters.child(scooterName).child("check").setValue(true); // sygnał sprawdzający

        // nasłuchiwanie feedbacku
        eventListener = nscooters.child(scooterName).child("feedback").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Boolean data =  dataSnapshot.exists(); // jeżeli feedback istnieje
                if(data){
                    wait.cancel();//odliczanie skończone
                    if(reserve !=null)
                        reserve.cancel();//jeżeli było odliczanie rezerwacji to kasujemy
                    textClock.setText(null); // ukrycie wyświetlacza countdown
                    cStart.setVisibility(View.VISIBLE); // ukazanie chromometra do odliczania czasu usługi
                    progressBar.setVisibility(View.INVISIBLE); //ukrycie progressbaru
                    erasePolyline(); // wykasowanie routingu

                    Distance.setVisibility(View.INVISIBLE);//brak dystansu routingu podczas usługi
                    nWalk.setImageResource(R.drawable.distance_faded);//ukrycie ikonki dystansu
                    nOut.setVisibility(View.INVISIBLE);//brak możliwości wyjścia
                    nHelmet.setVisibility(View.VISIBLE);//możliwość otwarcia kufra
                    nParking.setVisibility(View.VISIBLE);//możliwość pauzy
                    nStart.setText(getResources().getString(R.string.finish));//opcja wyłączenia skutera

                    //tylko w przypadku wejścia pierwszy raz do startedScooter wysyłamy timestamp'a (przy restarcie omija to polecenie)
                    if((!recover)&&(iState!=state.pA))
                        nscooters.child(scooterName).child("start").setValue(ServerValue.TIMESTAMP);//tylko w przypadku naciśnięcia pierwszy raz
                    recover = false;//pomocnicza flaga do timestampa przy nacisnieciu startu musi być z powrotem false


                    Distance.setText("");//wyzerowanie wyświetlacza dystansu
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));//wypośrodkowanie do pozycji skutera
                    mMap.moveCamera(CameraUpdateFactory.zoomTo(15));//zzoomowanie

                    nscooters.child(scooterName).child("state").setValue("*oN&");//włączenie skutera
                    nscooters.child(scooterName).child("feedback").removeValue();//usunięcie z bazy feedbacku
                    nscooters.child(scooterName).child("feedback").removeEventListener(eventListener);//usunięcie listenera feedbacku
                    nStart.setClickable(true);
                    cStart.setBase(SystemClock.elapsedRealtime()-time);// ustawienie bazy chromometra z firebase
                    cStart.start();//wystartowanie countera z podanej bazy na poziomie aplikacji
                }
                else {
                    progressBar.setVisibility(View.VISIBLE);
                    wait.start();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /*
    Wyłączenie w stan pauzy skutera przyciskiem lub przy starcie aplikacji ustawienie tego stanu.
     */
    private void parkedScooter(final String scooterName){
        wait = new CountDownTimer(40000,1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                cancelPost(scooterName);
            }
        };
        nscooters.child(scooterName).child("check").setValue(true);
        nStart.setClickable(false);
        eventListener = nscooters.child(scooterName).child("feedback").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Boolean data =  dataSnapshot.exists();
                if(data){
                    wait.cancel();
                    cStart.setVisibility(View.VISIBLE);
                    nscooters.child(reservedScooter).child("state").setValue("*pA&");
                    nParking.setVisibility(View.INVISIBLE);
                    nHelmet.setVisibility(View.INVISIBLE);
                    nOut.setVisibility(View.INVISIBLE);
                    nWalk.setImageResource(R.drawable.distance_faded);
                    nStart.setText(getResources().getString(R.string.start));
                    nStart.setTextSize(12);
                    Distance.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                    nscooters.child(scooterName).child("feedback").removeValue();
                    nscooters.child(scooterName).child("feedback").removeEventListener(eventListener);
                    cStart.setBase(SystemClock.elapsedRealtime()-time);
                    cStart.start();
                    recover = false;
                    nStart.setClickable(true);
                }
                else {
                    progressBar.setVisibility(View.VISIBLE);
                    wait.start();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

        /*
    Zarezerwowanie skutera przyciskiem lub przy starcie aplikacji włączenie stanu rezerwacji.
     */
    private void reservedScooter(final String scooterName, final LatLng latLng, Long time){
    if(user!=null) {
        if (user.isEmailVerified()) {
          if(bVerified) {
              if (balance > 0) {
                  wait = new CountDownTimer(40000, 1000) {
                      @Override
                      public void onTick(long l) {

                      }

                      @Override
                      public void onFinish() {
                          cancelPost(scooterName);
                      }

                  };
                  reserve = new CountDownTimer(time, 1000) {

                      @Override
                      public void onTick(long l) {
                          String v = String.format(Locale.GERMANY, "%02d", l / 60000);
                          int va = (int) ((l % 60000) / 1000);
                          textClock.setText(v + ":" + String.format(Locale.GERMANY, "%02d", va));
                      }

                      @Override
                      public void onFinish() {
                          nuser.child("alert").setValue("overtime");
                      }
                  };
                  bWait = true;
                  nscooters.child(scooterName).child("check").setValue(true);
                  nStart.setClickable(false);
                  eventListener = nscooters.child(scooterName).child("feedback").addValueEventListener(new ValueEventListener() {
                      @Override
                      public void onDataChange(DataSnapshot dataSnapshot) {
                          Boolean data = dataSnapshot.exists();
                          if (data) {
                              wait.cancel();
                              reserve.start();
                              progressBar.setVisibility(View.INVISIBLE);
                              if (!recover)
                                  nscooters.child(scooterName).child("start").setValue(ServerValue.TIMESTAMP);
                              nscooters.child(scooterName).child("userKey").setValue(Uid);
                              nuser.child("scooterName").setValue(scooterName);
                              nscooters.child(scooterName).child("state").setValue("*rE&");
                              Distance.setVisibility(View.VISIBLE);
                              nHelmet.setVisibility(View.INVISIBLE);
                              nParking.setVisibility(View.INVISIBLE);
                              nOut.setVisibility(View.VISIBLE);
                              nWalk.setImageResource(R.drawable.distance);
                              findScooter(nLastLatLng, latLng);
                              LatLngBounds latLngBounds = LatLngBounds.builder()
                                      .include(nLastLatLng)
                                      .include(latLng)
                                      .build();

                              CameraUpdate zoom = CameraUpdateFactory.newLatLngBounds(latLngBounds, 200); //TODO Pobrać screen
                              try {
                                  mMap.moveCamera(zoom);
                              }finally {

                              }
                              nStart.setText(getResources().getString(R.string.start));
                              nscooters.child(scooterName).child("feedback").removeValue();
                              nscooters.child(scooterName).child("feedback").removeEventListener(eventListener);
                              recover = false;
                              bWait = false;
                              nStart.setClickable(true);
                          } else {
                              progressBar.setVisibility(View.VISIBLE);
                              wait.start();
                          }
                      }

                      @Override
                      public void onCancelled(DatabaseError databaseError) {

                      }
                  });
              } else {
                  Toast.makeText(this, "You need to top up balance", Toast.LENGTH_SHORT).show();
              }
          }else {
              Toast.makeText(this, "You need to wait for document verification", Toast.LENGTH_SHORT).show();
          }
        } else {
            Toast.makeText(this, "You need to verify email", Toast.LENGTH_SHORT).show();
        }
    }else{
        Intent intent = new Intent(MapsActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    }

        /*
    Zatrzymanie skutera przyciskiem lub przy starcie aplikacji włączenie stanu OCZEKIWANIA na wyłączenie.
    */
    private void stoppedScooter(final String scooterName){
        final boolean contain = PolyUtil.containsLocation(scooterLatLng, PolyList, true); // contain oznacza zawarcie się markera w poligonie
//        nbase.removeEventListener(valueEventListener);
        if ((contain==true)||(iState==state.fIn)) { //fIn tylko przy braku baterii, wyłaczy się gdziekolwiek
            markerMemory = "";
            nHelmet.setVisibility(View.INVISIBLE);
            nParking.setVisibility(View.INVISIBLE);
            vMap.setClickable(false);
            progressBar.setVisibility(View.VISIBLE);  //To show ProgressBar
            reserveWindow.setClickable(false);
            nStart.setClickable(false);
            nOut.setClickable(false);
            if(iState!=state.fIn) {
                Toast.makeText(MapsActivity.this, getResources().getString(R.string.Stopping), Toast.LENGTH_LONG).show();
            }
            nuser.child("fIn").setValue(ServerValue.TIMESTAMP);// zapisujemy tą zmienną do użytkownika na wszelki wypadek w przypadku usterki.
            nscooters.child(scooterName).child("state").setValue("*fIn&"); //wysyłamy fin do serwera. Po stronie serwera następuje rozliczanie użytkownika
            //nasłuchujemy aż serwer wyłączy skuter, dostanie od niego odpowiedź i rozliczy użytkownika. Wtedy włączamy okno billing z ostatnim przejazdem.
            valueEventListener2 = nuser.child("scooterName").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String scooter = (String) dataSnapshot.getValue();
                    if (scooter.equals("")) {
                        cStart.setVisibility(View.INVISIBLE);
                        cStart.stop();
                        iState=state.oF;
                        progressBar.setVisibility(View.GONE);     // To Hide ProgressBar
                        Intent intent = new Intent(MapsActivity.this, Billing.class);
                        startActivity(intent);
                        nuser.child("scooterName").removeEventListener(valueEventListener2);
                        finish();
                    }
                    else;
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        else Toast.makeText(MapsActivity.this, getResources().getString(R.string.OutOfBounds), Toast.LENGTH_LONG).show();

    }


    //Nasłuchiwacze animacji chowania i pokazywania okna reserveWindow oraz side bara
    private void animationListener(Animation animation, final View view1, final View view2, final float setting){
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view1.setAlpha(setting);
                view2.setAlpha(setting);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }
    private void animationListener(Animation animation , final View view1,  final int visibility){
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view1.setVisibility(visibility);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    // Wyłączenie wszelkich komend po nieudanym połączeniu ze skuterem i wyświetlenie komunikatu.
    private void cancelPost(final String scooterName){
        progressBar.setVisibility(View.INVISIBLE);
        nscooters.child(scooterName).child("check").removeValue();
        nscooters.child(scooterName).child("feedback").removeEventListener(eventListener);
        Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
    }
}

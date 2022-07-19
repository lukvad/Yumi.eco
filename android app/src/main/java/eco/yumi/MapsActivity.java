package eco.yumi;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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
import com.google.android.gms.maps.model.MapStyleOptions;
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

import org.json.JSONException;
import org.json.JSONObject;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, GoogleMap.OnMarkerClickListener, RoutingListener {
    private static final String TAG = "MapsActivity";  // TAG do logów

    // GOOGLE MAPS

    private GoogleMap mMap; // Serwis Google Maps
    private SupportMapFragment mapFragment; // Fragment Google Maps
    private LocationRequest nLocationRequest; // Serwis lokalizacji GPS telefonu
    private static GoogleApiClient nGoogleApiClient; // Google API
    // FIREBASE
    private static Polygon polygon;
    private static ValueEventListener eventListener, valueEventListener1, valueEventListener2, valueEventListener3;
    private static ChildEventListener childEventListener;
    private static DatabaseReference nscooters, nuser,ncard, npoly; //Scooters - zakładka skuterów , User - zakładka użytkowników

    // Inicjacja zmiennej dystansu między użytkownikiem a skuterem ; Uid - klucz użytkownika Firebase
    // reservedScooter - zarezerwowany skuter pobierany z firebase'a : users/$Uid/scooterName
    private static String nDistance = null, addressLine, reservedScooter, pay="";
    protected static String Uid;
    protected Double balance;
    protected static LatLng latLng[];
    private static PolygonOptions polygonOptions = new PolygonOptions();
    protected static Integer poly=0;
    private static TextView Distance, Register;
    private static String chosenScooter, takeDrive=null;
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
    final String[]  battery = {null};
    protected String fEmail, fAlert;
    /* ostatnia pozycja użytkownika. Google Location API ma opóźnienie przy starcie aplikacji,
     a routing(w stanie rezerwacji) przy włączeniu potrafi być szybszy i wymagać tej zmiennej - dlatego LatLng(0,0)
     */
    private static LatLng nLastLatLng, scooterLatLng = new LatLng(054.404705, 18.603867);
    ;


    //LAYOUT

    /*
    Logout, profil i help(do zrobienia) - SIDE BAR. Start - przycisk rezerwacji, startu lub wyłączenia skutera.
    Out - wyłączenie kontrolnego okna lub rezerwacji, Helmet - otworzenie kufra, Parking - pauza usługi. Menu - side bar
    Walk - image do dystansu od skutera - znika poza stanem rezerwacji.
    */
    private static Button nLogout, nProfile, nStart , nLoupe, nPromotion, nLogin;
    private static ImageButton nHelmet, nParking, nMenu, nOut, nPhone;
    private static ImageView nWalk, userGrey;

    public PopupWindow sideBar; // SIDEBAR
    private int once = 0; // Location Update tylko raz po włączeniu aplikacji - OnLocationChanged

    // Relativelayout - okno kontroli skutera, rWindow - odniesienie się do głównego okna aby wyświetlać progressbar
    public static RelativeLayout reserveWindow, rWindow;
    private View vMap; // widok samej mapy


    // GPS Persmission Request
    private static final int REQUEST_CODE_ASK_PERMISSIONS = 123;

    //Lista polylines dla Routingu
    private List<Polyline> polylines = new ArrayList<>();
    //Kolor dla Routingu
    private static final int[] COLORS = new int[]{R.color.colorAccent};

    //Mapa skuterów
    private static Map<String, Marker> markerMap = new HashMap<>();
    private static String markerMemory = "", cost="", version="", nVersion ="";
    ImageView promotion,logOut;
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
    private TextView textClock2, sName, sTime, charge, date, sBalance, loggedas, price;
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
        nStart = (Button) findViewById(R.id.start); // przycisk kontroli zapłonu skutera
        nOut =  findViewById(R.id.out); // wyjście z okna kontroli skutera
        nHelmet = (ImageButton) findViewById(R.id.helmet); // przycisk do otwierania kufra z kaskiem
        nParking = (ImageButton) findViewById(R.id.parking); // przycisk pauzy usługi
        nWalk = (ImageView) findViewById(R.id.walk); // Walk - image do dystansu od skutera - znika poza stanem rezerwacji.
        cStart = findViewById(R.id.chronometer); //start - chronometer liczący czas usługi.
        Distance = reserveWindow.findViewById(R.id.distance);
        textClock2 = (TextView) reserveWindow.findViewById(R.id.clock2);// Wyświetlacz count down timera
        nPhone = findViewById(R.id.phone);
        nMenu = findViewById(R.id.menu); // SIDE BAR
        cStart.setVisibility(View.INVISIBLE);
        reserveWindow.setVisibility(View.INVISIBLE);
        date = findViewById(R.id.date);
        sTime = findViewById(R.id.time);
        sName = findViewById(R.id.sName);
        charge = findViewById(R.id.charge);
        sBalance = findViewById(R.id.sBalance);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Yumi");
        /*
        Inicjacja progressbar
         */
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        progressBar = new ProgressBar(MapsActivity.this, null, android.R.attr.progressBarStyleLarge);
        rWindow.addView(progressBar, params);
        progressBar.setVisibility(View.INVISIBLE);
        if(savedInstanceState==null) {
            Bundle register = getIntent().getExtras();
            if (register != null) {
                takeDrive = register.getString("name");
            }
        }
        /*
        Odniesniea do bazy danych Firebase
         */

        Uid = FirebaseAuth.getInstance().getUid();
        nscooters = FirebaseDatabase.getInstance().getReference("service/scooters");
        npoly = FirebaseDatabase.getInstance().getReference("poly");
        if (user != null) {
            ncard = FirebaseDatabase.getInstance().getReference("service/card").child(Uid);
            nuser = FirebaseDatabase.getInstance().getReference("service/users").child(Uid);
        }
        //Inicjacja Google Maps
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        version = BuildConfig.VERSION_NAME;

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        markerMemory="";
        if((wait!=null)&&(iState==state.oF)) {
            if(!chosenScooter.equals("")) {
                nscooters.child(chosenScooter).child("userKey").setValue("");
                nscooters.child(chosenScooter).child("check").removeValue();
            }
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            System.exit(0);
        }
        return super.onKeyDown(keyCode, event);
    } // Przycisk wstecz wyłącza aplikację

    @Override
    public void onMapReady(GoogleMap googleMap) {

        // Sprawdzenie pozwolenia na funkcję GPS
        mMap = googleMap;//przypisanie zmiennej mapy
        nLocationRequest = new LocationRequest();
        nLocationRequest.setInterval(10000);
        nLocationRequest.setFastestInterval(10000);
        nLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
        buildGoogleApiClient();

        //Inicjacja usług google

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
        markerMemory = "";
        bVerified = false;
        vMap = findViewById(R.id.map); // przypisanie zmiennej widoku mapy
        /*

        1. Wypośrodkowanie widoku na trójmiasto.
        2. Domyślny zoom na trójmiasto.
         */
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(54.406201, 18.594825), 11.5f));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(54.442, 18.569)));
//        mMap.moveCamera(CameraUpdateFactory.zoomTo(11));

        FirebaseDatabase.getInstance().getReference().child("cost").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Double dCost = dataSnapshot.getValue(Double.class);
                cost = String.valueOf(dCost);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        FirebaseDatabase.getInstance().getReference().child("version").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                nVersion = dataSnapshot.getValue(String.class);
                if(!nVersion.equals(version)) {
                    if (nVersion.charAt(0)==version.charAt(0)&&nVersion.charAt(2)!=version.charAt(2))
                        update(false,getResources().getString(R.string.update2));
                    else
                        update(true,getResources().getString(R.string.update3));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (user != null) {
            valueEventListener3 = nuser.addValueEventListener((new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                /*
                nasłuchiwanie zakładki użytkownika
                 */
                    bVerified = dataSnapshot.exists();
                    fEmail = (String) dataSnapshot.child("email").getValue();// Imię do side baru
                    balance =  dataSnapshot.child("balance").getValue(Double.class);
                    pay =  dataSnapshot.child("pay").getValue(String.class);
                    if (bVerified) {
                        UserInformation userInformation = dataSnapshot.getValue(UserInformation.class);
                        if(dataSnapshot.child("newUser").exists()){
                            nuser.child("newUser").removeValue();
                            showNotification(getResources().getString(R.string.verificationNote),getResources().getString(R.string.verificationNote2),MapsActivity.class, 1);
                        }
                        nuser.child("version").setValue(version);
                        String phone = dataSnapshot.child("phone").getValue(String.class);
                        if(!dataSnapshot.child("verPhone").exists()){
                            if(phone!=null&&phone.length()>8)//+48530184485
                            {
                                if(!phone.contains("+")){
                                    phone = "+48"+phone; //+48530184485
                                }
                                Intent intent = new Intent(MapsActivity.this, VerificationPhoneActivity.class);
                                intent.putExtra("phone", phone);
                                startActivity(intent);
                            }else
                                phonePopUp();
                        }

                        reservedScooter = userInformation.scooterName; //zapamiętanie zarezewowanego skutera users/$Uid/scooterName
                        if (!reservedScooter.equals("")) {
                            bReserve = true;//użytkownik ma przypisany skuter
                        } else {
                            reserveWindow.setVisibility(View.INVISIBLE);//użytkownik nie ma przypisanego skutera
                            bReserve = false;
                        }

                        // ALERTY użytkownika
                        fAlert = userInformation.alert;
                        if(!fAlert.equals("")){
                            alertPopUp(fAlert);
                        }
                    }
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
                                .fromResource(R.drawable.placeholder_full))
                        .anchor(0.5f, 0.5f).zIndex(0.4f)); // Stworzenie markera
                markerBattery(scooter.battery, marker); // Metoda tworzenia markera na podstawie stanu baterii
                markerMap.put(childName, marker); // Przypisanie markera do nazwy.

                    /*
                     sprawdzenie przy odpaleniu czy w bazie mamy jakiś skuter
                     uruchomiony lub zarezerwowany na tego użytkownika.
                     */
                if (scooter.userKey.equals(Uid)) {
                    scooterLatLng = location;
                    bReserve=true;
                    recover=true;
                    marker.setVisible(true);
                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.placeholder_big)); // większa ikonka wybranego markera
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
                if(takeDrive!=null&&childName.equals(takeDrive)){
                    scooterDetails(marker);
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
                    markerBattery(scooter.battery, marker); // Metoda tworzenia markera na podstawie stanu baterii
                    marker.setVisible(true);
                } else {
                    marker.setVisible(false);
                }

                //zmiana globalnie stanu użytkownika
                if (scooter.userKey.equals(Uid)) {
                    scooterLatLng = location;
                    bReserve = true;
                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.placeholder_big)); // większa ikonka wybranego markera
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
                    if (nState.equals("*fIn&")) { //TODO SWITCH
                        iState = state.fIn;
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


        npoly.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(polygon!=null)
                    polygon.remove();
                polygonOptions = new PolygonOptions();
                polygonOptions.strokeWidth(5)
                        .strokeColor(getResources().getColor(R.color.colorSecondary))
                        .fillColor(getResources().getColor(R.color.colorPolygon));
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

        /*
        Zielona strefa serwisu
         */


        nPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startActivity(new Intent(Intent.ACTION_VIEW,
//                        Uri.parse("fb://messaging/#ecoyumi")));
                String phone = "+48577711733";
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
                startActivity(intent);
            }
        });

        //SIDE BAR
        nMenu.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                // Animacja
                View popUpView = getLayoutInflater().inflate(R.layout.navwindow, null);
                View popUpView2 = getLayoutInflater().inflate(R.layout.navwindow2, null);
                AlphaAnimation mapOff = new AlphaAnimation(1f, 0.5f);
                mapOff.setDuration(400);
                final AlphaAnimation mapOn = new AlphaAnimation(0.5f, 1f);
                mapOn.setDuration(400);
                if (user == null) {
                    sideBar = new PopupWindow(popUpView2, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.FILL_PARENT, true);
                    nLogin = popUpView2.findViewById(R.id.login);
                    nLogin.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(MapsActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });

                }
                else {
                    sideBar = new PopupWindow(popUpView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.FILL_PARENT, true);
                    // Inicjacja przycisków menu
                    price = popUpView.findViewById(R.id.pricevalue);
                    logOut = popUpView.findViewById(R.id.logOut);
                    loggedas = popUpView.findViewById(R.id.loggedas);
                    promotion = popUpView.findViewById(R.id.promotion);
                    nLoupe = popUpView.findViewById(R.id.mapButton);
                    nLogout = popUpView.findViewById(R.id.logout);
                    userGrey = popUpView.findViewById(R.id.usergrey);
                    nProfile = popUpView.findViewById(R.id.Profile);
                    nPromotion = popUpView.findViewById(R.id.Balance);
                    final TextView nName = popUpView.findViewById(R.id.user_email);

                    price.setText(cost+" zł/min");
                    nName.setText(fEmail);// Imię użytkownika
                    nLoupe.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            sideBar.dismiss();
                        }
                    });
                    nLogout.setOnClickListener(new View.OnClickListener() { // Wylogowanie
                        @Override
                        public void onClick(View view) {
                            if (bReserve == false) {
                                FirebaseAuth.getInstance().signOut();
                                Intent intent = new Intent(MapsActivity.this, MainActivity.class);
                                nscooters.removeEventListener(childEventListener);
                                if(chosenScooter!=null&&valueEventListener1!=null) {
                                    nscooters.child(chosenScooter).removeEventListener(valueEventListener1);
                                }
                                if(chosenScooter!=null&&eventListener!=null) {
                                    nscooters.child(chosenScooter).child("feedback").removeEventListener(eventListener);
                                }
                                if(user!=null&&valueEventListener3!=null)
                                    nuser.removeEventListener(valueEventListener3);
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
                            if(bVerified) {
                                Intent intent = new Intent(MapsActivity.this, Profile.class);
                                startActivity(intent);
                                sideBar.dismiss();
                            }else
                                Toast.makeText(MapsActivity.this, getResources().getString(R.string.verification),Toast.LENGTH_LONG).show();
                        }
                    });
                    nPromotion.setOnClickListener(new View.OnClickListener() { // opcja pomocy itp.
                        @Override
                        public void onClick(View view) {
                            detailPopUp();
                        }
                    });
                }
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

            }
        });
        googleMap.setOnMarkerClickListener(this);
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
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mMap.setMyLocationEnabled(true);
                LocationServices.FusedLocationApi.requestLocationUpdates(nGoogleApiClient, nLocationRequest, this);
            }else{
                checkLocationPermission();
            }
        }
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
        markerMemory="";
        if(chosenScooter!=null&&valueEventListener1!=null) {
            nscooters.child(chosenScooter).removeEventListener(valueEventListener1);
        }

        if(chosenScooter!=null&&eventListener!=null) {
            nscooters.child(chosenScooter).child("feedback").removeEventListener(eventListener);
        }
        if(user!=null&&valueEventListener3!=null)
            nuser.removeEventListener(valueEventListener3);
        nscooters.removeEventListener(childEventListener);
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {

        scooterDetails(marker);
        return true;
    }

//    private void requestPermission(String permissionName, int permissionRequestCode) {
//        ActivityCompat.requestPermissions(this,
//                new String[]{permissionName}, permissionRequestCode);
//    }

    private void checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                new android.app.AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.GPSpermitTitle))
                        .setMessage(getResources().getString(R.string.GPSpermitText))
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MapsActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            }
            else{
                ActivityCompat.requestPermissions(MapsActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
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

    private void update(final Boolean mandatory, String update) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.update))
                .setMessage(update)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse
                                ("market://details?id=eco.yumi&hl=pl")));
                        finish();
                    }
                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                if(mandatory){
                    finish();
                }
            }
        });
        builder.create().show();
    } // Pop up przy wyłączaniu skutera

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 1:{
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        LocationServices.FusedLocationApi.requestLocationUpdates(nGoogleApiClient, nLocationRequest, this);
                        mMap.setMyLocationEnabled(true);
                    }
                } else{
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    private Integer markerBattery (String mBattery , Marker marker){
        Integer p=null;
        Integer battery = Integer.valueOf(mBattery);
        if (battery>30){
            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.placeholder_full));
            p=4;
        }
        if ((battery<=30)&(battery>=20)){
            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.placeholder_80));
            p=3;
        }
        if ((battery<20)&(battery>=10)){
            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.placeholder_50));
            p=2;
        }
        if (battery<10){
            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.placeholder_20));
            p=1;
        }
        return p;
    }

    private void findScooter (LatLng start, LatLng end) {
        if(nLastLatLng != null) {
            Routing routing = new Routing.Builder()
                    .travelMode(Routing.TravelMode.WALKING)
                    .withListener(this)
                    .alternativeRoutes(false)
                    .waypoints(start, end)
                    .build();
            routing.execute();
        }
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

        if((!bWait)&&(!bReserve||recover)) { // przy rezerwacji jak i oczekiwaniu na inny skuter nie możemy nacisnąć na inny marker.
            mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()), 300, new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    if(iState==state.oF){ // stan przed rezerwacją
                        nHelmet.setVisibility(View.INVISIBLE); // Nie można otworzyć kufra
                        nParking.setVisibility(View.INVISIBLE);//nie można pauzować w tym stanie
                        nOut.setVisibility(View.INVISIBLE);// można wyjść z tego okna
                        nWalk.setVisibility(View.INVISIBLE);// nie widać odległości i routingu
                        nStart.setText(getResources().getString(R.string.reserve)); // przycisk z funkcją rezerwacji
                        nStart.setTextSize(12);
                        textClock2.setVisibility(View.INVISIBLE);
                        Distance.setVisibility(View.INVISIBLE);// nie widać odległości i routingu
                        nStart.setTextColor(getResources().getColor(R.color.colorSecondary));
                        nStart.setBackground(getResources().getDrawable(R.drawable.button_shape_start));
                    }
                    if(iState==state.oN){
                        nParking.setVisibility(View.VISIBLE);// Można zapauzować
                        nHelmet.setVisibility(View.VISIBLE);// Można otworzyć kufer
                        Distance.setVisibility(View.INVISIBLE); // Odległości nie pokazuje
                        nOut.setVisibility(View.INVISIBLE); // Nie można wyjść(trzeba nacisnąć "wyłącz")
                        nWalk.setVisibility(View.INVISIBLE);
                        textClock2.setVisibility(View.INVISIBLE);
                        nStart.setText(getResources().getString(R.string.finish));// opcja "wyłącz"
                        nStart.setTextSize(12);
                        nStart.setTextColor(getResources().getColor(R.color.colorPrimary));
                        nStart.setBackground(getResources().getDrawable(R.drawable.button_shape_end));
                    }
                    if(iState==state.pA){
                        nParking.setVisibility(View.INVISIBLE);// Nie widać
                        nHelmet.setVisibility(View.INVISIBLE);// Nie widać
                        Distance.setVisibility(View.INVISIBLE); // Nie widać
                        nOut.setVisibility(View.INVISIBLE);// Nie widać
                        nWalk.setVisibility(View.INVISIBLE);
                        textClock2.setVisibility(View.INVISIBLE);
                        nStart.setText(getResources().getString(R.string.start)); // Można wrócić do usługi i uruchomić skuter tylko
                        nStart.setTextSize(12);
                        nStart.setTextColor(getResources().getColor(R.color.colorPrimary));
                        nStart.setBackground(getResources().getDrawable(R.drawable.button_shape_end));
                    }
                    if(iState==state.rE) {
                        nHelmet.setVisibility(View.INVISIBLE);// Nie widać
                        nParking.setVisibility(View.INVISIBLE);// Nie widać
                        nWalk.setVisibility(View.VISIBLE);
                        textClock2.setVisibility(View.INVISIBLE);
                        nOut.setVisibility(View.VISIBLE);// Można wyłączyć rezerwację i wyjść z okna
                        nStart.setText(getResources().getString(R.string.start));// Opcja wystartowania skutera
                        nStart.setTextSize(12);
                        Distance.setVisibility(View.VISIBLE); // Widać dystans od skutera wraz z routingiem
                        nWalk.setVisibility(View.VISIBLE);
                        nStart.setTextColor(getResources().getColor(R.color.colorPrimary));
                        nStart.setBackground(getResources().getDrawable(R.drawable.button_shape_end));
                    }
                    /*
                    Przydaje się przy zmienianiu z jednego markera na drugi - pamięć zapamiętuje
                    wcześniejszy marker i zmienia ikonę na stan podstawowy tak żeby tylko wybrany marker
                    miał wyszczególnioną ikonkę np. placeholder_red_big.
                                         */
                    if(markerMemory!=""){
                        Marker tempMarker = markerMap.get(markerMemory);
                        nscooters.child(markerMemory).removeEventListener(valueEventListener1);
                        markerBattery( tempMarker.getTitle() , tempMarker);
                        markerMemory="";
                    }else {//jeżeli wcześniej nie był wybrany inny marker i reserveWindow nie było wyświetlone włączamy animację wysuwania okna
                        Animation animationShow = AnimationUtils.loadAnimation(MapsActivity.this, R.anim.popup_show);
                        reserveWindow.startAnimation(animationShow);
                        animationListener(animationShow, reserveWindow, View.VISIBLE);
                    }
                    final Integer[] bat = new Integer[1]; // inicjacja stanu baterii
                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.placeholder_big)); // większa ikonka wybranego markera
                    marker.setAnchor(0.5f, 0.5f);

                    //LAYOUT
                    final Animation animationHide = AnimationUtils.loadAnimation(MapsActivity.this, R.anim.popup_hide);
                    final TextView myAddress = (TextView) reserveWindow.findViewById(R.id.address);
                    final TextView Range = (TextView) reserveWindow.findViewById(R.id.range);
                    final TextView Name = (TextView) reserveWindow.findViewById(R.id.name);

                    Name.setTextSize(13);
                    Range.setTextSize(13);
                    Distance.setTextSize(13);
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
                                myAddress.setText(addressLine);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    for (Map.Entry<String, Marker> entry : markerMap.entrySet()) {
                        if (entry.getValue().equals(marker)) {
                            chosenScooter = entry.getKey();//odczytanie nazwy skutera z mapy za pomocą markera
                        }
                    }

                    markerMemory = chosenScooter; // zapisanie ostatniego markera na wypadek przeskoczenia do innego markera (zmiana ikonki patrz wyżej)
                    valueEventListener1 = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            ScooterInformation scooter = dataSnapshot.getValue(ScooterInformation.class);
                            time = new Date().getTime() - new Date(scooter.start).getTime(); // timestamp ostatniej komendy rezerwacji lub startu
                            marker.setTitle(scooter.battery); // zapamiętanie stanu baterii w tytule markera

                            battery[0] = scooter.battery;// zmienna globalna stanu baterii
                            scooterLatLng = new LatLng(Double.parseDouble(scooter.latitude), Double.parseDouble(scooter.longitude));
                            if((iState==state.rE)||(iState==state.oN)||(iState==state.pA))
                                marker.setPosition(scooterLatLng);
                            Range.setText(battery[0]+" km");// Wyświetlenie stanu baterii
                            Name.setText(scooter.name);
                            if(!scooter.userKey.equals("")&&!scooter.userKey.equals(Uid)&&user!=null) {// TODO upewnienie się czy ktoś nie zdążył zarezerwować przed użytkownikiem, może wyłączać okno
                                markerMemory = "";
                                nuser.child("scooterName").setValue("");
                                if(wait!=null) {
                                    nscooters.child(chosenScooter).child("feedback").removeEventListener(eventListener);
                                }
                                nscooters.child(chosenScooter).removeEventListener(valueEventListener1);
                                animationListener(animationHide, reserveWindow, View.INVISIBLE );
                                markerBattery(battery[0], marker);
                                reserveWindow.startAnimation(animationHide);
                                reserveWindow.setVisibility(View.GONE);
                                //listener feedback'u ze skutera(patrz wait)
                                bReserve=false;
                                bWait=false;
                                progressBar.setVisibility(View.INVISIBLE);
                            }
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
                    nscooters.child(chosenScooter).addValueEventListener(valueEventListener1);


                    mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                        @Override
                        public void onMapClick(LatLng latLng) {


                            // działa tylko w stanie state.oF, w innym przypadku trzeba wyłączyć przyciskiem
                            if ((!markerMemory.equals(""))&&(iState == state.oF)) {
                                animationListener(animationHide, reserveWindow, View.INVISIBLE );
                                markerMemory = "";
                                reserveWindow.startAnimation(animationHide);
                                markerBattery(battery[0], marker);
                                if(wait!=null) {// gdy jest oczekiwanie na odpowiedź skutera
                                    wait.cancel();
                                    if(!chosenScooter.equals("")) {
                                        nscooters.child(chosenScooter).child("userKey").setValue("");
                                        nscooters.child(chosenScooter).child("check").removeValue(); // wycofanie się z komendy
                                    }
                                    progressBar.setVisibility(View.INVISIBLE);
                                    bWait = false;
                                }
                                if(eventListener !=null) {
                                    nscooters.child(chosenScooter).child("feedback").removeEventListener(eventListener);
                                }
                                nscooters.child(chosenScooter).removeEventListener(valueEventListener1);
                                reserveWindow.setVisibility(View.GONE);
                                bReserve = false;
                            }
                        }

                    });
                    nOut.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            markerMemory = "";
                            erasePolyline();
                            if(!reservedScooter.equals("")) {
                                nscooters.child(reservedScooter).child("userKey").setValue("");
                                nscooters.child(reservedScooter).child("state").setValue("*oF&");
                                nuser.child("scooterName").setValue("");
                            }
                            reserve.cancel();
                            textClock2.setText(null);
                            animationListener(animationHide, reserveWindow, View.INVISIBLE );
                            markerBattery(battery[0], marker);
                            reserveWindow.startAnimation(animationHide);
                            reserveWindow.setVisibility(View.GONE);
                            //listener feedback'u ze skutera(patrz wait)
                            nscooters.child(chosenScooter).child("feedback").removeEventListener(eventListener);
                            nscooters.child(chosenScooter).removeEventListener(valueEventListener1);
                            progressBar.setVisibility(View.INVISIBLE);
                            iState = state.oF;
                            bReserve=false;
                            Distance.setText("");
                        }
                    });
                    // Odzyskanie stanu po restarcie :
                    if(iState.equals(state.rE)){
                        recover = true;
                        reservedScooter(chosenScooter,latLng, 900000 - time );
                        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.placeholder_big)); // większa ikonka wybranego markera
                    }
                    if(iState.equals(state.fIn)){
                        recover = true;
                        stoppedScooter(chosenScooter);
                    }
                    if(iState.equals(state.oN)){
                        recover = true;
                        startedScooter(chosenScooter,latLng);
                    }
                    if(iState.equals(state.pA)){
                        recover = true;
                        parkedScooter(chosenScooter);
                    }
                    nStart.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (iState == state.oF) {
                                if (!bTaken) {
                                    reservedScooter(chosenScooter, latLng,Long.valueOf(900000) );
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.placeholder_big)); // większa ikonka wybranego markera
                                } else
                                    Toast.makeText(MapsActivity.this, getResources().getString(R.string.ScooterTaken), Toast.LENGTH_LONG).show();

                            }
                            if (iState == state.rE){
                                Toast.makeText(MapsActivity.this, getResources().getString(R.string.Starting), Toast.LENGTH_LONG).show();
                                startedScooter(chosenScooter, latLng);
                            }
                            if (iState == state.pA){
                                Toast.makeText(MapsActivity.this, getResources().getString(R.string.Starting), Toast.LENGTH_LONG).show();
                                startedScooter(chosenScooter, latLng);
                            }
                            if (iState == state.oN){
                                confirmSwitchingOff(chosenScooter); // Trzeba potwierdzić wyłączenie skutera
                                bat[0]=null;
                            }
                        }
                    });
                    nHelmet.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(!reservedScooter.equals(""))
                                nscooters.child(reservedScooter).child("state").setValue("*hE&");//TODO Otworzenie kufra
                        }
                    });
                    nParking.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            confirmParking(chosenScooter);// Trzeba potwierdzić wyłączenie skutera
                        }
                    });
                }

                @Override
                public void onCancel() {
                    if(iState==state.oF){ // stan przed rezerwacją
                        nHelmet.setVisibility(View.INVISIBLE); // Nie można otworzyć kufra
                        nParking.setVisibility(View.INVISIBLE);//nie można pauzować w tym stanie
                        nOut.setVisibility(View.INVISIBLE);// można wyjść z tego okna
                        nWalk.setVisibility(View.INVISIBLE);// nie widać odległości i routingu
                        nStart.setText(getResources().getString(R.string.reserve)); // przycisk z funkcją rezerwacji
                        nStart.setTextSize(12);
                        textClock2.setVisibility(View.INVISIBLE);
                        Distance.setVisibility(View.INVISIBLE);// nie widać odległości i routingu
                        nStart.setTextColor(getResources().getColor(R.color.colorSecondary));
                        nStart.setBackground(getResources().getDrawable(R.drawable.button_shape_start));
                    }
                    if(iState==state.oN){
                        nParking.setVisibility(View.VISIBLE);// Można zapauzować
                        nHelmet.setVisibility(View.VISIBLE);// Można otworzyć kufer
                        Distance.setVisibility(View.INVISIBLE); // Odległości nie pokazuje
                        nOut.setVisibility(View.INVISIBLE); // Nie można wyjść(trzeba nacisnąć "wyłącz")
                        nWalk.setVisibility(View.INVISIBLE);
                        textClock2.setVisibility(View.INVISIBLE);
                        nStart.setText(getResources().getString(R.string.finish));// opcja "wyłącz"
                        nStart.setTextSize(12);
                        nStart.setTextColor(getResources().getColor(R.color.colorPrimary));
                        nStart.setBackground(getResources().getDrawable(R.drawable.button_shape_end));
                    }
                    if(iState==state.pA){
                        nParking.setVisibility(View.INVISIBLE);// Nie widać
                        nHelmet.setVisibility(View.INVISIBLE);// Nie widać
                        Distance.setVisibility(View.INVISIBLE); // Nie widać
                        nOut.setVisibility(View.INVISIBLE);// Nie widać
                        nWalk.setVisibility(View.INVISIBLE);
                        textClock2.setVisibility(View.INVISIBLE);
                        nStart.setText(getResources().getString(R.string.start)); // Można wrócić do usługi i uruchomić skuter tylko
                        nStart.setTextSize(12);
                        nStart.setTextColor(getResources().getColor(R.color.colorPrimary));
                        nStart.setBackground(getResources().getDrawable(R.drawable.button_shape_end));
                    }
                    if(iState==state.rE) {
                        nHelmet.setVisibility(View.INVISIBLE);// Nie widać
                        nParking.setVisibility(View.INVISIBLE);// Nie widać
                        nWalk.setVisibility(View.VISIBLE);
                        textClock2.setVisibility(View.INVISIBLE);
                        nOut.setVisibility(View.VISIBLE);// Można wyłączyć rezerwację i wyjść z okna
                        nStart.setText(getResources().getString(R.string.start));// Opcja wystartowania skutera
                        nStart.setTextSize(12);
                        Distance.setVisibility(View.VISIBLE); // Widać dystans od skutera wraz z routingiem
                        nWalk.setVisibility(View.VISIBLE);
                        nStart.setTextColor(getResources().getColor(R.color.colorPrimary));
                        nStart.setBackground(getResources().getDrawable(R.drawable.button_shape_end));
                    }
                    /*
                    Przydaje się przy zmienianiu z jednego markera na drugi - pamięć zapamiętuje
                    wcześniejszy marker i zmienia ikonę na stan podstawowy tak żeby tylko wybrany marker
                    miał wyszczególnioną ikonkę np. placeholder_red_big.
                                         */
                    if(markerMemory!=""){
                        Marker tempMarker = markerMap.get(markerMemory);
                        nscooters.child(markerMemory).removeEventListener(valueEventListener1);
                        markerBattery( tempMarker.getTitle() , tempMarker);
                        markerMemory="";
                    }else {//jeżeli wcześniej nie był wybrany inny marker i reserveWindow nie było wyświetlone włączamy animację wysuwania okna
                        Animation animationShow = AnimationUtils.loadAnimation(MapsActivity.this, R.anim.popup_show);
                        reserveWindow.startAnimation(animationShow);
                        animationListener(animationShow, reserveWindow, View.VISIBLE);
                    }
                    final Integer[] bat = new Integer[1]; // inicjacja stanu baterii
                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.placeholder_big)); // większa ikonka wybranego markera
                    marker.setAnchor(0.5f, 0.5f);

                    //LAYOUT
                    final Animation animationHide = AnimationUtils.loadAnimation(MapsActivity.this, R.anim.popup_hide);
                    final TextView myAddress = (TextView) reserveWindow.findViewById(R.id.address);
                    final TextView Range = (TextView) reserveWindow.findViewById(R.id.range);
                    final TextView Name = (TextView) reserveWindow.findViewById(R.id.name);

                    Name.setTextSize(13);
                    Range.setTextSize(13);
                    Distance.setTextSize(13);
                    // GEOLOCATION
                    Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
                    final LatLng latLng = marker.getPosition();
                    List<Address> addresses;
                    try {
                        addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                        if (addresses != null && addresses.size() > 0) {
                            addressLine = addresses.get(0).getAddressLine(0);
                            String address[]= addressLine.split(","); // Usuwamy ostatnie słowo z adresu :"Poland"
                            if(address[0].equals("Unnamed Road"))
                                address[0]=null; // pierwsza linijka
                            if(address[1].charAt(3)=='-'){//nie pokazujemy kodu pocztowego
                                String[] sCity = address[1].split(" ");
                                address[1]=sCity[2]; // miasto pokazane w linijce poniżej
                            }
                            myAddress.setText(address[0]+", "+address[1]);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    for (Map.Entry<String, Marker> entry : markerMap.entrySet()) {
                        if (entry.getValue().equals(marker)) {
                            chosenScooter = entry.getKey();//odczytanie nazwy skutera z mapy za pomocą markera
                        }
                    }

                    markerMemory = chosenScooter; // zapisanie ostatniego markera na wypadek przeskoczenia do innego markera (zmiana ikonki patrz wyżej)
                    valueEventListener1 = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            ScooterInformation scooter = dataSnapshot.getValue(ScooterInformation.class);
                            time = new Date().getTime() - new Date(scooter.start).getTime(); // timestamp ostatniej komendy rezerwacji lub startu
                            marker.setTitle(scooter.battery); // zapamiętanie stanu baterii w tytule markera

                            battery[0] = scooter.battery;// zmienna globalna stanu baterii
                            scooterLatLng = new LatLng(Double.parseDouble(scooter.latitude), Double.parseDouble(scooter.longitude));
                            if((iState==state.rE)||(iState==state.oN)||(iState==state.pA))
                                marker.setPosition(scooterLatLng);
                            Range.setText(battery[0]+" km");// Wyświetlenie stanu baterii
                            Name.setText(scooter.name);
                            if(!scooter.userKey.equals("")&&!scooter.userKey.equals(Uid)&&user!=null) {// TODO upewnienie się czy ktoś nie zdążył zarezerwować przed użytkownikiem, może wyłączać okno
                                markerMemory = "";
                                nuser.child("scooterName").setValue("");
                                if(wait!=null) {
                                    nscooters.child(chosenScooter).child("feedback").removeEventListener(eventListener);
                                }
                                nscooters.child(chosenScooter).removeEventListener(valueEventListener1);
                                animationListener(animationHide, reserveWindow, View.INVISIBLE );
                                markerBattery(battery[0], marker);
                                reserveWindow.startAnimation(animationHide);
                                reserveWindow.setVisibility(View.GONE);
                                //listener feedback'u ze skutera(patrz wait)
                                bReserve=false;
                                bWait=false;
                                progressBar.setVisibility(View.INVISIBLE);
                            }
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
                    nscooters.child(chosenScooter).addValueEventListener(valueEventListener1);


                    mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                        @Override
                        public void onMapClick(LatLng latLng) {


                            // działa tylko w stanie state.oF, w innym przypadku trzeba wyłączyć przyciskiem
                            if ((!markerMemory.equals(""))&&(iState == state.oF)) {
                                animationListener(animationHide, reserveWindow, View.INVISIBLE );
                                markerMemory = "";
                                reserveWindow.startAnimation(animationHide);
                                markerBattery(battery[0], marker);
                                if(wait!=null) {// gdy jest oczekiwanie na odpowiedź skutera
                                    wait.cancel();
                                    if(!chosenScooter.equals("")) {
                                        nscooters.child(chosenScooter).child("userKey").setValue("");
                                        nscooters.child(chosenScooter).child("check").removeValue(); // wycofanie się z komendy
                                    }
                                    progressBar.setVisibility(View.INVISIBLE);
                                    bWait = false;
                                }
                                if(eventListener !=null) {
                                    nscooters.child(chosenScooter).child("feedback").removeEventListener(eventListener);
                                }
                                nscooters.child(chosenScooter).removeEventListener(valueEventListener1);
                                reserveWindow.setVisibility(View.GONE);
                                bReserve = false;
                            }
                        }

                    });
                    nOut.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            markerMemory = "";
                            erasePolyline();
                            if(!reservedScooter.equals("")) {
                                nscooters.child(reservedScooter).child("userKey").setValue("");
                                nscooters.child(reservedScooter).child("state").setValue("*oF&");
                                nuser.child("scooterName").setValue("");
                            }
                            reserve.cancel();
                            textClock2.setText(null);
                            animationListener(animationHide, reserveWindow, View.INVISIBLE );
                            markerBattery(battery[0], marker);
                            reserveWindow.startAnimation(animationHide);
                            reserveWindow.setVisibility(View.GONE);
                            //listener feedback'u ze skutera(patrz wait)
                            nscooters.child(chosenScooter).child("feedback").removeEventListener(eventListener);
                            nscooters.child(chosenScooter).removeEventListener(valueEventListener1);
                            progressBar.setVisibility(View.INVISIBLE);
                            iState = state.oF;
                            bReserve=false;
                            Distance.setText("");
                        }
                    });
                    // Odzyskanie stanu po restarcie :
                    if(iState.equals(state.rE)){
                        recover = true;
                        reservedScooter(chosenScooter,latLng, 900000 - time );
                        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.placeholder_big)); // większa ikonka wybranego markera
                    }
                    if(iState.equals(state.fIn)){
                        recover = true;
                        stoppedScooter(chosenScooter);
                    }
                    if(iState.equals(state.oN)){
                        recover = true;
                        startedScooter(chosenScooter,latLng);
                    }
                    if(iState.equals(state.pA)){

                        recover = true;
                        parkedScooter(chosenScooter);
                    }
                    nStart.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (iState == state.oF) {
                                if (!bTaken) {
                                    reservedScooter(chosenScooter, latLng,Long.valueOf(900000) );
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.placeholder_big)); // większa ikonka wybranego markera
                                } else
                                    Toast.makeText(MapsActivity.this, getResources().getString(R.string.ScooterTaken), Toast.LENGTH_LONG).show();

                            }
                            if (iState == state.rE){
                                Toast.makeText(MapsActivity.this, getResources().getString(R.string.Starting), Toast.LENGTH_LONG).show();
                                startedScooter(chosenScooter, latLng);
                            }
                            if (iState == state.pA){
                                Toast.makeText(MapsActivity.this, getResources().getString(R.string.Starting), Toast.LENGTH_LONG).show();
                                startedScooter(chosenScooter, latLng);
                            }
                            if (iState == state.oN){
                                confirmSwitchingOff(chosenScooter); // Trzeba potwierdzić wyłączenie skutera
                                bat[0]=null;
                            }
                        }
                    });
                    nHelmet.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(!reservedScooter.equals(""))
                                nscooters.child(reservedScooter).child("state").setValue("*hE&");//TODO Otworzenie kufra
                        }
                    });
                    nParking.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            confirmParking(chosenScooter);// Trzeba potwierdzić wyłączenie skutera
                        }
                    });
                }
            });
        }
    } // OKNO KONTROLI SKUTERA

    /*
    Wystartowanie skutera przyciskiem lub przy starcie aplikacji włączenie stanu startu.
     */
    private void startedScooter(final String scooterName,  final LatLng latLng){
        wait = new CountDownTimer(20000,1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                cancelPost(scooterName);
                nStart.setClickable(true);
                nOut.setClickable(true);
            }
        }; // oczekiwanie na odpowiedź skutera
        nStart.setClickable(false);
        nOut.setClickable(false);
        bWait = true;
        if(!scooterName.equals(""))
            nscooters.child(scooterName).child("check").setValue(true); // sygnał sprawdzający

        // nasłuchiwanie feedbacku
        eventListener = nscooters.child(scooterName).child("feedback").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Boolean data =  dataSnapshot.exists(); // jeżeli feedback istnieje
                if(data){
                    wait.cancel();//odliczanie skończone
                    wait=null;
                    nHelmet.setClickable(true);
                    nParking.setClickable(true);
                    nStart.setClickable(true);
                    if(reserve !=null)
                        reserve.cancel();//jeżeli było odliczanie rezerwacji to kasujemy
                    textClock2.setText(null); // ukrycie wyświetlacza countdown
                    textClock2.setVisibility(View.INVISIBLE);
                    cStart.setVisibility(View.VISIBLE); // ukazanie chromometra do odliczania czasu usługi
                    progressBar.setVisibility(View.INVISIBLE); //ukrycie progressbaru
                    erasePolyline(); // wykasowanie routingu

                    Distance.setVisibility(View.INVISIBLE);//brak dystansu routingu podczas usługi
                    nWalk.setVisibility(View.INVISIBLE);
                    nOut.setVisibility(View.INVISIBLE);//brak możliwości wyjścia
                    nHelmet.setVisibility(View.VISIBLE);//możliwość otwarcia kufra
                    nParking.setVisibility(View.VISIBLE);//możliwość pauzy
                    nStart.setText(getResources().getString(R.string.finish));//opcja wyłączenia skutera
                    nStart.setTextSize(12);
                    //tylko w przypadku wejścia pierwszy raz do startedScooter wysyłamy timestamp'a (przy restarcie omija to polecenie)
                    if((!recover)&&(iState!=state.pA))
                        if(!scooterName.equals(""))
                            nscooters.child(scooterName).child("start").setValue(ServerValue.TIMESTAMP);//tylko w przypadku naciśnięcia pierwszy raz
                    recover = false;//pomocnicza flaga do timestampa przy nacisnieciu startu musi być z powrotem false


                    Distance.setText("");//wyzerowanie wyświetlacza dystansu
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));//wypośrodkowanie do pozycji skutera
                    mMap.moveCamera(CameraUpdateFactory.zoomTo(15));//zzoomowanie
                    if(!scooterName.equals(""))
                        nscooters.child(scooterName).child("state").setValue("*oN&");//włączenie skutera
                    nscooters.child(scooterName).child("feedback").removeValue();//usunięcie z bazy feedbacku
                    nscooters.child(scooterName).child("feedback").removeEventListener(eventListener);//usunięcie listenera feedbacku
                    nStart.setClickable(true);
                    bWait=false;
                    cStart.setBase(SystemClock.elapsedRealtime()-time);// ustawienie bazy chromometra z firebase
                    cStart.start();//wystartowanie countera z podanej bazy na poziomie aplikacji
                }
                else {
                    progressBar.setVisibility(View.VISIBLE);
                    nOut.setClickable(false);
                    nHelmet.setClickable(false);
                    nParking.setClickable(false);
                    nStart.setClickable(false);
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
        wait = new CountDownTimer(25000,1000) {
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
        bWait = true;
        eventListener = nscooters.child(scooterName).child("feedback").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Boolean data =  dataSnapshot.exists();
                if(data){
                    wait.cancel();
                    wait=null;
                    nStart.setClickable(true);
                    cStart.setVisibility(View.VISIBLE);
                    if(!reservedScooter.equals(""))
                        nscooters.child(reservedScooter).child("state").setValue("*pA&");
                    nParking.setVisibility(View.INVISIBLE);
                    nHelmet.setVisibility(View.INVISIBLE);
                    nOut.setVisibility(View.INVISIBLE);
                    nWalk.setVisibility(View.INVISIBLE);                    nStart.setText(getResources().getString(R.string.start));
                    nStart.setTextSize(12);
                    Distance.setVisibility(View.INVISIBLE);
                    textClock2.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                    nscooters.child(scooterName).child("feedback").removeValue();
                    nscooters.child(scooterName).child("feedback").removeEventListener(eventListener);
                    cStart.setBase(SystemClock.elapsedRealtime()-time);
                    cStart.start();
                    recover = false;
                    bWait=false;
                    nStart.setClickable(true);
                }
                else {
                    progressBar.setVisibility(View.VISIBLE);
                    nOut.setClickable(false);
                    nHelmet.setClickable(false);
                    nParking.setClickable(false);
                    nStart.setClickable(false);
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
                        wait = new CountDownTimer(30000, 1000) {
                            @Override
                            public void onTick(long l) {

                            }

                            @Override
                            public void onFinish() {
                                if(!scooterName.equals("")) {
                                    nscooters.child(scooterName).child("userKey").setValue("");
                                }
                                cancelPost(scooterName);
                            }

                        };
                        reserve = new CountDownTimer(time, 1000) {

                            @Override
                            public void onTick(long l) {
                                String v = String.format(Locale.GERMANY, "%02d", l / 60000);
                                int va = (int) ((l % 60000) / 1000);
                                textClock2.setText(v + ":" + String.format(Locale.GERMANY, "%02d", va));
                            }

                            @Override
                            public void onFinish() {
                            }
                        };
                        bWait = true;
                        if(!scooterName.equals("")) {
                            nscooters.child(scooterName).child("check").setValue(true);
                            nscooters.child(scooterName).child("userKey").setValue(Uid);
                        }
                        nStart.setClickable(false);
                        eventListener = nscooters.child(scooterName).child("feedback").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Boolean data = dataSnapshot.exists();
                                if (data) {
                                    wait.cancel();
                                    wait=null;
                                    reserve.start();
                                    nOut.setClickable(true);
                                    nStart.setClickable(true);
                                    progressBar.setVisibility(View.INVISIBLE);
                                    textClock2.setVisibility(View.VISIBLE);
                                    nStart.setTextColor(getResources().getColor(R.color.colorPrimary));
                                    nStart.setBackground(getResources().getDrawable(R.drawable.button_shape_end));
                                    if(!scooterName.equals("")) {
                                        if (!recover)
                                            nscooters.child(scooterName).child("start").setValue(ServerValue.TIMESTAMP);
                                        nuser.child("scooterName").setValue(scooterName);
                                        nscooters.child(scooterName).child("state").setValue("*rE&");
                                    }
                                    nWalk.setVisibility(View.VISIBLE);
                                    Distance.setVisibility(View.VISIBLE);
                                    nHelmet.setVisibility(View.INVISIBLE);
                                    nParking.setVisibility(View.INVISIBLE);
                                    nOut.setVisibility(View.VISIBLE);
                                    findScooter(nLastLatLng, latLng);
                                    if(nLastLatLng!=null) {
                                        LatLngBounds latLngBounds = LatLngBounds.builder()
                                                .include(nLastLatLng)
                                                .include(latLng)
                                                .build();
                                        CameraUpdate zoom = CameraUpdateFactory.newLatLngBounds(latLngBounds, 200); //TODO Pobrać screen
                                        try {
                                            mMap.moveCamera(zoom);
                                        } finally {

                                        }
                                    }
                                    nStart.setText(getResources().getString(R.string.start));
                                    nStart.setTextSize(12);
                                    nscooters.child(scooterName).child("feedback").removeValue();
                                    nscooters.child(scooterName).child("feedback").removeEventListener(eventListener);
                                    recover = false;
                                    bWait = false;
                                    nStart.setClickable(true);
                                } else {
                                    progressBar.setVisibility(View.VISIBLE);
                                    nOut.setClickable(false);
                                    nHelmet.setClickable(false);
                                    nParking.setClickable(false);
                                    nStart.setClickable(false);
                                    wait.start();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    } else {
                        Toast.makeText(this, getResources().getString(R.string.needtopup), Toast.LENGTH_SHORT).show();
                        if(pay.equals("account")){
                            Intent intent = new Intent(MapsActivity.this, Balance.class);
                            startActivity(intent);
                            finish();
                        }else if (pay.equals("card")){
                            cardPopUp();
                        }
                    }
                }else {
                    Toast.makeText(this, getResources().getString(R.string.verification), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, getResources().getString(R.string.needemail), Toast.LENGTH_SHORT).show();
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
        if ((contain==true)||(iState==state.fIn)) { //fIn tylko przy braku baterii, wyłaczy się gdziekolwiek
            markerMemory = "";
            nHelmet.setVisibility(View.INVISIBLE);
            nParking.setVisibility(View.INVISIBLE);
            nWalk.setVisibility(View.INVISIBLE);
            vMap.setClickable(false);
            progressBar.setVisibility(View.VISIBLE);  //To show ProgressBar
            reserveWindow.setClickable(false);
            nStart.setClickable(false);
            nOut.setClickable(false);
            bWait=true;
            if(iState!=state.fIn) {
                Toast.makeText(MapsActivity.this, getResources().getString(R.string.Stopping), Toast.LENGTH_LONG).show();
            }

            if(!recover) {
                nuser.child("fIn").setValue(ServerValue.TIMESTAMP);// zapisujemy tą zmienną do użytkownika na wszelki wypadek w przypadku usterki.
            }
            if(!scooterName.equals(""))
                nscooters.child(scooterName).child("state").setValue("*fIn&"); //wysyłamy fin do serwera. Po stronie serwera następuje rozliczanie użytkownika
            //nasłuchujemy aż serwer wyłączy skuter, dostanie od niego odpowiedź i rozliczy użytkownika. Wtedy włączamy okno billing z ostatnim przejazdem.
            valueEventListener2 = nuser.child("scooterName").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String scooter = (String) dataSnapshot.getValue();
                    if (scooter.equals("")) {
                        cStart.setVisibility(View.INVISIBLE);
                        nuser.child("alert").setValue("");
                        cStart.stop();
                        iState=state.oF;
                        bWait=false;
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
    private void detailPopUp() {
        final EditText input = new EditText(MapsActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(input);

        builder.setTitle("Kod promocjny")
                .setMessage("Wprowadź kod")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int id) {
                        final String code = input.getText().toString();
                        final InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        nuser.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                DataSnapshot fCode = dataSnapshot.child("code");
                                DataSnapshot fCode2 = dataSnapshot.child("code2");
                                DataSnapshot fCode3 = dataSnapshot.child("code3");
                                if(fCode.exists()) {
                                    if (fCode.getValue().equals(code)) {
                                        nuser.child("balance").setValue(balance + 10);
                                        nuser.child("code").removeValue();
                                        dialog.dismiss();
                                        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                                        Toast.makeText(MapsActivity.this, "Kod promocyjny zgadza się! Doładowano 10 zł", Toast.LENGTH_LONG).show();
                                    }
                                    else {
                                        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                                        dialog.dismiss();
                                        Toast.makeText(MapsActivity.this, getResources().getString(R.string.wrongCode), Toast.LENGTH_LONG).show();
                                    }
                                }
                                if(fCode2.exists()) {
                                    if (fCode2.getValue().equals(code)) {
                                        nuser.child("balance").setValue(balance + 10);
                                        nuser.child("code2").removeValue();
                                        dialog.dismiss();
                                        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                                        Toast.makeText(MapsActivity.this, "Kod promocyjny zgadza się! Doładowano 10 zł", Toast.LENGTH_LONG).show();
                                    }
                                    else {
                                        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                                        dialog.dismiss();
                                        Toast.makeText(MapsActivity.this, getResources().getString(R.string.wrongCode), Toast.LENGTH_LONG).show();
                                    }
                                }
                                if(fCode3.exists()) {
                                    if (fCode3.getValue().equals(code)) {
                                        nuser.child("balance").setValue(balance + 10);
                                        nuser.child("code3").removeValue();
                                        dialog.dismiss();
                                        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                                        Toast.makeText(MapsActivity.this, "Kod promocyjny zgadza się! Doładowano 10 zł", Toast.LENGTH_LONG).show();
                                    }
                                    else {
                                        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                                        dialog.dismiss();
                                        Toast.makeText(MapsActivity.this, getResources().getString(R.string.wrongCode), Toast.LENGTH_LONG).show();
                                    }
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
//                        else Toast.makeText(MapsActivity.this, "Kod jest nieprawidłowy", Toast.LENGTH_LONG).show();

                    }
                });
        builder.create().show();
    }
    private void phonePopUp() {
        final EditText input = new EditText(MapsActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(input);

        builder.setTitle(getResources().getString(R.string.givePhone))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int id) {
                        String phone = input.getText().toString();
                        final InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        if(!phone.contains("+")){
                            phone = "+48"+phone; //+48530184485
                        }
                        if (phone.length()>11) {
                            dialog.dismiss();
                            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                            Intent intent = new Intent(MapsActivity.this, VerificationPhoneActivity.class);
                            intent.putExtra("phone", phone);
                            startActivity(intent);
                        }else {
                            Toast.makeText(MapsActivity.this, getResources().getString(R.string.wrongPhone), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        builder.create().show();
    }
    private void alertPopUp(final String alert) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(alert)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(alert.equals("Skuter ma za niski stan baterii. Wyłączam ...")){
                            Intent intent = new Intent(MapsActivity.this, Billing.class);
                            startActivity(intent);
                            finish();
                        }
                        dialog.dismiss();
                        nuser.child("alert").setValue("");
                    }
                }).setCancelable(false).setNeutralButton(getResources().getString(R.string.call), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                nuser.child("alert").setValue("");
                String phone = "+48577711733";
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
                startActivity(intent);
            };
        });
        builder.create().show();
    }// Pop up przy wyłączaniu skutera na pauze
    private void cardPopUp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.repay)).setMessage(getResources().getString(R.string.repayTry))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ncard.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                                    String key = childDataSnapshot.getKey();
                                    ncard.child(key).child("sign").setValue("pay");
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });
        builder.create().show();
    }// Pop up przy wyłączaniu skutera na pauze


    private void showNotification(String title, String text, Class a, int s) {
//        Intent intent =  new Intent(this, MapsActivity.this);
//        PendingIntent pi = PendingIntent.getActivity(this, s, intent, 0);
        Resources r = getResources();
        Notification notification = new NotificationCompat.Builder(this)
                .setPriority(Notification.PRIORITY_HIGH)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.logotypesmall))
                .setDefaults(NotificationCompat.DEFAULT_VIBRATE|NotificationCompat.DEFAULT_SOUND)
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle(title)
                .setContentText(text)
//                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();
        finish();
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(s, notification);
    }
}

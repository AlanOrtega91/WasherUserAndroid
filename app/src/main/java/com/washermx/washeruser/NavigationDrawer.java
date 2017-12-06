package com.washermx.washeruser;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.washermx.washeruser.model.AppData;
import com.washermx.washeruser.model.Car;
import com.washermx.washeruser.model.Cleaner;
import com.washermx.washeruser.model.Database.DataBase;
import com.washermx.washeruser.model.Precio;
import com.washermx.washeruser.model.Reportes;
import com.washermx.washeruser.model.Service;
import com.washermx.washeruser.model.User;
import com.washermx.washeruser.model.UserCard;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class NavigationDrawer extends AppCompatActivity implements View.OnClickListener, LocationListener, OnMapReadyCallback, TextView.OnEditorActionListener {

    private DrawerLayout drawerLayout;
    private ArrayList<String> navigationItems = new ArrayList<>();
    private ArrayList<Pair<String,Drawable>> listItems = new ArrayList<>();
    private static final int PAYMENT = 1;
    private static final int BILLING = 2;
    private static final int HISTORY = 3;
    private static final int CARS = 4;
    private static final int PROMOS = 5;
    private static final int HELP = 6;
    private static final int BE_PART_OF_TEAM = 7;
    private static final int CONFIGURATION = 8;
    private static final int ABOUT = 9;
    Handler handler = new Handler(Looper.getMainLooper());
    private int viewState;
    private static final int STANDBY = 0;
    private static final int VEHICLE_SELECTED = 1;
    private static final int OUTSIDE_OR_INSIDE_SELECTED = 2;
    private static final int SERVICE_START = 3;
    LinearLayout upLayout;
    LinearLayout lowLayout;
    LinearLayout startLayout;
    LinearLayout rightLayout;
    LinearLayout leftLayout;
    TextView leftButton;
    TextView leftDescription_left;
    TextView rightButton;
    TextView cleanerInfo;
    ImageView cleanerImageInfo;
    TextView serviceInfo;
    String service;
    String vehicleType;
    Cleaner cleaner;
    Boolean serviceRequestedFlag = false;
    /*
     * Map
     */
    private final static int ONE_SECOND = 1000;
    private GoogleMap map;
    LatLng requestLocation = new LatLng(0, 0);
    EditText serviceLocationText;
    Marker centralMarker;
    Marker cleanerMarker;
    List<Cleaner> cleaners = new ArrayList<>();
    List<Marker> markers = new ArrayList<>();
    LocationManager locationManager;
    ImageView vehiclesButton;
    /*
     * Timers
     */
    Timer nearbyCleanersTimer;
    Timer reloadMapTimer;
    Timer reloadAddressTimer;
    Timer clock;
    Timer cancelAlarmClock;
    /*
     * Service
     */
    SharedPreferences settings;
    Service activeService;
    String idClient;
    User user;
    UserCard creditCard = null;
    Button cancelButton;
    Thread activeServiceCycleThread;
    String token;
    Boolean cancelSent = false;
    int cancelCode = 0;
    Boolean showCancelAlert = false;
    AlertDialog requestingAlert;
    AlertDialog alertBox;
    Boolean noSessionFound = false;
    String metodoDePago = "t";
    TextView metodoDePagoTexto;

    List<Precio> precios;
    Integer idRegion;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);
        Intent firebaseIntent = new Intent(getBaseContext(),FirebaseMessagingService.class);
        startService(firebaseIntent);
        initView();
        initLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cleaners.clear();
        initValues();
        configureServices();
        readUserImage();
        initTimers();
        AppData.saveInBackground(settings,false);
        if (showCancelAlert){
            buildAlertForCancel();
            showCancelAlert = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        cancelTimers();
        AppData.saveInBackground(settings,true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTimers();
    }

    public void startActiveServiceCycle() {
        if (activeServiceCycleThread == null || !activeServiceCycleThread.isAlive()) {
            activeServiceCycleThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    activeServiceCycle();
                }
            });
            activeServiceCycleThread.start();
        }
    }

    public void activeServiceCycle(){
        DataBase db = new DataBase(getBaseContext());
        while((activeService = db.getActiveService()) != null) {
            configureActiveServiceView();
            while (!settings.getBoolean(AppData.SERVICE_CHANGED,false));
        }
        configureServiceForDelete();
        checkNotification();
    }
    private void configureActiveServiceView() {
        checkNotification();
        switch (activeService.status) {
            case "Looking":
                configureActiveServiceForLooking();
                break;
            case "Accepted":
                Calendar cal = Calendar.getInstance();
                cal.setTime(activeService.acceptedTime);
                cal.add(Calendar.MINUTE, 5);
                long diffInMillis = Service.getDifferenceTimeInMillis(cal.getTime());
                if (diffInMillis < 0) {
                    diffInMillis = 0;
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            cancelButton.setVisibility(View.VISIBLE);
                        }
                    });
                }
                cancelCode = 1;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        cancelButton.setVisibility(View.GONE);
                    }
                },diffInMillis);
                configureActiveService(getString(R.string.accepted));
                cancelAlarmClock = new Timer();
                cancelAlarmClock.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (activeService == null || cancelSent)
                                    return;
                                cancelCode = 2;
                                buildAlertForCancel();
                                if (settings.getBoolean(AppData.IN_BACKGROUND,false)) {
                                    ServiceStatusNotification.notify(getBaseContext(), getString(R.string.wish_cancel), NavigationDrawer.class);
                                    showCancelAlert = true;
                                }
                            }
                        });
                    }
                },ONE_SECOND*60*15);
                break;
            case "On The Way":
                if (cancelAlarmClock != null) cancelAlarmClock.cancel();
                configureActiveService(getString(R.string.on_the_way));
                break;
            case "Started":
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        cancelButton.setVisibility(View.GONE);
                    }
                });
                if (cancelAlarmClock != null) cancelAlarmClock.cancel();
                clock = new Timer();
                clock.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (activeService == null || activeService.finalTime == null)
                            return;
                        String display;
                        long diff = Service.getDifferenceTimeInMillis(activeService.finalTime);
                        int minutes = (int)diff/1000/60 + 1;
                        if (diff < 0) {
                            display = getResources().getString(R.string.started) + " 0 min";
                        }
                        else {
                            display = getResources().getString(R.string.started) + " " + minutes + " min";
                        }
                        configureActiveService(display);
                    }
                },1,1000);
                break;
            case "Finished":
                configureActiveServiceForFinished();
                break;
        }
        AppData.notifyNewData(settings,false);
    }

    private void configureActiveServiceForLooking() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                cleanerInfo.setVisibility(View.GONE);
                cleanerImageInfo.setVisibility(View.GONE);
                serviceInfo.setText(R.string.looking);
                cancelButton.setVisibility(View.VISIBLE);
                if (activeService != null)
                    centralMarker.setPosition(new LatLng(activeService.latitud, activeService.longitud));
                for (int i = 0; i < markers.size(); i++) {
                    Marker marker = markers.get(i);
                    marker.remove();
                }
            }
        });
    }

    private void configureActiveService(final String display) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (activeService == null)
                    return;
                cleanerInfo.setVisibility(View.VISIBLE);
                cleanerImageInfo.setVisibility(View.VISIBLE);
                cleanerInfo.setText(activeService.cleanerName);
                serviceInfo.setText(display);
                centralMarker.setPosition(new LatLng(activeService.latitud, activeService.longitud));
            }
        });
        setImageDrawableForActiveService();
    }

    private void configureActiveServiceForFinished() {
        if (clock != null) clock.cancel();
        if (activeService.rating == -1)
            changeActivity(SummaryActivity.class,false);
        serviceRequestedFlag = false;
        handler.post(new Runnable() {
            @Override
            public void run() {
                cleanerInfo.setVisibility(View.GONE);
                cleanerImageInfo.setVisibility(View.GONE);
                cleanerInfo.setText(getString(R.string.init_string));
                cleanerImageInfo.setImageDrawable(null);
                serviceInfo.setText(getString(R.string.looking));
                if (alertBox != null) {
                    alertBox.dismiss();
                }
            }
        });
    }

    private void configureServiceForDelete() {
        serviceRequestedFlag = false;
        handler.post(new Runnable() {
            @Override
            public void run() {
                cleanerInfo.setVisibility(View.GONE);
                cleanerImageInfo.setVisibility(View.GONE);
                cleanerInfo.setText(getString(R.string.init_string));
                cleanerImageInfo.setImageDrawable(null);
                serviceInfo.setText(getString(R.string.init_string));
                onResume();
                if (alertBox != null) {
                    alertBox.dismiss();
                }
            }
        });
    }


    private void checkNotification() {
        final String message = settings.getString(AppData.MESSAGE,null);
        if (message != null && !message.equals("Finished")) {
            AppData.deleteMessage(settings);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    createAlert(message);
                }
            });
        }
    }

    private void buildAlertForCancel(){
        if (cancelCode == 2) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.service_taking_long))
                    .setMessage(getString(R.string.service_taking_long_message))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            sendCancel();
                            if (cancelAlarmClock != null) cancelAlarmClock.cancel();
                        }
                    })
                    .setNegativeButton(R.string.wait, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            dialog.cancel();
                            if (cancelAlarmClock != null) cancelAlarmClock.cancel();
                            cancelAlarmClock = new Timer();
                            cancelAlarmClock.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            buildAlertForCancel();
                                            if (settings.getBoolean(AppData.IN_BACKGROUND, false))
                                                ServiceStatusNotification.notify(getBaseContext(), getString(R.string.wish_cancel), NavigationDrawer.class);
                                        }
                                    });
                                }
                            }, ONE_SECOND * 60 * 15);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.cancel))
                    .setMessage(getString(R.string.cancel_with_charge))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            sendCancel();
                            if (cancelAlarmClock != null) cancelAlarmClock.cancel();
                        }
                    })
                    .setNegativeButton(R.string.wait, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            dialog.cancel();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

    }


    public void setImageDrawableForActiveService() {
        try {
            URL url = new URL("http://54.218.50.2/api/imagenes/lavadores/" + activeService.cleanerId + "/profile_image.jpg");
            InputStream is = url.openStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            Bitmap bm = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();
            if (bm == null)
                return;

            final Drawable image = new BitmapDrawable(getResources(), bm);
            image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    cleanerImageInfo.setImageDrawable(image);
                }
            });

        } catch (Exception e) {
            Log.i("Error","");
        }
    }


    private void initValues() {
        settings = getSharedPreferences(AppData.FILE, 0);
        idClient = settings.getString(AppData.IDCLIENTE, null);
        token = settings.getString(AppData.TOKEN,null);
        DataBase db = new DataBase(getBaseContext());
        user = db.readUser();
        creditCard = db.readCard();
        activeService = db.getActiveService();
        if (activeService == null) {
            viewState = STANDBY;
            configureState();
        } else if (activeService.status.equals("Finished")) {
            changeActivity(SummaryActivity.class, false);
            viewState = SERVICE_START;
            configureState();
        }
        else {
            viewState = SERVICE_START;
            configureState();
            startActiveServiceCycle();
        }
    }

    private void initView() {
        configureMenu();
        configureActionBar();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        vehiclesButton =  findViewById(R.id.vehiclesImage);
        startLayout =  findViewById(R.id.startLayout);
        lowLayout =  findViewById(R.id.lowLayout);
        upLayout =  findViewById(R.id.upLayout);
        rightLayout =  findViewById(R.id.rightLayout);
        leftLayout =  findViewById(R.id.leftLayout);
        leftButton =  findViewById(R.id.leftButton);
        leftDescription_left =  findViewById(R.id.leftDescription_left);
        rightButton =  findViewById(R.id.rightButton);
        cleanerInfo =  findViewById(R.id.cleanerInfo);
        cleanerImageInfo =  findViewById(R.id.cleanerImageInfo);
        serviceInfo =  findViewById(R.id.serviceInfo);
        serviceLocationText =  findViewById(R.id.serviceLocationText);
        cancelButton =  findViewById(R.id.cancelButton);
        serviceLocationText.setOnEditorActionListener(this);
        startLayout.setOnClickListener(this);
        metodoDePagoTexto =  findViewById(R.id.metodoDePago);
        if (new DataBase(getBaseContext()).readCard() == null) {
            metodoDePago = "e";
            metodoDePagoTexto.setText(getString(R.string.efectivo));
        } else {
            metodoDePago = "t";
            metodoDePagoTexto.setText(getString(R.string.tarjeta));
        }
    }

    private void readUserImage() {
        TextView headerTitle =  findViewById(R.id.menuName);
        ImageView menuImage =  findViewById(R.id.menuImage);
        headerTitle.setText(getString(R.string.user_name,user.name,user.lastName));
        if (!user.imagePath.equals("")) {
            menuImage.setImageBitmap(User.readImageBitmapFromFile(user.imagePath));
        } else {
            menuImage.setImageDrawable(ContextCompat.getDrawable(getBaseContext(),R.drawable.default_image));
        }
    }

    private void configureServices() {
        vehiclesButton.setAlpha(0.5f);
        vehiclesButton.setClickable(false);
        int type = 6;
        Car selectedCar = new DataBase(getBaseContext()).getFavoriteCar();
        if (selectedCar != null)
            type = Integer.valueOf(selectedCar.type);

        switch (type) {
            case Service.BIKE:
                vehiclesButton.setAlpha(1.0f);
                vehiclesButton.setClickable(true);
                vehiclesButton.setImageDrawable(ContextCompat.getDrawable(getBaseContext(),R.drawable.bike_active));
                break;
            case Service.CAR:
                vehiclesButton.setAlpha(1.0f);
                vehiclesButton.setClickable(true);
                vehiclesButton.setImageDrawable(ContextCompat.getDrawable(getBaseContext(),R.drawable.car_active));
                break;
            case Service.SUV:
                vehiclesButton.setAlpha(1.0f);
                vehiclesButton.setClickable(true);
                vehiclesButton.setImageDrawable(ContextCompat.getDrawable(getBaseContext(),R.drawable.small_van_active));
                break;
            case Service.VAN:
                vehiclesButton.setAlpha(1.0f);
                vehiclesButton.setClickable(true);
                vehiclesButton.setImageDrawable(ContextCompat.getDrawable(getBaseContext(),R.drawable.big_van_active));
                break;
            default:
                break;
        }
    }


    private void initLocation() {
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        } catch (SecurityException e) {
            postAlert("Location services not working");
        }
    }

    private void initTimers() {
        nearbyCleanersTimer = new Timer();
        nearbyCleanersTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                    try {
                        leeYDibujaLavadores();
                    } catch (Cleaner.noSessionFound e) {
                        if (!noSessionFound) {
                            noSessionFound = true;
                            postAlert(getString(R.string.error_sesion));
                            changeActivity(MainActivity.class, true);
                        }
                        finish();
                    }
            }
        }, 0, ONE_SECOND / 10);
        reloadMapTimer = new Timer();
        reloadMapTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (activeService != null && !activeService.status.equals("Looking")) {
                            centralMarker.setPosition(new LatLng(activeService.latitud, activeService.longitud));
                        } else if (requestLocation != null){
                            requestLocation = map.getCameraPosition().target;
                            centralMarker.setPosition(new LatLng(requestLocation.latitude, requestLocation.longitude));
                        }
                    }
                });
            }
        }, 0, ONE_SECOND/50);
        reloadAddressTimer = new Timer();
        reloadAddressTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                getGeoLocation();
            }
        }, 0, ONE_SECOND);
    }


    private void cancelTimers() {
        if (nearbyCleanersTimer != null) {
            nearbyCleanersTimer.cancel();
        }
        if (reloadMapTimer != null) {
            reloadMapTimer.cancel();
        }
        if (reloadAddressTimer != null) {
            reloadAddressTimer.cancel();
        }
    }

    public void sendRequestService(){
        try {
            Car favCar = new DataBase(getBaseContext()).getFavoriteCar();
            Service serviceRequested = Service.requestService("", String.valueOf(requestLocation.latitude),
                    String.valueOf(requestLocation.longitude), service, token, vehicleType, favCar.id, metodoDePago, idRegion);
            DataBase db = new DataBase(getBaseContext());
            List<Service> services = db.readServices();
            services.add(serviceRequested);
            db.saveServices(services);
            cancelCode = 0;
            activeService = serviceRequested;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    upLayout.setVisibility(LinearLayout.GONE);
                    lowLayout.setVisibility(LinearLayout.GONE);
                    startLayout.setVisibility(LinearLayout.VISIBLE);
                    cancelButton .setVisibility(View.VISIBLE);
                    cleanerInfo.setVisibility(View.GONE);
                    serviceLocationText.setEnabled(true);
                    serviceInfo.setText(R.string.looking);
                    requestingAlert.cancel();
                    if (activeService.metodoDePago.equals("e")) {
                        createAlert(getString(R.string.pagoEfectivo,activeService.precioAPagar));
                    }
                }
            });
            startActiveServiceCycle();
            cancelSent = false;
        } catch (Service.errorRequestingService e) {
            postAlert("Error pidiendo el servicio, revisa que no tengas adeudos.");
            serviceRequestedFlag = false;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onResume();
                    requestingAlert.cancel();
                }
            });
        } catch (Service.noSessionFound e){
            if (!noSessionFound) {
                noSessionFound = true;
                postAlert(getString(R.string.error_sesion));
                changeActivity(MainActivity.class, true);
            }
            finish();
        } catch (Service.userBlock e){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    createAlert(getString(R.string.user_block));
                    serviceRequestedFlag = false;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onResume();
                            requestingAlert.cancel();
                        }
                    });
                }
            });
        }
    }

    private void leeYDibujaLavadores() throws Cleaner.noSessionFound
    {
        if (activeService != null && !activeService.status.equals("Looking")) {
            readCleanerLocation();
            actualizaMarcadorDeLavador();
        } else if (requestLocation != null){
            getNearbyCleaners();
            actualizaMarcadorDeLavadores();
        }
    }
    private void readCleanerLocation() throws Cleaner.noSessionFound{
        try{
            cleaner = Cleaner.getCleanerLocation(activeService.cleanerId,token);
        } catch (Cleaner.errorGettingCleaners e){
            Log.i("ERROR","Reading cleaner location");
        }
    }

    private void getNearbyCleaners() throws Cleaner.noSessionFound {
        try {
            cleaners = Cleaner.getNearbyCleaners(requestLocation.latitude, requestLocation.longitude,token);
        } catch (Cleaner.errorGettingCleaners e){
            Log.i("Cleaners Error","Couldnt retrieve cleaners try again later");
        }
    }

    private void actualizaMarcadorDeLavador() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (activeService != null && !activeService.status.equals("Looking") && cleaner != null) {
                    cleanerMarker.setVisible(true);
                    cleanerMarker.setPosition(new LatLng(cleaner.latitud, cleaner.longitud));
                }
            }
        });
    }

    private void actualizaMarcadorDeLavadores() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                cleanerMarker.setVisible(false);
                if (cleaners.size() >= markers.size()) {
                    addMarkersAndUpdate();
                } else {
                    removeMarkersAndUpdate();
                }
            }
        });
    }


    private void removeMarkersAndUpdate() {
        List<Marker> aux = new ArrayList<>();
        for (int i = 0; i < cleaners.size();i++){
            aux.add(markers.get(i));
            Cleaner cleaner = cleaners.get(i);
            aux.get(i).setPosition(new LatLng(cleaner.latitud,cleaner.longitud));
        }
        for (int i = cleaners.size(); i < markers.size(); i++) {
            markers.get(i).setVisible(false);
            markers.get(i).remove();
        }
        markers = aux;
    }

    private void addMarkersAndUpdate() {
        try {
            List<Marker> aux = new ArrayList<>();
            LatLng auxLocation = new LatLng(0.0, 0.0);
            BitmapDrawable drawable = (BitmapDrawable) ContextCompat.getDrawable(getBaseContext(), R.drawable.washer_bike);
            Bitmap b = drawable.getBitmap();
            Bitmap bitmapLavador = Bitmap.createScaledBitmap(b, 100, 100, false);
            drawable = (BitmapDrawable) ContextCompat.getDrawable(getBaseContext(), R.drawable.washer_bike_ocupado);
            b = drawable.getBitmap();
            Bitmap bitmapLavadorOcupado = Bitmap.createScaledBitmap(b, 100, 100, false);
            for (int i = 0; i < cleaners.size(); i++) {
                if (i < markers.size()) {
                    aux.add(markers.get(i));
                } else {
                    aux.add(map.addMarker(new MarkerOptions()
                            .position(auxLocation)
                            .icon(BitmapDescriptorFactory.fromBitmap(bitmapLavador))));
                }
                Cleaner cleaner = cleaners.get(i);
                aux.get(i).setPosition(new LatLng(cleaner.latitud, cleaner.longitud));
                if (cleaner.ocupado) {
                    aux.get(i).setIcon(BitmapDescriptorFactory.fromBitmap(bitmapLavadorOcupado));
                } else {
                    if (map.getProjection().getVisibleRegion().latLngBounds.contains(aux.get(i).getPosition()) && aux.get(i).isVisible())
                        aux.get(i).setIcon(BitmapDescriptorFactory.fromBitmap(bitmapLavador));
                }
            }
            markers = aux;
        } catch (IllegalArgumentException e) {
            Log.i("Error","Mapa");
        }
    }

    private void getGeoLocation() {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(requestLocation.latitude, requestLocation.longitude, 1);
            if (addresses.size() < 1)
                return;
            final String address = addresses.get(0).getAddressLine(0);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (serviceLocationText.didTouchFocusSelect())
                        serviceLocationText.setText(address);
                }
            });
        } catch (Throwable e) {
            //Log.i("LOCATION","Error gettin geo location = " + e.getMessage());
        }
    }

    public void vehicleClicked(View view) {
        Double latitud = requestLocation.latitude;
        Double longitud = requestLocation.longitude;
        new BuscarPreciosAsync().execute(latitud, longitud);
    }

    private void configureState() {
        switch (viewState) {
            case STANDBY:
                configureStandbyState();
                break;
            case VEHICLE_SELECTED:
                configureVehicleSelectedState();
                break;
            case OUTSIDE_OR_INSIDE_SELECTED:
                configureServiceTypeState();
                viewState = SERVICE_START;
                break;
            case SERVICE_START:
                configureServiceStartState();
                viewState = -1;
                break;
        }
    }

    private void configureStandbyState() {
        upLayout.setVisibility(LinearLayout.VISIBLE);
        lowLayout.setVisibility(LinearLayout.GONE);
        startLayout.setVisibility(LinearLayout.GONE);
        serviceLocationText.setVisibility(View.VISIBLE);
    }

    private void configureVehicleSelectedState() {
        upLayout.setVisibility(LinearLayout.VISIBLE);
        lowLayout.setVisibility(LinearLayout.VISIBLE);
        startLayout.setVisibility(LinearLayout.GONE);
        serviceLocationText.setVisibility(View.GONE);
        rightLayout.setVisibility(View.VISIBLE);
        String leftTitle = getString(R.string.outside);
        String rightTitle = getString(R.string.outside_and_inside);
        leftDescription_left.setText(R.string.outside_description_left);
        String precio;
        try {
            switch (Integer.parseInt(vehicleType)) {
                case Service.BIKE:
                    precio = buscarPrecioPorIdServicioYIdVehiculo(1, Service.BIKE);
                    leftTitle += " $" + precio;
                    rightLayout.setVisibility(View.INVISIBLE);
                    leftDescription_left.setText(R.string.outside_description_bike_left);
                    break;
                case Service.CAR:
                    precio = buscarPrecioPorIdServicioYIdVehiculo(1, Service.CAR);
                    leftTitle += " $" + precio;
                    precio = buscarPrecioPorIdServicioYIdVehiculo(2, Service.CAR);
                    rightTitle += " $" + precio;
                    break;
                case Service.SUV:
                    precio = buscarPrecioPorIdServicioYIdVehiculo(1, Service.SUV);
                    leftTitle += " $" + precio;
                    precio = buscarPrecioPorIdServicioYIdVehiculo(2, Service.SUV);
                    rightTitle += " $" + precio;
                    break;
                case Service.VAN:
                    precio = buscarPrecioPorIdServicioYIdVehiculo(1, Service.VAN);
                    leftTitle += " $" + precio;
                    precio = buscarPrecioPorIdServicioYIdVehiculo(2, Service.VAN);
                    rightTitle += " $" + precio;
                    break;
                default:
                    throw new errorBuscandoPrecio();
            }
            leftButton.setText(leftTitle);
            rightButton.setText(rightTitle);
            serviceLocationText.setEnabled(true);
        } catch (errorBuscandoPrecio e) {
            this.createAlert("No se encontro precio para este vehiculo");
            viewState = STANDBY;
            configureState();
        }
    }

    private String buscarPrecioPorIdServicioYIdVehiculo(int idServicio, int idVehiculo) throws errorBuscandoPrecio {
        for (Precio precio : this.precios) {
            if (precio.idServicio == idServicio && precio.idVehiculo == idVehiculo)
            {
                NumberFormat nf= NumberFormat.getInstance();
                nf.setMaximumFractionDigits(2);
                nf.setMinimumFractionDigits(2);
                return nf.format(precio.precio);
            }
        }
        throw new errorBuscandoPrecio();
    }

    private void configureServiceTypeState() {
        if (serviceRequestedFlag)
            return;
        serviceRequestedFlag = true;
        createAlertRequestConfirmation();

    }

    private void createAlertRequestConfirmation() {
        new AlertDialog.Builder(this)
                .setMessage("Confirmar pedido del servicio")
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        createAlertRequesting();
                        Thread sendRequestServiceThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                sendRequestService();
                            }
                        });
                        sendRequestServiceThread.start();
                        dialog.cancel();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        serviceRequestedFlag = false;
                        viewState = OUTSIDE_OR_INSIDE_SELECTED;
                        dialog.cancel();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void configureServiceStartState() {
        upLayout.setVisibility(LinearLayout.GONE);
        lowLayout.setVisibility(LinearLayout.GONE);
        startLayout.setVisibility(LinearLayout.VISIBLE);
        serviceLocationText.setVisibility(View.GONE);
        cancelButton.setVisibility(View.VISIBLE);
        serviceInfo.setText(R.string.looking);
        serviceLocationText.setEnabled(false);
        configureActiveServiceView();
    }

    public void leftClick(View view) {
        service = String.valueOf(Service.OUTSIDE);
        viewState = OUTSIDE_OR_INSIDE_SELECTED;
        serviceRequestedFlag = false;
        configureState();
    }

    public void rightClick(View view) {
        service = String.valueOf(Service.OUTSIDE_INSIDE);
        viewState = OUTSIDE_OR_INSIDE_SELECTED;
        serviceRequestedFlag = false;
        configureState();
    }

    public void clickedMyLocation(View view) {
        try {
            Location lastKnownLocation = getBestKnownLocation();
            requestLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(requestLocation, 15);
            map.moveCamera(cameraUpdate);
        } catch (errorReadingLocation e) {
            postAlert("No se tiene acceso a la ubicacion");
        }
    }

    private Location getBestKnownLocation () throws errorReadingLocation{
        try {
            Location ubicacion = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            if (ubicacion == null) {
                throw new errorReadingLocation();
            } else {
                return ubicacion;
            }
        } catch (SecurityException e) {
            throw new errorReadingLocation();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            BitmapDrawable cleanerDrawable = (BitmapDrawable) ContextCompat.getDrawable(getBaseContext(),R.drawable.washer_bike);
            Bitmap b = cleanerDrawable.getBitmap();
            Bitmap bitmapResized = Bitmap.createScaledBitmap(b, 60, 60, false);
            map = googleMap;
            map.setTrafficEnabled(true);
            map.setMyLocationEnabled(true);
            cleanerMarker = map.addMarker(new MarkerOptions()
                    .position(new LatLng(0,0))
                    .icon(BitmapDescriptorFactory.fromBitmap(bitmapResized)));
            cleanerMarker.setVisible(false);
            centralMarker = map.addMarker(new MarkerOptions()
                    .position(new LatLng(0,0))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            Location lastKnownLocation = getBestKnownLocation();
            requestLocation = new LatLng(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(requestLocation,15);
            map.moveCamera(cameraUpdate);
            centralMarker.setPosition(requestLocation);
        } catch (errorReadingLocation e){
            createAlert(getString(R.string.no_location_service));
        } catch (SecurityException e) {
            Log.i("Security","Location");
        }
    }


    private void createAlert(String title) {
        if (alertBox != null) {
            alertBox.dismiss();
        }
        alertBox = new AlertDialog.Builder(this)
                .setMessage(title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void createAlertRequesting( ) {
        requestingAlert = new AlertDialog.Builder(this)
                .setMessage(getString(R.string.requesting))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void configureActionBar() {
        ActionBar optionsTitleBar = getSupportActionBar();
        if (optionsTitleBar != null) {
            optionsTitleBar.setDisplayShowHomeEnabled(false);
            optionsTitleBar.setDisplayShowCustomEnabled(true);
            optionsTitleBar.setDisplayShowTitleEnabled(false);
            optionsTitleBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            optionsTitleBar.setCustomView(R.layout.titlebar_map);
            Toolbar parent =(Toolbar) optionsTitleBar.getCustomView().getParent();
            parent.setContentInsetsAbsolute(0,0);
        }
        TextView menuTitle = findViewById(R.id.menuMapTitle);
        menuTitle.setText(R.string.app_name_display);
        menuTitle.setTextColor(Color.rgb(7,96,53));
    }

    private void configureMenu() {
        drawerLayout = findViewById(R.id.drawer_layout);
        ListView menuList = findViewById(R.id.menuList);
        View header = getLayoutInflater().inflate(R.layout.menu_header,menuList,false);
        menuList.addHeaderView(header);
        String [] titles = getResources().getStringArray(R.array.menu_options);
        Collections.addAll(navigationItems, titles);
        listItems.add(Pair.create(titles[0], ContextCompat.getDrawable(getBaseContext(),R.drawable.pay_icon)));
        listItems.add(Pair.create(titles[1], ContextCompat.getDrawable(getBaseContext(),R.drawable.billing_icon)));
        listItems.add(Pair.create(titles[2], ContextCompat.getDrawable(getBaseContext(),R.drawable.history_icon)));
        listItems.add(Pair.create(titles[3], ContextCompat.getDrawable(getBaseContext(),R.drawable.vehicle_icon)));
        listItems.add(Pair.create(titles[4], ContextCompat.getDrawable(getBaseContext(),R.drawable.promo_icon)));
        listItems.add(Pair.create(titles[5], ContextCompat.getDrawable(getBaseContext(),R.drawable.help_icon)));
        listItems.add(Pair.create(titles[6], ContextCompat.getDrawable(getBaseContext(),R.drawable.work_icon)));
        listItems.add(Pair.create(titles[7], ContextCompat.getDrawable(getBaseContext(),R.drawable.config_icon)));
        listItems.add(Pair.create(titles[8], ContextCompat.getDrawable(getBaseContext(),R.drawable.line_white)));
        final MenuAdapter adapter = new MenuAdapter();
        menuList.setAdapter(adapter);
        menuList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                decideFragment(position);
            }
        });
    }

    private void decideFragment(int position) {
        switch (position){
            case PAYMENT:
                changeActivity(PaymentActivity.class, false);
                return;
            case BILLING:
                changeActivity(BillingActivity.class, false);
                return;
            case HISTORY:
                changeActivity(HistoryActivity.class, false);
                return;
            case PROMOS:
                changeActivity(Promociones.class, false);
                return;
            case CARS:
                changeActivity(CarsActivity.class, false);
                return;
            case HELP:
                changeActivity(HelpActivity.class, false);
                return;
            case BE_PART_OF_TEAM:
                Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.washer.mx/team"));
                startActivity(myIntent);
                break;
            case CONFIGURATION:
                changeActivity(ConfigurationActivity.class, false);
                return;
            case ABOUT:
                changeActivity(AboutActivity.class, false);
                return;
            default:
                break;
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.startLayout) {
            changeActivity(InformationActivity.class, false);
        }
    }

    public void onClickMenu(View view){
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else
            drawerLayout.openDrawer(GravityCompat.START);
    }

    private void changeActivity(Class activity, Boolean clear) {
        Intent intent = new Intent(getBaseContext(), activity);
        if (clear) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        startActivity(intent);
    }

    private void postAlert(final String message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else if (viewState == SERVICE_START || viewState == STANDBY) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
        } else {
            viewState = STANDBY;
            configureState();
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        final String location = serviceLocationText.getText().toString();
        Thread sendModifyLocationThread = new Thread(new Runnable() {
            @Override
            public void run() {
                modifyLocation(location);
            }
        });
        sendModifyLocationThread.start();
        return true;
    }

    private void modifyLocation(String location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocationName(location,5);
            Criteria crit = new Criteria();
            crit.setAccuracy(Criteria.ACCURACY_FINE);
            crit.setPowerRequirement(Criteria.POWER_HIGH);
            String provider = locationManager.getBestProvider(crit,true);
            Location lastLocation = getBestKnownLocation();
            if (addresses.size() < 1)
                return;
            Address closestAddress = addresses.get(0);
            float lastDistance = 0;
            for (Address address:addresses){
                Location newLocation = new Location(provider);
                newLocation.setLatitude(address.getLatitude());
                newLocation.setLongitude(address.getLongitude());
                float distance = lastLocation.distanceTo(newLocation);
                if (lastDistance > distance)
                    closestAddress = address;
                lastDistance = distance;
            }
            final LatLng locationLatLng = new LatLng(closestAddress.getLatitude(),closestAddress.getLongitude());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(locationLatLng,15);
                    map.animateCamera(cameraUpdate);
                    InputMethodManager imn = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imn != null)
                    {
                        imn.hideSoftInputFromWindow(serviceLocationText.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                    serviceLocationText.clearFocus();
                }
            });
        } catch (errorReadingLocation e){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    createAlert(getString(R.string.no_location_service));
                }
            });
        } catch (Exception e) {
            postAlert("Error getting street name");
        }
    }

    public void onClickCancel(View view) {
        if (cancelCode == 0)
            sendCancel();
        else if (cancelCode == 1)
            buildAlertForCancel();
    }

    private void sendCancel(){
        Thread sendCancelThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (cancelSent)
                        return;
                    cancelSent = true;
                    Service.cancelService(activeService.id,token,cancelCode);
                    if (cancelAlarmClock != null){
                        cancelAlarmClock.cancel();
                        cancelAlarmClock.purge();
                        cancelAlarmClock = null;
                    }
                } catch (Throwable e) {
                    Log.i("CANCEL","Error canceling");
                    postAlert(getString(R.string.error_on_cancel));
                    cancelSent = false;
                }
            }
        });
        sendCancelThread.start();
    }

    public void mostrarMetodoDePago(View view) {
        if (creditCard != null) {
            alertBox = new AlertDialog.Builder(this)
                    .setTitle("¿Que metodo de pago deseas utilizar?")
                    .setMessage("Si seleccionas efectivo deberas estar presente cuando llegue el lavador para pagar antes de iniciar el servicio")
                    .setPositiveButton(getString(R.string.efectivo), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            metodoDePago = "e";
                            metodoDePagoTexto.setText(getString(R.string.efectivo));
                            dialog.cancel();
                        }
                    })
                    .setNegativeButton(getString(R.string.tarjeta), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            metodoDePago = "t";
                            metodoDePagoTexto.setText(getString(R.string.tarjeta));
                            dialog.cancel();
                        }
                    })
                    .show();
        }
    }

    private class MenuAdapter extends ArrayAdapter<Pair<String,Drawable>> {
        MenuAdapter() { super(NavigationDrawer.this,R.layout.menu_item,R.id.listItemName,listItems); }

        @NonNull
        @Override
        public View getView(int position, View convertView,@NonNull ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.menu_item, parent, false);
            }
            try {
                Pair<String,Drawable> item = listItems.get(position);
                Drawable icon = item.second;
                TextView itemName = itemView.findViewById(R.id.listItemName);
                ImageView itemImage = itemView.findViewById(R.id.listItemImage);
                if (position < navigationItems.size() - 1) {
                    itemImage.setImageDrawable(icon);
                } else {
                    itemName.setCompoundDrawablesWithIntrinsicBounds(null,icon,null,null);
                    itemImage.setImageDrawable(null);
                }
                itemName.setText(item.first);
                return itemView;
            } catch (Exception e){
                return itemView;
            }
        }
    }

    private class BuscarPreciosAsync extends AsyncTask<Double, Void, String>
    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            findViewById(R.id.layoutCargando).setVisibility(View.VISIBLE);
            findViewById(R.id.layoutCargando).bringToFront();
        }

        List<Precio> preciosLeidos;
        @Override
        protected String doInBackground(Double... ubicacion) {
            try {
                preciosLeidos = Service.leerPrecios(ubicacion[0], ubicacion[1]);
                return "ok";
            } catch (Service.errorLeyendoPrecios e) {
                return "error";
            }
        }

        @Override
        protected void onPostExecute(String resultado) {
            super.onPostExecute(resultado);
            findViewById(R.id.layoutCargando).setVisibility(View.GONE);
            switch (resultado) {
                case "ok":
                    if (preciosLeidos.size() < 1)
                    {
                        createAlert("Error al leer el precio");
                    } else {
                        precios = preciosLeidos;
                        idRegion = precios.get(0).region;
                        if (viewState != STANDBY)
                            return;
                        if (cleaners.size() < 1) {
                            postAlert(getString(R.string.no_cleaners));
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Reportes.sendReport("Demanda", requestLocation.latitude, requestLocation.longitude);
                                }
                            }).start();
                            return;
                        }
                        viewState = VEHICLE_SELECTED;
                        vehicleType = new DataBase(getBaseContext()).getFavoriteCar().type;
                        configureState();
                    }
                    break;
                default:
                    createAlert("Error al leer el precio");
                    break;
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {  }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }
    @Override
    public void onProviderEnabled(String provider) { }
    @Override
    public void onProviderDisabled(String provider) { }
    private static class errorReadingLocation extends Throwable {}
    private static class errorBuscandoPrecio extends Throwable{}
}

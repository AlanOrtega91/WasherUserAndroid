package com.washermx.washeruser;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.washermx.washeruser.model.AppData;
import com.washermx.washeruser.model.Database.DataBase;
import com.washermx.washeruser.model.ProfileReader;
import com.washermx.washeruser.model.User;

public class InitActivity extends AppCompatActivity {

    SharedPreferences settings;
    private Handler handler = new Handler(Looper.getMainLooper());
    String token;

    private static final int ACCESS_FINE_LOCATION = 1;
    private static final int INTERNET = 2;
    private boolean allPermissionsOk = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);
        FirebaseMessaging.getInstance().subscribeToTopic("test");
        FirebaseInstanceId.getInstance().getToken();
        initValues();
        initView();
        reviewPermissions();
    }

    private void initValues() {
        settings = getSharedPreferences(AppData.FILE, 0);
        token = settings.getString(AppData.TOKEN, null);
    }

    private void reviewPermissions() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION);
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, INTERNET);
        } else if (status != ConnectionResult.SUCCESS) {
            googleApiAvailability.getErrorDialog(this,status,2404).show();
        } else if (settings.getString(AppData.APP_VERSION,null) == null){
            createAlertTerms();
        } else{
            allPermissionsOk = true;
            Thread sendDecideNextView = new Thread(new Runnable() {
                @Override
                public void run() {
                    decideNextView();
                }
            });
            sendDecideNextView.start();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,@NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (!allPermissionsOk)
                reviewPermissions();
        } else {
            createAlert();
        }
    }

    private void createAlert() {
        new AlertDialog.Builder(this)
                .setTitle("Permission denied")
                .setMessage(getResources().getString(R.string.app_name) + " Can't work without this permission")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        AppData.saveVersion(settings,"1.0.0");
                        reviewPermissions();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void createAlertTerms() {
        new AlertDialog.Builder(this)
                .setMessage("Al utilizar esta aplicacion aceptas los terminos y condiciones de uso en la pagina http://www.washerm.mx")
                .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        AppData.saveVersion(settings,"1");
                        reviewPermissions();
                    }
                })
                .show();
    }

    private void initView() {
        ActionBar optionsTitleBar = getSupportActionBar();
        if (optionsTitleBar != null) optionsTitleBar.hide();
    }

    private void decideNextView() {
        if (token == null)
            changeActivity(MainActivity.class);
        else {
            tryReadUser(token);
        }
    }

    private void tryReadUser(final String token) {
        try {
            ProfileReader.run(getBaseContext());
            DataBase db = new DataBase(getBaseContext());
            User user = db.readUser();
            if (user == null) {
                postAlert(getString(R.string.error_reading_user));
                changeActivity(MainActivity.class);
                return;
            }
            user.token = token;

            String fireBaseToken = settings.getString(AppData.FB_TOKEN, "");
            User.saveFirebaseToken(token, fireBaseToken);

            changeActivity(NavigationDrawer.class);
        } catch (ProfileReader.errorReadingProfile e) {
            Log.i("PROFILE", "Session not found");
            ProfileReader.delete(getBaseContext());
            changeActivity(MainActivity.class);
        } catch (User.errorSavingFireBaseToken e) {
            e.printStackTrace();
            ProfileReader.delete(getBaseContext());
            changeActivity(MainActivity.class);
        } catch (User.noSessionFound e) {
            if (!MainActivity.onScreen) postAlert(getString(R.string.session_error));
            changeActivity(MainActivity.class);
        }
    }

    private void postAlert(final String message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void changeActivity(Class activity) {
        Intent intent = new Intent(getBaseContext(),activity);
        startActivity(intent);
        finish();
    }
}

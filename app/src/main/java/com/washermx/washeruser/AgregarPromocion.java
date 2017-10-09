package com.washermx.washeruser;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.washermx.washeruser.model.Database.DataBase;
import com.washermx.washeruser.model.Promocion;
import com.washermx.washeruser.model.User;

import org.w3c.dom.Text;

public class AgregarPromocion extends AppCompatActivity implements View.OnClickListener, LocationListener {

    EditText codigo;
    ProgressBar barraCargando;
    User usuario;
    Button botonAgregar;
    LocationManager locationManager;
    Double latitud;
    Double longitud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_promocion);
        configureActionBar();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            latitud = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude();
            longitud = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude();
        }

    }

    private void configureActionBar() {
        ActionBar optionsTitleBar = getSupportActionBar();
        if (optionsTitleBar != null) {
            optionsTitleBar.setDisplayShowHomeEnabled(false);
            optionsTitleBar.setDisplayShowCustomEnabled(true);
            optionsTitleBar.setDisplayShowTitleEnabled(false);
            optionsTitleBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            optionsTitleBar.setCustomView(R.layout.titlebar_menu);
            Toolbar parent =(Toolbar) optionsTitleBar.getCustomView().getParent();
            parent.setContentInsetsAbsolute(0,0);
        }
        TextView menuButton = (TextView)findViewById(R.id.menuButton);
        TextView menuTitle = (TextView)findViewById(R.id.menuTitle);
        menuTitle.setText(R.string.promocionesTitulo);
        menuButton.setText(R.string.back);
        menuButton.setOnClickListener(this);
    }

    public void agregarCodigo(View view) {
        if (botonAgregar == null) {
            botonAgregar = (Button) view;
        }
        botonAgregar.setEnabled(false);
        codigo = (EditText) findViewById(R.id.codigoAgregar);
        barraCargando = (ProgressBar) findViewById(R.id.barraCargando);
        barraCargando.setVisibility(View.VISIBLE);
        usuario = new DataBase(this).readUser();
        new AgregarCodigo().execute(codigo.getText().toString());
    }

    @Override
    public void onClick(View view) {
        finish();
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }


    private class AgregarCodigo extends AsyncTask<String, Void, String> {

        // Descarga los datos en un thread no principal
        @Override
        protected String doInBackground(String... codigo) {
            try {
                Promocion.agregarPromocion(usuario.id,codigo[0],latitud.toString(),longitud.toString());
            } catch (Promocion.errorAgregandoCodigo errorAgregandoCodigo) {
                return getString(R.string.errorAgregandoCodigo);
            } catch (Promocion.codigoUsado codigoUsado) {
                return getString(R.string.errorCodigoUsado);
            } catch (Promocion.codigoExpirado codigoExpirado) {
                return getString(R.string.errorCodigoExpirado);
            } catch (Promocion.ubicacion ubicacion) {
                return getString(R.string.errorUbicacion);
            } catch (Promocion.codigoNoExiste codigoNoExiste) {
                return getString(R.string.errorCodigoNoExiste);
            }
            return "ok";
        }

        // Se ejecuta despues de doInBackground en el thread principal
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            barraCargando.setVisibility(View.INVISIBLE);
            if (!result.equals("ok")) {
                TextView error = (TextView) findViewById(R.id.resultadoAgregarPromocion);
                error.setText(result);
                error.setTextColor(Color.RED);
                error.setVisibility(View.VISIBLE);
                botonAgregar.setEnabled(true);
            } else {
                finish();
            }
        }
    }
}

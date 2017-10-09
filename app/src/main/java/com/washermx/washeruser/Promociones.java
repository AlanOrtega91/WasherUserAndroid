package com.washermx.washeruser;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.washermx.washeruser.model.Database.DataBase;
import com.washermx.washeruser.model.Promocion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Promociones extends AppCompatActivity implements View.OnClickListener {

    ListView listaPromociones;
    TextView codigoPersonal;
    ProgressBar barraCargando;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promociones);
        configureActionBar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        inicializaVista();
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
        menuButton.setText(R.string.menu);
        menuButton.setOnClickListener(this);
    }

    private void inicializaVista() {
        listaPromociones = (ListView)findViewById(R.id.promociones);
        codigoPersonal = (TextView) findViewById(R.id.codigoPersonal);
        DataBase db = new DataBase(this);
        codigoPersonal.setText(db.readUser().codigo);
        barraCargando = (ProgressBar) findViewById(R.id.barraCargando);
        barraCargando.setVisibility(View.VISIBLE);
        new LeerPromociones().execute(db.readUser().id);
    }

    private SimpleAdapter creaAdaptador(List<Promocion> promociones){
        List<Map<String, String>> datos = new ArrayList<>();
        for (Promocion promocion:promociones) {
            Map<String, String> dato = new HashMap<>(2);
            dato.put("codigo",promocion.codigo);
            dato.put("nombre",promocion.nombre);
            datos.add(dato);
        }
        return new SimpleAdapter(this, datos,
                android.R.layout.simple_list_item_2,
                new String[] {"codigo", "nombre" },
                new int[] {android.R.id.text1, android.R.id.text2 });
    }

    public void cambiarAAgregar(View view) {
        Intent intent = new Intent(getBaseContext(), AgregarPromocion.class);
        startActivity(intent);
    }

    public void copiarAPortaPapaeles(View view) {
        ClipboardManager portaPapeles = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Promocion", codigoPersonal.getText());
        portaPapeles.setPrimaryClip(clip);
        Toast.makeText(getBaseContext(), getString(R.string.copiado), Toast.LENGTH_SHORT).show();
    }


    private class LeerPromociones extends AsyncTask<String, Void, String> {

        List<Promocion> promociones;
        // Descarga los datos en un thread no principal
        @Override
        protected String doInBackground(String... id) {
            try {
                promociones = Promocion.leerPromocion(id[0]);
            } catch (Promocion.errorLeyendoPromociones errorLeyendoPromociones) {
                return getString(R.string.errorLeyendoPromociones);
            }
            return "ok";
        }

        // Se ejecuta despues de doInBackground en el thread principal
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            barraCargando.setVisibility(View.INVISIBLE);
            if (result.equals("ok")) {
                SimpleAdapter adaptador = creaAdaptador(promociones);
                listaPromociones.setAdapter(adaptador);
            }
        }
    }

    @Override
    public void onClick(View view) {
        finish();
    }
}

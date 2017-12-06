package com.washermx.washeruser.model;


import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

public class Promocion {
    private static String HTTP_LOCATION = "promocion/";
    public String codigo;
    public String nombre;


    public static void agregarPromocion(String id, String codigo, String latitud, String longitud) throws codigoNoExiste, codigoUsado, codigoExpirado, ubicacion, errorAgregandoCodigo {
        String url = HttpServerConnection.buildURL(HTTP_LOCATION + "agregarPromocion");
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("id",id));
        params.add(new BasicNameValuePair("codigo",String.valueOf(codigo)));
        params.add(new BasicNameValuePair("latitud",latitud));
        params.add(new BasicNameValuePair("longitud",longitud));
        try {
            String jsonResponse = HttpServerConnection.sendHttpRequestPost(url,params);
            JSONObject response = new JSONObject(jsonResponse);
            if (response.getString("estado").compareTo("ok") != 0)
            {
                if (response.getString("clave").compareTo("codigoNoExiste") == 0) {
                    throw new codigoNoExiste();
                } else if (response.getString("clave").compareTo("codigoUsado") == 0) {
                    throw new codigoUsado();
                } else if (response.getString("clave").compareTo("codigoExpirado") == 0) {
                    throw new codigoExpirado();
                } else if (response.getString("clave").compareTo("ubicacion") == 0) {
                    throw new ubicacion();
                } else{
                    throw new errorAgregandoCodigo();
                }
            }

        } catch (JSONException e) {
            Log.i("ERROR","JSON ERROR");
            throw new errorAgregandoCodigo();
        } catch (HttpServerConnection.connectionException e){
            throw new errorAgregandoCodigo();
        }
    }

    public static List<Promocion> leerPromocion(String id) throws errorLeyendoPromociones {
        String url = HttpServerConnection.buildURL(HTTP_LOCATION + "leerPromociones");
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("id",id));
        try {
            String jsonResponse = HttpServerConnection.sendHttpRequestPost(url,params);
            JSONObject response = new JSONObject(jsonResponse);
            if (response.getString("estado").compareTo("ok") != 0) {
                throw new errorLeyendoPromociones();
            }

            JSONArray servicesResponse = response.getJSONArray("promociones");
            List<Promocion> promociones = new ArrayList<>();
            for (int i=0;i < servicesResponse.length(); i++) {
                JSONObject jsonService = servicesResponse.getJSONObject(i);
                Promocion promocion = new Promocion();
                promocion.codigo = jsonService.getString("codigo");
                promocion.nombre = jsonService.getString("nombre");
                promociones.add(promocion);
            }
            return promociones;
        } catch (JSONException e) {
            Log.i("ERROR","JSON ERROR");
            throw new errorLeyendoPromociones();
        }  catch (HttpServerConnection.connectionException e) {
            throw new errorLeyendoPromociones();
        }
    }

    public static class codigoNoExiste extends Exception {
    }
    public static class codigoUsado extends Exception {
    }
    public static class codigoExpirado extends Exception {
    }
    public static class ubicacion extends Exception {
    }
    public static class errorAgregandoCodigo extends Exception {
    }
    public static class errorLeyendoPromociones extends Exception {
    }
}

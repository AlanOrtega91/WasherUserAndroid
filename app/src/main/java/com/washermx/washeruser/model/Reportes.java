package com.washermx.washeruser.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;


public class Reportes {

    public static void sendReport(String descripcion, double latitud, double longitud) {
        String HTTP_LOCATION = "Reporte/";
        String url = HttpServerConnection.buildURL(HTTP_LOCATION);
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("descripcion",descripcion));
        params.add(new BasicNameValuePair("latitud",String.valueOf(latitud)));
        params.add(new BasicNameValuePair("longitud",String.valueOf(longitud)));
        try {
            HttpServerConnection.sendHttpRequestPost(url,params);

        } catch (Exception e) {
            Log.i("ERROR","JSON ERROR Reporte");
        }
    }
}

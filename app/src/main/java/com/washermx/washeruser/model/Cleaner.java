package com.washermx.washeruser.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

public class Cleaner {

    private static String HTTP_LOCATION = "Service/";
    public String name;
    public String lastName;
    public double latitud;
    public double longitud;
    public String id;



    public static List<Cleaner> getNearbyCleaners(double latitud, double longitud, String token) throws errorGettingCleaners, noSessionFound {
        String url = HttpServerConnection.buildURL(HTTP_LOCATION + "GetNearbyCleaners");
        List<Cleaner> cleaners = new ArrayList<>();
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("latitud",String.valueOf(latitud)));
        params.add(new BasicNameValuePair("longitud",String.valueOf(longitud)));
        params.add(new BasicNameValuePair("token",token));
        try {
            String jsonResponse = HttpServerConnection.sendHttpRequestPost(url,params);
            JSONObject response = new JSONObject(jsonResponse);
            if ((response.getString("Status").compareTo("SESSION ERROR") == 0))
                throw new noSessionFound();
            if (!(response.getString("Status").compareTo("OK") == 0))
                throw new errorGettingCleaners();

            JSONArray cleanersResponse = response.getJSONArray("cleaners");
            for (int i=0;i < cleanersResponse.length(); i++) {
                JSONObject jsonCleaner = cleanersResponse.getJSONObject(i);
                Cleaner cleaner = new Cleaner();
                cleaner.name = jsonCleaner.getString("Nombre");
                cleaner.lastName = jsonCleaner.getString("PrimerApellido");
                cleaner.latitud = jsonCleaner.getDouble("Latitud");
                cleaner.longitud = jsonCleaner.getDouble("Longitud");
                cleaner.id = jsonCleaner.getString("idLavador");
                cleaners.add(cleaner);
            }
            return cleaners;
        } catch (JSONException e) {
            Log.i("ERROR","JSON ERROR");
            throw new errorGettingCleaners();
        } catch (HttpServerConnection.connectionException e){
            throw new errorGettingCleaners();
        }
    }

    public static Cleaner getCleanerLocation(String cleanerId, String token) throws errorGettingCleaners, noSessionFound {
        String url = HttpServerConnection.buildURL(HTTP_LOCATION + "GetCleanerLocation");
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("cleanerId",cleanerId));
        params.add(new BasicNameValuePair("token",token));
        try {
            String jsonResponse = HttpServerConnection.sendHttpRequestPost(url,params);
            JSONObject response = new JSONObject(jsonResponse);
            if ((response.getString("Status").compareTo("SESSION ERROR") == 0))
                throw new noSessionFound();
            if (!(response.getString("Status").compareTo("OK") == 0))
                throw new errorGettingCleaners();

            JSONObject cleanerResponse = response.getJSONObject("cleaner");
            Cleaner cleaner = new Cleaner();
            cleaner.latitud = cleanerResponse.getDouble("Latitud");
            cleaner.longitud = cleanerResponse.getDouble("Longitud");
            cleaner.id = cleanerResponse.getString("idLavador");

            return cleaner;
        } catch (JSONException e) {
            Log.i("ERROR","JSON ERROR");
            throw new errorGettingCleaners();
        } catch (HttpServerConnection.connectionException e){
            throw new errorGettingCleaners();
        }
    }

    public static class errorGettingCleaners extends Exception {
    }

    public static class noSessionFound extends Throwable {
    }
}

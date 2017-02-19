package com.washermx.washeruser.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

public class Service {
    private static String HTTP_LOCATION = "Service/";
    public String status;
    public String cleanerName;
    public String car;
    public String service;
    public String price;
    public String description;
    public Date finalTime;
    public Date acceptedTime;
    public Double latitud;
    public Double longitud;
    public String startedTime;
    public String cleanerId;
    public int rating;
    public String id;

    public static final int OUTSIDE = 1;
    public static final int OUTSIDE_INSIDE = 2;
    public static final int BIKE = 1;
    public static final int CAR = 2;
    public static final int SUV = 3;
    public static final int VAN = 4;


    public static long getDifferenceTimeInMillis(Date finalTime) {
        return finalTime.getTime() - new Date().getTime();
    }

    public static Service requestService(String direccion, String latitud, String longitud,
                                         String idServicio, String token, String idCoche, String idCocheFavorito) throws errorRequestingService, noSessionFound, userBlock {
        String url = HttpServerConnection.buildURL(HTTP_LOCATION + "RequestService");
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("direccion",direccion));
        params.add(new BasicNameValuePair("latitud",String.valueOf(latitud)));
        params.add(new BasicNameValuePair("longitud",String.valueOf(longitud)));
        params.add(new BasicNameValuePair("idServicio",idServicio));
        params.add(new BasicNameValuePair("token",token));
        params.add(new BasicNameValuePair("idCoche",idCoche));
        params.add(new BasicNameValuePair("idCocheFavorito",idCocheFavorito));
        try {
            String jsonResponse = HttpServerConnection.sendHttpRequestPost(url,params);
            JSONObject response = new JSONObject(jsonResponse);
            if ((response.getString("Status").compareTo("SESSION ERROR") == 0))
                throw new noSessionFound();
            if ((response.getString("Status").compareTo("USER BLOCK") == 0))
                throw new userBlock();
            if (!(response.getString("Status").compareTo("OK") == 0))
                throw new errorRequestingService();

            JSONObject parameters = response.getJSONObject("info");
            Service service = new Service();
            service.id = parameters.getString("id");
            service.car = parameters.getString("coche");
            service.status = parameters.getString("status");
            service.service = parameters.getString("servicio");
            service.price = parameters.getString("precio");
            service.description = parameters.getString("descripcion");
            service.latitud = parameters.getDouble("latitud");
            service.longitud = parameters.getDouble("longitud");
            service.rating = -1;

            return service;
        } catch (JSONException e) {
            Log.i("ERROR","JSON ERROR");
            throw new errorRequestingService();
        } catch (HttpServerConnection.connectionException e){
            throw new errorRequestingService();
        }
    }

    public static void cancelService(String idServicio, String token, int timeOutCancel) throws errorCancelingRequest, noSessionFound {
        String url = HttpServerConnection.buildURL(HTTP_LOCATION + "ChangeServiceStatus");
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("serviceId",idServicio));
        params.add(new BasicNameValuePair("statusId","6"));
        params.add(new BasicNameValuePair("token",token));
        params.add(new BasicNameValuePair("cancelCode",String.valueOf(timeOutCancel)));
        try {
            String jsonResponse = HttpServerConnection.sendHttpRequestPost(url,params);
            JSONObject response = new JSONObject(jsonResponse);
            if ((response.getString("Status").compareTo("SESSION ERROR") == 0))
                throw new noSessionFound();
            if (!(response.getString("Status").compareTo("OK") == 0))
                throw new errorCancelingRequest();

        } catch (JSONException e) {
            Log.i("ERROR","JSON ERROR");
            throw new errorCancelingRequest();
        } catch (HttpServerConnection.connectionException e){
            throw new errorCancelingRequest();
        }
    }

    public static void sendReview(String idServicio, int rating, String token) throws errorCancelingRequest, noSessionFound {
        String url = HttpServerConnection.buildURL(HTTP_LOCATION + "SendReview");
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("serviceId",idServicio));
        params.add(new BasicNameValuePair("rating",String.valueOf(rating)));
        params.add(new BasicNameValuePair("token",token));
        try {
            String jsonResponse = HttpServerConnection.sendHttpRequestPost(url,params);
            JSONObject response = new JSONObject(jsonResponse);
            if ((response.getString("Status").compareTo("SESSION ERROR") == 0))
                throw new noSessionFound();
            if (!(response.getString("Status").compareTo("OK") == 0))
                throw new errorCancelingRequest();

        } catch (JSONException e) {
            Log.i("ERROR","JSON ERROR");
            throw new errorCancelingRequest();
        } catch (HttpServerConnection.connectionException e){
            throw new errorCancelingRequest();
        }
    }

    public static Double readCleanerRating(String idLavador, String token) throws errorCancelingRequest, noSessionFound {
        String url = HttpServerConnection.buildURL(HTTP_LOCATION + "ReadCleanerRating");
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("idLavador",idLavador));
        params.add(new BasicNameValuePair("token",token));
        try {
            String jsonResponse = HttpServerConnection.sendHttpRequestPost(url,params);
            JSONObject response = new JSONObject(jsonResponse);
            if ((response.getString("Status").compareTo("SESSION ERROR") == 0))
                throw new noSessionFound();
            if (!(response.getString("Status").compareTo("OK") == 0))
                throw new errorCancelingRequest();

            return response.getDouble("Calificacion");
        } catch (JSONException e) {
            Log.i("ERROR","JSON ERROR");
            throw new errorCancelingRequest();
        } catch (HttpServerConnection.connectionException e){
            throw new errorCancelingRequest();
        }
    }


    public static class errorRequestingService extends Exception {
    }

    public static class errorCancelingRequest extends Exception {
    }

    public static class noSessionFound extends Throwable {
    }

    public static class userBlock extends Throwable {
    }
}

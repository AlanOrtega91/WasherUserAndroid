package com.washermx.washeruser.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

public class Car {

    private static String HTTP_LOCATION = "User/Car/";
    public String id;
    public String type;
    public String plates;
    public String color;
    public int favorite;
    public String brand;



    public static String addNewFavoriteCar(Car car, String token) throws errorAddingCar, noSessionFound {
        String url = HttpServerConnection.buildURL(HTTP_LOCATION + "NewCar");
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("vehiculoId",car.type));
        params.add(new BasicNameValuePair("token",token));
        params.add(new BasicNameValuePair("color",car.color));
        params.add(new BasicNameValuePair("placas",car.plates));
        params.add(new BasicNameValuePair("marca",car.brand));

        try {
            String jsonResponse = HttpServerConnection.sendHttpRequestPost(url,params);
            JSONObject response = new JSONObject(jsonResponse);
            if (response.getString("estado").compareTo("ok") != 0)
            {
                if (response.getString("clave").compareTo("sesion") == 0) {
                    throw new noSessionFound();
                } else {
                    throw new errorAddingCar() ;
                }
            }

            return response.getString("id");
        } catch (JSONException e) {
            Log.i("ERROR","JSON ERROR");
            throw new errorAddingCar();
        } catch (HttpServerConnection.connectionException e){
            throw new errorAddingCar();
        }
    }

    public static void deleteFavoriteCar(String id, String token) throws errorDeletingCar, noSessionFound {
        String url = HttpServerConnection.buildURL(HTTP_LOCATION + "DeleteCar");
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("favoriteCarId",id));
        params.add(new BasicNameValuePair("token",token));
        try {
            String jsonResponse = HttpServerConnection.sendHttpRequestPost(url,params);
            JSONObject response = new JSONObject(jsonResponse);
            if (response.getString("estado").compareTo("ok") != 0)
            {
                if (response.getString("clave").compareTo("sesion") == 0)
                {
                    throw new noSessionFound();
                } else {
                    throw new errorDeletingCar() ;
                }
            }

        } catch (JSONException e) {
            Log.i("ERROR","JSON ERROR");
            throw new errorDeletingCar();
        } catch (HttpServerConnection.connectionException e){
            throw new errorDeletingCar();
        }
    }

    public static void editFavoriteCar(Car car, String token) throws errorEditingCar, noSessionFound {
        String url = HttpServerConnection.buildURL(HTTP_LOCATION + "EditCar");
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("vehiculoId",car.type));
        params.add(new BasicNameValuePair("vehiculoFavoritoId",car.id));
        params.add(new BasicNameValuePair("color",car.color));
        params.add(new BasicNameValuePair("placas",car.plates));
        params.add(new BasicNameValuePair("marca",car.brand));
        params.add(new BasicNameValuePair("token",token));
        try {
            String jsonResponse = HttpServerConnection.sendHttpRequestPost(url,params);
            JSONObject response = new JSONObject(jsonResponse);
            if (response.getString("estado").compareTo("ok") != 0)
            {
                if (response.getString("clave").compareTo("sesion") == 0)
                {
                    throw new noSessionFound();
                } else {
                    throw new errorEditingCar();
                }
            }

        } catch (JSONException e) {
            Log.i("ERROR","JSON ERROR");
            throw new errorEditingCar();
        } catch (HttpServerConnection.connectionException e){
            throw new errorEditingCar();
        }
    }

    public static void selectFavoriteCar(String idCar, String token) throws errorAddingFavoriteCar, noSessionFound {
        String url = HttpServerConnection.buildURL(HTTP_LOCATION + "SetFavoriteCar");
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("vehiculoFavoritoId",idCar));
        params.add(new BasicNameValuePair("token",token));
        try {
            String jsonResponse = HttpServerConnection.sendHttpRequestPost(url,params);
            JSONObject response = new JSONObject(jsonResponse);
            if (response.getString("estado").compareTo("ok") != 0)
            {
                if (response.getString("clave").compareTo("sesion") == 0)
                {
                    throw new noSessionFound();
                } else {
                    throw new errorAddingFavoriteCar() ;
                }
            }


        } catch (JSONException e) {
            Log.i("ERROR","JSON ERROR");
            throw new errorAddingFavoriteCar();
        } catch (HttpServerConnection.connectionException e){
            throw new errorAddingFavoriteCar();
        }
    }

    public static class errorAddingCar extends Exception {
    }
    public static class errorDeletingCar extends Exception {
    }
    public static class errorEditingCar extends Exception {
    }
    public static class errorAddingFavoriteCar extends Exception {
    }
    public static class noSessionFound extends Throwable {
    }
}

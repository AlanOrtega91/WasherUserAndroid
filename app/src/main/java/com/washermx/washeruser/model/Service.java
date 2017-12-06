package com.washermx.washeruser.model;

import android.util.Log;

import org.json.JSONArray;
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
    public String metodoDePago;

    public static final int OUTSIDE = 1;
    public static final int OUTSIDE_INSIDE = 2;
    public static final int BIKE = 1;
    public static final int CAR = 2;
    public static final int SUV = 3;
    public static final int VAN = 4;
    public String precioAPagar;


    public static long getDifferenceTimeInMillis(Date finalTime) {
        return finalTime.getTime() - new Date().getTime();
    }

    public static Service requestService(String direccion, String latitud, String longitud,
                                         String idServicio, String token, String idCoche, String idCocheFavorito, String metodoDePago,
                                         Integer idRegion)
            throws errorRequestingService, noSessionFound, userBlock
    {
        String url = HttpServerConnection.buildURL(HTTP_LOCATION + "RequestService");
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("direccion",direccion));
        params.add(new BasicNameValuePair("latitud",String.valueOf(latitud)));
        params.add(new BasicNameValuePair("longitud",String.valueOf(longitud)));
        params.add(new BasicNameValuePair("idServicio",idServicio));
        params.add(new BasicNameValuePair("token",token));
        params.add(new BasicNameValuePair("idCoche",idCoche));
        params.add(new BasicNameValuePair("idCocheFavorito",idCocheFavorito));
        params.add(new BasicNameValuePair("metodoDePago",metodoDePago));
        params.add(new BasicNameValuePair("idRegion", idRegion.toString()));
        try {
            String jsonResponse = HttpServerConnection.sendHttpRequestPost(url,params);
            JSONObject response = new JSONObject(jsonResponse);
            if (response.getString("estado").compareTo("ok") != 0)
            {
                if (response.getString("clave").compareTo("sesion") == 0) {
                    throw new noSessionFound();
                } else if (response.getString("clave").compareTo("bloqueo") == 0){
                    throw new userBlock();
                } else {
                    throw new errorRequestingService();
                }
            }

            JSONObject parameters = response.getJSONObject("servicio");
            Service service = new Service();
            service.id = parameters.getString("id");
            service.car = parameters.getString("coche");
            service.status = parameters.getString("status");
            service.service = parameters.getString("servicio");
            service.price = parameters.getString("precio");
            service.description = parameters.getString("descripcion");
            service.latitud = parameters.getDouble("latitud");
            service.longitud = parameters.getDouble("longitud");
            service.metodoDePago = metodoDePago;
            service.precioAPagar = parameters.getString("precioAPagar");
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
            if (response.getString("estado").compareTo("ok") != 0)
            {
                if (response.getString("clave").compareTo("sesion") == 0) {
                    throw new noSessionFound();
                } else {
                    throw new errorCancelingRequest();
                }
            }

        } catch (JSONException e) {
            Log.i("ERROR","JSON ERROR");
            throw new errorCancelingRequest();
        } catch (HttpServerConnection.connectionException e){
            throw new errorCancelingRequest();
        }
    }

    public static void sendReview(String idServicio, int rating, String token) throws errorMandandoCalificacion, noSessionFound {
        String url = HttpServerConnection.buildURL(HTTP_LOCATION + "SendReview");
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("serviceId",idServicio));
        params.add(new BasicNameValuePair("rating",String.valueOf(rating)));
        params.add(new BasicNameValuePair("token",token));
        try {
            String jsonResponse = HttpServerConnection.sendHttpRequestPost(url,params);
            JSONObject response = new JSONObject(jsonResponse);
            if (response.getString("estado").compareTo("ok") != 0)
            {
                if (response.getString("clave").compareTo("sesion") == 0) {
                    throw new noSessionFound();
                } else {
                    throw new errorMandandoCalificacion();
                }
            }

        } catch (JSONException e) {
            Log.i("ERROR","JSON ERROR");
            throw new errorMandandoCalificacion();
        } catch (HttpServerConnection.connectionException e){
            throw new errorMandandoCalificacion();
        }
    }


    public static Double readCleanerRating(String idLavador, String token) throws errorLeyendoCalificacion, noSessionFound {
        String url = HttpServerConnection.buildURL(HTTP_LOCATION + "ReadCleanerRating");
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("idLavador",idLavador));
        params.add(new BasicNameValuePair("token",token));
        try {
            String jsonResponse = HttpServerConnection.sendHttpRequestPost(url,params);
            JSONObject response = new JSONObject(jsonResponse);
            if (response.getString("estado").compareTo("ok") != 0)
            {
                if (response.getString("clave").compareTo("sesion") == 0) {
                    throw new noSessionFound();
                } else {
                    throw new errorLeyendoCalificacion();
                }
            }

            return response.getDouble("calificacion");
        } catch (JSONException e) {
            Log.i("ERROR","JSON ERROR");
            throw new errorLeyendoCalificacion();
        } catch (HttpServerConnection.connectionException e){
            throw new errorLeyendoCalificacion();
        }
    }

    public static List<Precio> leerPrecios(Double latitud, Double longitud) throws errorLeyendoPrecios {
        String url = HttpServerConnection.buildURL(HTTP_LOCATION + "leerPrecios");
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("latitud", latitud.toString()));
        params.add(new BasicNameValuePair("longitud", longitud.toString()));
        try {
            String jsonResponse = HttpServerConnection.sendHttpRequestPost(url,params);
            JSONObject response = new JSONObject(jsonResponse);
            if (response.getString("estado").compareTo("ok") != 0)
            {
                throw new errorLeyendoPrecios();
            }
            JSONArray preciosJson = response.getJSONArray("precios");
            List<Precio> precios = new ArrayList<>();
            for (int i = 0; i < preciosJson.length(); i++) {
                JSONObject precioJson = preciosJson.getJSONObject(i);
                Precio precio = new Precio();
                precio.idVehiculo = precioJson.getInt("idVehiculo");
                precio.nombre = precioJson.getString("Nombre");
                precio.idServicio = precioJson.getInt("idServicio");
                precio.servicio = precioJson.getString("Servicio");
                precio.precio = precioJson.getDouble("Precio");
                precio.region = precioJson.getInt("id");
                precios.add(precio);
            }
            return precios;
        } catch (JSONException e) {
            Log.i("ERROR","JSON ERROR");
            throw new errorLeyendoPrecios();
        } catch (HttpServerConnection.connectionException e){
            throw new errorLeyendoPrecios();
        }
    }

    public static class errorRequestingService extends Exception {
    }
    private static class errorCancelingRequest extends Exception {
    }
    public static class noSessionFound extends Throwable {
    }
    public static class userBlock extends Throwable {
    }
    public static class errorMandandoCalificacion extends Throwable {
    }
    private static class errorLeyendoCalificacion extends Throwable {
    }
    public static class errorLeyendoPrecios extends Throwable {
    }
}

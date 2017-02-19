package com.washermx.washeruser.model;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;
import com.washermx.washeruser.model.Database.DataBase;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

public class ProfileReader {

    private static String HTTP_LOCATION = "User/";
    private User user = new User();
    private List<Car> cars = new ArrayList<>();
    private List<Service> services = new ArrayList<>();
    private List<UserCard> cards = new ArrayList<>();

    public static void run(Context context,String email, String password) throws errorReadingProfile {
        try {
            SharedPreferences settings = context.getSharedPreferences(AppData.FILE, 0);
            ProfileReader profile = new ProfileReader();
            profile.logIn(email,password,context);
            DataBase db = new DataBase(context);
            db.saveUser(profile.user);
            AppData.saveData(settings,profile.user);
            db.saveCars(profile.cars);
            db.saveServices(profile.services);
            if (profile.cards.size() > 0){
                db.saveCard(profile.cards.get(0));
            }
        } catch (errorReadingData e){
            Log.i("READING","Error reading in profile");
            throw new errorReadingProfile();
        }
    }

    private void logIn(String email, String password, Context context) throws errorReadingData {
        String url = HttpServerConnection.buildURL(HTTP_LOCATION + "LogIn");
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("email",email));
        params.add(new BasicNameValuePair("password",password));
        params.add(new BasicNameValuePair("device","android"));
        try {
            String jsonResponse = HttpServerConnection.sendHttpRequestPost(url,params);
            JSONObject response = new JSONObject(jsonResponse);
            if (!(response.getString("Status").compareTo("OK") == 0))
                throw new errorReadingData();

            readUser(response.getJSONObject("User Info"), context);
            readCars(response.getJSONArray("carsList"));
            readHistory(response.getJSONArray("History"));
            if (!response.isNull("cards")){
                readCard(response.getJSONObject("cards"));
            }
        } catch (Exception e) {
            throw new errorReadingData();
        }
    }

    public static void run(Context context) throws errorReadingProfile {
        try {
            SharedPreferences settings = context.getSharedPreferences(AppData.FILE, 0);
            String token = settings.getString(AppData.TOKEN, null);
            ProfileReader profile = new ProfileReader();
            long startTime = System.nanoTime();
            profile.initialRead(token, context);
            long endTime = System.nanoTime();
            long duration = (endTime - startTime)/1000000;
            Log.i("TIME","Time for request: " + duration);
            startTime = System.nanoTime();
            DataBase db = new DataBase(context);
            db.saveUser(profile.user);
            AppData.saveData(settings,profile.user);
            db.saveCars(profile.cars);
            db.saveServices(profile.services);
            if (profile.cards.size() > 0){
                db.saveCard(profile.cards.get(0));
            }
            endTime = System.nanoTime();
            duration = (endTime - startTime)/(1000*1000);
            Log.i("TIME","Time for database: " + duration);
        } catch (errorReadingData e){
            Log.i("READING","Error reading in profile");
            throw new errorReadingProfile();
        }
    }

    private void initialRead(String token, Context context) throws errorReadingData {
        String url = HttpServerConnection.buildURL(HTTP_LOCATION + "InitialRead");
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("token",token));
        params.add(new BasicNameValuePair("device","android"));
        try {
            String jsonResponse = HttpServerConnection.sendHttpRequestPost(url,params);
            JSONObject response = new JSONObject(jsonResponse);
            if (!(response.getString("Status").compareTo("OK") == 0))
                throw new errorReadingData();

            readUser(response.getJSONObject("User Info"), context);
            readCars(response.getJSONArray("carsList"));
            readHistory(response.getJSONArray("History"));
            if (!response.isNull("cards")) {
                readCard(response.getJSONObject("cards"));
            }
        } catch (Exception e) {
            throw new errorReadingData();
        }
    }

    private void readUser(JSONObject parameters, Context context){
        try {
            user.name = parameters.getString("Nombre");
            user.lastName = parameters.getString("PrimerApellido");
            user.email = parameters.getString("Email");
            user.id = parameters.getString("idCliente");
            user.token = parameters.getString("Token");
            user.phone = parameters.getString("Telefono");
            if (!parameters.isNull("NombreFactura")) user.billingName = parameters.getString("NombreFactura");
            if (!parameters.isNull("RFC")) user.rfc = parameters.getString("RFC");
            if (!parameters.isNull("DireccionFactura")) user.billingAddress = parameters.getString("DireccionFactura");
            if (!parameters.isNull("FotoURL")) {
                Bitmap encodedImage = User.getEncodedStringImageForUser(user.id);
                if (encodedImage != null) {
                    user.imagePath = User.saveEncodedImageToFileAndGetPath(encodedImage, context);
                } else {
                    user.imagePath = "";
                }
            }
        } catch (Exception e){
            Log.i("READ","Error reading user");
        }
    }

    private void readCars(JSONArray carsResponse){
        try {
            for (int i = 0; i < carsResponse.length(); i++) {
                JSONObject jsonCar = carsResponse.getJSONObject(i);
                Car car = new Car();
                car.id = jsonCar.getString("idVehiculoFavorito");
                car.type = jsonCar.getString("idVehiculo");
                car.color = jsonCar.getString("Color");
                car.plates = jsonCar.getString("Placas");
                car.brand = jsonCar.getString("Marca");
                //TODO:Agregar precios
                car.favorite = jsonCar.getInt("Favorito");
                cars.add(car);
            }
        } catch (Exception e){
            Log.i("READ","Error reading cars");
        }
    }

    private void readHistory(JSONArray servicesResponse){
        try{
            for (int i=0;i < servicesResponse.length(); i++) {
                Service service = new Service();
                JSONObject jsonService = servicesResponse.getJSONObject(i);
                service.id = jsonService.getString("id");
                service.car = jsonService.getString("coche");
                service.cleanerName = jsonService.getString("nombreLavador");
                service.status = jsonService.getString("status");
                service.service = jsonService.getString("servicio");
                service.price = jsonService.getString("precio");
                service.description = jsonService.getString("descripcion");
                service.startedTime = jsonService.getString("fechaEmpezado");
                service.latitud = jsonService.getDouble("latitud");
                service.longitud = jsonService.getDouble("longitud");
                service.cleanerId = jsonService.getString("idLavador");
                //DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                if (!jsonService.isNull("horaFinalEstimada"))
                    service.finalTime = format.parse(jsonService.getString("horaFinalEstimada"));
                if (!jsonService.isNull("fechaAceptado"))
                    service.acceptedTime = format.parse(jsonService.getString("fechaAceptado"));
                if (jsonService.isNull("Calificacion"))
                    service.rating = -1;
                else
                    service.rating = jsonService.getInt("Calificacion");

                services.add(service);
            }
        }catch (Exception e){
            Log.i("READ","Error reading services history");
        }
    }

    private void readCard(JSONObject cardsResponse){
        try{
            UserCard card = new UserCard();
            card.cardNumber = "xxxx-xxxx-xxxx-" + cardsResponse.getString("cardNumber");
            card.expirationMonth = cardsResponse.getString("cardExpirationMonth");
            card.expirationYear = cardsResponse.getString("cardExpirationYear");
            cards.add(card);
        }catch (Exception e){
            Log.i("READ","Error reading services cards");
        }
    }

    public static void delete(Context context) {
        SharedPreferences settings = context.getSharedPreferences(AppData.FILE, 0);
        AppData.eliminateData(settings);
        DataBase db = new DataBase(context);
        db.deleteTableUser();
        db.deleteTableCard();
        db.deleteTableService();
        db.deleteTableCar();
    }

    public static class errorReadingProfile extends Exception {
    }

    private class errorReadingData extends Exception {
    }
}

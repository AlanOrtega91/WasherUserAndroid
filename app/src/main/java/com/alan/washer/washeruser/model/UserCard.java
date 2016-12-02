package com.alan.washer.washeruser.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

public class UserCard {

    public String expirationMonth;
    public String expirationYear;
    public String cardNumber;
    public String cvv;
    public String token;
    private static String HTTP_LOCATION = "User/Card/";

    public static void saveNewCardToken(String token, String cardToken) throws errorSavingCardToken {
        String url = HttpServerConnection.buildURL(HTTP_LOCATION + "SaveNewCard");
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("token", token));
        params.add(new BasicNameValuePair("cardToken", cardToken));
        try {
            String jsonResponse = HttpServerConnection.sendHttpRequestPost(url,params);
            JSONObject response = new JSONObject(jsonResponse);
            if (!(response.getString("Status").compareTo("OK") == 0))
                throw new errorSavingCardToken();

        } catch (JSONException e) {
            Log.i("ERROR","JSON ERROR");
            throw new errorSavingCardToken();
        } catch (HttpServerConnection.connectionException e){
            throw new errorSavingCardToken();
        }
    }

    public static class errorSavingCardToken extends Exception {
    }
}

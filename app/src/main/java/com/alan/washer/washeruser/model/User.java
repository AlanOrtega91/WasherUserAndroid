package com.alan.washer.washeruser.model;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;


public class User {
    private static String HTTP_LOCATION = "User/";
    public String name;
    public String lastName;
    public String email;
    public String phone;
    public String id;
    public String token;
    public String imagePath = "";
    public String billingName;
    public String rfc;
    public String billingAddress;



    public static User sendNewUser(User user,String password) throws errorWithNewUser {
        String url = HttpServerConnection.buildURL(HTTP_LOCATION + "NewUser");
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("name",user.name));
        params.add(new BasicNameValuePair("lastName",user.lastName));
        params.add(new BasicNameValuePair("email",user.email));
        params.add(new BasicNameValuePair("password",password));
        params.add(new BasicNameValuePair("phone",user.phone));
        params.add(new BasicNameValuePair("device","android"));
        if ( (user.imagePath != null) && !user.imagePath.equals("")) {
            Bitmap bm = User.readImageBitmapFromFile(user.imagePath);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG,100,stream);
            byte[] array = stream.toByteArray();
            String encodedImage = Base64.encodeToString(array,0);
            params.add(new BasicNameValuePair("encoded_string", encodedImage));
        }
        try {
            String jsonResponse = HttpServerConnection.sendHttpRequestPost(url,params);
            JSONObject response = new JSONObject(jsonResponse);
            if (!(response.getString("Status").compareTo("OK") == 0))
                if (!response.getString("Status").equals("CREATE PAYMENT ACCOUNT ERROR"))
                    throw new errorWithNewUser();

            JSONObject parameters = response.getJSONObject("User Info");
            user.id = parameters.getString("idCliente");
            user.token = parameters.getString("Token");
            return user;
        } catch (JSONException e) {
            Log.i("ERROR","JSON ERROR");
            throw new errorWithNewUser();
        } catch (HttpServerConnection.connectionException e){
            throw new errorWithNewUser();
        }
    }

    public void sendChangeUserData(String token) throws errorChangeData, noSessionFound {
        String url = HttpServerConnection.buildURL(HTTP_LOCATION + "ChangeUserData");
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("newName",name));
        params.add(new BasicNameValuePair("newLastName",lastName));
        params.add(new BasicNameValuePair("newEmail",email));
        params.add(new BasicNameValuePair("token",token));
        params.add(new BasicNameValuePair("newPhone",phone));
        params.add(new BasicNameValuePair("newBillingName",billingName));
        params.add(new BasicNameValuePair("newRFC",rfc));
        params.add(new BasicNameValuePair("newBillingAddress",billingAddress));
        if ( (imagePath != null) && !imagePath.equals("")) {
            Bitmap bm = User.readImageBitmapFromFile(imagePath);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.PNG,100,stream);
            byte[] array = stream.toByteArray();
            String encodedImage = Base64.encodeToString(array,0);
            params.add(new BasicNameValuePair("encoded_string", encodedImage));
        }
        try {
            String jsonResponse = HttpServerConnection.sendHttpRequestPost(url,params);
            JSONObject response = new JSONObject(jsonResponse);
            if ((response.getString("Status").compareTo("SESSION ERROR") == 0))
                throw new noSessionFound();
            if (!(response.getString("Status").compareTo("OK") == 0))
                throw new errorChangeData();

        } catch (JSONException e) {
            Log.i("ERROR","JSON ERROR");
            throw new errorChangeData();
        } catch (HttpServerConnection.connectionException e){
            throw new errorChangeData();
        }
    }

    public void sendLogout() throws errorWithLogOut {
        String url = HttpServerConnection.buildURL(HTTP_LOCATION + "LogOut");
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("email", email));
        try {
            String jsonResponse = HttpServerConnection.sendHttpRequestPost(url,params);
            JSONObject response = new JSONObject(jsonResponse);
            if (!(response.getString("Status").compareTo("OK") == 0))
                throw new errorWithLogOut();

        } catch (JSONException e) {
            Log.i("ERROR","JSON ERROR");
            throw new errorWithLogOut();
        } catch (HttpServerConnection.connectionException e){
            throw new errorWithLogOut();
        }
    }

    public static void saveFirebaseToken(String token, String pushNotificationToken) throws errorSavingFireBaseToken, noSessionFound {
        String url = HttpServerConnection.buildURL(HTTP_LOCATION + "SavePushNotificationToken");
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("token",token));
        params.add(new BasicNameValuePair("pushNotificationToken",pushNotificationToken));
        try {
            String jsonResponse = HttpServerConnection.sendHttpRequestPost(url,params);
            JSONObject response = new JSONObject(jsonResponse);
            if ((response.getString("Status").compareTo("SESSION ERROR") == 0))
                throw new noSessionFound();
            if (!(response.getString("Status").compareTo("OK") == 0))
                throw new errorSavingFireBaseToken();

        } catch (JSONException e) {
            Log.i("ERROR","JSON ERROR");
            throw new errorSavingFireBaseToken();
        } catch (HttpServerConnection.connectionException e){
            throw new errorSavingFireBaseToken();
        }
    }

    public static Bitmap getEncodedStringImageForUser(String id) {
        try {
            URL url = new URL("http://192.168.0.7/Vashen/images/users/" + id + "/profile_image.jpg");
            InputStream is = url.openStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            Bitmap bm = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();
            if (bm == null) {
                return null;
            } else {
                return bm;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static String saveEncodedImageToFileAndGetPath(Bitmap imageBitmap, Context context) {
        ContextWrapper cw = new ContextWrapper(context.getApplicationContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File mypath = new File(directory,"profile.png");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return mypath.getAbsolutePath();
    }

    public static Bitmap readImageBitmapFromFile(String path) {
        try {
            File f = new File(path);
            return BitmapFactory.decodeStream(new FileInputStream(f));
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static class errorWithNewUser extends Exception {
    }

    public static class noSessionFound extends Throwable {
    }

    public static class errorChangeData extends Exception {
    }

    public static class errorWithLogOut extends Exception {
    }

    public static class errorSavingFireBaseToken extends Exception {
    }


}

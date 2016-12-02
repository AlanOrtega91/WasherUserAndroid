package com.alan.washer.washeruser.model;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;

import java.io.File;

/**
 * AppData
 * Created by Alan on 26/05/2016.
 */
public class AppData
{
    public static String FILE = "userdetailsVashenUser";
    public static String TOKEN = "token";
    public static String IDCLIENTE = "idCliente";
    public static String SENT_ALERT = "alert";
    public static String IN_BACKGROUND = "inBackground";
    public static final String FB_TOKEN = "firebase";
    public static final String MESSAGE = "notificationMessage";
    public static final String SERVICE_CHANGED = "serviceChanged";
    public static final String IMAGE_PATH = "imagePath";


    public static void saveImagePath(SharedPreferences settings,Context context) {
        SharedPreferences.Editor editor = settings.edit();
        ContextWrapper cw = new ContextWrapper(context.getApplicationContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        editor.putString(IMAGE_PATH, directory.getAbsolutePath());
        editor.apply();
    }

    public static String readImagePath(SharedPreferences settings) {
        return settings.getString(IMAGE_PATH,"");
    }

    //TODO: Implement gets
    public static void saveData(SharedPreferences settings,User user) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(TOKEN, user.token);
        editor.putString(IDCLIENTE, user.id);
        editor.apply();
    }

    public static void saveInBackground(SharedPreferences settings,Boolean inBackground){
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(IN_BACKGROUND, inBackground);
        editor.putBoolean(IN_BACKGROUND, inBackground);
        editor.apply();
    }

    public static void eliminateData(SharedPreferences settings) {
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(TOKEN);
        editor.remove(IDCLIENTE);
        editor.remove(SENT_ALERT);
        editor.apply();
    }

    public static void saveFBToken(SharedPreferences settings,String token) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(FB_TOKEN,token);
        editor.apply();
    }


    public static void deleteMessage(SharedPreferences settings) {
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(MESSAGE);
        editor.apply();
    }

    public static void saveMessage(SharedPreferences settings, String message) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(MESSAGE,message);
        editor.apply();
    }

    public static void notifyNewData(SharedPreferences settings, boolean newData) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(SERVICE_CHANGED,newData);
        editor.apply();
    }
}

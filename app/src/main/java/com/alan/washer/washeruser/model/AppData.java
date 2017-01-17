package com.alan.washer.washeruser.model;

import android.content.SharedPreferences;


/**
 * AppData
 * Created by Alan on 26/05/2016.
 */
public class AppData
{
    public static String FILE = "userdetailsVashenUser";
    public static String TOKEN = "token";
    public static String IDCLIENTE = "idCliente";
    public static String IN_BACKGROUND = "inBackground";
    public static final String FB_TOKEN = "firebase";
    public static final String MESSAGE = "notificationMessage";
    public static final String SERVICE_CHANGED = "serviceChanged";

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

    static void eliminateData(SharedPreferences settings) {
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(TOKEN);
        editor.remove(IDCLIENTE);
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

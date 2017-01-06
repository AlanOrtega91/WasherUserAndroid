package com.alan.washer.washeruser;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;
import com.alan.washer.washeruser.model.AppData;
import com.alan.washer.washeruser.model.Database.DataBase;
import com.alan.washer.washeruser.model.Service;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService{

    SharedPreferences settings;
    int stateRecevied = 0;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String message;
        settings = getSharedPreferences(AppData.FILE, 0);
        Boolean inBackground = settings.getBoolean(AppData.IN_BACKGROUND,false);
        String serviceJson;
        String state = remoteMessage.getData().get("state");
        if (state == null)
            return;
        switch (state){
            case "2":
                if (stateRecevied != 2) {
                    stateRecevied = 2;
                    message = getString(R.string.service_accepted);
                    serviceJson = remoteMessage.getData().get("serviceInfo");
                    if (serviceJson == null)
                        return;
                    saveNewServiceState(serviceJson);
                    if (inBackground)
                        ServiceStatusNotification.notify(getBaseContext(), message, NavigationDrawer.class);
                    else
                        sendPopUp(message);
                }
                break;
            case "4":
                if (stateRecevied != 4) {
                    stateRecevied = 4;
                    message = getString(R.string.service_started);
                    serviceJson = remoteMessage.getData().get("serviceInfo");
                    if (serviceJson == null)
                        return;
                    saveNewServiceState(serviceJson);
                    if (inBackground)
                        ServiceStatusNotification.notify(getBaseContext(), message, NavigationDrawer.class);
                    else
                        sendPopUp(message);
                }
                break;
            case "5":
                if (stateRecevied != 5) {
                    stateRecevied = 5;
                    serviceJson = remoteMessage.getData().get("serviceInfo");
                    if (serviceJson == null)
                        return;
                    saveNewServiceState(serviceJson);
                    if (inBackground)
                        ServiceStatusNotification.notify(getBaseContext(), getString(R.string.finished), NavigationDrawer.class);
                }
                break;
            case "6":
                if (stateRecevied != 6) {
                    stateRecevied = 6;
                    if (new DataBase(getBaseContext()).getActiveService() != null) {
                        message = getString(R.string.canceled);
                        if (inBackground)
                            ServiceStatusNotification.notify(getBaseContext(), message, NavigationDrawer.class);
                        else
                            sendPopUp(message);
                        serviceJson = remoteMessage.getData().get("serviceInfo");
                        if (serviceJson == null)
                            return;
                        deleteService(serviceJson);
                    }
                }
                break;
            default:
                Log.i("Error","No message detected");
                break;
        }
    }

    private void saveNewServiceState(String serviceJson) {
        try {
            DataBase db = new DataBase(getBaseContext());
            List<Service> services = db.readServices();
            int i;
            JSONObject jsonService = new JSONObject(serviceJson);
            for (i = 0; i < services.size() ; i++) {
                if (services.get(i).id.equals(jsonService.getString("id")))
                    break;
            }
            services.get(i).cleanerName = jsonService.getString("nombreLavador");
            services.get(i).cleanerId = jsonService.getString("idLavador");
            services.get(i).status = jsonService.getString("status");
            services.get(i).startedTime = jsonService.getString("fechaEmpezado");
            //Todo: Check for change
            //SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd KK:mm:ss");
            if (!jsonService.isNull("horaFinalEstimada"))
                services.get(i).finalTime = format.parse(jsonService.getString("horaFinalEstimada"));
            if (!jsonService.isNull("fechaAceptado"))
                services.get(i).acceptedTime = format.parse(jsonService.getString("fechaAceptado"));
            if (jsonService.isNull("Calificacion"))
                services.get(i).rating = -1;
            else
                services.get(i).rating = jsonService.getInt("Calificacion");
            db.saveServices(services);
            AppData.notifyNewData(settings,true);
        } catch (Exception e){
            Log.i("ERROR","FireBase data");
        }
    }

    private void deleteService(String serviceJson){
        try {
            DataBase db = new DataBase(getBaseContext());
            List<Service> services = db.readServices();
            int i;
            JSONObject jsonService = new JSONObject(serviceJson);
            for (i = 0; i < services.size() ; i++)
            {
                if (services.get(i).id.equals(jsonService.getString("id")))
                    break;
            }
            services.remove(i);
            db.saveServices(services);
            AppData.notifyNewData(settings,true);
        } catch (Exception e){
            Log.i("ERROR","FireBase data");
        }
    }

    private void sendPopUp(final String message) {
        AppData.saveMessage(settings,message);
    }
}

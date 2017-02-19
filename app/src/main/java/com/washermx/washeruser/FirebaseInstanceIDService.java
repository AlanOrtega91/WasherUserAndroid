package com.washermx.washeruser;

import android.content.SharedPreferences;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.washermx.washeruser.model.AppData;

public class FirebaseInstanceIDService extends FirebaseInstanceIdService {

    SharedPreferences settings;
    String fireBaseToken;
    @Override
    public void onTokenRefresh() {
        fireBaseToken = FirebaseInstanceId.getInstance().getToken();
        settings = getSharedPreferences(AppData.FILE, 0);
        AppData.saveFBToken(settings,fireBaseToken);
    }
}

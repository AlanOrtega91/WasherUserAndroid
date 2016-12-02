package com.alan.washer.washeruser;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.alan.washer.washeruser.model.AppData;
import com.alan.washer.washeruser.model.Database.DataBase;
import com.alan.washer.washeruser.model.User;

public class BillingActivity extends AppCompatActivity implements View.OnClickListener  {

    User user;
    SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing);
        settings = getSharedPreferences(AppData.FILE, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initValues();
        initView();
        AppData.saveInBackground(settings,false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppData.saveInBackground(settings,true);
    }

    private void initValues() {
        DataBase db = new DataBase(this);
        user = db.readUser();
    }

    private void initView() {
        configureActionBar();
        if (user.billingName != null) ((TextView)findViewById(R.id.billingName)).setText(user.billingName);
        if (user.billingName != null) ((TextView)findViewById(R.id.billingRFC)).setText(user.rfc);
        if (user.billingName != null) ((TextView)findViewById(R.id.billingAddress)).setText(user.billingAddress);
    }

    private void configureActionBar() {
        ActionBar optionsTitleBar = getSupportActionBar();
        if (optionsTitleBar != null) {
            optionsTitleBar.setDisplayShowHomeEnabled(false);
            optionsTitleBar.setDisplayShowCustomEnabled(true);
            optionsTitleBar.setDisplayShowTitleEnabled(false);
            optionsTitleBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            optionsTitleBar.setCustomView(R.layout.titlebar_menu);
            Toolbar parent =(Toolbar) optionsTitleBar.getCustomView().getParent();
            parent.setContentInsetsAbsolute(0,0);
        }
        TextView menuButton = (TextView)findViewById(R.id.menuButton);
        TextView menuTitle = (TextView)findViewById(R.id.menuTitle);
        menuTitle.setText(R.string.billing_title);
        menuButton.setText(R.string.cancel);
        menuButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        finish();
    }

    public void onClickChangeData(View view) {
        changeActivity(EditBillingActivity.class);
    }

    private void changeActivity(Class activity) {
        Intent intent = new Intent(getBaseContext(),activity);
        startActivity(intent);
    }
}

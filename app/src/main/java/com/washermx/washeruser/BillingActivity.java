package com.washermx.washeruser;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.washermx.washeruser.model.AppData;
import com.washermx.washeruser.model.Database.DataBase;
import com.washermx.washeruser.model.User;

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
        if (user.billingName != null && !user.billingName.equals("")){
            ((TextView)findViewById(R.id.billingName)).setText(user.billingName);
        }
        if (user.rfc != null && !user.rfc.equals("")){
            ((TextView)findViewById(R.id.billingRFC)).setText(user.rfc);
        }
        if (user.billingAddress != null && !user.billingAddress.equals("")){
            ((TextView)findViewById(R.id.billingAddress)).setText(user.billingAddress);
        }
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
        TextView menuButton = findViewById(R.id.menuButton);
        TextView menuTitle = findViewById(R.id.menuTitle);
        menuTitle.setText(R.string.billing_title);
        menuButton.setText(R.string.menu);
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

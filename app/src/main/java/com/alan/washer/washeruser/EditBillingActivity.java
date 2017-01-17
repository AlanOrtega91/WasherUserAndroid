package com.alan.washer.washeruser;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.alan.washer.washeruser.model.AppData;
import com.alan.washer.washeruser.model.Database.DataBase;
import com.alan.washer.washeruser.model.User;

public class EditBillingActivity extends AppCompatActivity implements View.OnClickListener {


    EditText name;
    EditText rfc;
    EditText address;
    User user;
    SharedPreferences settings;
    Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_billing);
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
        settings = getSharedPreferences(AppData.FILE, 0);
        DataBase db = new DataBase(this);
        user = db.readUser();
    }

    private void initView() {
        name = (EditText) findViewById(R.id.billingNameEdit);
        rfc = (EditText) findViewById(R.id.billingRFCEdit);
        address = (EditText) findViewById(R.id.billingAddressEdit);
        if (user.billingName != null && !user.billingName.equals("")){
            name.setText(user.billingName);
        }
        if (user.rfc != null && !user.rfc.equals("")){
            rfc.setText(user.rfc);
        }
        if (user.billingAddress != null && !user.billingAddress.equals("")){
            address.setText(user.billingAddress);
        }
        configureActionBar();
    }

    private void configureActionBar() {
        ActionBar optionsTitleBar = getSupportActionBar();
        if (optionsTitleBar != null) {
            optionsTitleBar.setDisplayShowHomeEnabled(false);
            optionsTitleBar.setDisplayShowCustomEnabled(true);
            optionsTitleBar.setDisplayShowTitleEnabled(false);
            optionsTitleBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            optionsTitleBar.setCustomView(R.layout.titlebar_options);
            Toolbar parent =(Toolbar) optionsTitleBar.getCustomView().getParent();
            parent.setContentInsetsAbsolute(0,0);
        }
        TextView leftButton = (TextView)findViewById(R.id.leftButtonOptionsTitlebar);
        TextView rightButton = (TextView)findViewById(R.id.rightButtonOptionsTitlebar);
        TextView title = (TextView)findViewById(R.id.titleOptionsTitlebar);
        leftButton.setText(R.string.cancel);
        rightButton.setText(R.string.save);
        title.setText(R.string.change_data);
        rightButton.setOnClickListener(this);
        leftButton.setOnClickListener(this);
    }

    private void changeData() {
        if (name.getText().toString().equals("") || rfc.getText().toString().equals("") || address.getText().toString().equals(""))
        {
            postAlert(getString(R.string.mising_values));
            return;
        }
        user.billingName = name.getText().toString();
        user.rfc = rfc.getText().toString();
        user.billingAddress = address.getText().toString();
        changeActivityForResult(user);
    }

    private void postAlert(final String message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void changeActivityForResult(User user){
        Intent intent = new Intent(this, LoadingActivity.class);
        intent.putExtra(LoadingActivity.ACTIVITY_ACTION,LoadingActivity.EDIT_ACCOUNT);
        intent.putExtra(EditAccountActivity.USER, new Gson().toJson(user));
        startActivityForResult(intent,LoadingActivity.EDIT_ACCOUNT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LoadingActivity.EDIT_ACCOUNT){
            if (resultCode == RESULT_OK){
                finish();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.leftButtonOptionsTitlebar:
                finish();
                break;
            case R.id.rightButtonOptionsTitlebar:
                Thread changeDataThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        changeData();
                    }
                });
                changeDataThread.start();
                break;
        }
    }
}

package com.alan.washer.washeruser;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alan.washer.washeruser.model.AppData;
import com.alan.washer.washeruser.model.Database.DataBase;
import com.alan.washer.washeruser.model.ProfileReader;
import com.alan.washer.washeruser.model.User;

public class ConfigurationActivity extends AppCompatActivity implements View.OnClickListener {

    TextView name;
    TextView lastName;
    TextView email;
    TextView phone;
    ImageView userImage;
    Handler handler = new Handler(Looper.getMainLooper());
    SharedPreferences settings;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);
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
        DataBase db = new DataBase(getBaseContext());
        user = db.readUser();
    }

    private void initView() {
        configureActionBar();
        name = (TextView) findViewById(R.id.userName);
        userImage = (ImageView) findViewById(R.id.userImage);
        lastName = (TextView) findViewById(R.id.userLastName);
        email = (TextView) findViewById(R.id.userEmail);
        phone = (TextView) findViewById(R.id.userPhone);
        readUserImage();
        fillUserTextFields();
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
        menuTitle.setText(R.string.configuration_title);
        menuButton.setText(R.string.cancel);
        menuButton.setOnClickListener(this);
    }

    public void readUserImage() {
        userImage.setImageBitmap(User.readImageBitmapFromFile(user.imagePath));
    }

    private void fillUserTextFields() {
        name.setText(user.name);
        lastName.setText(user.lastName);
        email.setText(user.email);
        phone.setText(user.phone);
    }

    public void onClickLogOut(View view) {
        Thread sendLogOutThread = new Thread(new Runnable() {
            @Override
            public void run() {
                sendLogOut();
            }
        });
        sendLogOutThread.start();
    }
    private void sendLogOut() {
        try {
            ProfileReader.delete(getBaseContext());
            user.sendLogout();
            changeActivity(MainActivity.class);
            NavigationDrawer.instance.finish();
            finish();
        } catch (User.errorWithLogOut e) {
            postAlert(getString(R.string.error_logging_out));
            changeActivity(MainActivity.class);
            finish();
        }
    }

    private void postAlert(final String message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void changeActivity(Class activity) {
        Intent intent = new Intent(getBaseContext(), activity);
        startActivity(intent);
    }
    @Override
    public void onClick(View v) {
        finish();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
    public void onClickEditAccount(View view) {
        changeActivity(EditAccountActivity.class);
    }
}
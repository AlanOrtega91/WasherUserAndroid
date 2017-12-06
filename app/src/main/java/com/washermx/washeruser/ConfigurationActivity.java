package com.washermx.washeruser;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.washermx.washeruser.model.AppData;
import com.washermx.washeruser.model.Database.DataBase;
import com.washermx.washeruser.model.ProfileReader;
import com.washermx.washeruser.model.User;

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
        name =  findViewById(R.id.userName);
        userImage =  findViewById(R.id.userImage);
        lastName =  findViewById(R.id.userLastName);
        email =  findViewById(R.id.userEmail);
        phone =  findViewById(R.id.userPhone);
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
        TextView menuButton = findViewById(R.id.menuButton);
        TextView menuTitle = findViewById(R.id.menuTitle);
        menuTitle.setText(R.string.configuration_title);
        menuButton.setText(R.string.menu);
        menuButton.setOnClickListener(this);
    }

    public void readUserImage() {
        if (!user.imagePath.equals("") ) {
            userImage.setImageBitmap(User.readImageBitmapFromFile(user.imagePath));
        } else {
            userImage.setImageDrawable(ContextCompat.getDrawable(getBaseContext(),R.drawable.default_image));
        }
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
            Intent firebaseIntent = new Intent(getBaseContext(),FirebaseMessagingService.class);
            stopService(firebaseIntent);
            user.sendLogout();
        } catch (User.errorWithLogOut e) {
            postAlert(getString(R.string.error_logging_out));
        } finally {
            changeActivity(MainActivity.class, true);
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

    private void changeActivity(Class activity, Boolean clear) {
        Intent intent = new Intent(getBaseContext(), activity);
        if (clear) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
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
        changeActivity(EditAccountActivity.class, false);
    }
}

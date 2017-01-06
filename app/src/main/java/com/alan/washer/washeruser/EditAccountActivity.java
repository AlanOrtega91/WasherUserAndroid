package com.alan.washer.washeruser;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.alan.washer.washeruser.model.AppData;
import com.alan.washer.washeruser.model.Database.DataBase;
import com.alan.washer.washeruser.model.User;

import java.io.ByteArrayOutputStream;

public class EditAccountActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String USER = "USER";

    EditText name;
    EditText lastName;
    EditText email;
    EditText cel;
    ImageView editImage;
    Handler handler = new Handler(Looper.getMainLooper());
    SharedPreferences settings;
    User user;
    String token;
    private String encodedString;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_account);
        initValues();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppData.saveInBackground(settings,false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppData.saveInBackground(settings,true);
    }

    private void initValues() {
        settings = getSharedPreferences(AppData.FILE, 0);
        token = settings.getString(AppData.TOKEN,null);
        DataBase db = new DataBase(getBaseContext());
        user = db.readUser();
    }

    private void initView() {
        name = (EditText) findViewById(R.id.nameEdit);
        lastName = (EditText) findViewById(R.id.lastNameEdit);
        email = (EditText) findViewById(R.id.emailEdit);
        cel = (EditText) findViewById(R.id.celEdit);
        fillUserTextFields();
        editImage = (ImageView)findViewById(R.id.editImage);
        readUserImage();
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
        title.setText(R.string.edit_account);
        rightButton.setOnClickListener(this);
        leftButton.setOnClickListener(this);
    }

    private void postAlert(final String message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void readUserImage() {
        if (!user.imagePath.equals("") ) {
            editImage.setImageBitmap(User.readImageBitmapFromFile(user.imagePath));
        } else {
            editImage.setImageDrawable(ContextCompat.getDrawable(getBaseContext(),R.drawable.default_image));
        }
    }

    private void fillUserTextFields() {
        if (user.name != null) name.setText(user.name);
        if (user.lastName != null) lastName.setText(user.lastName);
        if (user.email != null) email.setText(user.email);
        if (user.phone != null) cel.setText(user.phone);
    }


    public void sendModifyData()
    {
        String name = this.name.getText().toString();
        String lastName = this.lastName.getText().toString();
        String email = this.email.getText().toString();
        String cel = this.cel.getText().toString();
        if (name.equals("") || lastName.equals(""))
        {
            postAlert(getString(R.string.name_and_lastname_missing));
            return;
        }

        user.name = name;
        user.lastName = lastName;
        user.email = email;
        user.phone = cel;
        if (bitmap != null) {
            user.imagePath = User.saveEncodedImageToFileAndGetPath(bitmap,getBaseContext());
        }
        try {
            reviewCredentials(user.email);
        } catch (invalidCredentials e) {
            postAlert("Invaid credentials");
        }
        changeActivityForResult(user);
    }

    private void reviewCredentials(String email) throws invalidCredentials {
        if (email == null || !email.contains("@"))
            throw new invalidCredentials();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.leftButtonOptionsTitlebar:
                onBackPressed();
                break;
            case R.id.rightButtonOptionsTitlebar:
                sendModifyData();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public void onClickImage(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,1);
    }

    private void changeActivityForResult(User user){
        Intent intent = new Intent(this, LoadingActivity.class);
        intent.putExtra(LoadingActivity.ACTIVITY_ACTION,LoadingActivity.EDIT_ACCOUNT);
        intent.putExtra(USER, new Gson().toJson(user));
        startActivityForResult(intent,LoadingActivity.EDIT_ACCOUNT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK){
            bitmap = (Bitmap)data.getExtras().get("data");
            editImage.setImageBitmap(bitmap);
        }
        if (requestCode == LoadingActivity.EDIT_ACCOUNT){
            if (resultCode == RESULT_OK){
                finish();
            }
        }
    }

    private class invalidCredentials extends Throwable {
    }
}

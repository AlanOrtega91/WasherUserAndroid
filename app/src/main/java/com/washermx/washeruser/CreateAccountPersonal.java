package com.washermx.washeruser;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.washermx.washeruser.model.AppData;
import com.washermx.washeruser.model.User;


public class CreateAccountPersonal extends AppCompatActivity implements View.OnClickListener{

    public static final String NAME = "NAME";
    public static final String LAST_NAME = "LAST_NAME";
    public static final String PHONE = "PHONE";
    public static final String ENCODED_IMAGE = "ENCODED_IMAGE";
    public static final String EMAIL = "EMAIL";
    public static final String PASSWORD = "PASSWORD";
    String email;
    String phone;
    String password;
    TextView bName;
    TextView bLastName;
    ImageView profileImage;
    Handler handler = new Handler(Looper.getMainLooper());
    SharedPreferences settings;
    private Bitmap bitmap;
    String fireBaseToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account_personal);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initValues();
        initView();
    }

    private void initValues() {
        settings = getSharedPreferences(AppData.FILE, 0);
        Intent intent = getIntent();
        email = intent.getStringExtra(CreateAccount.EMAIL);
        phone = intent.getStringExtra(CreateAccount.PHONE);
        password = intent.getStringExtra(CreateAccount.PASSWORD);
        fireBaseToken = settings.getString(AppData.FB_TOKEN,null);
    }

    private void initView() {
        bName = (EditText) findViewById(R.id.createName);
        bLastName = (EditText) findViewById(R.id.createLastName);
        profileImage = findViewById(R.id.createImage);
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
        TextView leftButton =  findViewById(R.id.leftButtonOptionsTitlebar);
        TextView rightButton = findViewById(R.id.rightButtonOptionsTitlebar);
        TextView title = findViewById(R.id.titleOptionsTitlebar);
        leftButton.setText(R.string.cancel);
        rightButton.setText(R.string.next);
        title.setText(R.string.create_account_title);
        rightButton.setOnClickListener(this);
        leftButton.setOnClickListener(this);
    }

    public void sendRegistration() {
        String name = bName.getText().toString();
        String lastName = bLastName.getText().toString();
        if (name.equals("") || lastName.equals(""))
        {
            postAlert(getString(R.string.name_and_lastname_missing));
            return;
        }
        changeActivityForResult(name,lastName,email,phone,password);
    }

    private void changeActivityForResult(String name, String lastName, String email, String phone, String password){
        Intent intent = new Intent(this, LoadingActivity.class);
        intent.putExtra(LoadingActivity.ACTIVITY_ACTION,LoadingActivity.REGISTER);
        intent.putExtra(NAME,name);
        intent.putExtra(LAST_NAME,lastName);
        intent.putExtra(PHONE,phone);
        if (bitmap != null) {
            intent.putExtra(ENCODED_IMAGE, User.saveEncodedImageToFileAndGetPath(bitmap,getBaseContext()));
        }
        intent.putExtra(EMAIL,email);
        intent.putExtra(PASSWORD,password);
        startActivityForResult(intent,LoadingActivity.REGISTER);
    }

    private void changeActivity(Class activity) {
        Intent intent = new Intent(getBaseContext(), activity);
        startActivity(intent);
    }

    private void postAlert(final String message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.leftButtonOptionsTitlebar:
                changeActivity(MainActivity.class);
                finish();
                break;
            case R.id.rightButtonOptionsTitlebar:
                sendRegistration();
                break;
        }
    }

    public void onClickImage(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK){
            bitmap = (Bitmap)data.getExtras().get("data");
            profileImage.setImageBitmap(bitmap);
        }
        if (requestCode == LoadingActivity.REGISTER){
            if (resultCode == RESULT_OK){
                changeActivity(CreateAccountPayment.class);
            }
        }
    }
}

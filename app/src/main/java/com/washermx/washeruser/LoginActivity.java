package com.washermx.washeruser;

import android.content.Intent;
import android.net.Uri;
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


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    EditText bEmail;
    EditText bPassword;
    private Handler handler = new Handler(Looper.getMainLooper());
    public static final String EMAIL = "EMAIL";
    public static final String PASSWORD = "PASSWORD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
    }

    private void initView() {
        bEmail =  findViewById(R.id.email);
        bPassword =  findViewById(R.id.password);
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
        TextView leftButton = findViewById(R.id.leftButtonOptionsTitlebar);
        TextView rightButton = findViewById(R.id.rightButtonOptionsTitlebar);
        TextView title = findViewById(R.id.titleOptionsTitlebar);
        leftButton.setText(R.string.cancel);
        rightButton.setText(R.string.ok);
        title.setText(R.string.log_in_title);
        rightButton.setOnClickListener(this);
        leftButton.setOnClickListener(this);
    }

    public void sendLogIn() {
        try {
            String email = bEmail.getText().toString();
            String password = bPassword.getText().toString();
            reviewCredentials(email, password);
            changeActivityForResult(email,password);
        } catch (invalidCredentialsEmail e) {
            postAlert(getString(R.string.error_invalid_email));
        } catch (invalidCredentialsPassword e) {
            postAlert(getString(R.string.error_invalid_password));
        }
    }

    private void reviewCredentials(String email,String password) throws invalidCredentialsEmail, invalidCredentialsPassword {
        if (email == null || !email.contains("@") || email.contains(" ") || !email.substring(email.indexOf("@")).contains("."))
            throw new invalidCredentialsEmail();
        if (password == null || password.length() < 6) {
            throw new invalidCredentialsPassword();
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

    private void changeActivityForResult(String email, String password) {
        Intent intent = new Intent(this, LoadingActivity.class);
        intent.putExtra(LoadingActivity.ACTIVITY_ACTION,LoadingActivity.LOGIN);
        intent.putExtra(EMAIL,email);
        intent.putExtra(PASSWORD,password);
        startActivityForResult(intent,LoadingActivity.LOGIN);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LoadingActivity.LOGIN){
            if (resultCode == RESULT_OK){
                changeActivity(NavigationDrawer.class);
                finish();
            }
        }
    }

    private void changeActivity(Class activity) {
        Intent intent = new Intent(getBaseContext(), activity);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.leftButtonOptionsTitlebar:
                onBackPressed();
                break;
            case R.id.rightButtonOptionsTitlebar:
                sendLogIn();
                break;
        }
    }


    public void onClickForgotPassword(View view) {
        Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://54.218.50.2/recuperar"));
        startActivity(myIntent);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private class invalidCredentialsEmail extends Throwable {
    }
    private class invalidCredentialsPassword extends Throwable {
    }
}

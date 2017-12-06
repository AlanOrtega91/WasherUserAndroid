package com.washermx.washeruser;

import android.content.Intent;
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

public class CreateAccount extends AppCompatActivity implements View.OnClickListener{

    public static String EMAIL = "com.example.gilton.CreateAccount.EMAIL";
    public static String PHONE = "com.example.gilton.CreateAccount.PHONE";
    public static String PASSWORD = "com.example.gilton.CreateAccount.PASSWORD";
    EditText bEmail;
    EditText bPhone;
    EditText bPassword;
    EditText bPassword2;
    Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        initView();
        initThreads();
    }

    private void initView() {
        bEmail =  findViewById(R.id.createEmail);
        bPhone =  findViewById(R.id.createPhone);
        bPassword =  findViewById(R.id.createPassword);
        bPassword2 =  findViewById(R.id.createPassword2);
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

    private void initThreads() {
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.leftButtonOptionsTitlebar:
                changeActivity(MainActivity.class);
                finish();
                break;
            case R.id.rightButtonOptionsTitlebar:
                sendContinue();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        changeActivity(MainActivity.class);
    }

    private void sendContinue() {
        String email = bEmail.getText().toString();
        String password = bPassword.getText().toString();
        String password2 = bPassword2.getText().toString();
        String telefono = bPhone.getText().toString();
        try{
            reviewCredentials(email,password);
            reviewPassword(password,password2);
            revisaDatos(telefono);
            changeActivity(CreateAccountPersonal.class);
        } catch (invalidCredentialsEmail e) {
            postAlert(getResources().getString(R.string.invalid_email));
        } catch (invalidCredentialsPassword e){
            postAlert(getResources().getString(R.string.invalid_password));
        } catch (passwordDontMatch e) {
            postAlert(getResources().getString(R.string.invalid_password_dont_march));
        } catch (CreateAccount.telefonoInvalido telefonoInvalido) {
            postAlert(getResources().getString(R.string.invalid_phone));
        }
    }

    private void changeActivity(Class activity) {
        Intent intent = new Intent(getBaseContext(), activity);
        intent.putExtra(EMAIL,bEmail.getText().toString());
        intent.putExtra(PHONE, bPhone.getText().toString());
        intent.putExtra(PASSWORD,bPassword.getText().toString());
        startActivity(intent);
    }

    private void reviewCredentials(String email,String password) throws invalidCredentialsEmail,invalidCredentialsPassword {
        if (email == null || !email.contains("@") || email.contains(" ") || !email.substring(email.indexOf("@")).contains("."))
            throw new invalidCredentialsEmail();
        if (password == null || password.length() < 6)
            throw new invalidCredentialsPassword();
    }

    private void reviewPassword(String password, String passwordRepeated) throws passwordDontMatch {
        if (password.compareTo(passwordRepeated) != 0) {
            throw new passwordDontMatch();
        }
    }

    private void revisaDatos(String telefono) throws telefonoInvalido {
        if (telefono.length() < 10)
        {
            throw new telefonoInvalido();
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

    private class invalidCredentialsEmail extends Throwable {
    }
    private class invalidCredentialsPassword extends Throwable {
    }
    private class passwordDontMatch extends Throwable {
    }
    private class telefonoInvalido extends Throwable {
    }
}

package com.washermx.washeruser;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.washermx.washeruser.model.AppData;
import com.washermx.washeruser.model.Database.DataBase;
import com.washermx.washeruser.model.User;

import io.conekta.conektasdk.Card;

public class CreateAccountPayment extends AppCompatActivity implements View.OnClickListener, TextWatcher, View.OnTouchListener {

    public static final String CARD = "CARD";
    Handler handler = new Handler(Looper.getMainLooper());
    SharedPreferences settings;
    String token;
    User user;
    EditText cardNumber;
    EditText cvv;
    Spinner cardExpirationMonth;
    Spinner cardExpirationYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account_payment);
        initValues();
        initView();
    }

    private void initValues() {
        settings = getSharedPreferences(AppData.FILE, 0);
        token = settings.getString(AppData.TOKEN,null);
        DataBase db = new DataBase(getBaseContext());
        user = db.readUser();
    }

    private void initView() {
        configureActionBar();
        cardNumber = (EditText)findViewById(R.id.cardNumber);
        cvv = (EditText)findViewById(R.id.cvv);
        cardExpirationMonth = (Spinner)findViewById(R.id.cardExpirationMonth);
        cardExpirationYear = (Spinner)findViewById(R.id.cardExpirationYear);
        cardNumber.addTextChangedListener(this);

        ArrayAdapter<CharSequence> monthsAdapter = ArrayAdapter.createFromResource(this,R.array.months,android.R.layout.simple_spinner_dropdown_item);
        monthsAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        cardExpirationMonth.setAdapter(monthsAdapter);
        cardExpirationMonth.setOnTouchListener(this);

        ArrayAdapter<CharSequence> yearsAdapter = ArrayAdapter.createFromResource(this,R.array.years,android.R.layout.simple_spinner_dropdown_item);
        yearsAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        cardExpirationYear.setAdapter(yearsAdapter);
        cardExpirationYear.setOnTouchListener(this);
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
        TextView leftButton = (TextView) findViewById(R.id.leftButtonOptionsTitlebar);
        TextView rightButton = (TextView)findViewById(R.id.rightButtonOptionsTitlebar);
        TextView title = (TextView)findViewById(R.id.titleOptionsTitlebar);
        leftButton.setText(R.string.cancel);
        rightButton.setText(R.string.add_later);
        title.setText(R.string.create_account_title);
        rightButton.setOnClickListener(this);
        leftButton.setOnClickListener(this);
    }


    private void changeActivity(Class activity, Boolean clear) {
        Intent intent = new Intent(getBaseContext(), activity);
        if (clear) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        startActivity(intent);
    }

    private void changeActivityForResult(Card conektaCard){
        Intent intent = new Intent(this, LoadingActivity.class);
        intent.putExtra(LoadingActivity.ACTIVITY_ACTION,LoadingActivity.NEW_CARD);
        intent.putExtra(CreateAccountPayment.CARD, new Gson().toJson(conektaCard));
        startActivityForResult(intent,LoadingActivity.NEW_CARD);
    }

    @Override
    public void onBackPressed() {
        changeActivity(NavigationDrawer.class,true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.leftButtonOptionsTitlebar:
                changeActivity(NavigationDrawer.class,true);
                finish();
                break;
            case R.id.rightButtonOptionsTitlebar:
                if (cardNumber.getText().toString().length() > 0)
                    addPayment();
                else {
                    changeActivity(NavigationDrawer.class, true);
                    finish();
                }
                break;
        }
    }

    public void addPayment() {
        Card conektaCard = new Card("Josue Camara", cardNumber.getText().toString(), cvv.getText().toString(),
                cardExpirationMonth.getSelectedItem().toString(), cardExpirationYear.getSelectedItem().toString());
        changeActivityForResult(conektaCard);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LoadingActivity.NEW_CARD){
            if (resultCode == RESULT_OK){
                changeActivity(NavigationDrawer.class,true);
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void afterTextChanged(Editable editable) {
        TextView rightButton = (TextView)findViewById(R.id.rightButtonOptionsTitlebar);
        if (cardNumber.getText().toString().length() > 0)
            rightButton.setText(R.string.add_payment);
        else
            rightButton.setText(R.string.add_later);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        return false;
    }
}

package com.alan.washer.washeruser;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.alan.washer.washeruser.model.AppData;
import com.alan.washer.washeruser.model.Database.DataBase;
import com.alan.washer.washeruser.model.User;
import com.alan.washer.washeruser.model.UserCard;

import io.conekta.conektasdk.Card;


public class EditCardActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    Handler handler = new Handler(Looper.getMainLooper());
    SharedPreferences settings;
    User user;
    String token;
    EditText cardNumber;
    EditText cvv;
    Spinner cardExpirationMonth;
    Spinner cardExpirationYear;
    UserCard card;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_card);
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
        DataBase db = new DataBase(this);
        card = db.readCard();
        user = db.readUser();
    }

    private void initView() {
        configureActionBar();
        cardNumber = (EditText)findViewById(R.id.cardNumber);
        cvv = (EditText)findViewById(R.id.cvv);
        cardExpirationMonth = (Spinner)findViewById(R.id.cardExpirationMonth);
        cardExpirationYear = (Spinner)findViewById(R.id.cardExpirationYear);

        ArrayAdapter<CharSequence> monthsAdapter = ArrayAdapter.createFromResource(this,R.array.months,android.R.layout.simple_spinner_dropdown_item);
        monthsAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        cardExpirationMonth.setAdapter(monthsAdapter);
        cardExpirationMonth.setOnTouchListener(this);

        ArrayAdapter<CharSequence> yearsAdapter = ArrayAdapter.createFromResource(this,R.array.years,android.R.layout.simple_spinner_dropdown_item);
        yearsAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        cardExpirationYear.setAdapter(yearsAdapter);
        cardExpirationYear.setOnTouchListener(this);
        fillCardsListView();
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
        rightButton.setText(R.string.save);
        title.setText(R.string.edit_card);
        rightButton.setOnClickListener(this);
        leftButton.setOnClickListener(this);
    }

    private void fillCardsListView() {
        if (card.cardNumber != null) cardNumber.setText(card.cardNumber);
        if (card.expirationMonth != null) {
            String month = card.expirationMonth;
            for (int i = 0; i < getResources().getStringArray(R.array.months).length; i++) {
                if (month.equals(cardExpirationMonth.getItemAtPosition(i).toString())) {
                    cardExpirationMonth.setSelection(i);
                    break;
                }
            }

            String year = card.expirationYear;
            for (int i = 0; i < getResources().getStringArray(R.array.years).length; i++) {
                if (year.equals(cardExpirationYear.getItemAtPosition(i).toString())) {
                    cardExpirationYear.setSelection(i);
                    break;
                }
            }
        }
        if (card.cvv != null) cvv.setText(card.cvv);
    }


    public void addPayment() {
        Card conektaCard = new Card(getString(R.string.user_name,user.name,user.lastName), cardNumber.getText().toString(), cvv.getText().toString(),
                cardExpirationMonth.getSelectedItem().toString(), cardExpirationYear.getSelectedItem().toString());
        changeActivityForResult(conektaCard);
    }

    private void changeActivityForResult(Card conektaCard){
        Intent intent = new Intent(this, LoadingActivity.class);
        intent.putExtra(LoadingActivity.ACTIVITY_ACTION,LoadingActivity.NEW_CARD);
        intent.putExtra(CreateAccountPayment.CARD, new Gson().toJson(conektaCard));
        startActivityForResult(intent,LoadingActivity.NEW_CARD);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LoadingActivity.NEW_CARD){
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
                addPayment();
                break;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        return false;
    }
}

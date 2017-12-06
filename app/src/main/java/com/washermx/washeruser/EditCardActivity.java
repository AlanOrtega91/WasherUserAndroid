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
import android.text.format.DateFormat;
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
import com.washermx.washeruser.model.UserCard;

import java.util.Date;

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
        cardNumber = findViewById(R.id.cardNumber);
        cvv = findViewById(R.id.cvv);
        cardExpirationMonth = findViewById(R.id.cardExpirationMonth);
        cardExpirationYear = findViewById(R.id.cardExpirationYear);

        ArrayAdapter<CharSequence> monthsAdapter = ArrayAdapter.createFromResource(this,R.array.months,android.R.layout.simple_spinner_dropdown_item);
        monthsAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        cardExpirationMonth.setAdapter(monthsAdapter);
        cardExpirationMonth.setOnTouchListener(this);

        int año = Integer.parseInt(DateFormat.format("yy", new Date()).toString());
        Integer[] fechas = new Integer[]{0,0,0,0,0,0,0,0,0,0};
        int posicion = 0;
        for (int c = año; c < año+10; c++)
        {
            fechas[posicion] = c;
            posicion++;
        }

        ArrayAdapter<Integer> yearsAdapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item, fechas);
        yearsAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        cardExpirationYear.setAdapter(yearsAdapter);
        cardExpirationYear.setOnTouchListener(this);
        fillCardsListView(fechas);
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
        rightButton.setText(R.string.save);
        title.setText(R.string.edit_card);
        rightButton.setOnClickListener(this);
        leftButton.setOnClickListener(this);
    }

    private void fillCardsListView(Integer[] fechas) {
        if (card != null) {
            cardNumber.setText(card.cardNumber);
            String month = card.expirationMonth;
            for (int i = 0; i < getResources().getStringArray(R.array.months).length; i++) {
                if (month.equals(cardExpirationMonth.getItemAtPosition(i).toString())) {
                    cardExpirationMonth.setSelection(i);
                    break;
                }
            }

            Integer year = Integer.valueOf(card.expirationYear);
            int c = 0;
            for (Integer año:fechas) {
                if (año.equals(year))
                {
                    cardExpirationYear.setSelection(c);
                    break;
                }
                c++;
            }
            cvv.setText(card.cvv);
        }
    }


    public void addPayment() {
        //TODO: Check for missing fields
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
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
        return false;
    }
}

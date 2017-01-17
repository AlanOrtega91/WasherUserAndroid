package com.alan.washer.washeruser;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.alan.washer.washeruser.model.AppData;
import com.alan.washer.washeruser.model.Database.DataBase;
import com.alan.washer.washeruser.model.UserCard;

import java.util.ArrayList;
import java.util.List;

public class PaymentActivity extends AppCompatActivity implements View.OnClickListener  {

    Handler handler;
    SharedPreferences settings;
    List<UserCard> cards = new ArrayList<>();
    TextView creditNumber;
    UserCard card;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
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
        card = db.readCard();
    }

    private void fillCardsListView() {
        if (card != null){
            creditNumber.setText(card.cardNumber);
        }
    }


    private void initView() {
        configureActionBar();
        creditNumber = (TextView)findViewById(R.id.cardNumberRow);
        fillCardsListView();
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
        menuTitle.setText(R.string.payment_title);
        menuButton.setText(R.string.menu);
        menuButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        finish();
    }

    public void onClickEditCard(View view) {
        changeActivity(EditCardActivity.class);
    }

    private void changeActivity(Class activity) {
        Intent intent = new Intent(getBaseContext(), activity);
        startActivity(intent);
    }
}

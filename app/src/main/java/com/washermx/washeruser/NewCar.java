package com.washermx.washeruser;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.washermx.washeruser.model.AppData;
import com.washermx.washeruser.model.Car;


public class NewCar extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    public static final String CAR = "CAR";
    EditText mPlates;
    Spinner types;
    Spinner colors;
    Spinner brands;
    SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_car);
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
    }

    private void initView() {
        mPlates =  findViewById(R.id.platesNew);
        types =  findViewById(R.id.typesSpinner);
        colors =  findViewById(R.id.colorSpinner);
        brands =  findViewById(R.id.brandSpinner);

        ArrayAdapter<CharSequence> typesAdapter = ArrayAdapter.createFromResource(this,R.array.vehicles_types,android.R.layout.simple_spinner_dropdown_item);
        typesAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        types.setAdapter(typesAdapter);
        types.setOnTouchListener(this);

        ArrayAdapter<CharSequence> colorsAdapter = ArrayAdapter.createFromResource(this,R.array.vehicles_colors,android.R.layout.simple_spinner_dropdown_item);
        colorsAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        colors.setAdapter(colorsAdapter);
        colors.setOnTouchListener(this);

        ArrayAdapter<CharSequence> brandsAdapter = ArrayAdapter.createFromResource(this,R.array.vehicles_brands,android.R.layout.simple_spinner_dropdown_item);
        brandsAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        brands.setAdapter(brandsAdapter);
        brands.setOnTouchListener(this);

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
        rightButton.setText(R.string.save);
        title.setText(R.string.add_car);
        rightButton.setOnClickListener(this);
        leftButton.setOnClickListener(this);
    }

    public void sendAddNewCar() {
        String plates = mPlates.getText().toString();
        String brand = brands.getSelectedItem().toString();
        String color = colors.getSelectedItem().toString();
        Car car = new Car();
        car.plates = plates;
        car.color = color;
        car.type = String.valueOf(types.getSelectedItemPosition() + 1);
        car.brand = brand;
        changeActivityForResult(car);
    }

    private void changeActivityForResult(Car car) {
        Intent intent = new Intent(this, LoadingActivity.class);
        intent.putExtra(LoadingActivity.ACTIVITY_ACTION,LoadingActivity.NEW_CAR);
        intent.putExtra(NewCar.CAR, new Gson().toJson(car));
        startActivityForResult(intent,LoadingActivity.NEW_CAR);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LoadingActivity.NEW_CAR){
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
                sendAddNewCar();
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

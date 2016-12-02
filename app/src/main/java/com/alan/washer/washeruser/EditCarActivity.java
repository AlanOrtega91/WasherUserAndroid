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
import android.widget.Toast;

import com.google.gson.Gson;
import com.alan.washer.washeruser.model.AppData;
import com.alan.washer.washeruser.model.Car;
import com.alan.washer.washeruser.model.Database.DataBase;

import java.util.ArrayList;
import java.util.List;

public class EditCarActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    public static final String CAR = "CAR";
    public static final String SELECTED_INDEX = "SELECTED_INDEX";

    int selectedVehicleId;
    EditText plates;
    Spinner types;
    Spinner colors;
    Spinner brands;
    Handler handler = new Handler(Looper.getMainLooper());
    List<Car> cars = new ArrayList<>();
    int selectedCarIndex;
    SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_car);
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

    private void initValues(){
        settings = getSharedPreferences(AppData.FILE,0);
        selectedVehicleId = getIntent().getIntExtra(CarsActivity.SELECTED_CAR_ID,-1);
        DataBase db = new DataBase(this);
        cars = db.readCars();
        for (int i = 0; i < cars.size(); i++)
        {
            if (Integer.valueOf(cars.get(i).id) == selectedVehicleId){
                selectedCarIndex = i;
                break;
            }
        }
    }

    private void initView() {
        if (cars.get(selectedCarIndex) == null) {
            postAlert("Error reading car");
            return;
        }
        plates = (EditText) findViewById(R.id.platesNew);
        types = (Spinner) findViewById(R.id.typesSpinner);
        colors = (Spinner) findViewById(R.id.colorSpinner);
        brands = (Spinner) findViewById(R.id.brandSpinner);
        ArrayAdapter<CharSequence> typesAdapter = ArrayAdapter.createFromResource(this,R.array.vehicles_types,android.R.layout.simple_spinner_dropdown_item);
        typesAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        types.setAdapter(typesAdapter);
        types.setSelection(Integer.valueOf(cars.get(selectedCarIndex).type) - 1);
        types.setOnTouchListener(this);

        ArrayAdapter<CharSequence> colorsAdapter = ArrayAdapter.createFromResource(this,R.array.vehicles_colors,android.R.layout.simple_spinner_dropdown_item);
        colorsAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        colors.setAdapter(colorsAdapter);
        colors.setOnTouchListener(this);

        ArrayAdapter<CharSequence> brandsAdapter = ArrayAdapter.createFromResource(this,R.array.vehicles_brands,android.R.layout.simple_spinner_dropdown_item);
        brandsAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        brands.setAdapter(brandsAdapter);
        brands.setOnTouchListener(this);

        fillCarData();
        configureActionBar();
    }

    private void fillCarData() {
        plates.setText(cars.get(selectedCarIndex).plates);
        for (int i = 0; i < getResources().getStringArray(R.array.vehicles_colors).length; i++){
            if (cars.get(selectedCarIndex).color.equals(colors.getItemAtPosition(i)))
                colors.setSelection(i);
        }
        for (int i = 0; i < getResources().getStringArray(R.array.vehicles_brands).length; i++){
            if (cars.get(selectedCarIndex).brand.equals(brands.getItemAtPosition(i)))
                brands.setSelection(i);
        }
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
        title.setText(R.string.edit_car_title);
        rightButton.setOnClickListener(this);
        leftButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.rightButtonOptionsTitlebar:
                sendEdit();
                break;
            case R.id.leftButtonOptionsTitlebar:
                finish();
                break;
        }
    }

    private void sendEdit() {
            cars.get(selectedCarIndex).type = String.valueOf(types.getSelectedItemPosition() + 1);
            cars.get(selectedCarIndex).color = colors.getSelectedItem().toString();
            cars.get(selectedCarIndex).brand = brands.getSelectedItem().toString();
            cars.get(selectedCarIndex).plates = plates.getText().toString();
            Car car = cars.get(selectedCarIndex);
            changeActivityForResult(car,selectedCarIndex);
    }

    private void postAlert(final String message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void changeActivityForResult(Car car, int selectedCarIndex) {
        Intent intent = new Intent(this, LoadingActivity.class);
        intent.putExtra(LoadingActivity.ACTIVITY_ACTION,LoadingActivity.EDIT_CAR);
        intent.putExtra(EditCarActivity.CAR, new Gson().toJson(car));
        intent.putExtra(EditCarActivity.SELECTED_INDEX, selectedCarIndex);
        startActivityForResult(intent,LoadingActivity.EDIT_CAR);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LoadingActivity.EDIT_CAR){
            if (resultCode == RESULT_OK){
                finish();
            }
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        return false;
    }
}

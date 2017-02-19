package com.washermx.washeruser;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.washermx.washeruser.model.AppData;
import com.washermx.washeruser.model.Car;
import com.washermx.washeruser.model.Database.DataBase;

import java.util.ArrayList;
import java.util.List;

public class CarsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener {

    private static final int EDIT = 0;
    private static final int DELETE = 1;
    public static final String SELECTED_CAR_ID = "com.example.gilton.CarsActivity.SELECTED_CAR_ID";
    private Handler handler = new Handler(Looper.getMainLooper());
    SharedPreferences settings;
    String token;
    String idUser;
    List<Car> cars = new ArrayList<>();
    ListView carList;
    Car selectedCar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cars);
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
        token = settings.getString(AppData.TOKEN,null);
        idUser = settings.getString(AppData.IDCLIENTE,null);
        DataBase db = new DataBase(this);
        cars = db.readCars();
        selectedCar = db.getFavoriteCar();
    }

    private void initView() {
        carList = (ListView)findViewById(R.id.carsList);
        carList.setOnItemClickListener(this);
        registerForContextMenu(carList);
        populateCarsListView();
        configureActionBar();
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
        menuTitle.setText(R.string.cars_title);
        menuButton.setText(R.string.menu);
        menuButton.setOnClickListener(this);
    }


    private void populateCarsListView() {
        carList.setAdapter(new CarsAdapter());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectedCar = cars.get(position);
        Thread sendSelectFavCarThread = new Thread(new Runnable() {
            @Override
            public void run() {
                selectFavCar();
            }
        });
        sendSelectFavCarThread.start();
    }


    private void selectFavCar() {
        try {
            Car.selectFavoriteCar(selectedCar.id,token);
            new DataBase(getBaseContext()).setFavoriteCar(selectedCar.id);
            cars = new DataBase(getBaseContext()).readCars();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    carList.setAdapter(new CarsAdapter());
                }
            });
        } catch (Car.errorAddingFavoriteCar errorAddingFavoriteCar) {
            postAlert(getString(R.string.error_setting_favorite_car));
        } catch (Car.noSessionFound e){
            if (!MainActivity.onScreen) postAlert(getString(R.string.session_error));
            changeActivity(MainActivity.class, true);
            finish();
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.carsList){
            String[] contextMenuItems = getResources().getStringArray(R.array.context_menu_items);
            for (int i = 0; i < contextMenuItems.length; i++)
                menu.add(Menu.NONE,i,i,contextMenuItems[i]);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int menuItem = item.getItemId();
        int position = info.position;
        selectedCar = cars.get(position);
        switch (menuItem){
            case EDIT:
                //Edit
                changeActivity(EditCarActivity.class,Integer.valueOf(selectedCar.id));
                break;
            case DELETE:
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.delete_alert))
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                sendDeleteCar();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .show();
                break;
        }
        return true;
    }

    private void sendDeleteCar() {
        Thread deleteCarThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DataBase db = new DataBase(getBaseContext());
                    Car.deleteFavoriteCar(selectedCar.id,token);
                    cars = db.readCars();
                    for (int i = 0; i < cars.size(); i++){
                        if (cars.get(i).id.equals(selectedCar.id))
                            cars.remove(i);
                    }
                    if (cars.size() == 1) {
                        Car.selectFavoriteCar(cars.get(0).id, token);
                        cars.get(0).favorite = 1;
                    }
                    db.saveCars(cars);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onResume();
                        }
                    });
                } catch (Car.errorDeletingCar e) {
                    postAlert(getString(R.string.error_deleting_car));
                } catch (Car.errorAddingFavoriteCar e){
                    postAlert(getString(R.string.error_setting_favorite_car));
                    onResume();
                } catch (Car.noSessionFound e){
                    if (!MainActivity.onScreen) postAlert(getString(R.string.session_error));
                    changeActivity(MainActivity.class, true);
                    finish();
                }
            }
        });
        deleteCarThread.start();
    }

    public void onClickAddCar(View view) {
        changeActivity(NewCar.class, false);
    }

    private void changeActivity(Class activity, Boolean clear) {
        Intent intent = new Intent(getBaseContext(), activity);
        if (clear) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        startActivity(intent);
    }

    private void changeActivity(Class activity,int id) {
        Intent intent = new Intent(getBaseContext(), activity);
        intent.putExtra(SELECTED_CAR_ID,id);
        startActivity(intent);
    }

    private class CarsAdapter extends ArrayAdapter<Car> {
        CarsAdapter()
        {
            super(CarsActivity.this,R.layout.car_row,cars);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView,@NonNull ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.car_row, parent, false);
            }
            try {
                Car car = cars.get(position);
                TextView plates = (TextView)itemView.findViewById(R.id.plates);
                TextView brand = (TextView)itemView.findViewById(R.id.brand);
                plates.setText(car.plates);
                brand.setText(car.brand);
                RelativeLayout selectedIndicator = (RelativeLayout)itemView.findViewById(R.id.selectedIndicator);
                if (car.favorite != 1) {
                    selectedIndicator.setVisibility(View.GONE);
                } else {
                    selectedIndicator.setVisibility(View.VISIBLE);
                }

                return itemView;
            } catch (Exception e){
                return itemView;
            }
        }
    }

    @Override
    public void onClick(View v) {
        finish();
    }
}

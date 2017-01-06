package com.alan.washer.washeruser;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alan.washer.washeruser.model.AppData;
import com.alan.washer.washeruser.model.Database.DataBase;
import com.alan.washer.washeruser.model.Service;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity implements View.OnClickListener  {

    SharedPreferences settings;
    Handler handler = new Handler(Looper.getMainLooper());
    List<Service> services = new ArrayList<>();
    List<Bitmap> servicesMap = new ArrayList<>();
    List<Bitmap> servicesCleaner = new ArrayList<>();
    ListView historyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
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
        settings = getSharedPreferences(AppData.FILE,0);
        DataBase db = new DataBase(this);
        services = db.getFinishedServices();
    }


    private void populateListView() {
        HistoryAdapter adapter = new HistoryAdapter();
        historyList.setAdapter(adapter);
    }

    private void initView() {
        historyList = (ListView)findViewById(R.id.historyList);
        configureActionBar();
        populateListView();
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
        menuTitle.setText(R.string.history_title);
        menuButton.setText(R.string.menu);
        menuButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        finish();
    }

    private class HistoryAdapter extends ArrayAdapter<Service> {
        public HistoryAdapter()
        {
            super(HistoryActivity.this,R.layout.history_row,services);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.history_row, parent, false);
            }
            try {
                Service service = services.get(position);
                TextView date = (TextView)itemView.findViewById(R.id.serviceDate);
                TextView price = (TextView)itemView.findViewById(R.id.servicePrice);
                TextView type = (TextView)itemView.findViewById(R.id.serviceType);
                ImageView cleanerImage = (ImageView)itemView.findViewById(R.id.cleanerImage);
                ImageView map = (ImageView)itemView.findViewById(R.id.imageViewMap);
                date.setText(service.startedTime);
                price.setText(getString(R.string.price,service.price));
                type.setText(service.service);

                if (position < servicesMap.size()) {
                    map.setImageBitmap(servicesMap.get(position));
                } else {
                    setMapImage(map, service);
                }
                if (position < servicesCleaner.size()) {
                    cleanerImage.setImageBitmap(servicesCleaner.get(position));
                } else {
                    setCleanerImage(cleanerImage, service.cleanerId);
                }
                return itemView;
            } catch (Exception e){
                return itemView;
            }
        }

        private void setMapImage(final ImageView map, final Service service) {
            Thread setMapImage = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        URL url = new URL("https://maps.googleapis.com/maps/api/staticmap?center=" + service.latitud + "," + service.longitud +
                                "&markers=color:red%7Clabel:S%7C" + service.latitud + "," + service.longitud + "&zoom=16&size=" + map.getWidth() + "x" + map.getHeight() + "&key=" + getString(R.string.google_api_key));
                        InputStream is = url.openStream();
                        BufferedInputStream bis = new BufferedInputStream(is);
                        final Bitmap bm = BitmapFactory.decodeStream(bis);
                        bis.close();
                        is.close();
                        servicesMap.add(bm);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                map.setImageBitmap(bm);
                            }
                        });
                    }
                    catch (Exception e) {
                        Log.i("Error","");
                    }
                }
            });
            setMapImage.start();
        }

        private void setCleanerImage(final ImageView cleanerImage, final String id) {
            Thread setEncodedImage = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        URL url = new URL("http://imanio.zone/Vashen/images/cleaners/" + id + "/profile_image.jpg");
                        InputStream is = url.openStream();
                        BufferedInputStream bis = new BufferedInputStream(is);
                        final Bitmap bm = BitmapFactory.decodeStream(bis);
                        bis.close();
                        is.close();
                        if (bm == null)
                            return;
                        servicesCleaner.add(bm);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                cleanerImage.setImageBitmap(bm);
                            }
                        });
                    }catch (Exception e){
                        Log.i("Error","");
                    }
                }
            });
            setEncodedImage.start();
        }
    }
}

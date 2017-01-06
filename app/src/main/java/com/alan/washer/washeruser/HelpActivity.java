package com.alan.washer.washeruser;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alan.washer.washeruser.model.AppData;
import com.alan.washer.washeruser.model.Database.DataBase;
import com.alan.washer.washeruser.model.Service;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HelpActivity extends AppCompatActivity implements View.OnClickListener {

    SharedPreferences settings;
    String idClient;
    Handler handler = new Handler(Looper.getMainLooper());
    List<Service> services = new ArrayList<>();

    TextView date;
    TextView price;
    TextView type;
    ImageView cleanerImage;
    ImageView map;
    Service activeService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
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
        idClient = settings.getString(AppData.IDCLIENTE, null);
        DataBase db = new DataBase(this);
        services = db.getFinishedServices();
    }


    private void populateListView() {
        if (services.size() < 1)
            return;
        activeService = services.get(0);
        date.setText(activeService.startedTime);
        price.setText(getString(R.string.price,activeService.price));
        type.setText(activeService.service);
        setCleanerImage();
        setMapImage(map,activeService);
    }


    private void initView() {
        configureActionBar();
        date = (TextView)findViewById(R.id.serviceDate);
        price = (TextView)findViewById(R.id.servicePrice);
        type = (TextView)findViewById(R.id.serviceType);
        cleanerImage = (ImageView)findViewById(R.id.cleanerImage);
        map = (ImageView)findViewById(R.id.imageViewMap);
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
        menuTitle.setText(R.string.help_title);
        menuButton.setText(R.string.menu);
        menuButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        finish();
    }

    public void onClickHelp(View view) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("plain/text");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@gilton.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Help");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Question");
        startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }

    private void setMapImage(final ImageView map, final Service service) {
        Thread setMapImage = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("https://maps.googleapis.com/maps/api/staticmap?center=" + service.latitud + "," + service.longitud +
                            "&markers=color:red%7Clabel:S%7C" + service.latitud + "," + service.longitud + "&zoom=16&size=1000x400&key=" + getString(R.string.google_api_key));
                    InputStream is = url.openStream();
                    BufferedInputStream bis = new BufferedInputStream(is);
                    final Bitmap bm = BitmapFactory.decodeStream(bis);
                    bis.close();
                    is.close();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            map.setImageBitmap(bm);
                        }
                    });
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        setMapImage.start();
    }

    private void setCleanerImage() {
        Thread setEncodedImage = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://imanio.zone/Vashen/images/cleaners/" + activeService.cleanerId + "/profile_image.jpg");
                    InputStream is = url.openStream();
                    BufferedInputStream bis = new BufferedInputStream(is);
                    final Bitmap bm = BitmapFactory.decodeStream(bis);
                    bis.close();
                    is.close();
                    if (bm == null)
                        return;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            cleanerImage.setImageBitmap(bm);
                        }
                    });
                }catch (Exception e){
                    return;
                }
            }
        });
        setEncodedImage.start();
    }
}

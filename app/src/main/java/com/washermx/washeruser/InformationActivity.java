package com.washermx.washeruser;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.washermx.washeruser.model.AppData;
import com.washermx.washeruser.model.Database.DataBase;
import com.washermx.washeruser.model.Service;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;

public class InformationActivity extends AppCompatActivity implements View.OnClickListener {

    Service service;
    ImageView cleanerImage;
    TextView cleanerName;
    ImageView ratingImage;
    String token;
    Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);
        initValues();
        initView();
        initThreads();
    }

    private void initValues() {
        DataBase db = new DataBase(this);
        service = db.getActiveService();
        SharedPreferences settings = getSharedPreferences(AppData.FILE, 0);
        token = settings.getString(AppData.TOKEN,null);
    }

    private void initView() {
        cleanerName = (TextView)findViewById(R.id.cleanerNameInformation);
        cleanerImage = (ImageView)findViewById(R.id.cleanerImageInformation);
        configureActionBar();
        cleanerName.setText(service.cleanerName);
        setCleanerImage();
        ratingImage = (ImageView) findViewById(R.id.ratingImage);
    }

    private void configureActionBar() {
        ActionBar optionsTitleBar = getSupportActionBar();
        if (optionsTitleBar != null) {
            optionsTitleBar.hide();
        }
    }

    private void setCleanerImage() {
        Thread setEncodedImage = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://washer.mx/api/1.0.0/images/cleaners/" + service.cleanerId + "/profile_image.jpg");
                    InputStream is = url.openStream();
                    BufferedInputStream bis = new BufferedInputStream(is);
                    final Bitmap bm = BitmapFactory.decodeStream(bis);
                    bis.close();
                    is.close();
                    if (bm != null) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                cleanerImage.setImageBitmap(bm);
                            }
                        });
                    }
                }catch (Exception e){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            cleanerImage.setImageDrawable(ContextCompat.getDrawable(getBaseContext(),R.drawable.default_image));
                        }
                    });
                }
            }
        });
        setEncodedImage.start();
    }

    void initThreads() {
        Thread readRating = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final int rating = (int) Math.round(Service.readCleanerRating(service.cleanerId, token));
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            switch (rating) {
                                case 0:
                                    ratingImage.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.rating_white0));
                                    break;
                                case 1:
                                    ratingImage.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.rating_white1));
                                    break;
                                case 2:
                                    ratingImage.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.rating_white2));
                                    break;
                                case 3:
                                    ratingImage.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.rating_white3));
                                    break;
                                case 4:
                                    ratingImage.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.rating_white4));
                                    break;
                                case 5:
                                    ratingImage.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.rating_white5));
                                    break;
                                default:
                                    ratingImage.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.rating_white0));
                                    break;
                            }
                        }
                    });
                } catch (Throwable e) {
                    ratingImage.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.rating_white0));
                }
            }
        });
        readRating.start();
    }

    @Override
    public void onClick(View view) {
        finish();
    }
}

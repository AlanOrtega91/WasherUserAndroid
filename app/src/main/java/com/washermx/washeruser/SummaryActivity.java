package com.washermx.washeruser;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.washermx.washeruser.model.AppData;
import com.washermx.washeruser.model.Database.DataBase;
import com.washermx.washeruser.model.Service;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class SummaryActivity extends AppCompatActivity {

    SharedPreferences settings;
    Service activeService;
    Handler handler = new Handler(Looper.getMainLooper());

    TextView date;
    TextView price;
    ImageView cleaner;
    ImageView location;
    int rating = 0;
    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initValues();
        initView();
    }

    private void initValues() {
        settings = getSharedPreferences(AppData.FILE,0);
        token = settings.getString(AppData.TOKEN,null);
        DataBase db = new DataBase(getBaseContext());
        activeService = db.getActiveService();
        if (activeService == null)
            finish();
    }

    private void initView() {
        date = (TextView) findViewById(R.id.date);
        price = (TextView) findViewById(R.id.price);
        location = (ImageView) findViewById(R.id.locationImage);
        cleaner = (ImageView) findViewById(R.id.cleanerImage);
        configureView();
    }

    private void configureView() {
        if (activeService == null)
            return;
        date.setText(activeService.startedTime);
        price.setText(getString(R.string.price,activeService.price));
        setMapImage(activeService);
        setCleanerImage();
    }

    private void setMapImage(final Service service) {
        Thread setMapImage = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("https://maps.googleapis.com/maps/api/staticmap?center=" + service.latitud + "," + service.longitud +
                            "&markers=color:red%7Clabel:S%7C" + service.latitud + "," + service.longitud + "&zoom=14&size=100x100&key=" + getString(R.string.google_api_key));
                    InputStream is = url.openStream();
                    BufferedInputStream bis = new BufferedInputStream(is);
                    final Bitmap bm = BitmapFactory.decodeStream(bis);
                    bis.close();
                    is.close();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            location.setImageBitmap(bm);
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
                    URL url = new URL("http://washer.mx/Washer/images/cleaners/" + activeService.cleanerId + "/profile_image.jpg");
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
                            cleaner.setImageBitmap(bm);
                        }
                    });
                }catch (Exception e){
                    Log.i("Error","Setting image");
                }
            }
        });
        setEncodedImage.start();
    }

    public void trySendReview(View view) {
        Thread sendReviewThread = new Thread(new Runnable() {
            @Override
            public void run() {
                sendReview();
            }
        });
        sendReviewThread.start();
    }

    private void sendReview() {
        try {
            Service.sendReview(activeService.id,rating,token);
            DataBase db = new DataBase(getBaseContext());
            List<Service> services = db.readServices();
            for (int i = 0; i < services.size(); i++){
                if (services.get(i).id.equals(activeService.id))
                    services.get(i).rating = rating;
            }
            db.saveServices(services);
            AppData.notifyNewData(settings,true);
            AppData.deleteMessage(settings);
            finish();
        } catch (Service.errorCancelingRequest errorCancelingRequest) {
            postAlert(getResources().getString(R.string.error_sending_review));
        }   catch (Service.noSessionFound e){
            if (!MainActivity.onScreen) postAlert(getString(R.string.session_error));
            changeActivity(MainActivity.class);
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
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }


    public void onClickStar(View view) {
        ImageView one = (ImageView)findViewById(R.id.oneStar);
        ImageView two = (ImageView)findViewById(R.id.twoStar);
        ImageView three = (ImageView)findViewById(R.id.threeStar);
        ImageView four = (ImageView)findViewById(R.id.fourStar);
        ImageView five = (ImageView)findViewById(R.id.fiveStar);

        switch (view.getId()){
            case R.id.oneStar:
                one.setImageResource(R.drawable.rating_selected);
                two.setImageResource(R.drawable.rating_empty);
                three.setImageResource(R.drawable.rating_empty);
                four.setImageResource(R.drawable.rating_empty);
                five.setImageResource(R.drawable.rating_empty);
                rating = 1;
                break;
            case R.id.twoStar:
                one.setImageResource(R.drawable.rating_selected);
                two.setImageResource(R.drawable.rating_selected);
                three.setImageResource(R.drawable.rating_empty);
                four.setImageResource(R.drawable.rating_empty);
                five.setImageResource(R.drawable.rating_empty);
                rating = 2;
                break;
            case R.id.threeStar:
                one.setImageResource(R.drawable.rating_selected);
                two.setImageResource(R.drawable.rating_selected);
                three.setImageResource(R.drawable.rating_selected);
                four.setImageResource(R.drawable.rating_empty);
                five.setImageResource(R.drawable.rating_empty);
                rating = 3;
                break;
            case R.id.fourStar:
                one.setImageResource(R.drawable.rating_selected);
                two.setImageResource(R.drawable.rating_selected);
                three.setImageResource(R.drawable.rating_selected);
                four.setImageResource(R.drawable.rating_selected);
                five.setImageResource(R.drawable.rating_empty);
                rating = 4;
                break;
            case R.id.fiveStar:
                one.setImageResource(R.drawable.rating_selected);
                two.setImageResource(R.drawable.rating_selected);
                three.setImageResource(R.drawable.rating_selected);
                four.setImageResource(R.drawable.rating_selected);
                five.setImageResource(R.drawable.rating_selected);
                rating = 5;
                break;
        }
    }

    private void changeActivity(Class activity) {
        Intent intent = new Intent(getBaseContext(), activity);
        startActivity(intent);
    }
}


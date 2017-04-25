package com.washermx.washeruser;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.washermx.washeruser.model.AppData;
import com.washermx.washeruser.model.Car;
import com.washermx.washeruser.model.Database.DataBase;
import com.washermx.washeruser.model.ProfileReader;
import com.washermx.washeruser.model.User;
import com.washermx.washeruser.model.UserCard;

import org.json.JSONObject;

import java.util.List;

import io.conekta.conektasdk.Card;
import io.conekta.conektasdk.Conekta;
import io.conekta.conektasdk.Token;

public class LoadingActivity extends AppCompatActivity {

    public static final int LOGIN = 10;
    public static final int REGISTER = 20;
    public static final int NEW_CARD = 30;
    public static final int NEW_CAR = 40;
    public static final int EDIT_CAR = 50;
    public static final int EDIT_ACCOUNT = 60;
    public static final String ACTIVITY_ACTION = "LoadingActivityAction";
    SharedPreferences settings;
    String token;
    Intent intent;
    Bundle bundle;
    //Register and Login
    String name;
    String lastName;
    String phone;
    String imagePath;
    String email;
    String password;
    String fireBaseToken;
    //NewCard
    Card conektaCard;
    Token tokenConekta;
    //EditCar
    int selectedIndex;
    //NewCar
    Car car;
    //Edit Account
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        initView();
        initValues();
    }

    private void initValues() {
        settings = getSharedPreferences(AppData.FILE,0);
        fireBaseToken = settings.getString(AppData.FB_TOKEN,null);
        token = settings.getString(AppData.TOKEN,null);
        intent = getIntent();
        bundle = intent.getExtras();
        int callingActivity = bundle.getInt(ACTIVITY_ACTION);
        switch (callingActivity){
            case LOGIN:
                configureLogin();
                tryLogin();
                break;
            case REGISTER:
                configureRegister();
                tryRegister();
                break;
            case NEW_CARD:
                configureNewCard();
                tryNewCard();
                break;
            case NEW_CAR:
                configureNewCar();
                tryNewCar();
                break;
            case EDIT_CAR:
                configureEditCar();
                tryEditCar();
                break;
            case EDIT_ACCOUNT:
                configureEditAccount();
                tryEditAccount();
                break;
            default:
                returnResult(RESULT_CANCELED);
                break;
        }
    }

    private void tryNewCard() {
        tokenConekta.onCreateTokenListener(new Token.CreateToken() {
            @Override
            public void onCreateTokenReady(JSONObject data) {
                final JSONObject conektaData = data;
                Thread tryNewCardThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String id = conektaData.getString("id");
                            Log.i("Token::::", conektaData.getString("id"));
                            UserCard.saveNewCardToken(token, id);
                            ProfileReader.run(getBaseContext());
                            returnResult(RESULT_OK);
                        } catch (Exception err) {
                            Log.i("Error: ", err.toString());
                            postAlert(getString(R.string.error_processing_payment));
                            returnResult(RESULT_CANCELED);
                        }
                    }
                });
                tryNewCardThread.start();
            }
        });
        tokenConekta.create(conektaCard);
    }

    private void configureNewCard() {
        String jsonObject;
        jsonObject = bundle.getString(CreateAccountPayment.CARD);
        conektaCard = new Gson().fromJson(jsonObject,Card.class);
        Conekta.setPublicKey(getString(R.string.conekta_key));
        Conekta.collectDevice(this);
        tokenConekta = new Token(this);
    }

    private void tryEditCar() {
        Thread tryEditCarThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Car> cars = new DataBase(getBaseContext()).readCars();
                    cars.set(selectedIndex,car);
                    Car.editFavoriteCar(car, token);
                    DataBase db = new DataBase(getBaseContext());
                    db.saveCars(cars);
                    returnResult(RESULT_OK);
                } catch (Car.errorEditingCar e) {
                    postAlert(getString(R.string.error_editing_car));
                    returnResult(RESULT_CANCELED);
                } catch (Car.noSessionFound e) {
                    if (!MainActivity.onScreen) postAlert(getString(R.string.session_error));
                    changeActivity(MainActivity.class, true);
                    finish();
                    returnResult(RESULT_CANCELED);
                }
            }
        });
        tryEditCarThread.start();
    }

    private void configureEditCar() {
        selectedIndex = bundle.getInt(EditCarActivity.SELECTED_INDEX);
        String jsonObject;
        jsonObject = bundle.getString(NewCar.CAR);
        car = new Gson().fromJson(jsonObject,Car.class);
    }

    private void tryNewCar() {
        Thread tryNewCarThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DataBase db = new DataBase(getBaseContext());
                    List<Car> cars = db.readCars();
                    car.id = Car.addNewFavoriteCar(car, token);
                    if (cars.size() == 0) {
                        Car.selectFavoriteCar(car.id, token);
                        car.favorite = 1;
                    }
                    cars.add(car);
                    db.saveCars(cars);
                    returnResult(RESULT_OK);
                } catch (Car.errorAddingCar e) {
                    postAlert(getString(R.string.error_adding_car));
                    returnResult(RESULT_CANCELED);
                } catch (Car.errorAddingFavoriteCar e) {
                    postAlert(getString(R.string.error_setting_favorite_car));
                    returnResult(RESULT_CANCELED);
                } catch (Car.noSessionFound e) {
                    if (!MainActivity.onScreen) postAlert(getString(R.string.session_error));
                    changeActivity(MainActivity.class, true);
                    returnResult(RESULT_CANCELED);
                    finish();
                }
            }
        });
        tryNewCarThread.start();
    }

    private void configureNewCar() {
        String jsonObject;
        jsonObject = bundle.getString(NewCar.CAR);
        car = new Gson().fromJson(jsonObject,Car.class);
    }

    private void configureEditAccount() {
        String jsonObject;
        jsonObject = bundle.getString(EditAccountActivity.USER);
        user = new Gson().fromJson(jsonObject,User.class);
    }

    private void tryEditAccount() {
        Thread tryEditAccountThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    user.sendChangeUserData(token);
                    DataBase db = new DataBase(getBaseContext());
                    db.saveUser(user);
                    returnResult(RESULT_OK);
                } catch (User.errorChangeData e) {
                    postAlert("Error changing data");
                    returnResult(RESULT_CANCELED);
                } catch (User.noSessionFound e){
                    if (!MainActivity.onScreen) postAlert(getString(R.string.session_error));
                    changeActivity(MainActivity.class, true);
                    returnResult(RESULT_CANCELED);
                }
            }
        });
        tryEditAccountThread.start();
    }


    private void configureRegister() {
        name = intent.getStringExtra(CreateAccountPersonal.NAME);
        lastName = intent.getStringExtra(CreateAccountPersonal.LAST_NAME);
        phone = intent.getStringExtra(CreateAccountPersonal.PHONE);
        email = intent.getStringExtra(CreateAccountPersonal.EMAIL);
        imagePath = intent.getStringExtra(CreateAccountPersonal.ENCODED_IMAGE);
        password = intent.getStringExtra(CreateAccountPersonal.PASSWORD);
    }

    private void configureLogin() {
        email = intent.getStringExtra(LoginActivity.EMAIL);
        password = intent.getStringExtra(LoginActivity.PASSWORD);
    }

    private void initView() {
        ActionBar optionsTitleBar = getSupportActionBar();
        if (optionsTitleBar != null) optionsTitleBar.hide();
    }

    private void tryRegister() {
        Thread tryRegisterThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    User user = new User();
                    user.name = name;
                    user.lastName = lastName;
                    user.email = email;
                    user.phone = phone;
                    if (imagePath != null) {
                        user.imagePath = imagePath;
                    } else {
                        user.imagePath = "";
                    }
                    user = User.sendNewUser(user,password);

                    DataBase db = new DataBase(getBaseContext());
                    db.saveUser(user);

                    AppData.saveData(settings,user);
                    User.saveFirebaseToken(user.token, fireBaseToken);

                    returnResult(RESULT_OK);
                } catch (User.errorWithNewUser e) {
                    postAlert(getResources().getString(R.string.error_registering_user));
                    returnResult(RESULT_CANCELED);
                } catch (User.errorSavingFireBaseToken e) {
                    postAlert(getString(R.string.error_firebase));
                    returnResult(RESULT_CANCELED);
                } catch (User.noSessionFound e){
                    if (!MainActivity.onScreen) postAlert(getString(R.string.session_error));
                    returnResult(RESULT_CANCELED);
                }
            }
        });
        tryRegisterThread.start();
    }

    private void tryLogin() {
        Thread tryLoginThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SharedPreferences settings = getSharedPreferences(AppData.FILE, 0);
                    ProfileReader.run(getBaseContext(),email,password);
                    token = settings.getString(AppData.TOKEN,null);
                    String fireBaseToken = settings.getString(AppData.FB_TOKEN, "");
                    User.saveFirebaseToken(token, fireBaseToken);
                    returnResult(RESULT_OK);
                } catch (User.errorSavingFireBaseToken e) {
                    Log.i("ERROR","FIREBASE" + e.getMessage());
                    postAlert(getString(R.string.error_logging_in));
                    ProfileReader.delete(getBaseContext());
                    returnResult(RESULT_CANCELED);
                } catch (ProfileReader.errorReadingProfile e) {
                    postAlert(getString(R.string.error_logging_in));
                    ProfileReader.delete(getBaseContext());
                    returnResult(RESULT_CANCELED);
                } catch (User.noSessionFound e){
                    if (!MainActivity.onScreen) postAlert(getString(R.string.session_error));
                    returnResult(RESULT_CANCELED);
                }
            }
        });
        tryLoginThread.start();
    }

    private void postAlert(final String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void returnResult(int result){
        setResult(result);
        finish();
    }

    private void changeActivity(Class activity, Boolean clear) {
        Intent intent = new Intent(getBaseContext(), activity);
        if (clear) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        startActivity(intent);
    }
}

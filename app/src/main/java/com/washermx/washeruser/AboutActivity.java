package com.washermx.washeruser;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.washermx.washeruser.model.AppData;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

    SharedPreferences settings;
    public static final String TEXTO_INFO = "com.example.gilton.CarsActivity.SELECTED_CAR_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        settings = getSharedPreferences(AppData.FILE, 0);
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

    private void initView() {
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
        menuTitle.setText(R.string.about_title);
        menuButton.setText(R.string.menu);
        menuButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        finish();
    }

    public void openTerms(View view) {
        Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://docs.wixstatic.com/ugd/3b7cab_b86be706129e4d23b9f51e90d1095c34.pdf"));
        startActivity(myIntent);
    }

    public void openPrivacy(View view) {
        Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://docs.wixstatic.com/ugd/3b7cab_639435d1b717435bbe95bc4639ccc092.pdf"));
        startActivity(myIntent);
    }

    public void abrirInfoRestricciones(View view) {
        Intent intent = new Intent(getBaseContext(), RestriccionesInfo.class);
        intent.putExtra(TEXTO_INFO,getString(R.string.restricciones_info));
        startActivity(intent);
    }

    public void abrirInfoUbicaciones(View view) {
        Intent intent = new Intent(getBaseContext(), RestriccionesInfo.class);
        intent.putExtra(TEXTO_INFO,getString(R.string.ubicaciones_info));
        startActivity(intent);
    }

    public void openWeb(View view) {
        Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.washer.mx"));
        startActivity(myIntent);
    }
}

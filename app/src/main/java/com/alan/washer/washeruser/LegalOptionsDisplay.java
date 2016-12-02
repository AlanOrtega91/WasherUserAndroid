package com.alan.washer.washeruser;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class LegalOptionsDisplay extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_legal_options_display);
        TextView display = (TextView)findViewById(R.id.displayLegalInfo);
        Intent intent = getIntent();
        switch (intent.getIntExtra("OPTION",0)){
            case 1:
                display.setText(getString(R.string.copyright_info));
                break;
            case 2:
                display.setText(getString(R.string.terms_and_conditions_info));
                break;
            case 3:
                display.setText(getString(R.string.privacy_info));
                break;
            case 4:
                display.setText(getString(R.string.software_license_info));
                break;
        }
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
        menuTitle.setText(R.string.legal);
        menuButton.setText(R.string.cancel);
        menuButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        finish();
    }
}

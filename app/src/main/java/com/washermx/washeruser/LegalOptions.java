package com.washermx.washeruser;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class LegalOptions extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_legal_options);
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
        TextView menuButton = (TextView)findViewById(R.id.leftButtonOptionsTitlebar);
        TextView invi = (TextView)findViewById(R.id.rightButtonOptionsTitlebar);
        TextView menuTitle = (TextView)findViewById(R.id.titleOptionsTitlebar);
        menuTitle.setText(R.string.about);
        menuButton.setText(R.string.back);
        invi.setVisibility(View.INVISIBLE);
        menuButton.setOnClickListener(this);
    }

    public void onClickChange(View view) {
        switch (view.getId()){
            case R.id.copyright:
                changeActivity(LegalOptionsDisplay.class,1);
                break;
            case R.id.terms:
                changeActivity(LegalOptionsDisplay.class,2);
                break;
            case R.id.privacy:
                changeActivity(LegalOptionsDisplay.class,3);
                break;
            case R.id.license:
                changeActivity(LegalOptionsDisplay.class,4);
                break;
        }
    }

    private void changeActivity(Class activity,int i) {
        Intent intent = new Intent(getBaseContext(),activity);
        intent.putExtra("OPTION",i);
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        finish();
    }
}

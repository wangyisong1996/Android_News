package com.java.news_44;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

public class SettingsActivity extends AppCompatActivity {

    Switch switch_textmode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportActionBar().setTitle("设置");

        switch_textmode = (Switch) findViewById(R.id.settings_textmode);

        if (NewsManager.getInstance().is_text_mode()) {
            switch_textmode.setChecked(true);
        } else {
            switch_textmode.setChecked(false);
        }

        switch_textmode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                NewsManager.getInstance().set_text_mode(b);
            }
        });
    }


}

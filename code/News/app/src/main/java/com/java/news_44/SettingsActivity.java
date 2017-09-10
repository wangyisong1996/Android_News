package com.java.news_44;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;

import java.util.Set;
import java.util.zip.Inflater;

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

        String categories[] = NewsManager.getInstance().getCategoryNames();
        int n = categories.length;
        LayoutInflater inflater = getLayoutInflater();
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.settings_linearlayout);
        for (int i = 0; i < n; i++) {
            Switch s = (Switch) inflater.inflate(R.layout.settings_element, null);
            s.setText(categories[i]);
            final int id = NewsManager.getInstance().getCategoryIDFromName(categories[i]);
            s.setChecked(NewsManager.getInstance().getCategoryEnabled(id));
            s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    NewsManager.getInstance().setCategoryEnabled(id, b);
                }
            });
            linearLayout.addView(s);
        }
    }


}

package com.java.news_44;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.TreeMap;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        // init news manager
        NewsManager.getInstance().setActivity(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setCheckedItem(R.id.nav_all_news);


        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.main_list_item, R.id.main_list_item_text, arr);

        RecyclerView main_recyclerview = (RecyclerView) findViewById(R.id.main_recyclerview);
        MainAdapter adapter = new MainAdapter();
        main_recyclerview.setAdapter(adapter);
        main_recyclerview.setLayoutManager(new LinearLayoutManager(this));

        final NewsManager newsManager = NewsManager.getInstance();

        newsManager.setAdapter(adapter);
        newsManager.init();

        // init tabs
        LinearLayout tabs_layout = (LinearLayout) findViewById(R.id.main_tabs_layout);
        String category_names[] = newsManager.getCategoryNames();
        LayoutInflater inflater = getLayoutInflater();
        final MainActivity mainActivity = this;
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView textView = (TextView) view;
                mainActivity.onCategoryClicked(newsManager.getCategoryIDFromName(textView.getText().toString()));
            }
        };
        {
            TextView textView = (TextView) inflater.inflate(R.layout.main_tabs_element, null);
            textView.setText("热点");
            textView.setOnClickListener(listener);
            tabs_layout.addView(textView);
            map_category_id_to_view.put(-1, textView);
        }
        for (String name : category_names) {
            TextView textView = (TextView) inflater.inflate(R.layout.main_tabs_element, null);
            textView.setText(name);
            textView.setOnClickListener(listener);
            tabs_layout.addView(textView);
            map_category_id_to_view.put(newsManager.getCategoryIDFromName(name), textView);
        }
        tabs_layout.requestLayout();

        this.onCategoryClicked(-1);
    }

    private TreeMap<Integer, TextView> map_category_id_to_view = new TreeMap<>();

    void onCategoryClicked(int id) {
        int prev_id = NewsManager.getInstance().getCurrentCategory();
        TextView prev_view = map_category_id_to_view.get(prev_id);
        prev_view.setTextSize(16);
        prev_view.setTextColor(Color.BLACK);

        TextView now_view = map_category_id_to_view.get(id);
        now_view.setTextSize(17);
        now_view.setTextColor(Color.RED);

        NewsManager.getInstance().setCurrentCategory(id);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_about) {
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        NewsManager.getInstance().setActivity(null);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}

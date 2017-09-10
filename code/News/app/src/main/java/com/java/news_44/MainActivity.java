package com.java.news_44;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.inputmethodservice.InputMethodService;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.SharedPreferencesCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.w3c.dom.Text;

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

        getSupportActionBar().setTitle("新闻");

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
        NewsManager.getInstance().set_nav_news();

        EditText e = (EditText) findViewById(R.id.main_search_text);
        e.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.requestFocus();
            }
        });
        e.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                NewsManager.getInstance().setSearchKeyword(((TextView) findViewById(R.id.main_search_text)).getText().toString());
                NewsManager.getInstance().refreshNewsLists();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        e.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                TextView textView = (TextView) view;

                if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    textView.clearFocus();
                    ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(textView.getWindowToken(), 0);
                    return true;
                }

                return false;
            }
        });

        ((TextView) findViewById(R.id.main_search_cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(((TextView) findViewById(R.id.main_search_text)).getWindowToken(), 0);
                toggleSearch();
            }
        });

        newsManager.setSearchKeyword("");
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

    private MenuItem search_menu_item;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        search_menu_item = menu.getItem(0);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_main_search) {
            this.toggleSearch();
            if (!searchEnabled) {
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(((TextView) findViewById(R.id.main_search_text)).getWindowToken(), 0);
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private int main_category_scroll_height = -1;

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.nav_favorites) {
            if (searchEnabled) {
                toggleSearch();
            }

            getSupportActionBar().setTitle("收藏");
            NewsManager.getInstance().set_nav_favorites();

            HorizontalScrollView v = (HorizontalScrollView) findViewById(R.id.main_category_scroll);
            if (main_category_scroll_height == -1) main_category_scroll_height = v.getLayoutParams().height;
            v.getLayoutParams().height = 0;
            v.setVisibility(View.INVISIBLE);
            v.requestLayout();

            search_menu_item.setVisible(false);

        } else if (id == R.id.nav_all_news) {
            getSupportActionBar().setTitle("新闻");
            NewsManager.getInstance().set_nav_news();

            HorizontalScrollView v = (HorizontalScrollView) findViewById(R.id.main_category_scroll);
            v.getLayoutParams().height = main_category_scroll_height;
            v.setVisibility(View.VISIBLE);
            v.requestLayout();

            search_menu_item.setVisible(true);
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

    private boolean searchEnabled = false;

    private void toggleSearch() {
        if (!searchEnabled) {
            searchEnabled = true;
            ((RelativeLayout) this.findViewById(R.id.main_search_layout)).setVisibility(View.VISIBLE);
            EditText e = (EditText) this.findViewById(R.id.main_search_text);
            e.requestFocus();
            ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).showSoftInput(e, InputMethodManager.SHOW_IMPLICIT);
        } else {
            searchEnabled = false;
            ((RelativeLayout) this.findViewById(R.id.main_search_layout)).setVisibility(View.GONE);
            EditText e = (EditText) this.findViewById(R.id.main_search_text);
            e.clearFocus();
            e.setText("");

            NewsManager.getInstance().setSearchKeyword("");
            NewsManager.getInstance().refreshNewsLists();
        }
    }
}

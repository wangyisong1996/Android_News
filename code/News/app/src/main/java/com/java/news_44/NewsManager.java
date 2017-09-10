package com.java.news_44;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.MainThread;
import android.support.annotation.RequiresPermission;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.Cache;
import com.android.volley.ExecutorDelivery;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.TtsMode;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by wys on 9/5/2017.
 */

class NewsManager {
    private static final NewsManager ourInstance = new NewsManager();

    static NewsManager getInstance() {
        return ourInstance;
    }

    private NewsManager() {
    }

    private RequestQueue listReqQueue, newsReqQueue;

    private MainActivity activity;
    private MainAdapter adapter;

    private ArrayList<NewsAbstract> newsList = new ArrayList<>();

    private HashSet<Integer> loadingPages = new HashSet<>();

    void setActivity(MainActivity activity) {
        this.activity = activity;
    }

    void setAdapter(MainAdapter adapter) {
        this.adapter = adapter;
    }

    private TreeMap<Integer, String> map_id_to_category = new TreeMap<>();
    private TreeMap<String, Integer> map_category_to_id = new TreeMap<>();

    private int currentCategory = -1;  // all

    private Cache listCache;
    private Cache newsCache;

    void init() {
        // TODO init

        map_id_to_category.put(1, "科技");
        map_id_to_category.put(2, "教育");
        map_id_to_category.put(3, "军事");
        map_id_to_category.put(4, "国内");
        map_id_to_category.put(5, "社会");
        map_id_to_category.put(6, "文化");
        map_id_to_category.put(7, "汽车");
        map_id_to_category.put(8, "国际");
        map_id_to_category.put(9, "体育");
        map_id_to_category.put(10, "财经");
        map_id_to_category.put(11, "健康");
        map_id_to_category.put(12, "娱乐");

        for (TreeMap.Entry<Integer, String> e : map_id_to_category.entrySet()) {
            map_category_to_id.put(e.getValue(), e.getKey());
        }

        currentCategory = -1;


        listCache = new DiskBasedCache(activity.getCacheDir(), 1024 * 1024 * 10);
        newsCache = new DiskBasedCache(activity.getCacheDir(), 1024 * 1024 * 100);
        listCache.initialize();
        newsCache.initialize();

        this.listReqQueue = Volley.newRequestQueue(activity.getApplicationContext());
        this.newsReqQueue = new RequestQueue(newsCache, new BasicNetwork(new HurlStack()));
        this.listReqQueue.start();
        this.newsReqQueue.start();


        init_read();
        init_favorites();
        load_text_mode();
        loadCategoryEnabled();



        newsList = new ArrayList<>();
        loadingPages = new HashSet<>();
        adapter.notifyDataSetChanged();



        SpeechSynthesizer speechSynthesizer = SpeechSynthesizer.getInstance();
        speechSynthesizer.setApiKey("pQsI1FoGP8L3qwoVamZoBtAj", "e0f91a9beb647837b5afd3fa2d68abf7");
        speechSynthesizer.setAppId("10108778");
        speechSynthesizer.setStereoVolume(1.0f, 1.0f);
        speechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "3");
        speechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_LICENCE_FILE, "temp_license");
        speechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
        speechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, "7");
        speechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME, "9");
        speechSynthesizer.auth(TtsMode.MIX);
        speechSynthesizer.initTts(TtsMode.MIX);
    }

    int getNewsCount() {
        return 0;
    }

    private final String baseURL = "http://166.111.68.66:2042/news";

    private final int pageSize = 100;

    private void onListResponse(JSONObject response) {
        try {
            JSONArray arr = response.getJSONArray("list");
            int pageNo = response.getInt("pageNo");
            loadingPages.remove(pageNo);
            int pageSize = response.getInt("pageSize");
            int offset = (pageNo - 1) * pageSize;
            for (int i = 0; i < arr.length(); i++) {
                this.addNews(new NewsAbstract(arr.getJSONObject(i), offset + i));
            }
        } catch (Exception e) {}
    }

    private Cache.Entry newEntry(byte data[]) {
        Cache.Entry e = new Cache.Entry();
        e.data = data;
        e.ttl = 1L << 50;
        e.softTtl = 1L << 50;
        return e;
    }

    private class ListResponseListener implements Response.Listener<JSONObject> {
        ListResponseListener(String url) {
            this.url = url;
        }

        private String url = "";

        @Override
        public void onResponse(JSONObject response) {
            try {
                int pageNo = response.getInt("pageNo");
                int pageSize = response.getInt("pageSize");
                int totalPages = response.getInt("totalPages");
                int totalRecords = response.getInt("totalRecords");
                Log.wtf("NewsApp", "list query : pageNo " + pageNo + " pageSize " + pageSize + " totalPages " + totalPages + " totalRecords " + totalRecords);
            } catch (Exception e) {}
            listCache.put(this.url, newEntry(response.toString().getBytes()));
            onListResponse(response);
        }
    }

    private JsonObjectRequest newListRequest(int pageNo, int category) {
        String url = "";
        if (!SearchKeyword.equals("")) {
            try {
                url = baseURL + "/action/query/search?keyword=" + URLEncoder.encode(SearchKeyword, "UTF-8") + "&pageNo=" + pageNo + "&pageSize=" + pageSize + "&category=" + category;
            } catch (Exception _) {}
        } else {
            url = baseURL + "/action/query/latest?pageNo=" + pageNo + "&pageSize=" + pageSize + "&category=" + category;
        }
        return new JsonObjectRequest(Request.Method.GET,
                url,
                null,
                new ListResponseListener(url),
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // do nothing
                    }
                });
    }

    private ImageRequest newImageRequest(final String url, int width, int height, ImageView.ScaleType scaleType, final ImageHolder img) {
        return new ImageRequest(url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        Log.wtf("233", "loaded image " + url);
                        img.onLoad(response);
                    }
                }, width, height, scaleType, Bitmap.Config.ARGB_8888,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // nothing
                        Log.wtf("233", "error loading image " + url);
                    }
                });
    }

    private class NewsRequestListener implements Response.Listener<JSONObject> {
        NewsRequestListener(String url, JsonHolder holder) {
            this.url = url;
            this.holder = holder;
        }

        private String url;
        private JsonHolder holder;

        @Override
        public void onResponse(JSONObject response) {
            newsCache.put(url, newEntry(response.toString().getBytes()));

            holder.onLoad(response);
        }
    }

    private JsonObjectRequest newNewsDetailRequest(String newsID, final JsonHolder holder) {
        String url = baseURL + "/action/query/detail?newsId=" + newsID;
        return new JsonObjectRequest(Request.Method.GET,
                url,
                null,
                new NewsRequestListener(url, holder),
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // nothing
                    }
                });
    }

    private void addNews(NewsAbstract news) {
        // lower bound
        int l = 0, r = newsList.size();
        while (l < r) {
            int mid = (l + r) / 2;
            if (newsList.get(mid).compareTo(news) < 0) {
                l = mid + 1;
            } else {
                r = mid;
            }
        }
        if (l < newsList.size() && newsList.get(l).compareTo(news) == 0) {
            return;
        }

        newsList.add(l, news);

        adapter.insert(l);
    }

    NewsAbstract getNewsFromPosition(int pos) {
        if (nav_tab_state == NAV_TAB_FAVORITES) {
            return favoriteNewsAbstract.get(pos);
        } else {
            return newsList.get(pos);
        }
    }

    void loadNewsList(int index) {
        int pageNo = index / pageSize + 1;

        if (nav_tab_state == NAV_TAB_FAVORITES) {
            // this won't happen
            return;
        }

        if (loadingPages.contains(pageNo)) {
            return;
        }

        loadingPages.add(pageNo);

        JsonObjectRequest req = newListRequest(pageNo, currentCategory);

        Cache.Entry e = listCache.get(req.getUrl());
        Log.wtf("cache get", req.getUrl());
        if (e != null) {
            try {
                onListResponse(new JSONObject(new String(e.data)));
            } catch (Exception _) {}
        } else {
            listReqQueue.add(req);
        }
    }

    void loadImage(String url, int width, int height, ImageView.ScaleType scaleType, ImageHolder img) {
        if (is_text_mode()) return;

        newsReqQueue.add(newImageRequest(url, width, height, scaleType, img));
    }

    private static final String PACKAGE_NAME = "com.java.news_44";
    static final String NEWS_ID = PACKAGE_NAME + ".NEWS_ID";
    static final String SCREEN_WIDTH = PACKAGE_NAME + ".SCREEN_WIDTH";
    static final String LIST_INDEX = PACKAGE_NAME + ".LIST_INDEX";

    void showNewsDetail(int index) {
        NewsAbstract news = this.getNewsFromPosition(index);
        Intent intent = new Intent(this.activity, NewsDetailActivity.class);
        intent.putExtra(NEWS_ID, news.getId());
        intent.putExtra(SCREEN_WIDTH, activity.findViewById(R.id.main_recyclerview).getWidth());
        intent.putExtra(LIST_INDEX, index);
        activity.startActivity(intent);
    }

    void loadNewsDetail(String newsID, JsonHolder holder) {
        JsonObjectRequest req = newNewsDetailRequest(newsID, holder);
        Cache.Entry e = newsCache.get(req.getUrl());
        if (e != null) {
            try {
                holder.onLoad(new JSONObject(new String(e.data)));
            } catch (Exception _) {}
        } else {
            newsReqQueue.add(req);
        }
    }

    String[] getCategoryNames() {
        String ret[] = new String[map_id_to_category.size()];
        int id = 0;
        for (TreeMap.Entry<Integer, String> e : map_id_to_category.entrySet()) {
            ret[id++] = e.getValue();
        }
        return ret;
    }

    int getCategoryIDFromName(String name) {
        if (map_category_to_id.containsKey(name)) {
            return map_category_to_id.get(name);
        } else {
            return -1;
        }
    }

    int getCurrentCategory() {
        return currentCategory;
    }

    void setCurrentCategory(int id) {
        currentCategory = id;

        if (nav_tab_state != NAV_TAB_NEWS) {
            return;
        }

        clearNewsLists();

        // load the first item in the list
        loadNewsList(0);
    }

    private void clearNewsLists() {
        // clear lists
        this.listReqQueue = Volley.newRequestQueue(activity.getApplicationContext());
        this.newsReqQueue = new RequestQueue(newsCache, new BasicNetwork(new HurlStack()));
        this.listReqQueue.start();
        this.newsReqQueue.start();
        newsList = new ArrayList<>();
        loadingPages = new HashSet<>();
        adapter.clear();
        adapter.notifyDataSetChanged();
    }

    // read mark

    private HashSet<String> readNews;

    private void init_read() {
        loadReadState();
    }

    void setRead(String id) {
        readNews.add(id);

        saveReadState();
    }

    boolean isRead(String id) {
        return readNews.contains(id);
    }

    private final String ReadStateFilename = "read_state.txt";

    private void saveReadState() {
        File file = new File(activity.getApplicationContext().getFilesDir(), ReadStateFilename);
        PrintWriter p;
        try {
            p = new PrintWriter(file);
        } catch (Exception _) {
            return;
        }
        p.println(readNews.size());
        for (String s : readNews) {
            p.println(s);
        }
        p.close();
    }

    private void loadReadState() {
        readNews = new HashSet<>();

        File file = new File(activity.getApplicationContext().getFilesDir(), ReadStateFilename);
        Scanner s;
        try {
            s = new Scanner(file);
        } catch (Exception _) {
            return;
        }
        int size = Integer.parseInt(s.nextLine());
        for (int i = 0; i < size; i++) {
            readNews.add(s.nextLine());
        }
    }

    // favorites

    private ArrayList<String> favoriteNews;
    private ArrayList<NewsAbstract> favoriteNewsAbstract;

    private void init_favorites() {
        loadFavorites();
    }

    void setFavorite(String id, NewsAbstract news) {
        if (favoriteNews.contains(id)) {
            return;
        }

        favoriteNews.add(0, id);
        favoriteNewsAbstract.add(0, news);

        if (nav_tab_state == NAV_TAB_FAVORITES) {
            adapter.insert(0);
        }

        saveFavorites();
    }

    void unsetFavorite(String id) {
        if (!favoriteNews.contains(id)) {
            return;
        }

        int pos = favoriteNews.indexOf(id);
        favoriteNews.remove(pos);
        favoriteNewsAbstract.remove(pos);

        if (nav_tab_state == NAV_TAB_FAVORITES) {
            adapter.remove(pos);
        }

        saveFavorites();
    }

    boolean isFavorite(String id) {
        return favoriteNews.contains(id);
    }

    private final String FavoritesFilename = "favorites.txt";

    private void saveFavorites() {
        File file = new File(activity.getApplicationContext().getFilesDir(), FavoritesFilename);
        PrintWriter p;
        try {
            p = new PrintWriter(file);
        } catch (Exception _) {
            return;
        }
        int size = favoriteNews.size();
        p.println(size);
        for (int i = 0; i < size; i++) {
            p.println(favoriteNews.get(i));
            p.println(favoriteNewsAbstract.get(i).toString());
        }
        p.close();
    }

    private void loadFavorites() {
        favoriteNews = new ArrayList<>();
        favoriteNewsAbstract = new ArrayList<>();

        File file = new File(activity.getApplicationContext().getFilesDir(), FavoritesFilename);
        Scanner s;
        try {
            s = new Scanner(file);
        } catch (Exception _) {
            return;
        }
        int size = Integer.parseInt(s.nextLine());
        for (int i = 0; i < size; i++) {
            favoriteNews.add(s.nextLine());
            favoriteNewsAbstract.add(NewsAbstract.fromString(s.nextLine()));
        }
    }

    // fav list

    private final int NAV_TAB_NEWS = 1;
    private final int NAV_TAB_FAVORITES = 2;

    private int nav_tab_state = NAV_TAB_NEWS;

    void set_nav_news() {
        if (nav_tab_state == NAV_TAB_NEWS) {
            return;
        }

        nav_tab_state = NAV_TAB_NEWS;
        clearNewsLists();
        loadNewsList(0);
    }

    void set_nav_favorites() {
        if (nav_tab_state == NAV_TAB_FAVORITES) {
            return;
        }

        nav_tab_state = NAV_TAB_FAVORITES;
        clearNewsLists();
        adapter.setItemCount(favoriteNewsAbstract.size());
        adapter.notifyDataSetChanged();
    }

    boolean is_on_favorites_tab() {
        return nav_tab_state == NAV_TAB_FAVORITES;
    }

    // search

    private String SearchKeyword = "";

    void setSearchKeyword(String keyword) {
        SearchKeyword = keyword;
    }

    void refreshNewsLists() {
        clearNewsLists();
        loadNewsList(0);
    }

    // settings/text_mode

    private boolean text_mode = false;

    boolean is_text_mode() {
        return text_mode;
    }

    void set_text_mode(boolean f) {
        text_mode = f;
        save_text_mode();
        adapter.notifyDataSetChanged();  // refresh
    }

    private final String TextModeFileName = "text_mode.txt";

    private void save_text_mode() {
        File file = new File(activity.getApplicationContext().getFilesDir(), TextModeFileName);
        PrintWriter p;
        try {
            p = new PrintWriter(file);
        } catch (Exception _) {
            return;
        }

        p.println(text_mode ? 1 : 0);

        p.close();
    }

    private void load_text_mode() {
        text_mode = false;

        File file = new File(activity.getApplicationContext().getFilesDir(), TextModeFileName);
        Scanner s;
        try {
            s = new Scanner(file);
        } catch (Exception _) {
            return;
        }

        int x = s.nextInt();
        text_mode = x == 1;
    }

    // settings/categories

    TreeMap<Integer, Boolean> CategoryEnabled;

    boolean getCategoryEnabled(int id) {
        if (!CategoryEnabled.containsKey(id)) {
            CategoryEnabled.put(id, true);
        }
        return CategoryEnabled.get(id);
    }

    void setCategoryEnabled(int id, boolean b) {
        CategoryEnabled.put(id, b);

        if (id == currentCategory && !b) {
            activity.onCategoryClicked(-1);
        }

        activity.setCategoryEnabled(id, b);

        saveCategoryEnabled();
    }

    private final String CategoryEnabledFilename = "category_settings.txt";

    private void saveCategoryEnabled() {
        File file = new File(activity.getApplicationContext().getFilesDir(), CategoryEnabledFilename);
        PrintWriter p;
        try {
            p = new PrintWriter(file);
        } catch (Exception _) {
            return;
        }

        p.println(CategoryEnabled.size());

        for (TreeMap.Entry<Integer, Boolean> e : CategoryEnabled.entrySet()) {
            p.println(e.getKey());
            p.println(e.getValue() ? 1 : 0);
        }

        p.close();
    }

    private void loadCategoryEnabled() {
        CategoryEnabled = new TreeMap<>();

        File file = new File(activity.getApplicationContext().getFilesDir(), CategoryEnabledFilename);
        Scanner s;
        try {
            s = new Scanner(file);
        } catch (Exception _) {
            return;
        }

        int n = s.nextInt();

        for (int i = 0; i < n; i++) {
            int id = s.nextInt();
            int b = s.nextInt();
            CategoryEnabled.put(id, b == 1);
        }
    }

}

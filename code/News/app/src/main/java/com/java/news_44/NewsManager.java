package com.java.news_44;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
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

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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

    private Activity activity;
    private MainAdapter adapter;

    private ArrayList<NewsAbstract> newsList = new ArrayList<>();

    private HashSet<Integer> loadingPages = new HashSet<>();

    void setActivity(Activity activity) {
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
        String url = baseURL + "/action/query/latest?pageNo=" + pageNo + "&pageSize=" + pageSize + "&category=" + category;
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
        return newsList.get(pos);
    }

    void loadNewsList(int index) {
        int pageNo = index / pageSize + 1;

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
        newsReqQueue.add(newImageRequest(url, width, height, scaleType, img));
    }

    static final String NEWS_ID = "com.java.news_44.NEWS_ID";
    static final String SCREEN_WIDTH = "com.java.news_44.SCREEN_WIDTH";

    void showNewsDetail(int index) {
        NewsAbstract news = this.getNewsFromPosition(index);
        Intent intent = new Intent(this.activity, NewsDetailActivity.class);
        intent.putExtra(NEWS_ID, news.getId());
        intent.putExtra(SCREEN_WIDTH, activity.findViewById(R.id.main_recyclerview).getWidth());
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

        // clear lists
        this.listReqQueue = Volley.newRequestQueue(activity.getApplicationContext());
        this.newsReqQueue = new RequestQueue(newsCache, new BasicNetwork(new HurlStack()));
        this.listReqQueue.start();
        this.newsReqQueue.start();
        newsList = new ArrayList<>();
        loadingPages = new HashSet<>();
        adapter.clear();
        adapter.notifyDataSetChanged();

        loadNewsList(0);
    }
}

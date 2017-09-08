package com.java.news_44;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.TtsMode;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;

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

    void init() {
        // TODO init

        this.listReqQueue = Volley.newRequestQueue(activity.getApplicationContext());
        this.newsReqQueue = Volley.newRequestQueue(activity.getApplicationContext());


        loadNewsList(0);
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

    private JsonObjectRequest newListRequest(int pageNo, int category) {
        return new JsonObjectRequest(Request.Method.GET,
                baseURL + "/action/query/latest?pageNo=" + pageNo + "&pageSize=" + pageSize + "&category=" + category,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int pageNo = response.getInt("pageNo");
                            int pageSize = response.getInt("pageSize");
                            int totalPages = response.getInt("totalPages");
                            int totalRecords = response.getInt("totalRecords");
                            Log.wtf("NewsApp", "list query : pageNo " + pageNo + " pageSize " + pageSize + " totalPages " + totalPages + " totalRecords " + totalRecords);
                        } catch (Exception e) {}
                        onListResponse(response);
                    }
                },
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

    private JsonObjectRequest newNewsDetailRequest(String newsID, final JsonHolder holder) {
        return new JsonObjectRequest(Request.Method.GET,
                baseURL + "/action/query/detail?newsId=" + newsID,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        holder.onLoad(response);
                    }
                },
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
        listReqQueue.add(newListRequest(pageNo, 2));
    }

    void loadImage(String url, int width, int height, ImageView.ScaleType scaleType, ImageHolder img) {
        newsReqQueue.add(newImageRequest(url, width, height, scaleType, img));
    }

    static final String NEWS_ID = "com.java.news_44.NEWS_ID";

    void showNewsDetail(int index) {
        NewsAbstract news = this.getNewsFromPosition(index);
        Intent intent = new Intent(this.activity, NewsDetailActivity.class);
        intent.putExtra(NEWS_ID, news.getId());
        activity.startActivity(intent);
    }

    void loadNewsDetail(String newsID, JsonHolder holder) {
        newsReqQueue.add(newNewsDetailRequest(newsID, holder));
    }

}

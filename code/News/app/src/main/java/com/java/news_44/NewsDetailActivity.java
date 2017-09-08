package com.java.news_44;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;

import org.json.JSONObject;

public class NewsDetailActivity extends AppCompatActivity implements SpeechSynthesizerListener {

    private String tag = "";
    private String author = "";
    private String title = "";
    private String source = "";
    private String time = "";
    private String content = "";
    private String pictures[] = new String[0];

    private TextView text_title;
    private TextView text_author;
    private TextView text_source;
    private TextView text_time;
    private TextView text_content;
    private LinearLayout images_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);

        Intent intent = getIntent();
        String newsID = intent.getStringExtra(NewsManager.NEWS_ID);

        final NewsDetailActivity newsDetailActivity = this;
        NewsManager.getInstance().loadNewsDetail(newsID, new JsonHolder() {
            @Override
            public void onLoad(JSONObject obj) {
                newsDetailActivity.load(obj);
            }
        });

        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.setTitle("新闻详情");
    }

    private void load(JSONObject obj) {
        try {
            tag = obj.getString("newsClassTag");
            author = obj.getString("news_Author");
            source = obj.getString("news_Source");
            title = obj.getString("news_Title");
            time = convertTimeString(obj.getString("news_Time"));
            content = processContent(obj.getString("news_Content"));

            String pics[] = ((String) obj.get("news_Pictures")).split("\\ |;");
            int picCnt = 0;
            for (String pic : pics) {
                if (pic.length() > 0) {
                    ++picCnt;
                }
            }
            this.pictures = new String[picCnt];
            int picIndex = 0;
            for (String pic : pics) {
                if (pic.length() > 0) {
                    this.pictures[picIndex++] = pic;
                }
            }
        } catch (Exception e) {}

        text_title = (TextView) findViewById(R.id.detail_title_text);
        text_author = (TextView) findViewById(R.id.detail_author_text);
        text_source = (TextView) findViewById(R.id.detail_source_text);
        text_time = (TextView) findViewById(R.id.detail_time_text);
        text_content = (TextView) findViewById(R.id.detail_content_text);
        images_layout = (LinearLayout) findViewById(R.id.detail_images_layout);

        text_title.setText(title);
        text_author.setText(author);
        text_source.setText(source);
        text_time.setText(time);
        text_content.setText(content);

        for (String pic : pictures) {
            final ImageView imageView = new ImageView(images_layout.getContext());
            final int width = images_layout.getWidth() - 20;
            final int height = width * 3 / 4;
            imageView.setPadding(10, 10, 10, 10);
            images_layout.addView(imageView);
            NewsManager.getInstance().loadImage(pic, width, height, ImageView.ScaleType.CENTER_CROP,
                    new ImageHolder() {
                        @Override
                        public void onLoad(Bitmap res) {
//                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
//                                    LinearLayout.LayoutParams.MATCH_PARENT,
//                                    LinearLayout.LayoutParams.WRAP_CONTENT);
//                            imageView.setLayoutParams(lp);
                            imageView.getLayoutParams().width = width;
                            imageView.getLayoutParams().height = height;
                            imageView.setImageBitmap(res);
                            imageView.requestLayout();
                        }
                    });
        }
    }

    private String convertTimeString(String time) {
        return Integer.parseInt(time.substring(0, 4)) + "年" + Integer.parseInt(time.substring(4, 6)) + "月" + Integer.parseInt(time.substring(6, 8)) + "日";
    }

    private String processContent(String content) {
        String list[] = content.split("　　");
        String ret = list[0];
        for (int i = 1; i < list.length; i++) {
            ret = ret + "\n　　" + list[i];
        }
        return ret;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_detail_speak) {
            speak(this.title + "。\n" + this.content);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean isSpeaking = false;

    private int lastSpeakID = 1000000000;
    private boolean hasStartedTTS = false;

    private void speak(String s) {
        if (!hasStartedTTS) {
            hasStartedTTS = true;
            startTTS();
        }

        if (isSpeaking) {
            try {
                speechSynthesizer.stop();
            } catch (Exception e) {}
            isSpeaking = false;
        } else {
            isSpeaking = true;
            String arr[] = s.split("。");
            String temp = "";
            for (String s1 : arr) {
                if ((temp + "。" + s1).length() > 500) {
                    speechSynthesizer.speak(temp, "" + ++lastSpeakID);
                    temp = s1;
                } else {
                    temp = temp + "。" + s1;
                }
            }
            speechSynthesizer.speak(temp, "" + ++lastSpeakID);
        }
    }

    private SpeechSynthesizer speechSynthesizer;

    private void startTTS() {
        speechSynthesizer = SpeechSynthesizer.getInstance();
        speechSynthesizer.setContext(this);
        speechSynthesizer.setSpeechSynthesizerListener(this);
    }

    @Override
    public void onSynthesizeStart(String s) {

    }

    @Override
    public void onSynthesizeDataArrived(String s, byte[] bytes, int i) {

    }

    @Override
    public void onSynthesizeFinish(String s) {

    }

    @Override
    public void onSpeechStart(String s) {

    }

    @Override
    public void onSpeechProgressChanged(String s, int i) {

    }

    @Override
    public void onSpeechFinish(String s) {

    }

    @Override
    public void onError(String s, SpeechError speechError) {

    }

    @Override
    protected void onDestroy() {
        try {
            speechSynthesizer.stop();
        } catch (Exception e) {}

        super.onDestroy();
    }
}

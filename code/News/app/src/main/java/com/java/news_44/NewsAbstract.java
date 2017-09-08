package com.java.news_44;

import android.support.annotation.NonNull;

import org.json.JSONObject;

/**
 * Created by wys on 9/6/2017.
 */

class NewsAbstract implements Comparable<NewsAbstract> {
    private String tag = "";
    private String author = "";
    private String id = "";
    private String intro = "";
    private String pictures[] = new String[0];
    private String source = "";
    private String time = "";
    private String title = "";
    private String url = "";
    private int listID = -1;

    NewsAbstract(JSONObject jsonObject, int listID) {
        try {
            this.tag = (String) jsonObject.get("newsClassTag");
            this.author = (String) jsonObject.get("news_Author");
            this.id = (String) jsonObject.get("news_ID");
            this.intro = (String) jsonObject.get("news_Intro");

            String pics[] = ((String) jsonObject.get("news_Pictures")).split("\\ |;");
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

            this.source = (String) jsonObject.get("news_Source");
            this.time = (String) jsonObject.get("news_Time");
            this.title = (String) jsonObject.get("news_Title");
            this.url = (String) jsonObject.get("news_URL");

            this.listID = listID;
        } catch (Exception e) {
        }
    }

    @Override
    public int compareTo(@NonNull NewsAbstract newsAbstract) {
        if (this.id.equals(newsAbstract.id)) {
            return 0;
        }

        int tmp;

        if ((tmp = this.time.compareTo(newsAbstract.time)) != 0) {
            return tmp > 0 ? -1 : 1;
        }

        if (this.listID != newsAbstract.listID) {
            return this.listID < newsAbstract.listID ? -1 : 1;
        }

        return this.id.compareTo(newsAbstract.id);
    }

    String getTag() {
        return tag;
    }

    String getAuthor() {
        return author;
    }

    String getId() {
        return id;
    }

    String getIntro() {
        return intro;
    }

    String getSource() {
        return source;
    }

    String getTime() {
        return time;
    }

    String getTitle() {
        return title;
    }

    String getUrl() {
        return url;
    }

    String[] getPictures() {
        return pictures;
    }

    int getListID() {
        return listID;
    }
}

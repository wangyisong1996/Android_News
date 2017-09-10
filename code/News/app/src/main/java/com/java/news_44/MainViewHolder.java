package com.java.news_44;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by wys on 9/5/2017.
 */

class MainViewHolder extends RecyclerView.ViewHolder {

    private View view;
    private TextView textTitle;
    private TextView textSource;
    private TextView textTime;
    private ImageView image0;
    private LinearLayout image0Layout;
    private int image0_oldwidth;
    private int image0_oldheight;
    private int image0_requestid = 0;

    private String NewsID = "";

    MainViewHolder(final View itemView) {
        super(itemView);

        this.view = itemView;

        textTitle = (TextView) view.findViewById(R.id.main_list_title_text);
        textSource = (TextView) view.findViewById(R.id.main_list_source_text);
        textTime = (TextView) view.findViewById(R.id.main_list_time_text);

        image0 = (ImageView) view.findViewById(R.id.main_list_image_0);
        image0Layout = (LinearLayout) view.findViewById(R.id.main_list_image_layout_0);
        image0_oldwidth = image0Layout.getLayoutParams().width;
        image0_oldheight = image0Layout.getLayoutParams().height;
    }

    View getView() {
        return view;
    }

    void setNews(NewsAbstract news) {
        textTitle.setText(news.getTitle());
        textSource.setText(news.getSource());
        textTime.setText(convertTimeString(news.getTime()));

        String pictures[] = news.getPictures();
        this.setNoImage();
        if (pictures.length > 0) {
            final MainViewHolder holder = this;
            ++image0_requestid;
            final int tmp_requestid = image0_requestid;
            NewsManager.getInstance().loadImage(pictures[0], image0_oldwidth, image0_oldheight, ImageView.ScaleType.CENTER_CROP, new ImageHolder() {
                @Override
                public void onLoad(Bitmap res) {
                    // to serialize the responses ...
                    if (tmp_requestid == image0_requestid) {
                        holder.setImage(res);
                    }
                }
            });
        }

        this.NewsID = news.getId();

        if (NewsManager.getInstance().isRead(news.getId())) {
            this.setRead();
        } else {
            this.setUnread();
        }
    }

    private String convertTimeString(String time) {
        return Integer.parseInt(time.substring(0, 4)) + "年" + Integer.parseInt(time.substring(4, 6)) + "月" + Integer.parseInt(time.substring(6, 8)) + "日";
    }

    private void setNoImage() {
        image0Layout.getLayoutParams().width = 0;
        image0Layout.getLayoutParams().height = 0;
        image0.setImageBitmap(null);
        image0Layout.requestLayout();
    }

    private void setImage(Bitmap bitmap) {
        image0Layout.getLayoutParams().width = image0_oldwidth;
        image0Layout.getLayoutParams().height = image0_oldheight;
        image0.setImageBitmap(bitmap);
        image0Layout.requestLayout();
    }


    void setRead() {
        if (NewsManager.getInstance().is_on_favorites_tab()) {
            textTitle.setTextColor(Color.BLACK);
        } else {
            textTitle.setTextColor(Color.GRAY);
        }
    }

    void setUnread() {
        textTitle.setTextColor(Color.BLACK);
    }

    String getNewsID() {
        return NewsID;
    }

}

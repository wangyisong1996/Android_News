package com.java.news_44;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by wys on 9/5/2017.
 */

public class MainViewHolder extends RecyclerView.ViewHolder {

    private TextView textview;

    public MainViewHolder(final View itemView) {
        super(itemView);

        textview = (TextView) itemView.findViewById(R.id.main_list_item_text);
    }

    public void setText(String text) {
        textview.setText(text);
    }
}

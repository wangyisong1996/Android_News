package com.java.news_44;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wys on 9/5/2017.
 */

class MainAdapter extends RecyclerView.Adapter<MainViewHolder> {

    private ArrayList<String> arr = new ArrayList<String>();
    private HashMap<View, Integer> map_view_to_position = new HashMap<View, Integer>();

    private int currentCount = 0;

    MainAdapter() {

    }

    int getIdFromView(View view) {
        return map_view_to_position.get(view);
    }

    /*void delete(int id) {

        arr.remove(id);
        for (Map.Entry<View, Integer> e : map_view_to_position.entrySet()) {
            if (e.getValue() > id) {
                map_view_to_position.put(e.getKey(), e.getValue() - 1);
            }
        }
        this.notifyItemRemoved(id);
        NewsManager.getInstance().test();
    }*/

    /*private class MyDeleteListener implements View.OnClickListener {
        private MainAdapter adapter;
        private int id;

        MyDeleteListener(MainAdapter adapter, int id) {
            this.adapter = adapter;
            this.id = id;
        }

        public void onClick(View view) {
            adapter.delete(id);
        }
    }*/

    private class MyOnClickListener implements View.OnClickListener {
        private MainAdapter adapter;

        MyOnClickListener(MainAdapter adapter) {
            this.adapter = adapter;
        }

        public void onClick(View view) {
            int id = adapter.getIdFromView(view);

            //Snackbar snackbar = Snackbar.make(view, "You clicked " + id, Snackbar.LENGTH_SHORT);
            //snackbar.setAction("Delete", new MyDeleteListener(adapter, id));
            //snackbar.show();

            NewsManager.getInstance().showNewsDetail(id);
        }
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.main_list_item, parent, false);
        view.setOnClickListener(new MyOnClickListener(this));
        return new MainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, int position) {
        /*int id = position % arr.size();
        map_view_to_position.put(holder.getView(), position);
        String name = arr.get(id);
        holder.setText(name);*/

        map_view_to_position.put(holder.getView(), position);

        holder.setNews(NewsManager.getInstance().getNewsFromPosition(position));

        if (getItemCount() - position <= 50) {
            NewsManager.getInstance().loadNewsList(getItemCount());
        }
    }

    @Override
    public int getItemCount() {
        return currentCount;
    }

    void insert(int pos) {
        ++currentCount;
        for (Map.Entry<View, Integer> e : map_view_to_position.entrySet()) {
            if (e.getValue() >= pos) {
                map_view_to_position.put(e.getKey(), e.getValue() + 1);
            }
        }
        this.notifyItemInserted(pos);
    }
}

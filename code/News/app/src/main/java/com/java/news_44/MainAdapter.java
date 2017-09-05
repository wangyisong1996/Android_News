package com.java.news_44;

import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by wys on 9/5/2017.
 */

public class MainAdapter extends RecyclerView.Adapter<MainViewHolder> {

    private ArrayList<String> arr = new ArrayList<String>();
    private HashMap<View, Integer> map_view_to_position = new HashMap<View, Integer>();
    private HashMap<MainViewHolder, View> map_viewholder_to_view = new HashMap<MainViewHolder, View>();

    public MainAdapter() {
        arr.add("233");
        arr.add("23333");
        arr.add("test");
        arr.add("ha");
        arr.add("%");
        arr.add("");
        arr.add("233");
        arr.add("kuaishou");
        arr.add("哦");
        arr.add("行吧");
        arr.add("行吧？");
        arr.add("行吧！");
        arr.add("666");
        arr.add("test");
    }

    public int getIdFromView(View view) {
        return map_view_to_position.get(view);
    }

    public void delete(int id) {
        arr.remove(id);
        for (Map.Entry<View, Integer> e : map_view_to_position.entrySet()) {
            if (e.getValue() > id) {
                map_view_to_position.put(e.getKey(), e.getValue() - 1);
            }
        }
        this.notifyItemRemoved(id);
    }

    class MyDeleteListener implements View.OnClickListener {
        private MainAdapter adapter;
        private int id;

        public MyDeleteListener(MainAdapter adapter, int id) {
            this.adapter = adapter;
            this.id = id;
        }

        public void onClick(View view) {
            adapter.delete(id);
        }
    }

    class MyOnClickListener implements View.OnClickListener {
        private MainAdapter adapter;

        public MyOnClickListener(MainAdapter adapter) {
            this.adapter = adapter;
        }

        public void onClick(View view) {
            int id = adapter.getIdFromView(view);

            Snackbar snackbar = Snackbar.make(view, "You clicked " + id, Snackbar.LENGTH_SHORT);
            snackbar.setAction("Delete", new MyDeleteListener(adapter, id));
            snackbar.show();
        }
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.main_list_item, parent, false);
        view.setOnClickListener(new MyOnClickListener(this));
        MainViewHolder holder = new MainViewHolder(view);
        map_viewholder_to_view.put(holder, view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, int position) {
        int id = position % arr.size();
        map_view_to_position.put(map_viewholder_to_view.get(holder), position);
        String name = arr.get(id);
        holder.setText(name);
    }

    @Override
    public int getItemCount() {
        return arr.size();
    }
}

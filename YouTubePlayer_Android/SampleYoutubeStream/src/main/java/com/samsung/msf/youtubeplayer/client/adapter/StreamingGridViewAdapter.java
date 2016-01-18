package com.samsung.msf.youtubeplayer.client.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.samsung.msf.youtubeplayer.R;
import com.samsung.msf.youtubeplayer.client.util.FeedParser;
import java.util.ArrayList;
import java.util.List;

public class StreamingGridViewAdapter extends BaseAdapter {
    private Context mContext;
    private DisplayImageOptions options;
    private List<FeedParser.Entry> entries = new ArrayList<FeedParser.Entry>();


    public StreamingGridViewAdapter(Context context) {
        mContext = context;

        // AUIL otions
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_action_refresh)
                .showImageForEmptyUri(R.drawable.ic_dialog_alert_holo_light)
                .showImageOnFail(R.drawable.ic_dialog_alert_holo_light)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if(view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.game_tile_item, null);
        }

        ImageView imageView = (ImageView) view.findViewById(R.id.gameGridImageView);
        TextView titleTextView = (TextView) view.findViewById(R.id.titleTextView);
        TextView installedTextView = (TextView) view.findViewById(R.id.installedTextView);

        FeedParser.Entry  entry = entries.get(position);
        titleTextView.setText(entry.title);
        if(entry.installed != 0) {
            installedTextView.setVisibility(View.VISIBLE);
        } else {
            installedTextView.setVisibility(View.GONE);
        }

        //AUIL LIB
        ImageLoader.getInstance()
                .displayImage(entry.thumnail,imageView,options);

        return view;
    }

    @Override
    public int getCount() {
        if(entries !=null) {
            return entries.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if(entries != null){
            return entries.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public void addItem(FeedParser.Entry e){
        entries.add(e);
    }

    public void clear(){
        entries.clear();
    }


    @Override
    public void notifyDataSetChanged() {
        Log.d("ViewAdapter", "notifyDataSetChanged  ");
        super.notifyDataSetChanged();
    }
}
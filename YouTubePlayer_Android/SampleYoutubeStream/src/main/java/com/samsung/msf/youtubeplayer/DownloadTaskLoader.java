package com.samsung.msf.youtubeplayer;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.samsung.msf.youtubeplayer.client.util.FeedParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;


public class DownloadTaskLoader extends AsyncTaskLoader<List<FeedParser.Entry>> {
    public static final String TAG = "DownloadTaskLoader";
    //NEW youtube API  v3
    // need API KEY
    //TO BE -  https://www.googleapis.com/youtube/v3/videos?id=<video_id>&key=<YOUR_API_KEY>&part=snippet
    // reference https://developers.google.com/youtube/v3/docs/search/list
    private  final String MY_API_KEY = "&key=";  //need to change OAuth2 token
    private  final String FEED_WHAT = "&part=snippet&q=movie+trailers";
    private  final String FEED_ORDER = "&order=relevance"; //date, rating relevance title videoCount viewCount
    private  final String FEED_MAX = "&maxResults=50"; //

    private  final String FEED_URL = "https://www.googleapis.com/youtube/v3/search?"
            +  FEED_MAX
            +  FEED_ORDER
            +  MY_API_KEY;

    // private  final String FEED_URL = "https://www.googleapis.com/youtube/v3/videos?id="+FEED_WHAT+"&key="+MY_API_KEY+"&part=snippet";

    public DownloadTaskLoader(Context context) {
        super(context);
    }


    @Override
    public  List<FeedParser.Entry> loadInBackground() {
        Log.d(TAG, "DownloadTaskLoader loadInBackground ");
        final URL location;
        InputStream stream = null;

        try {
        location = new URL(makeFeedURL(FEED_URL));
        Log.d(TAG, "DownloadTaskLoader Streaming data from network: " + location);
        stream = FeedParser.downloadUrl(location);

        } catch (IOException e) {
            e.printStackTrace();
        }

        final FeedParser feedParser = new FeedParser();
        Log.i(TAG, "DownloadTaskLoader feedParser.readJson");
        final List<FeedParser.Entry> entries = feedParser.readJson(stream);

        return entries;
    }


    @Override
    protected void onStartLoading() {
        Log.d(TAG, "DownloadTaskLoader onStartLoading: ");
        super.onStartLoading();
    }

    private String makeFeedURL(String what){
        String data = FeedParser.getmSearchData();

        if("".equals(data)){
            return FEED_URL+FEED_WHAT;
        }else{
            return FEED_URL+"&part=snippet&q="+data;
        }
    }
    public  boolean isAPIKeyEnable(){
        if("&key=".equals(MY_API_KEY)){
            return false;
        }
        else
            return true;
    }
}

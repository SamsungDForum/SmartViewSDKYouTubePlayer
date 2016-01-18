/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.samsung.msf.youtubeplayer.client.util;

import android.text.format.Time;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class FeedParser {
    public static final String TAG = "FeedParser";

    public static String mSearchData="";
    /**
     * Network connection timeout, in milliseconds.
     */
    private static final int NET_CONNECT_TIMEOUT_MILLIS = 15000;  // 15 seconds

    /**
     * Network read timeout, in milliseconds.
     */
    private static final int NET_READ_TIMEOUT_MILLIS = 10000;  // 10 seconds

    /* YouTube API v3
    {
 "kind": "youtube#searchListResponse",
 "etag": "\"dc9DtKVuP_z_ZIF9BZmHcN8kvWQ/mhmrS0QgtJ8UbCNKxvNidhgvjRE\"",
 "nextPageToken": "CAUQAA",
 "pageInfo": {
  "totalResults": 1000000,
  "resultsPerPage": 5
 },
 "items": [
  {
   "kind": "youtube#searchResult",
   "etag": "\"dc9DtKVuP_z_ZIF9BZmHcN8kvWQ/l9j4T_c6LJjOvGMFefCRRjxfoTs\"",
   "id": {
    "kind": "youtube#video",
    "videoId": "gEQAnGp8byY"
   },
   "snippet": {
    "publishedAt": "2015-08-10T23:23:40.000Z",
    "channelId": "UCuC_ph7ci_OfRnnXejOQeSg",
    "title": "Hindi movies 2015 Full Movie (HD 720p) Action Hindi Dubbed Movies 2015 Bollywood",
    "description": "Thank you so much for Watching ! Please subscribe to my youtube channel. If you like this video, please like, share and comment!",
    "thumbnails": {
     "default": {
      "url": "https://i.ytimg.com/vi/gEQAnGp8byY/default.jpg"
     },
     "medium": {
      "url": "https://i.ytimg.com/vi/gEQAnGp8byY/mqdefault.jpg"
     },
     "high": {
      "url": "https://i.ytimg.com/vi/gEQAnGp8byY/hqdefault.jpg"
     }
    },
    "channelTitle": "",
    "liveBroadcastContent": "none"
   }
  },
    */

    public List<Entry> readJson(InputStream stream) {
        List<Entry> entries = new ArrayList<Entry>();

// NEW Youtube API V3.0
        try {
            // Convert this response into a readable string
            String jsonString = convertToString(stream);

            JSONObject json = new JSONObject(jsonString);


            JSONArray jsonArray = json.getJSONArray("items");
            Log.d(TAG, "readJson jsonArray item count  = "+jsonArray.length());

// Loop round our JSON list of videos creating Video objects to use within our app
            for (int i = 0; i < jsonArray.length(); i++) {
                long publishedOn = 0;
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                // The title of the video

                String id;
                try{
                     id = jsonObject.getJSONObject("id").getString("videoId");
                }catch (JSONException e) {
                    continue;
                }

                JSONObject snippet = jsonObject.getJSONObject("snippet");


                String title = snippet.getString("title");
                String uploaded = snippet.getString("publishedAt");
                Log.d(TAG, "id = "+id + ", title = " + title + "uploaded = "+uploaded);
                Time t = new Time();
                t.parse3339(uploaded);
                publishedOn = t.toMillis(false);
                // The url link back to YouTube, this checks if it has a mobile url
                // if it doesnt it gets the standard url
                String url = "";

                JSONObject thumnail = snippet.getJSONObject("thumbnails");
                JSONObject defaultThumnail = thumnail.getJSONObject("default");
                JSONObject highThumnail = thumnail.getJSONObject("high");

                String thumbUrl;
                try {
                    thumbUrl = highThumnail.getString("url");
                } catch (JSONException ignore) {
                    thumbUrl = defaultThumnail.getString("url");
                }
                Log.d(TAG, "thumbUrl = "+thumbUrl);

                entries.add(new Entry(id, title, url, publishedOn,thumbUrl, 0)); // installed as default false;
            }
        }catch (IOException e) {
            Log.e(TAG, "Feck", e);
        }        catch (JSONException e) {
            Log.e(TAG,"Feck", e);
        }
        return entries;
    }

    /**
     * Given a string representation of a URL, sets up a connection and gets an input stream.
     */
    public static InputStream downloadUrl(final URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(NET_READ_TIMEOUT_MILLIS /* milliseconds */);
        conn.setConnectTimeout(NET_CONNECT_TIMEOUT_MILLIS /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        return conn.getInputStream();
    }

    /**
     * A helper method to convert an InputStream into a String
     * @param inputStream
     * @return the String or a blank string if the IS was null
     * @throws IOException
     */
    public  String convertToString(InputStream inputStream) throws IOException {

        BufferedReader streamReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        StringBuilder responseStrBuilder = new StringBuilder();
        String inputStr;

        while ((inputStr = streamReader.readLine()) != null)
            responseStrBuilder.append(inputStr);

        return responseStrBuilder.toString();

//        if (inputStream != null) {
//            Writer writer = new StringWriter();
//
//            char[] buffer = new char[1024];
//            try {
//                Reader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 1024);
//                int n;
//                while ((n = reader.read(buffer)) != -1) {
//                    writer.write(buffer, 0, n);
//                }
//            }finally {
//                inputStream.close();
//            }
//            return writer.toString();
//        } else {
//            return "";
//        }
    }

    /**
     * A helper method to convert an InputStream into a json
     * @param inputStream
     * @return the String or a blank string if the IS was null
     * @throws IOException
     */
    public  JSONObject convertToJson(InputStream inputStream) throws IOException, JSONException {
        BufferedReader streamReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        StringBuilder responseStrBuilder = new StringBuilder();

        String inputStr;
        while ((inputStr = streamReader.readLine()) != null)
            responseStrBuilder.append(inputStr);

        return new JSONObject(responseStrBuilder.toString());
    }

    public static String getmSearchData() {
        return mSearchData;
    }

    public static void setmSearchData(String mSearchData) {
        FeedParser.mSearchData = mSearchData;
    }

    public static class Entry {
        public final String id;
        public final String title;
        public final String link;
        public final long published;
        public final String thumnail;
        public final int installed;

        Entry(String id, String title, String link, long published, String thumnail, int installed) {
            this.id = id;
            this.title = title;
            this.link = link;
            this.published = published;
            this.thumnail = thumnail;
            this.installed = installed;
        }
    }
}

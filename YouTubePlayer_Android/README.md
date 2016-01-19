##Prerequisite
###1. [SmartView SDK Android Library](http://www.samsungdforum.com/AddLibrary/SmartViewDownload):  Android Package(Mobile)
	
	added source /libs/android-msf-api-2.0.13.jar

###2. [universal-image-loader libarary](https://github.com/nostra13/Android-Universal-Image-Loader)   :  for handling image of video thumnails.
	
	added source /libs/universal-image-loader-1.9.jar

###3. YouTube Data API  

1. [YouTube V3 Data AP](https://developers.google.com/youtube/v3/docs/)I
2. [Youtube V3 Data API "Search"](https://developers.google.com/youtube/v3/docs/search) 
3. [Creating a YouTube API Key](http://support.andromo.com/kb/common-questions/creating-a-youtube-api-key)

###4. android permmission
    <!-- Required for fetching feed data. -->
    <uses-permission android:name="android.permission.INTERNET"/>
    <!-- Required because we're use msf lib -->
    <uses-permission android:name="android.permission.CHANGE_WIFI_MULTICAST_STATE"/>
    <!-- Required because we're use msf lib -->
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
 
 

##Required Modification


	You must get Youtube API KEY to test this app.
	refer to below url
	Creating a YouTube API Key
	
	: http://support.andromo.com/kb/common-questions/creating-a-youtube-api-key

###1. Youtube API KEY ​[MUST]

	DownloadTaskLoader.java
	private final String MY_API_KEY = "&key=YOUR_API_KEY";


 
###2. Youtube API [Optional]​

 DownloadTaskLoader.java

 
    private  final String FEED_WHAT = "&part=snippet&q=movie+trailers";
    private  final String FEED_ORDER = "&order=relevance"; //date, rating relevance title videoCount viewCount
    private  final String FEED_MAX = "&maxResults=50"; //

    private  final String FEED_URL = "https://www.googleapis.com/youtube/v3/search?"
            +  FEED_MAX
            +  FEED_ORDER
            +  MY_API_KEY;

 

## Fix Application ID(TV App) and Channel ID

###1. Application ID

	1. Android (Mobile)  
	StreamingGridActivity.java
	private String mApplicationId = "0rLFmRVi9d.youtubetest";
   

	2. Tizen WebApp
	Config.xml
	<tizen:application id="0rLFmRVi9d.youtubetest" package="0rLFmRVi9d" required_version="2.3"/> 



###2. Channel ID

	1. Android (Mobile)  
	StreamingGridActivity.java  
	
	private String mChannelId = "com.samsung.msf.youtubetest";
	
	mApplication = mService.createApplication(mApplicationId, mChannelId);

 
	2. Tizen WebApp 
	main.js
	var mChannel = 'com.samsung.msf.youtubetest';
	
	var channel = service.channel(mChannel);

## Discover : Search devices around your mobile.
1. If you push search button in ActionBar, Start Search API.
2. You can configure the device list to override onFound(), onLost() listener.
3. Search stop API is called when you click a item of devices list.

 


	StreamingGridActivity.java
	
	private void CreateSearchTvDialog()
	{
	    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
	    alertBuilder.setIcon(R.drawable.ic_launcher);
	    alertBuilder.setTitle("Search TVs....");
	
	    //MSF Search
	    mSearch = Service.search(mContext);
	
	    Log.d(TAG, "menu_search : " + mSearch);
	
	    mSearch.setOnServiceFoundListener(
	            new Search.OnServiceFoundListener() {
	                @Override
	                public void onFound(Service service) {
	                    Log.d(TAG, "Search.onFound() service : " + service.toString());
	                    mTVListAdapter.add(service);
	                    mTVListAdapter.notifyDataSetChanged();
	
	                }
	            }
	    );
	
	    mSearch.setOnServiceLostListener(
	            new Search.OnServiceLostListener() {
	                @Override
	                public void onLost(Service service) {
	                    Log.d(TAG, "Search.onLost() service : " + service.toString());
	                    mTVListAdapter.remove(service);
	                    mTVListAdapter.notifyDataSetChanged();
	                }
	            }
	    );
	    mSearch.start();
		}


## Create Channel and launch a TV app.

1. Get a Service instance from devices list.
2. And create a application instance using tv application Id and Channel ID.
3. Now, you are ready to launch the TV app. Call application.connect() for launching tv app.



	StreamingGridActivity.java
	
        alertBuilder.setAdapter(mTVListAdapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSearch.stop();

                        //Save Service
                        mService =mTVListAdapter.getItem(which);

                        if(mApplication == null) {

                            mApplication = mService.createApplication(mApplicationId, mChannelId);
                            addAllListener(mApplication);

                            Log.d(TAG, "createApplications mApplicationId : " + mApplicationId + "mChannelId : " + mChannelId);
                            mApplication.connect(new Result<Client>() {
                                @Override
                                public void onSuccess(Client client) {
                                    mApplication.setDebug(true);
                                    Log.d(TAG, "application.connect onSuccess " + client.toString());
                                }

                                @Override
                                public void onError(com.samsung.Smart View.Error error) {
                                    Log.d(TAG, "application.connect onError " + error.toString());
                                    Toast.makeText(mContext,"Launch TV app error occurs : "+error.toString(),Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                        Log.d(TAG, "onClick : mService = " + mService.toString());

                        mApplication.addOnMessageListener("control_TV", new Application.OnMessageListener() {
                            @Override
                            public void onMessage(Message message) {
                                Log.d(TAG, "addOnMessageListener event control :  " + message.toString());
                            }
                        });

                        mApplication.addOnMessageListener("play_TV", new Application.OnMessageListener() {
                            @Override
                            public void onMessage(Message message) {
                                Log.d(TAG, "addOnMessageListener event PLAY :  " + message.toString());
                            }
                        });

                    }
                });
 

 

##Disconnect
1. Diconnect has "stop" parameter. 
2. you can choose TV app to stop or not.


		StreamingGridActivity.java
		
		public void disconnectTV(boolean stop, boolean destory){
		    if(mApplication != null){
		        mApplication.removeAllListeners();
		        mApplication.disconnect(stop, new Result<Client>() {
		            @Override
		            public void onSuccess(Client client) {
		                mApplication = null;
		                Log.d(TAG,"disconnect - onSuccess : "+ client.toString());
		                Toast.makeText(mContext,"Disconnect succussfully.",Toast.LENGTH_LONG).show();
		            }
		
		            @Override
		            public void onError(Error error) {
		                Log.d(TAG,"disconnect - onError : "+ error.toString());
		            }
		        });
		    }
		    else {
		        if (!destory)
		            Toast.makeText(mContext, "Make connection first", Toast.LENGTH_LONG).show();
		    }
		}
 
 


##Event Handling
1. You can check to connect, disconnect event and to join other devices, also to catch a error.

 


	    StreamingGridActivity.java

	    public void addAllListener(Application app)
        {

        app.setOnConnectListener(new Channel.OnConnectListener() {
            @Override
            public void onConnect(Client client) {
                Log.d(TAG,"onConnect - client : "+ client.toString());
            }
        });
        app.setOnDisconnectListener(new Channel.OnDisconnectListener() {
            @Override
            public void onDisconnect(Client client) {
                Log.d(TAG, "onDisconnect - client : " + client.toString());
                Toast.makeText(mContext,"Disconnect TV",Toast.LENGTH_LONG).show();
            }
        });

        app.setOnClientConnectListener(new Channel.OnClientConnectListener() {
            @Override
            public void onClientConnect(Client client) {
                Log.d(TAG, "onClientConnect - client : " + client.toString());
            }
        });
        app.setOnClientDisconnectListener(new Channel.OnClientDisconnectListener() {
            @Override
            public void onClientDisconnect(Client client) {
                Log.d(TAG, "onClientDisconnect - client : " + client.toString());
            }
        });

        app.setOnErrorListener(new Channel.OnErrorListener() {
            @Override
            public void onError(Error error) {
                Log.d(TAG, "onError - error : " + error.toString());
                Toast.makeText(mContext,"connection error occurs : "+error.toString(),Toast.LENGTH_LONG).show();
            }
        });

        app.setOnReadyListener(new Channel.OnReadyListener() {
            @Override
            public void onReady() {
                Log.d(TAG, "onReady -  : ");
            }
        });
    }
 

## Use YouTube API
1. You have to require a API key
2. And need to parse video data after query to youtube.



		DownloadTaskLoader.java
		
	    private  final String MY_API_KEY = "&key=YOUR_API_KEY";  //need to change OAuth2 token
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
 

and youtube retrun video info data

if we get the videoId then we can make two type of youtube URL
1. "http://www.youtube.com/watch?v="+ "videoId"

2. "http://www.youtube.com/embed/"+ "videoId"


Now, we send videoId to TV app and then TV app makes youtube URL like 2.


## Youtube Video Data

YouTube API v3



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
 

## Communicate with mobile to TV
1. Send youtube data to Tizen TV app


		StreamingGridActivity.java
	    String event = "play";
	
	     JSONObject messageData = new JSONObject();
	     try {
	         messageData.put("videoId",streamingItemInfo.id);
	         messageData.put("videoName",streamingItemInfo.title);
	         messageData.put("videoThumnail",streamingItemInfo.thumnail);
	     } catch (JSONException e) {
	         e.printStackTrace();
	     }
	
	     Log.d(TAG, "application.publish: " + "messageData" + messageData.toString());
	     mApplication.publish(event, messageData.toString());
 
 

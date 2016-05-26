package com.samsung.msf.youtubeplayer.client.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.samsung.msf.youtubeplayer.DownloadTaskLoader;
import com.samsung.msf.youtubeplayer.client.util.FeedParser;
import com.samsung.msf.youtubeplayer.client.util.TestUtil;
import com.samsung.multiscreen.*;
import com.samsung.multiscreen.Error;
import com.samsung.msf.youtubeplayer.client.adapter.StreamingGridViewAdapter;
import com.samsung.msf.youtubeplayer.R;
import com.samsung.multiscreen.util.JSONUtil;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class StreamingGridActivity extends Activity implements LoaderManager.LoaderCallbacks<List<FeedParser.Entry>> {
    private Context mContext;
    private GridView mGridView;
    private StreamingGridViewAdapter mGridViewAdapter;
    private String TAG = "StreamingGridActivity";

    private static final String CONNECT_DEVICE = "connect";
    private static final String DISCONNECT_DEVICE = "disconnect";
    private static final String CONNECTING_DEVICE = "connecting";

    /**
     * Options menu used to populate ActionBar.
     */
    private Menu mOptionsMenu;
    private String mConnectSatus;
    AnimationDrawable  mMenuCast;

    /**
     * for MSF Lib
     */
    private Search mSearch;
    private Service mService;
    private Application mApplication;
    private ArrayList mDeviceList = new ArrayList();
    private SimpleAdapter mTVListAdapter;
    private ArrayList<String> mDeviceNames = new ArrayList<String>();
    private List<Map<String,String>> mDeviceInfos = new ArrayList<Map<String,String>>();
    /**
     * reserved tv webapp. see tizen config.xml
     */
    private String mApplicationId = "0rLFmRVi9d.youtubetest";
    private String mChannelId = "com.samsung.msf.youtubetest";

    private String mTVModel;

    private boolean mFullScreen;
    @Override
    public Loader<List<FeedParser.Entry>> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader ");
        DownloadTaskLoader loader = new DownloadTaskLoader(this);
        if(!loader.isAPIKeyEnable()){
            Toast.makeText(mContext, "Set Your API KEY ", Toast.LENGTH_LONG).show();
            loader.stopLoading();
            return null;
        }
        if(!isNetworkEnable()){
            Toast.makeText(mContext,"Check Your Network.",Toast.LENGTH_LONG).show();
            loader.stopLoading();
            return null;
        }
        loader.forceLoad();
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<List<FeedParser.Entry>> loader, List<FeedParser.Entry> data) {
        Log.d(TAG, "onLoadFinished ");
        for(FeedParser.Entry e : data) {
            mGridViewAdapter.addItem(e);
        }
        mGridViewAdapter.notifyDataSetChanged();
        //setRefreshActionButtonState(false);

    }

    @Override
    public void onLoaderReset(Loader<List<FeedParser.Entry>> loader) {
        Log.d(TAG, "onLoaderReset ");
    }

    public boolean onQueryTextChanged(String newText) {
        // Called when the action bar search text has changed.  Update
        // the search filter, and restart the loader to do a new query
        // with this filter.

        if("".equals(newText))
            return false;
        else{
            FeedParser.setmSearchData(newText);
            mGridViewAdapter.clear();
            getLoaderManager().restartLoader(0, null, this);
            return true;
        }
    }

    private boolean isNetworkEnable(){
        // Get ConnectivityManager
        ConnectivityManager cm =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get infomation mobile network
        NetworkInfo ni = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        boolean isMobileConn = ni.isConnected();

        // Get infomation wifi network
        ni = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean isWifiConn = ni.isConnected();

        if(isWifiConn || isMobileConn) {
            return true;
        } else {
            return false;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate ");
        mContext = this.getBaseContext();
        setContentView(R.layout.main_activity);


        setConnectionStatus(DISCONNECT_DEVICE);

        getActionBar().setDisplayShowHomeEnabled(false);

         mMenuCast = (AnimationDrawable)getResources().getDrawable(R.drawable.menu_cast);
        mGridView = (GridView) findViewById(R.id.main_grid_view);
        mGridViewAdapter = new StreamingGridViewAdapter(this);
        mGridView.setAdapter(mGridViewAdapter);

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);

        // AUIL - Set ImageLoader config
        initImageLoader(mContext);

        mTVListAdapter = new SimpleAdapter(
                this,
                mDeviceInfos,
                android.R.layout.simple_list_item_2,
                new String[]{"name","type"},
                new int[]{android.R.id.text1,android.R.id.text2 });


        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                if(mGridViewAdapter != null) {
                    Log.d(TAG, "touched position is " + position);
                    FeedParser.Entry  streamingItemInfo = (FeedParser.Entry) mGridViewAdapter.getItem(position);
                    Log.d(TAG, "id is " + streamingItemInfo.id + ", installed is " + streamingItemInfo.installed);
                    mFullScreen = false;
                    //if run search device and choose device.
                    //item starts selected TV
                    //else starts local device.
                    if (mService != null) {
                        //MSF
                        //1.createApplication
                        //  1.1 need to uri , channelId
                        //2.launch : application.connect


                        if(mApplication == null){
                            mApplication = mService.createApplication(mApplicationId, mChannelId);
                            Log.d(TAG, "mApplication 1 : " + mApplication );
                            Log.d(TAG, "createApplications mApplicationId : " + mApplicationId + "mChannelId : " + mChannelId);



                            mApplication.connect(new Result<Client>() {
                                @Override
                                public void onSuccess(Client client) {
                                    mApplication.setDebug(true);
                                    setRefreshActionButtonState(CONNECT_DEVICE);
                                    Log.d(TAG, "application.connect onSuccess " + client.toString());
                                }

                                @Override
                                public void onError(com.samsung.multiscreen.Error error) {
                                    Log.d(TAG, "application.connect onError " + error.toString());

                                }
                            });
                        }

                        String event = "play";
//                        String messageData = "Hello world";
                        JSONObject messageData = new JSONObject();
                        try {
                            messageData.put("videoId",streamingItemInfo.id);
                            messageData.put("videoName",streamingItemInfo.title);
                            messageData.put("videoThumnail",streamingItemInfo.thumnail);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Log.d(TAG, "application.publish: " + "messageData" + messageData.toString());
//                        mApplication.publish(event, messageData.toString());
                        publish(event, messageData);

                        Log.d(TAG, "application.publish: " + "VideoId" + streamingItemInfo.id);

//                            mApplication.publish(event, "https://www.youtube.com/embed/" + streamingItemInfo.streamingVideoId);
//                            Log.d(TAG, "application.publish: " + "https://www.youtube.com/embed/UCOpcACMWblDls9Z6GERVi1A" + streamingItemInfo.streamingVideoId);

//                            mApplication.publish(event, streamingItemInfo.streamingImageUrl);
//                            Log.d(TAG, "application.publish: " + streamingItemInfo.streamingImageUrl);
                    } else {
                        // Get a URI for the selected item, then start an Activity that displays the URI. Any
                        // Activity that filters for ACTION_VIEW and a URI can accept this. In most cases, this will
                        // be a browser.

                        //New Youtube API3.0
                        // + videoId
                        String YoutubeVideo = "http://www.youtube.com/watch?v=" + streamingItemInfo.id;
                        Log.i(TAG, "Opening URL: " + YoutubeVideo);
                        // Get a Uri object for the URL string
                        Uri articleURL = Uri.parse(YoutubeVideo);
                        Intent i = new Intent(Intent.ACTION_VIEW, articleURL);
                        startActivity(i);

                    }
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume ");

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy ");

        if(mSearch != null) {
            mSearch.stop();
            mSearch = null;
        }
        disconnectTV(true, true);
    }

    /**
     * Create the ActionBar.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        mOptionsMenu = menu;
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Respond to user gestures on the ActionBar.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //1. disconnect  TV
            case R.id.menu_disconnect:
                disconnectTV(true,false);
                return true;

            //1. MSF TV Search
            case R.id.menu_search_device:
                if(DISCONNECT_DEVICE.equals(mConnectSatus)){
                    //Start Discover and show device list
                    CreateSearchTvDialog();
                }else if(CONNECT_DEVICE.equals(mConnectSatus)){
                    //Show control and disconnet button
                    CreateDisconnectTvDialogDialog();
                }
                return true;

            // If the user clicks the "Refresh" button.
            case R.id.menu_refresh:
                getLoaderManager().restartLoader(0,null,this);
                //setRefreshActionButtonState(true);
                return true;

            case R.id.menu_control:
                if(mApplication == null) {
                    Toast.makeText(mContext,"Make connection first.",Toast.LENGTH_LONG).show();
                }else{
                    CreateControlDialog();
                }

                return true;

            // . Youtube data  Search
            case R.id.menu_search_data:
                CreateSearchDataDialog();
                return true;

            // . TEST API
            case R.id.menu_start:
                TestUtil.startApplication();

                return true;
            // . TEST API
            case R.id.menu_stop:
                TestUtil.stopApplication();

                return true;
            // . TEST API
            case R.id.menu_getDeviceinfo:
                TestUtil.getInfoDevice();
                return true;
            // . TEST API
            case R.id.menu_getAppinfo:
                TestUtil.getInfoApplication();
                return true;
            // . TEST API
            case R.id.menu_install:
                TestUtil.installApplication();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setRefreshActionButtonState(String  state) {
        if (mOptionsMenu == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            return;
        }

        final MenuItem refreshItem = mOptionsMenu.findItem(R.id.menu_search_device);
        setConnectionStatus(state);
        refreshItem.setIcon(R.drawable.menu_cast);
        AnimationDrawable icon = (AnimationDrawable)refreshItem.getIcon();
        if (refreshItem != null) {
            if (CONNECT_DEVICE.equals(state)) {
                icon.stop();
                refreshItem.setIcon(R.drawable.ic_cast_connected_white_24dp);
            } else if(DISCONNECT_DEVICE.equals(state)){
                icon.stop();
                refreshItem.setIcon(R.drawable.ic_cast_white_24dp);
            } else if(CONNECTING_DEVICE.equals(state)){
                //TO DO : animate actionbar icon
                icon.start();
            }
        }
    }

    private void CreateSearchTvDialog()
    {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setIcon(R.drawable.ic_cast_white_24dp);
        alertBuilder.setTitle(R.string.connect_device);


        //MSF Search
        mSearch = Service.search(mContext);
        mSearch.start();



        Log.d(TAG, "menu_search : " + mSearch);

        mSearch.setOnServiceFoundListener(
                new Search.OnServiceFoundListener() {
                    @Override
                    public void onFound(Service service) {
                        Log.d(TAG, "Search.onFound() service : " + service.toString());
                        if (!mDeviceList.contains(service)) {
                            mDeviceList.add(service);
                            Map<String,String> device = new HashMap<String, String>();
                            device.put("name",service.getName());
                            device.put("type",service.getType());
                            mDeviceInfos.add(device);
//                            mTVListAdapter.add(service.getName());
                            mTVListAdapter.notifyDataSetChanged();
                        }

                    }
                }
        );

        mSearch.setOnServiceLostListener(
                new Search.OnServiceLostListener() {
                    @Override
                    public void onLost(Service service) {
                        Log.d(TAG, "Search.onLost() service : " + service.toString());
                        mDeviceList.remove(service);
                        mDeviceInfos.remove(service.getName());
                        mTVListAdapter.notifyDataSetChanged();
                    }
                }
        );

        //You can connect getByURI,getById
        /*
        Service.getByURI(Uri.parse("http://192.168.0.68:8001/api/v2/"), new Result<Service>() {
            @Override
            public void onSuccess(Service service) {
                mService = service;
                mApplication = mService.createApplication(mApplicationId, mChannelId);
                addAllListener(mApplication);
                mApplication.connect(new Result<Client>() {
                    @Override
                    public void onSuccess(Client client) {
                        mApplication.setDebug(true);
                        Log.d(TAG, "application.connect onSuccess " + client.toString());
                    }

                    @Override
                    public void onError(com.samsung.multiscreen.Error error) {
                        Log.d(TAG, "application.connect onError " + error.toString());
                        Toast.makeText(mContext, "Launch TV app error occurs : " + error.toString(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(Error error) {
                Log.d(TAG, "Service.getById  onError : ");
            }
        });

        Service.getById(mContext, "uuid:8304f069-639d-41a7-afcd-6bf8160dd88e", new Result<Service>() {
            @Override
            public void onSuccess(Service service) {
                mService = service;
                mApplication = mService.createApplication(mApplicationId, mChannelId);
                addAllListener(mApplication);
                mApplication.connect(new Result<Client>() {
                    @Override
                    public void onSuccess(Client client) {
                        mApplication.setDebug(true);
                        Log.d(TAG, "application.connect onSuccess " + client.toString());
                    }

                    @Override
                    public void onError(com.samsung.multiscreen.Error error) {
                        Log.d(TAG, "application.connect onError " + error.toString());
                        Toast.makeText(mContext, "Launch TV app error occurs : " + error.toString(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(Error error) {
                Log.d(TAG, "Service.getById  onError : ");
            }
        });
        */

        alertBuilder.setAdapter(mTVListAdapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setRefreshActionButtonState(CONNECTING_DEVICE);
                        mSearch.stop();
                        mFullScreen = false;
                        //Save Service
                        //String cName = mTVListAdapter.getItem(which).getClass().getName();
                        String cName = mDeviceList.get(which).getClass().getName();
                        Log.d(TAG, "cName : " + cName);
                        Log.d(TAG, "mService : " + Service.class.getName());

                        mService = (Service) mDeviceList.get(which);

                        if(mApplication == null) {
                            Map<String,Object> startArgs1 = new HashMap<String, Object>();
                            Map<String,Object> startArgs2 = new HashMap<String, Object>();
                            Object payload = "aaa";
                            startArgs1.put(Message.PROPERTY_MESSAGE_ID,payload);


                            JSONObject test2 = new JSONObject();
                            try {
                                test2.put("userID","XX");
                                test2.put("userPW","1234");
                                test2.put("pairCode","XX@1234");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            payload = test2.toString();

                            startArgs2.put(Message.PROPERTY_MESSAGE_ID, payload);


                            Log.d(TAG,"startArgs1 = "+startArgs1);
                            Log.d(TAG,"startArgs2 = "+startArgs2);
                            mApplication = mService.createApplication(mApplicationId, mChannelId,startArgs2);

                            mApplication.getInfo(new Result<ApplicationInfo>() {
                                @Override
                                public void onSuccess(ApplicationInfo applicationInfo) {
                                    Log.d(TAG, "getInfo " + applicationInfo.toString());
                                }

                                @Override
                                public void onError(Error error) {
                                    Log.d(TAG, "getInfo onError " + error.getCode());

                                }
                            });
                            addAllListener(mApplication);

                            Log.d(TAG, "createApplications mApplicationId : " + mApplicationId + "mChannelId : " + mChannelId);
                            mApplication.connect(new Result<Client>() {
                                @Override
                                public void onSuccess(Client client) {
                                    mApplication.setDebug(true);
                                    Log.d(TAG, "application.connect onSuccess " + client.toString());
                                }

                                @Override
                                public void onError(com.samsung.multiscreen.Error error) {
                                    Log.d(TAG, "application.connect onError " + error.toString());
                                    setRefreshActionButtonState(DISCONNECT_DEVICE);
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
                                CreateControlDialog();
                                Log.d(TAG, "addOnMessageListener event PLAY :  " + message.toString());
                            }
                        });

                    }
                });

        alertBuilder.show();

    }

    private void CreateDisconnectTvDialogDialog() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setIcon(R.drawable.ic_cast_connected_white_24dp);
        alertBuilder.setTitle(getDeviceName());

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.disconnect_dialog, (ViewGroup) findViewById(R.id.disconnect_root));

        // set dialog message
        alertBuilder.setNeutralButton(R.string.disconnect_device, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "CreateDisconnectTvDialogDialog onClick   ");

                disconnectTV(true, false);

            }
        });

//        alertBuilder.setView(layout);
        alertBuilder.show();
    }
    private void  CreateControlDialog(){
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setIcon(R.drawable.ic_sysbar_quicksettings);
        alertBuilder.setTitle("Control");

        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.control_dialog, (ViewGroup) findViewById(R.id.control_root));

        alertBuilder.setView(layout);
        alertBuilder.show();
    }

    private void  CreateSearchDataDialog(){
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
//        alertBuilder.setIcon(R.drawable.ic_sysbar_quicksettings);
//        alertBuilder.setTitle("Control");

        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.search_data_dialog, (ViewGroup) findViewById(R.id.layout_search));


        alertBuilder.setView(layout);

        final EditText userInput = (EditText) layout
                .findViewById(R.id.editTextDialogUserInput);

        // set dialog message
        alertBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // get user input and set it to result
                                // edit text
                                //Search data from userInput.getText()

                                onQueryTextChanged(userInput.getText().toString());
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });


        alertBuilder.show();


    }
    public void onControlbuttonClick(View v){
        Log.d(TAG, "onControlbuttonClick :  ");
        if(mApplication != null) {
            Log.d(TAG, "control    addOnMessageListener");

            switch (v.getId()) {
                case R.id.stop:
                    mApplication.publish("control", "stop");
                    return;

                case R.id.pause:
                    mApplication.publish("control", "pause");
                    return;

                case R.id.play:
                    mApplication.publish("control", "play");
                    return;

                case R.id.ff:
                    mApplication.publish("control", "ff");
                    return;

                case R.id.rew:
                    mApplication.publish("control", "rew");
                    return;

                case R.id.fullscreen:

                    if(mFullScreen) {
                        mFullScreen = false;
                        mApplication.publish("control", "originScreen");

                    } else {
                        mFullScreen = true;
                        mApplication.publish("control", "fullScreen");
                    }
                    return;

            }
        }
    }

    public void initImageLoader(Context context){
        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
        config.threadPriority(Thread.NORM_PRIORITY -2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .diskCacheSize(3*1024*1024)
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .writeDebugLogs();

        ImageLoader.getInstance().init(config.build());
    }

    public void disconnectTV(boolean stop, boolean destory){

        if (CONNECT_DEVICE.equals(mConnectSatus)) {
            Log.d(TAG, "disconnect  status = " + mConnectSatus.toString());
            setRefreshActionButtonState(DISCONNECT_DEVICE);
            mApplication.disconnect(stop, new Result<Client>() {
                @Override
                public void onSuccess(Client client) {
                    Log.d(TAG, "disconnect - onSuccess : " + client.toString());
                    Toast.makeText(mContext, "Disconnect succussfully.", Toast.LENGTH_LONG).show();
                    if(mApplication != null) {
                        //mApplication.removeAllListeners();
                        mService = null;
                        mApplication = null;
                    }

                }

                @Override
                public void onError(Error error) {
                    Log.d(TAG, "disconnect - onError : " + error.toString());
                }
            });
        } else {
            if (!destory)
                Toast.makeText(mContext, "Make connection first", Toast.LENGTH_LONG).show();
        }
    }


    public void addAllListener(Application app) {

        app.setOnConnectListener(new Channel.OnConnectListener() {
            @Override
            public void onConnect(Client client) {
                Log.d(TAG,"onConnect - client : "+ client.toString());
                setRefreshActionButtonState(CONNECT_DEVICE);
            }
        });
        app.setOnDisconnectListener(new Channel.OnDisconnectListener() {
            @Override
            public void onDisconnect(Client client) {
                Log.d(TAG, "onDisconnect - client : " + client.toString());
                Toast.makeText(mContext,"Disconnect TV from onDisconnect",Toast.LENGTH_LONG).show();
                setRefreshActionButtonState(DISCONNECT_DEVICE);
                mService = null;
                mApplication = null;
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

    public void setConnectionStatus(String status){
        mConnectSatus = status;
    }

    private String getDeviceName(){
        if(mService != null){
            return mService.getName();
        }
        return  "";
    }

}
package com.samsung.msf.youtubeplayer.client.util;



import android.net.Uri;
import android.util.Log;

import com.samsung.multiscreen.*;
import com.samsung.multiscreen.Error;

public class TestUtil {
    private static final String TAG = "TestUtil";
    private static final String TEST_URI = "http://192.168.0.75:8001/api/v2/";
    private static String TEST_APP_ID = "0rLFmRVi9d.youtubetest";
    private static String TEST_CHANNEL_ID = "com.samsung.msf.youtubetest";
    private static Service mService;
    private static Channel mChannel;
    private static Application mApplication;
    private static String text = "";


    public static void startApplication(){
        Log.d(TAG, "startApplication()");
        Uri uri = Uri.parse(TEST_URI);
        Service.getByURI(uri, new Result<Service>() {
            @Override
            public void onSuccess(Service service) {
                Log.d(TAG, "Get get Service Success");
//                Toast.makeText(mContext, "Get get Service Success", Toast.LENGTH_SHORT);
                text += "Service Success";
                mService = service;
                mApplication = mService.createApplication(TEST_APP_ID, TEST_CHANNEL_ID);

//                addAllListener(mChannel);
//                addMessageListener(mChannel);

                mApplication.connect(new Result<Client>() {
                    @Override
                    public void onSuccess(Client client) {
                        Log.d(TAG, " Channel Connect Success");
//                        Toast.makeText(mContext," Channel Connect  Success",Toast.LENGTH_SHORT);
                        text += " / Connect  Success";
                    }

                    @Override
                    public void onError(Error error) {
                        Log.d(TAG, "Channel Connect Error : " + error.toString());
//                        Toast.makeText(mContext,"Channel Connect Error : "+error.toString(),Toast.LENGTH_LONG);
                        text += " / Connect Error : "+ error.toString();
                    }
                });
            }

            @Override
            public void onError(com.samsung.multiscreen.Error error) {
//                Toast.makeText(mContext,"Get get Service Error : "+error.toString(),Toast.LENGTH_LONG);
            }
        });

    }

    public static void stopApplication() {
        if (mApplication != null) {
            mApplication.removeAllListeners();
            mApplication.disconnect(true, new Result<Client>() {
                @Override
                public void onSuccess(Client client) {
                    mApplication = null;
                    Log.d(TAG, "disconnect - onSuccess : " + client.toString());
//                    Toast.makeText(mContext,"Disconnect succussfully.",Toast.LENGTH_LONG).show();
                }

                @Override
                public void onError(Error error) {
                    Log.d(TAG,"disconnect - onError : "+ error.toString());
                }
            });

        }
    }

    public static void getInfoDevice(){
        if(mApplication != null){
            mService.getDeviceInfo(new Result<Device>() {
                @Override
                public void onSuccess(Device device) {
                    Log.d(TAG, "getInfoDevice - onSuccess : " + device.toString());
                }
                @Override
                public void onError(Error error) {
                    Log.d(TAG, "getInfoDevice - onError : " + error.toString());
                }
            });
        }
    }
    public static void getInfoApplication(){
        if(mApplication != null){
            mApplication.getInfo(new Result<ApplicationInfo>() {
                @Override
                public void onSuccess(ApplicationInfo applicationInfo) {
                    Log.d(TAG, "getInfoApplication - onSuccess : " + applicationInfo.toString());
                }

                @Override
                public void onError(Error error) {
                    Log.d(TAG,"getInfoApplication - onError : "+ error.toString());
                }
            });
        }
    }



    public static void installApplication(){
        if(mApplication !=null){
            mApplication.install(new Result<Boolean>() {
                @Override
                public void onSuccess(Boolean aBoolean) {
                    Log.d(TAG, "installApplication - onSuccess : " + aBoolean);
                }

                @Override
                public void onError(Error error) {
                    Log.d(TAG,"installApplication - onError : "+ error.toString());
                }
            });
        }

    }

}

package com.mygame.music_alpha;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

public class ApplicationClass extends Application {

    public static final String CHANNEL_ID_1 = "channel1";
    public static final String CHANNEL_ID_2 = "channel2";
    public static final String ACTION_PREVIOUS = "actionPrevious";
    public static final String ACTION_NEXT = "actionNext";
    public static final String ACTION_PLAY = "actionPlay";
    public static final String ACTION_MYSELF = "actionMySelf";
    public static final String ACTION_MODE = "actionMode";
    public static final String ACTION_FAVORITE = "actionFavorite";
    public static final String ACTION_DELETE_MYSELF = "actionDeleteMySelf";
    private static final String TAG = "app";
    private static Activity mActivity;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate()");
        createNotificationChannel();
        registerActivityLifecycleCallbacks(new MyActivityLifecycleCallbacks());

    }

    private void createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel1 = new
                    NotificationChannel(CHANNEL_ID_1, "Channel(1)", NotificationManager.IMPORTANCE_HIGH);
            channel1.setDescription("Channel 1 Dec..");

            NotificationChannel channel2 = new
                    NotificationChannel(CHANNEL_ID_2, "Channel(2)", NotificationManager.IMPORTANCE_HIGH);
            channel2.setDescription("Channel 2 Dec..");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel1);
            notificationManager.createNotificationChannel(channel2);
        }
    }



    @Override
    public void onTerminate (){
        super.onTerminate();
    }

    @Override
    public void onConfigurationChanged (Configuration newConfig){
        super.onConfigurationChanged(newConfig);
    }

    private static final class MyActivityLifecycleCallbacks implements ActivityLifecycleCallbacks {

        public void onActivityCreated(Activity activity, Bundle bundle) {
            Log.e(TAG,"onActivityCreated:" + activity.getLocalClassName());
        }

        public void onActivityDestroyed(Activity activity) {
            Log.e(TAG,"onActivityDestroyed:" + activity.getLocalClassName());
        }

        public void onActivityPaused(Activity activity) {
            Log.e(TAG,"onActivityPaused:" + activity.getLocalClassName());
        }

        public void onActivityResumed(Activity activity) {
            Log.e(TAG,"onActivityResumed:" + activity.getLocalClassName());
        }

        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
//            mActivity = activity;
            Log.e(TAG,"onActivitySaveInstanceState:" + activity.getLocalClassName());
        }

        public void onActivityStarted(Activity activity) {
            mActivity = activity;
            Log.e(TAG,"onActivityStarted:" + activity.getLocalClassName());
        }

        public void onActivityStopped(Activity activity) {
            Log.e(TAG,"onActivityStopped:" + activity.getLocalClassName());
        }

    }

    public static Context getNameActivity() {
        return mActivity;
    }

    public static void setNameActivity(Activity activity) {
        mActivity = activity;
    }

    public static void close() {
        Log.e(TAG,"close:");
        mActivity.finishAndRemoveTask();
    }
}

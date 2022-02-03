package com.mygame.music_alpha;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

import static com.mygame.music_alpha.ApplicationClass.ACTION_DELETE_MYSELF;
import static com.mygame.music_alpha.ApplicationClass.ACTION_FAVORITE;
import static com.mygame.music_alpha.ApplicationClass.ACTION_MODE;
import static com.mygame.music_alpha.ApplicationClass.ACTION_MYSELF;
import static com.mygame.music_alpha.ApplicationClass.ACTION_NEXT;
import static com.mygame.music_alpha.ApplicationClass.ACTION_PLAY;
import static com.mygame.music_alpha.ApplicationClass.ACTION_PREVIOUS;
import static com.mygame.music_alpha.MusicLab.REPEAT;
import static com.mygame.music_alpha.MusicLab.SHUFFLE;

public class NotificationReceiver extends BroadcastReceiver {

    private static final String ACTION_PLAY_ONLY = "actionPlayOnly";

    @Override
    public void onReceive(Context context, Intent intent) {
        String actionName = intent.getAction();
        Intent serviceIntent = new Intent(context, MusicService.class);
        if (intent.getAction().equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
            Log.e("NotificationReceiver", "ACTION_AUDIO_BECOMING_NOISY");
            actionName = ACTION_PLAY_ONLY;
        }
        if(actionName != null){
            switch (actionName){
                case ACTION_PLAY:
                    serviceIntent.putExtra("ActionName", "playPause");
                    context.startService(serviceIntent);
                    break;
                case ACTION_PREVIOUS:
                    serviceIntent.putExtra("ActionName", "previous");
                    context.startService(serviceIntent);
                    break;
                case ACTION_NEXT:
                    serviceIntent.putExtra("ActionName", "next");
                    context.startService(serviceIntent);
                    break;
                case ACTION_MODE:
                    if(REPEAT) {
                        REPEAT = false;
                        SHUFFLE = true;
                    }
                    else if (SHUFFLE) {
                        SHUFFLE = false;
                        REPEAT = false;
                    }
                    else {
                        REPEAT = true;
                        SHUFFLE = false;
                    }
                    serviceIntent.putExtra("ActionName", "mode");
                    context.startService(serviceIntent);
                    break;
                case ACTION_MYSELF:
                    Log.e("NotificationReceiver", "ok, i am here, + " + ApplicationClass.getNameActivity());
                    Intent intentActivity = new Intent(context, ApplicationClass.getNameActivity().getClass());
                    intentActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intentActivity);
                    break;
                case ACTION_DELETE_MYSELF:
                    Log.e("NotificationReceiver", "ACTION_DELETE_MYSELF");
                    context.stopService(serviceIntent);
                    break;
                case ACTION_PLAY_ONLY:
                    Log.e("NotificationReceiver", "ACTION_PLAY_ONLY");
                    serviceIntent.putExtra("ActionName", "onlyPause");
                    context.startService(serviceIntent);
                    break;
                case ACTION_FAVORITE:
                    Log.e("NotificationReceiver", "ACTION_FAVORITE");
                    serviceIntent.putExtra("ActionName", "favorite");
                    context.startService(serviceIntent);
                    break;
            }
        }
    }
}

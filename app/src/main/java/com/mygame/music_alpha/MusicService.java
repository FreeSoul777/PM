package com.mygame.music_alpha;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import static com.mygame.music_alpha.ApplicationClass.ACTION_DELETE_MYSELF;
import static com.mygame.music_alpha.ApplicationClass.ACTION_FAVORITE;
import static com.mygame.music_alpha.ApplicationClass.ACTION_MODE;
import static com.mygame.music_alpha.ApplicationClass.ACTION_MYSELF;
import static com.mygame.music_alpha.ApplicationClass.ACTION_NEXT;
import static com.mygame.music_alpha.ApplicationClass.ACTION_PLAY;
import static com.mygame.music_alpha.ApplicationClass.ACTION_PREVIOUS;
import static com.mygame.music_alpha.ApplicationClass.CHANNEL_ID_2;
import static com.mygame.music_alpha.MusicFragment.KEY_FLAG;
import static com.mygame.music_alpha.MusicFragment.KEY_POSITION_PLAY;
import static com.mygame.music_alpha.MusicLab.POSITION_PLAY;
import static com.mygame.music_alpha.MusicLab.REPEAT;
import static com.mygame.music_alpha.MusicLab.SHUFFLE;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {

    public static boolean IS_LIVE;
    public static boolean PP;
    private static final String TAG = "MusicService";
    public static boolean FLAG = true;

    IBinder mBinder = new MyBinder();
    private static MediaPlayer mediaPlayer;
    private ArrayList<MusicFile> musicFiles;
    private Uri uri;
    private int position = POSITION_PLAY;
    public static ActionPlaying actionPlaying;
    public AudioManager audioManager;
    int result;
    private NotificationReceiver notificationReceiver;
    private TinyDB tinyDB;
    private PowerManager.WakeLock wakelock;
    private int volume;
    private MediaSession mediaSession;

    @SuppressLint("InvalidWakeLockTag")
    @Override
    public void onCreate() {
        super.onCreate();

        mediaPlayer = null;
        tinyDB = new TinyDB(getApplicationContext());
        updateMusicFiles();

        notificationReceiver = new NotificationReceiver();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        result = audioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) return;
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(notificationReceiver, intentFilter);


        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakelock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakelockTag");
        wakelock.acquire();

        mediaSession = new MediaSession(this, TAG);
        mediaSession.setCallback(mediaSessionCallback);
        mediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        PlaybackState state = new PlaybackState.Builder()
                .setActions(PlaybackState.ACTION_PLAY | PlaybackState.ACTION_SKIP_TO_NEXT
                        | PlaybackState.ACTION_PAUSE | PlaybackState.ACTION_SKIP_TO_PREVIOUS
                        | PlaybackState.ACTION_STOP | PlaybackState.ACTION_PLAY_PAUSE)
                .setState(PlaybackState.STATE_PLAYING, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 0)
                .build();
        mediaSession.setPlaybackState(state);
        mediaSession.setActive(true);


        Log.e(TAG, "onCreate() + musicFiles.size()" + musicFiles.size());

    }

    MediaSession.Callback mediaSessionCallback = new MediaSession.Callback() {

        @Override
        public boolean onMediaButtonEvent(Intent mediaButtonIntent) {
            Log.d(TAG, "onMediaButtonEvent called: " + mediaButtonIntent);
            KeyEvent ke = mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (ke != null && ke.getAction() == KeyEvent.ACTION_DOWN) {
                int keyCode = ke.getKeyCode();
                Log.d(TAG, "onMediaButtonEvent Received command: " + ke);
            }
            return super.onMediaButtonEvent(mediaButtonIntent);
        }

        @Override
        public void onPlay() {
            Log.e(TAG, "mediaSessionCallback.onPlay()");
            actionPlaying.playPauseBtnClicked();
            super.onPlay();
        }

        @Override
        public void onPause() {
            Log.e(TAG, "mediaSessionCallback.onPause()");
            actionPlaying.playPauseBtnClicked();
            super.onPause();
        }

        @Override
        public void onSkipToNext() {
            Log.e(TAG, "mediaSessionCallback.onSkipToNext()");
            actionPlaying.nextBtnClicked();
            super.onSkipToNext();
        }

        @Override
        public void onSkipToPrevious() {
            Log.e(TAG, "mediaSessionCallback.onSkipToPrevious()");
            actionPlaying.prevBtnClicked();
            super.onSkipToPrevious();
        }

        @Override
        public void onSeekTo(long position) {
            mediaPlayer.seekTo((int) position);
            showNotification(isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play,
                    isPlaying() ? 1F : 0F);
            super.onSeekTo(position);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseMP();
        if(notificationReceiver != null) {
            stopForeground(false);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.cancel(2);
            unregisterReceiver(notificationReceiver);
            Log.e(TAG, "unregisterReceiver(notificationReceiver)");
        }
        if(audioManager != null) {
            audioManager.abandonAudioFocus(audioFocusChangeListener);
            Log.e(TAG, "audioManager.abandonAudioFocus(this)");
        }
        if(wakelock != null) {
            wakelock.release();
            Log.e(TAG, " wakelock.release()");
        }
        if(mediaSession != null) {
            mediaSession.release();
            Log.e(TAG, " mediaSession.release()");
        }
        MiniPlayer.musicService = null;
        Log.e(TAG, "musicService = null");
        tinyDB.putInt(KEY_POSITION_PLAY, POSITION_PLAY);
        Log.e(TAG, "onDestroy()");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    public class MyBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int myPosition = intent.getIntExtra("servicePosition", -1);
        String actionName = intent.getStringExtra("ActionName");
        Log.e(TAG, "onStartCommand");
        if(myPosition != -1){
            playMedia(myPosition);
        }
        if(actionName != null){
            switch (actionName){
                case "playPause":
                    Toast.makeText(this, "PlayPause", Toast.LENGTH_SHORT).show();
                    actionPlaying.playPauseBtnClicked();
                    break;
                case "next":
                    Toast.makeText(this, "Next", Toast.LENGTH_SHORT).show();
                    actionPlaying.nextBtnClicked();
                    break;
                case "previous":
                    Toast.makeText(this, "Previous", Toast.LENGTH_SHORT).show();
                    actionPlaying.prevBtnClicked();
                    break;
                case "favorite":
                    Toast.makeText(this, "Favorite", Toast.LENGTH_SHORT).show();
                    MusicLab.get(getApplicationContext()).tabFavorite(musicFiles.get(POSITION_PLAY));
                    Player.updateRS();
                    showNotification(isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play,
                            isPlaying() ? 1F : 0F);
                    break;
                case "onlyPause":
                    Toast.makeText(this, "onlyPause", Toast.LENGTH_SHORT).show();
                    if(actionPlaying != null) {
                        if(mediaPlayer.isPlaying()) {
                            Log.e("Inside", "Action, onlyPause");
                            actionPlaying.playPauseBtnClicked();
                        }
                    }
                    break;
                case "mode":
                    Player.updateRS();
                    showNotification(isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play,
                            isPlaying() ? 1F : 0F);
            }
        }
        return START_STICKY;
    }

    public void createMediaPlayer(int positionInner) {
        Log.e(TAG, "createMediaPlayer");
        position = positionInner;
        uri = Uri.parse(musicFiles.get(position).getPath());
        MusicLab.get(getApplicationContext()).setSongPath(musicFiles.get(position).getPath());
        updateNotifyItemAll(musicFiles.get(position).getPath());
        mediaPlayer = null;
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(getApplicationContext(), uri);
            Log.e(TAG, "setDataSource");
        } catch (IOException ioException) {
            ioException.printStackTrace();
            Log.e(TAG, "Не возможно воспроизвести");
        }
        Log.i(TAG, "prepareAsync");
        prepare();
    }

    public void updateNotifyItemAll(String path) {
        SongFragment.updateNotifyItem(path);
        ArtistAlbumDetails.updateNotifyItem(path);
        PlaylistDetails.updateNotifyItem(path);
        Queue.updateNotifyItem(path);
    }

    private void playMedia(int startPosition) {
        updateMusicFiles();
        Log.e(TAG, "onPlayMedia(), musicFiles.size() - " + musicFiles.size());
        position = startPosition;
        if(mediaPlayer != null) {
            mediaPlayer.stop();
            releaseMP();
        }
        if(musicFiles != null) {
            createMediaPlayer(position);
//            mediaPlayer.start();
        }
    }

    public void prepare() {
        try {
            Log.e(TAG, "PREPARE()");
            mediaPlayer.prepare();
        } catch (IOException ioException) {
            ioException.printStackTrace();
            Log.e(TAG, "ERROR Prepare!");
        }
    }
    public void start() {
        mediaPlayer.start();
    }
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }
    public void stop() {
        mediaPlayer.stop();
    }
    public void pause() {
        mediaPlayer.pause();
    }
    public void release() {
        releaseMP();
    }
    public int getDuration() {
        return mediaPlayer.getDuration();
    }
    public void seekTo(int position) {
        mediaPlayer.seekTo(position);
    }
    public int getCurrentPosition() {
//        Log.i(TAG, "getCurrentPosition");
        return mediaPlayer.getCurrentPosition();
    }
    public int getAudioSessionId() {
        return mediaPlayer.getAudioSessionId();
    }

    public void setCallBack(ActionPlaying actionPlaying){
        this.actionPlaying = actionPlaying;
    }

    @SuppressLint("WrongConstant")
    void showNotification(int playPauseBtn, float playbackSpeed){
        position = POSITION_PLAY;
        Intent contentIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_MYSELF);
        PendingIntent contentPending = PendingIntent.getBroadcast(this,0, contentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Intent deleteIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_DELETE_MYSELF);
        PendingIntent deletePending = PendingIntent.getBroadcast(this,0, deleteIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Intent prevIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_PREVIOUS);
        PendingIntent prevPending = PendingIntent.getBroadcast(this,0, prevIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pauseIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_PLAY);
        PendingIntent pausePending = PendingIntent.getBroadcast(this,0, pauseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_NEXT);
        PendingIntent nextPending = PendingIntent.getBroadcast(this,0, nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Intent modeIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_MODE);
        PendingIntent modePending = PendingIntent.getBroadcast(this,0, modeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Intent favoriteIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_FAVORITE);
        PendingIntent favoritePending = PendingIntent.getBroadcast(this,0, favoriteIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);


        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(musicFiles.get(POSITION_PLAY).getPath());
        byte[] art = retriever.getEmbeddedPicture();
        Bitmap thumb;
        if(art != null){
            thumb = BitmapFactory.decodeByteArray(art, 0, art.length);
        }
        else{
            thumb = BitmapFactory.decodeResource(getResources(), R.drawable.cover_art_2);
        }

        boolean isGoing = playPauseBtn == R.drawable.ic_pause;
        PP = !isGoing;

        int mode = REPEAT ? R.drawable.ic_repeat_one_white :
                (SHUFFLE ? R.drawable.ic_shuffle_white :
                        R.drawable.ic_repeat);
        int favorite = musicFiles.get(POSITION_PLAY).getFavorite() ?
                R.drawable.ic_favorite_red : R.drawable.ic_favorite;

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID_2)
                .setSmallIcon(playPauseBtn)
                .setLargeIcon(thumb)
                .setContentTitle(musicFiles.get(POSITION_PLAY).getTitle())
                .setContentText(musicFiles.get(POSITION_PLAY).getArtist())
                .addAction(mode, "Mode", modePending)
                .addAction(R.drawable.ic_skip_previous, "Previous", prevPending)
                .addAction(playPauseBtn, "Pause", pausePending)
                .addAction(R.drawable.ic_skip_next, "Next", nextPending)
                .addAction(favorite, "Favorite", favoritePending)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(MediaSessionCompat.Token.fromToken(mediaSession.getSessionToken())))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(contentPending)
                .setDeleteIntent(deletePending)
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mediaSession.setMetadata(new MediaMetadata.Builder()
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mediaPlayer.getDuration())
                    .build());
            mediaSession.setPlaybackState(new PlaybackState.Builder()
                    .setState(PlaybackStateCompat.STATE_PLAYING, mediaPlayer.getCurrentPosition(), playbackSpeed)
                    .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                    .build());
        }

        startForeground(2, notification);
        if(!isGoing && IS_LIVE) {
            stopForeground(false);
        }
    }

    void OnCompleted(){
        mediaPlayer.setOnCompletionListener(this);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.e(TAG, "onCompletion");
        actionPlaying.nextBtnClicked();
        if(mediaPlayer != null){
            Log.e(TAG, " ok");
            mediaPlayer.start();
            OnCompleted();
        }
    }

    private void releaseMP() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();

                mediaPlayer.release();
                mediaPlayer = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void getVolume() {
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                volume, 0);
    }

    private void setVolume() {
        volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

//    @Override
//    public void onAudioFocusChange(int focusChange) {
//        switch (focusChange) {
//            case AudioManager.AUDIOFOCUS_GAIN:
//                // resume playback
//                if (mediaPlayer == null) createMediaPlayer(position);
//                else if (!mediaPlayer.isPlaying()) mediaPlayer.start();
//                mediaPlayer.setVolume(1.0f, 1.0f);
//                break;
//
//            case AudioManager.AUDIOFOCUS_LOSS:
//                // Lost focus for an unbounded amount of time: stop playback and release media player
//                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
//                releaseMP();
//                mediaPlayer = null;
//                break;
//
//            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
//                // Lost focus for a short time, but we have to stop
//                // playback. We don't release the media player because playback
//                // is likely to resume
//                if (mediaPlayer.isPlaying()) mediaPlayer.pause();
//                break;
//
//            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
//                // Lost focus for a short time, but it's ok to keep playing
//                // at an attenuated level
//                if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
//                break;
//        }
//    }

    AudioManager.OnAudioFocusChangeListener audioFocusChangeListener =
            new AudioManager.OnAudioFocusChangeListener() {
        boolean flag = false;

        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS: {
                    setVolume();
                    flag = mediaPlayer.isPlaying();
                    if (mediaPlayer.isPlaying()) {
                        actionPlaying.playPauseBtnClicked();
                    }
                    Log.e("onAudioFocusChange", "AUDIOFOCUS_LOSS");
                    break;
                }
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: {
                    setVolume();
                    flag = mediaPlayer.isPlaying();
                    if (mediaPlayer.isPlaying()) {
                        actionPlaying.playPauseBtnClicked();
                    }
                    Log.e("onAudioFocusChange", "AUDIOFOCUS_LOSS_TRANSIENT");
                    break;
                }
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: {
                    if (mediaPlayer != null) {
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.setVolume(0.3f, 0.3f);
                        }
                    }
                    Log.e("onAudioFocusChange", "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                    break;
                }
                case AudioManager.AUDIOFOCUS_GAIN: {
                    if (mediaPlayer == null) createMediaPlayer(position);
                    if(flag) {
                        actionPlaying.playPauseBtnClicked();
                        getVolume();
                    }
                    mediaPlayer.setVolume(1.0f, 1.0f);
                    Log.e("onAudioFocusChange", "AUDIOFOCUS_GAIN");
                    break;
                }
            }
        }
    };


    public static boolean is_play() {
        if(mediaPlayer != null) {
            Log.e(TAG, "mediaPlayer != null");
            return mediaPlayer.isPlaying();
        }
        else {
            Log.e(TAG, "mediaPlayer == null");
            return false;
        }
    }


    public void playPauseBtnClicked() {
        Log.e("Inside", "Action, PlayPause");
        if(mediaPlayer.isPlaying()) {
            FLAG = false;
            showNotification(R.drawable.ic_play, 0F);
            mediaPlayer.pause();
        }
        else {
            FLAG = true;
            showNotification(R.drawable.ic_pause, 1F);
            mediaPlayer.start();
        }
        tinyDB.putBoolean(KEY_FLAG, FLAG);
    }

    public void prevBtnClicked() {
        Log.e("Inside", "Action, Previous");
        if(mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            if(SHUFFLE && !REPEAT){
                POSITION_PLAY = getRandom(musicFiles.size() - 1);
            }
            else if (!SHUFFLE && REPEAT) {
                POSITION_PLAY = ((POSITION_PLAY - 1) < 0 ? (musicFiles.size() - 1) : (POSITION_PLAY - 1));
            }
            else if(SHUFFLE && REPEAT) {
                POSITION_PLAY = getRandom(musicFiles.size() - 1);
            }
            else if(!SHUFFLE && !REPEAT){
                POSITION_PLAY = ((POSITION_PLAY - 1) < 0 ? (musicFiles.size() - 1) : (POSITION_PLAY - 1));
            }
            createMediaPlayer(POSITION_PLAY);
            OnCompleted();
            showNotification(R.drawable.ic_pause, 1F);
            mediaPlayer.start();
        }
        else {
            mediaPlayer.stop();
            mediaPlayer.release();
            if(SHUFFLE && !REPEAT){
                POSITION_PLAY = getRandom(musicFiles.size() - 1);
            }
            else if(!SHUFFLE && !REPEAT){
                POSITION_PLAY = ((POSITION_PLAY - 1) < 0 ? (musicFiles.size() - 1) : (POSITION_PLAY - 1));
            }
            createMediaPlayer(POSITION_PLAY);
            OnCompleted();
            if(FLAG) {
                showNotification(R.drawable.ic_pause, 1F);
            } else {
                showNotification(R.drawable.ic_play, 0F);
            }
        }
        tinyDB.putInt(KEY_POSITION_PLAY, POSITION_PLAY);
    }

    public void nextBtnClicked() {
        Log.e("Inside", "Action, Next");
        if(mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            if(SHUFFLE && !REPEAT){
                POSITION_PLAY = getRandom(musicFiles.size() - 1);
            }
            else if (!SHUFFLE && REPEAT) {
                POSITION_PLAY = ((POSITION_PLAY + 1) % musicFiles.size());
            }
            else if (SHUFFLE && REPEAT) {
                POSITION_PLAY = ((POSITION_PLAY + 1) % musicFiles.size());
            }
            else if(!SHUFFLE && !REPEAT){
                POSITION_PLAY = ((POSITION_PLAY + 1) % musicFiles.size());
            }
            createMediaPlayer(POSITION_PLAY);
            OnCompleted();
            showNotification(R.drawable.ic_pause, 1F);
            mediaPlayer.start();
        }
        else {
            mediaPlayer.stop();
            mediaPlayer.release();
            if(SHUFFLE && !REPEAT){
                POSITION_PLAY = getRandom(musicFiles.size() - 1);
            }
            else if(!SHUFFLE && !REPEAT){
                POSITION_PLAY = ((POSITION_PLAY + 1) % musicFiles.size());
            }
            createMediaPlayer(POSITION_PLAY);
            OnCompleted();
            if(FLAG) {
                showNotification(R.drawable.ic_pause, 1F);
            } else {
                showNotification(R.drawable.ic_play, 0F);
            }
        }
        tinyDB.putInt(KEY_POSITION_PLAY, POSITION_PLAY);
    }

    private int getRandom(int i) {
        Random random = new Random();
        return random.nextInt(i + 1);
    }

    public void updateMusicFiles() {
        musicFiles = MusicLab.get(getApplicationContext()).getNowPlaying();
    }

}

package com.mygame.music_alpha;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.request.RequestOptions;
import com.gauravk.audiovisualizer.visualizer.BarVisualizer;

import java.util.ArrayList;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.gpu.BrightnessFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.ContrastFilterTransformation;

import static com.mygame.music_alpha.DialogMenuMore.RES;
import static com.mygame.music_alpha.MiniPlayer.musicService;
import static com.mygame.music_alpha.MusicFragment.KEY_POSITION_PLAY;
import static com.mygame.music_alpha.MusicFragment.KEY_QUEUE;
import static com.mygame.music_alpha.MusicFragment.KEY_REPEAT;
import static com.mygame.music_alpha.MusicFragment.KEY_SHUFFLE;
import static com.mygame.music_alpha.MusicLab.POSITION_PLAY;
import static com.mygame.music_alpha.MusicLab.POSITION_PLAY_NOW;
import static com.mygame.music_alpha.MusicLab.REPEAT;
import static com.mygame.music_alpha.MusicLab.SHUFFLE;
import static com.mygame.music_alpha.MusicLab.SINGLE;
import static com.mygame.music_alpha.MusicService.FLAG;
import static com.mygame.music_alpha.SongFragment.DIALOG_MENU_MORE;
import static com.mygame.music_alpha.SongFragment.FOR_RESULT;

public class Player extends AppCompatActivity implements ActionPlaying, ServiceConnection {

    public static final String TAG = "Player";

    private TextView song_name, artist_name, duration_played, duration_total;
    private ImageView cover_art, nextBtn, prevBtn, backBtn, playPauseBtn, background_player, menu;
    private static ImageView shuffleBtn, favoriteBtn;
    private SeekBar seekBar, seekBar_volume;
    private BarVisualizer visualizer;
    private int position;
    private static ArrayList<MusicFile> ListSongs;
    private Uri uri;
    private AudioManager audioManager;
    private Handler handler = new Handler();
    private boolean shuffleBoolean = false;
    private boolean repeatBoolean = false;
    private Thread playThread, prevThread, nextThread;
    private TinyDB tinyDB;
    private Runnable runnable;
    private static boolean is_live_player_activity = false;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        setContentView(R.layout.activity_player);
        getSupportActionBar().hide();
        tinyDB = new TinyDB(getApplicationContext());
        context = getApplicationContext();

        song_name = findViewById(R.id.song_name);
        artist_name = findViewById(R.id.song_artist);
        duration_played = findViewById(R.id.durationPlayed);
        duration_total = findViewById(R.id.durationTotal);
        cover_art = findViewById(R.id.cover_art);
        nextBtn = findViewById(R.id.id_next);
        prevBtn = findViewById(R.id.id_prev);
        backBtn = findViewById(R.id.back_btn);
        shuffleBtn = findViewById(R.id.id_shuffle);
        favoriteBtn = findViewById(R.id.id_favorite);
        seekBar = findViewById(R.id.seekbar);
        seekBar_volume = findViewById(R.id.seekbar_volume);
        playPauseBtn = findViewById(R.id.play_pause);
        visualizer = findViewById(R.id.blast);
        background_player = findViewById(R.id.background_player);
        menu = findViewById(R.id.menu_btn);

        getIntentMethod();

        runnable = new Runnable() {
            @Override
            public void run() {
                if(musicService != null) {
                    int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPosition);
                    duration_played.setText(formattedTime(mCurrentPosition));
                }
                handler.postDelayed(runnable, 500);
            }
        };
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(musicService != null && fromUser) {
                    musicService.seekTo(progress * 1000);
                    musicService.showNotification(musicService.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play,
                            musicService.isPlaying() ? 1F : 0F);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                int mode = REPEAT ? R.drawable.ic_repeat_one :
                        (SHUFFLE ? R.drawable.ic_shuffle_red :
                                R.drawable.ic_repeat);
                shuffleBtn.setImageResource(mode);
                if(musicService != null) {
                    if(musicService.isPlaying()) {
                        musicService.showNotification(R.drawable.ic_pause, 1F);
                    } else {
                        musicService.showNotification(R.drawable.ic_play, 0F);
                    }
                }
            }
        });
        favoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicLab.get(context).tabFavorite(ListSongs.get(POSITION_PLAY));
                int favorite = ListSongs.get(POSITION_PLAY).getFavorite() ?
                        R.drawable.ic_favorite_red : R.drawable.ic_favorite;
                favoriteBtn.setImageResource(favorite);
                if(musicService != null) {
                    if(musicService.isPlaying()) {
                        musicService.showNotification(R.drawable.ic_pause, 1F);
                    } else {
                        musicService.showNotification(R.drawable.ic_play, 0F);
                    }
                }
            }
        });
        backBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                finish();
            }
        });
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicFile musicFile = ListSongs.get(POSITION_PLAY);
                FragmentManager manager = getSupportFragmentManager();
                DialogMenuMore dialog = DialogMenuMore.newInstance(getS(), musicFile.getTitle());
                manager.setFragmentResultListener(FOR_RESULT, Player.this, new FragmentResultListener() {
                    @Override
                    public void onFragmentResult(String requestKey, Bundle result) {
                        String res = (String) result.getSerializable(RES);
                        if(res.equals(getString(R.string.add_to_playlist))) {
                            com.mygame.music_alpha.ForAll.get(context).addToPlaylist(musicFile, Player.this, SINGLE);
                        }
                        else if(res.equals(getString(R.string.delete_forever))) {
                            Log.i(TAG, "до ListSongs.size() - " + ListSongs.size());
                            ForAll.get(context).delete(musicFile);
                            Log.i(TAG, "после ListSongs.size() - " + ListSongs.size());
                            if(ListSongs.isEmpty()) finish();
                        }
                        else if(res.equals(getString(R.string.to_share))) {
                            ForAll.get(context).share(musicFile, Player.this);
                        }
                    }
                });
                dialog.show(manager, DIALOG_MENU_MORE);
            }
        });

    }

    private String[] getS() {
        String[] s = {getString(R.string.add_to_playlist), getString(R.string.delete_forever),
                getString(R.string.to_share)};
        return s;
    }

    private void setFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    //BUTTON CONTROL PLAYING------------------------------------------------------------------------
    private void playThreadBtn() {
        playThread = new Thread(){
            @Override
            public void run() {
                super.run();
                playPauseBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playPauseBtnClicked();
                    }
                });
            }
        };
        playThread.start();
    }

    private void nextThreadBtn() {
        nextThread = new Thread(){
            @Override
            public void run() {
                super.run();
                nextBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        nextBtnClicked();
                    }
                });
            }
        };
        nextThread.start();
    }

    private void prevThreadBtn() {
        prevThread = new Thread(){
            @Override
            public void run() {
                super.run();
                prevBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        prevBtnClicked();
                    }
                });
            }
        };
        prevThread.start();
    }


    public void playPauseBtnClicked() {
        if(is_live_player_activity) {
            if (musicService.isPlaying()) {
                playPauseBtn.setImageResource(R.drawable.ic_play);
            } else {
                playPauseBtn.setImageResource(R.drawable.ic_pause);
            }
            musicService.playPauseBtnClicked();
            setVisualizer();
        }
        else {
            musicService.playPauseBtnClicked();
        }
        Log.e(TAG, "PLAY/PAUSE");
    }

    public void prevBtnClicked() {
        if(is_live_player_activity) {
            if (musicService.isPlaying()) {
                playPauseBtn.setBackgroundResource(R.drawable.ic_pause);
            } else {
                playPauseBtn.setBackgroundResource(R.drawable.ic_play);
            }
            musicService.prevBtnClicked();
            metaData();
            setVisualizer();
            Log.e(TAG, "PREV");
        }
        else {
            musicService.prevBtnClicked();
        }
    }

    public void nextBtnClicked() {
        if(is_live_player_activity) {
            if (musicService.isPlaying()) {
                playPauseBtn.setBackgroundResource(R.drawable.ic_pause);
            } else {
                playPauseBtn.setBackgroundResource(R.drawable.ic_play);
            }
            musicService.nextBtnClicked();
            metaData();
            setVisualizer();
            Log.e(TAG, "NEXT");
        }
        else {
            musicService.nextBtnClicked();
        }
    }
    //BUTTON CONTROL PLAYING------------------------------------------------------------------------


    //Визулятор, что на дне плеерв------------------------------------------------------------------
    private void setVisualizer() {
        if(musicService != null) {
            if(visualizer != null && ContextCompat.checkSelfPermission(context,
                    Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                int audioSessionId = musicService.getAudioSessionId();
                if (audioSessionId != -1) {
                    visualizer.setAudioSessionId(audioSessionId);
                }
            }
        }
        Log.i(TAG, "VISUALIZER!!!");
    }
    //Визулятор, что на дне плеерв------------------------------------------------------------------

    private String formattedTime(int mCurrentPosition) {
        String totalout = "";
        String totalNew = "";
        String seconds = String.valueOf(mCurrentPosition % 60);
        String minutes = String.valueOf(mCurrentPosition / 60);
        totalout = minutes + ":" + seconds;
        totalNew = minutes + ":" + "0" + seconds;
        if(seconds.length() == 1){
            return totalNew;
        }
        else{
            return totalout;
        }
    }

    private void getIntentMethod() {
        FLAG = true;
        ListSongs = MusicLab.get(this).getNowPlaying();

        POSITION_PLAY = getIntent().getIntExtra(POSITION_PLAY_NOW, -1);
        String s = getIntent().getStringExtra(TAG);
        Log.e(TAG, s);
        boolean flag = s.equals("MiniFragment");

        Log.i(TAG, "ListSongs.size -  " + ListSongs.size() + " POSITION PLAY NOW = " + POSITION_PLAY);

        if(POSITION_PLAY == -1 && ListSongs == null) {
            MusicLab.get(this).setNowPlaying(MusicLab.get(this).getMusicFiles());
            ListSongs = MusicLab.get(this).getNowPlaying();
            POSITION_PLAY = 0;
        }
        if(ListSongs != null) {

            if(!flag) {
                if (musicService != null) {
                    musicService.stop();
                    musicService.release();
                }
                Intent intent = new Intent(this, MusicService.class);
                intent.putExtra("servicePosition", POSITION_PLAY);
                startService(intent);
                Log.e(TAG, "startService(intent);");
            }
        }

        tinyDB.putInt(KEY_POSITION_PLAY, POSITION_PLAY);
        tinyDB.putListString(KEY_QUEUE, MusicLab.get(getApplicationContext()).putSaveQueue());
    }

    //SET IMAGE-------------------------------------------------------------------------------------
    private void metaData(){
//        if(REPEAT && !song_name.getText().toString().equals("This is song Name")) return;
//        Log.i(TAG, "old uri - " + uri);
        if(uri != null) {
            if (uri.toString().equals(Uri.parse(ListSongs.get(POSITION_PLAY).getPath()).toString()))
                return;
        }
        Log.i(TAG, "ОБНОВЛЕНИЕ ФОТОГРАФИИ");
        updateRS();
        uri = Uri.parse(ListSongs.get(POSITION_PLAY).getPath());
        song_name.setText(ListSongs.get(POSITION_PLAY).getTitle());
        artist_name.setText(ListSongs.get(POSITION_PLAY).getArtist());
        seekBar.setMax(musicService.getDuration() / 1000);
        runnable.run();
        int durationTotal = Integer.parseInt(ListSongs.get(POSITION_PLAY).getDuration()) / 1000;
        duration_total.setText(formattedTime(durationTotal));
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        byte[] art = retriever.getEmbeddedPicture();
        Bitmap bitmap;
        if(art != null){
            bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
            ImageAnimation(this, cover_art, bitmap);
        }
        else{
            Glide.with(this).asBitmap().load(R.drawable.cover_art_2).into(cover_art);
            background_player.setImageResource(android.R.color.transparent);
        }
    }

    public void ImageAnimation(final Context context, final ImageView imageView, final Bitmap bitmap){
        Animation animOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        Animation animIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        animOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Glide.with(context).load(bitmap).into(imageView);

                Glide.with(context)
                        .load(bitmap)
                        .apply(RequestOptions.bitmapTransform(new MultiTransformation(
                                new BlurTransformation(10, 3),
                                new ContrastFilterTransformation(0.5F),
                                new BrightnessFilterTransformation(-0.3F))))
                        .into(background_player);

                animIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}

                    @Override
                    public void onAnimationEnd(Animation animation) {}

                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
                imageView.startAnimation(animIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        imageView.startAnimation(animOut);
    }
    //SET IMAGE-------------------------------------------------------------------------------------


    //SETTING VOLUME--------------------------------------------------------------------------------
    private void settingVolume() {
        try {
            Log.e(TAG, "Setting volume");
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            seekBar_volume.setMax(audioManager
                    .getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            seekBar_volume.setProgress(audioManager
                    .getStreamVolume(AudioManager.STREAM_MUSIC));


            seekBar_volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onStopTrackingTouch(SeekBar arg0) {
                }

                @Override
                public void onStartTrackingTouch(SeekBar arg0) {
                }

                @Override
                public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                            progress, 0);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            seekBar_volume.setProgress((seekBar_volume.getProgress() + 1 > seekBar_volume.getMax()) ?
                    seekBar_volume.getMax() : seekBar_volume.getProgress() + 1);
        }
        else if (keyCode== KeyEvent.KEYCODE_VOLUME_DOWN) {
            seekBar_volume.setProgress((seekBar_volume.getProgress() - 1 < 0) ?
                    0 : seekBar_volume.getProgress() - 1);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0);
            seekBar_volume.setProgress((seekBar_volume.getProgress() - 1 < 0) ?
                    0 : seekBar_volume.getProgress() - 1);
        }else if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0);
            seekBar_volume.setProgress((seekBar_volume.getProgress() + 1 > seekBar_volume.getMax()) ?
                    seekBar_volume.getMax() : seekBar_volume.getProgress() + 1);
        }
        if(event.getKeyCode() == KeyEvent.KEYCODE_BACK)   {
            finish();
        }
        return true;
    }
    //SETTING VOLUME--------------------------------------------------------------------------------


    @Override
    protected void onStart(){
        super.onStart();
        is_live_player_activity = true;
        MusicActivity.PER_RECORD_AUDIO = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        Log.i(TAG, "onStart()");
    }
    @Override
    protected void onResume(){
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, this, BIND_AUTO_CREATE);
        playThreadBtn();
        nextThreadBtn();
        prevThreadBtn();
        super.onResume();
        updateRS();
        if(FLAG) {
            playPauseBtn.setImageResource(R.drawable.ic_pause);
        }
        else {
            playPauseBtn.setImageResource(R.drawable.ic_play);
        }
        Log.i(TAG, "onResume() + MusicLab.POSITION_PLAY = " + MusicLab.POSITION_PLAY);
    }
    @Override
    protected void onPause(){
        super.onPause();
        unbindService(this);
        handler.removeCallbacks(runnable);
        tinyDB.putListString(KEY_QUEUE, MusicLab.get(getBaseContext()).putSaveQueue());
        tinyDB.putBoolean(KEY_SHUFFLE, SHUFFLE);
        tinyDB.putBoolean(KEY_REPEAT, REPEAT);
        Log.i(TAG, "onPause()");
    }
    @Override
    protected void onStop(){
        super.onStop();
        Log.i(TAG, "onStop()");
    }
    @Override
    protected void onDestroy(){
        if(visualizer != null) {
            visualizer.release();
        }
        super.onDestroy();
        handler.removeCallbacks(runnable);
        is_live_player_activity = false;
        Log.i(TAG, "onDestroy()");

    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MusicService.MyBinder myBinder = (MusicService.MyBinder) service;
        musicService = myBinder.getService();
        musicService.setCallBack(this);
        if(FLAG) {
            musicService.start();
            musicService.showNotification(R.drawable.ic_pause, 1F);
        } else {
            musicService.showNotification(R.drawable.ic_play, 0F);
        }
        Log.e(TAG, "Connected" + musicService);
        musicService.OnCompleted();
        if(musicService != null) {
            runnable.run();
            metaData();
            settingVolume();
            setVisualizer();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        musicService = null;
    }

    public static void updateRS() {
        if(is_live_player_activity) {
            int mode = REPEAT ? R.drawable.ic_repeat_one :
                    (SHUFFLE ? R.drawable.ic_shuffle_red :
                            R.drawable.ic_repeat);
            shuffleBtn.setImageResource(mode);

            int favorite = ListSongs.get(POSITION_PLAY).getFavorite() ?
                    R.drawable.ic_favorite_red : R.drawable.ic_favorite;
            favoriteBtn.setImageResource(favorite);
        }
    }
}

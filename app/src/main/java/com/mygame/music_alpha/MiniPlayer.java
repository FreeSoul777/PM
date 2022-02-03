package com.mygame.music_alpha;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;

import static android.content.Context.BIND_AUTO_CREATE;
import static com.mygame.music_alpha.MusicLab.POSITION_PLAY;
import static com.mygame.music_alpha.MusicLab.POSITION_PLAY_NOW;
import static com.mygame.music_alpha.MusicService.FLAG;

public class MiniPlayer extends Fragment implements ActionPlaying, ServiceConnection {

    private static final String TAG = "MiniFragment";

    public static MiniPlayer miniPlayer;
    private static ArrayList<MusicFile> MiniListSong;
    private static ImageView nextBtn, prevBtn, playPauseBtn;
    private static ImageView album_art;
    private static TextView file_name, file_artist_name;
    private static View v;
    private Context context;
    public static MusicService musicService;
    private Thread playThread, prevThread, nextThread;
    private boolean is_live_min_player_activity = true;

    public static MiniPlayer newInstance() {
        return new MiniPlayer();
    }

    public static MiniPlayer get(Context context) {
        if (miniPlayer == null) {
            miniPlayer = new MiniPlayer(context);
        }
        return miniPlayer;
    }
    private MiniPlayer(Context context) {
        this.context = context;
    }
    private MiniPlayer(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView()");
        v = inflater.inflate(R.layout.fragment_mini_player, container, false);
        context = getContext();

        nextBtn = v.findViewById(R.id.id_next);
        prevBtn = v.findViewById(R.id.id_prev);
        playPauseBtn = v.findViewById(R.id.play_pause);
        album_art = v.findViewById(R.id.music_img);
        file_name = v.findViewById(R.id.music_file_name);
        file_artist_name = v.findViewById(R.id.music_artist_name);

        if(!MusicLab.get(context).getMusicFiles().isEmpty() &&
                (POSITION_PLAY < 0 || (POSITION_PLAY >= MusicLab.get(context).getMusicFiles().size()))) {
            POSITION_PLAY = 0;
            MusicLab.get(context).setNowPlaying(MusicLab.get(context).getSongFiles());
        }
        MiniListSong = MusicLab.get(context).getNowPlaying();
//        if(POSITION_PLAY != -1) {
//            bind(MiniListSong.get(POSITION_PLAY));
//        }

//        if(MiniListSong.size() != 0) {
//            Intent intent = new Intent(context, MusicService.class);
//            intent.putExtra("servicePosition", POSITION_PLAY);
//            context.startService(intent);
//            Log.e(TAG, "startService(intent)");
//        }

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), Player.class);
                intent.putExtra(POSITION_PLAY_NOW, POSITION_PLAY);
                intent.putExtra(Player.TAG, TAG);
                startActivity(intent);
            }
        });

        return v;
    }

    public void bind(MusicFile musicFile) {
        Log.i(TAG, "bind() - " + musicFile.getTitle());
        file_name.setText(musicFile.getTitle());
        file_artist_name.setText((musicFile.getArtist() + " | " + musicFile.getAlbum()));
        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(context));
        ImageLoader.getInstance().displayImage(ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"),
                musicFile.getAlbumId()).toString(), album_art,
                new DisplayImageOptions.Builder().cacheInMemory(true).showImageOnLoading(R.drawable.cover_art_2).
                        resetViewBeforeLoading(true).build());
//        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//        retriever.setDataSource(Uri.parse(musicFile.getPath()).toString());
//        byte[] art = retriever.getEmbeddedPicture();
//        Bitmap bitmap;
//        if(art != null){
//            bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
//            Glide.with(this).load(bitmap).into(album_art);
//        }
//        else{
//            Glide.with(this).asBitmap().load(R.drawable.cover_art_2).into(album_art);
//        }
        Log.i(TAG, "bind end.");
    }

    @Override
    public void onStart(){
        super.onStart();
        is_live_min_player_activity = true;
        Log.i(TAG, "onStart()");

    }
    @Override
    public void onResume(){
        MiniListSong = MusicLab.get(getContext()).getNowPlaying();
        if(!MiniListSong.isEmpty()) {
            if(musicService == null) {
                Intent intent = new Intent(context, MusicService.class);
                intent.putExtra("servicePosition", POSITION_PLAY);
                context.startService(intent);
                Log.e(TAG, "startService(intent)");
            }
            FLAG = musicService.is_play();
            Intent intent = new Intent(context, MusicService.class);
            context.bindService(intent, this, BIND_AUTO_CREATE);
            Log.e(TAG, "bindService");
        }
        playThreadBtn();
        nextThreadBtn();
        prevThreadBtn();
        super.onResume();
        if(!MiniListSong.isEmpty() && POSITION_PLAY != -1) {
            bind(MiniListSong.get(POSITION_PLAY));
        }
        if(MiniListSong.isEmpty()) {
            playPauseBtn.setEnabled(false);
            nextBtn.setEnabled(false);
            prevBtn.setEnabled(false);
            v.setEnabled(false);
        }
        if(musicService.is_play()) {
            playPauseBtn.setImageResource(R.drawable.ic_pause);
        }
        else {
            playPauseBtn.setImageResource(R.drawable.ic_play);
        }
        Log.i(TAG, "MiniListSong.size() - " + MiniListSong.size() + ", position - " + POSITION_PLAY);
        Log.i(TAG, "onResume()");
    }
    @Override
    public void onPause(){
        super.onPause();
        if(musicService != null) {
            context.unbindService(this);
        }
        Log.i(TAG, "onPause()");
    }
    @Override
    public void onStop(){
        super.onStop();
        Log.i(TAG, "onStop()");
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        is_live_min_player_activity = false;
        Log.i(TAG, "onDestroy()");
    }

    public void updateMini(ArrayList<MusicFile> miniListSong, boolean flag) {
        Log.e(TAG, "MiniListSong.size() - " + MiniListSong.size() +
                ", NowPlaying.size() - " + MusicLab.get(context).getNowPlaying().size());
        MusicLab.get(context).setNowPlaying(miniListSong);
        MiniListSong = new ArrayList<>();
        MiniListSong.addAll(miniListSong);
        if(!MiniListSong.isEmpty() && (POSITION_PLAY == -1  || (POSITION_PLAY >= MiniListSong.size()))) {
            POSITION_PLAY = 0;
        }
        Log.e(TAG, "MiniListSong.size() " + MiniListSong.size() + " POSITION " + POSITION_PLAY +
                " flag " + flag);
        if(POSITION_PLAY != -1 && !MiniListSong.isEmpty()) {
            Log.e(TAG, "Good way");
            musicService.updateMusicFiles();
            bind(MiniListSong.get(POSITION_PLAY));
            if(flag) {
                playPauseBtnClicked();
                Intent serviceIntent = new Intent(context, MusicService.class);
                context.stopService(serviceIntent);
                Log.e(TAG, "stopService");

                if(POSITION_PLAY != -1 && !MiniListSong.isEmpty()) {
                    Intent intent = new Intent(context, MusicService.class);
                    intent.putExtra("servicePosition", POSITION_PLAY);
                    context.startService(intent);
                    Log.e(TAG, "startService");
                }
            }
        }
        else {
            Log.e(TAG, "Bad way");
            if(musicService != null) {
                musicService.updateMusicFiles();
                musicService.stop();
                FLAG = false;
                musicService.onDestroy();
                musicService = null;
                Log.e(TAG, "stopService");
            }

            file_name.setText("Song name");
            file_artist_name.setText("Artist name | Album name");
            Glide.with(context).asBitmap().load(R.drawable.cover_art_2).into(album_art);
            playPauseBtn.setImageResource(R.drawable.ic_play);
            playPauseBtn.setEnabled(false);
            nextBtn.setEnabled(false);
            prevBtn.setEnabled(false);
            v.setEnabled(false);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MusicService.MyBinder myBinder = (MusicService.MyBinder) service;
        musicService = myBinder.getService();
        Log.e(TAG, "Connected" + musicService);
        musicService.setCallBack(this);
        musicService.OnCompleted();
        if(FLAG) {
            musicService.showNotification(R.drawable.ic_pause, 1F);
        } else {
            musicService.showNotification(R.drawable.ic_play, 0F);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        musicService = null;
        Log.e(TAG, "onServiceDisconnected");
    }

    @Override
    public void playPauseBtnClicked() {
        if(is_live_min_player_activity) {
            if (musicService.isPlaying()) {
                playPauseBtn.setImageResource(R.drawable.ic_play);
            } else {
                playPauseBtn.setImageResource(R.drawable.ic_pause);
            }
        }
        musicService.playPauseBtnClicked();
        Log.e(TAG, "PLAY/PAUSE");
    }

    @Override
    public void prevBtnClicked() {
        if(is_live_min_player_activity) {
            if (musicService.isPlaying()) {
                playPauseBtn.setBackgroundResource(R.drawable.ic_pause);
            } else {
                playPauseBtn.setBackgroundResource(R.drawable.ic_play);
            }
            musicService.prevBtnClicked();
            bind(MiniListSong.get(POSITION_PLAY));
            Log.e(TAG, "PREV");
        }
        else {
            musicService.prevBtnClicked();
        }
    }

    @Override
    public void nextBtnClicked() {
        if(is_live_min_player_activity) {
            if (musicService.isPlaying()) {
                playPauseBtn.setBackgroundResource(R.drawable.ic_pause);
            } else {
                playPauseBtn.setBackgroundResource(R.drawable.ic_play);
            }
            musicService.nextBtnClicked();
            bind(MiniListSong.get(POSITION_PLAY));
            Log.e(TAG, "NEXT");
        }
        else {
            musicService.nextBtnClicked();
        }
    }

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
}

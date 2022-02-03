package com.mygame.music_alpha;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;

import java.io.File;
import java.util.ArrayList;

import static com.mygame.music_alpha.DialogArrayPlaylist.DIALOG_ARRAY_ADD;
import static com.mygame.music_alpha.MusicLab.ALBUM;
import static com.mygame.music_alpha.MusicLab.ARTIST;
import static com.mygame.music_alpha.MusicLab.ORDER;
import static com.mygame.music_alpha.MusicLab.PLAYLIST;
import static com.mygame.music_alpha.MusicLab.POSITION_PLAY;
import static com.mygame.music_alpha.MusicLab.SINGLE;
import static com.mygame.music_alpha.SongFragment.DIALOG_ADD_ARRAY_PLAYLIST;
import static com.mygame.music_alpha.SongFragment.FOR_RESULT;

public class ForAll extends FragmentActivity {

    private static final String TAG = "FOR_ALL";

    public static ForAll ForAll;
    private Context context;
    private TinyDB tinyDB;
    private ArrayList<MusicFile> garbage;

    public static ForAll get(Context context) {
        if (ForAll == null) {
            ForAll = new ForAll(context);
        }
        return ForAll;
    }

    private ForAll(Context context) {
        this.context = context.getApplicationContext();
        tinyDB = new TinyDB(context);
    }

    public void playNext(MusicFile song) {
        MusicLab.get(context).getNowPlaying().add(POSITION_PLAY + 1, song);
    }

    public void addToQueue(MusicFile song) {
        MusicLab.get(context).getNowPlaying().add(song);
    }

    public void playNextAll(ArrayList<MusicFile> songs) {
        MusicLab.get(context).getNowPlaying().addAll(POSITION_PLAY + 1, songs);
    }

    public void addToQueueAll(ArrayList<MusicFile> songs) {
        MusicLab.get(context).getNowPlaying().addAll(songs);
    }

    public void delete(MusicFile song) {
        Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                Long.parseLong(song.getId()));
        File file = new File(song.getPath());
        boolean deleted = file.delete();
        ArrayList<MusicFile> musicFiles = MusicLab.get(context).getMusicFiles();
        ArrayList<MusicFile> nowPlay = MusicLab.get(context).getNowPlaying();
        ArrayList<MusicFile> playlist = MusicLab.get(context).getPlaylistFiles();

        Log.i(TAG, "musicFiles - " + musicFiles.size() + " nowPlay - " + nowPlay.size()
                + " playlist - " + playlist.size() + ", POSITION " + POSITION_PLAY);

        if(deleted) {
            context.getContentResolver().delete(contentUri, null, null);
            while(nowPlay.contains(song)) {
                if(MusicLab.get(context).getSongPath().equals(song.getPath())) {
                    if(nowPlay.size() > 1) {
                        MusicService.actionPlaying.nextBtnClicked();
                    }
                    Log.e(TAG, "POSITION_PLAY до - " + POSITION_PLAY);
                    POSITION_PLAY = POSITION_PLAY - 1 < 0 && nowPlay.size() > 1 ?
                            POSITION_PLAY = 0 : POSITION_PLAY - 1;
                    Log.e(TAG, "POSITION_PLAY после - " + POSITION_PLAY);
                }
                else if (nowPlay.indexOf(song) < POSITION_PLAY) {
                    Log.e(TAG, "POSITION_PLAY до - " + POSITION_PLAY);
                    POSITION_PLAY -= 1;
                    Log.e(TAG, "POSITION_PLAY после - " + POSITION_PLAY);
                }
                nowPlay.remove(song);
                Log.e(TAG, "nowPlay.remove - " + song.getTitle());
            }
            while(musicFiles.contains(song)) {
                musicFiles.remove(song);
                Log.e(TAG, "musicFiles.remove - " + song.getTitle());
            }
            Log.i(TAG, "File deleted, " + song.getTitle());


            MusicLab.get(context).newMusicFiles(ORDER);

            ArrayList<MusicFile> newNow = new ArrayList<>();
            for(MusicFile now: nowPlay){
                for(MusicFile musicFile: MusicLab.get(context).getMusicFiles()){
                    if(musicFile.getPath().equals(now.getPath())) {
                        newNow.add(musicFile);
                        break;
                    }
                }
            }
            nowPlay = new ArrayList<>();
            nowPlay.addAll(newNow);
            MusicLab.get(context).setNowPlaying(newNow);

            MiniPlayer.get(context).updateMini(!nowPlay.isEmpty() ?
                            nowPlay : MusicLab.get(context).getMusicFiles(), nowPlay.isEmpty());
            SongFragment.updateSongFiles(MusicLab.get(context).getSongFiles());
            AlbumFragment.updateAlbumFiles(MusicLab.get(context).getAlbumFiles());
            ArtistFragment.updateArtistFiles(MusicLab.get(context).getArtistFiles());
        }
        else{
            Log.i(TAG, "Can't deleted, " + song.getTitle());
        }
        Log.e(TAG, "Delete, успешно удалено!");
    }

    public void deleteAll(ArrayList<MusicFile> music) {
        ArrayList<MusicFile> musicFiles = MusicLab.get(context).getMusicFiles();
        ArrayList<MusicFile> nowPlay = MusicLab.get(context).getNowPlaying();
        ArrayList<MusicFile> playlist = MusicLab.get(context).getPlaylistFiles();
        Log.i(TAG, "musicFiles - " + musicFiles.size() + " nowPlay - " + nowPlay.size()
                + " playlist - " + playlist.size() + ", POSITION " + POSITION_PLAY);

        for(MusicFile song: music) {
            Log.i(TAG, "song - " + song.getTitle() + ", music.size() - " + music.size());
            Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    Long.parseLong(song.getId()));
            File file = new File(song.getPath());
            boolean deleted = file.delete();
            if(deleted) {
                context.getContentResolver().delete(contentUri, null, null);
                while(nowPlay.contains(song)) {
                    if(MusicLab.get(context).getSongPath().equals(song.getPath())) {
                        if(nowPlay.size() > 1) {
                            MusicService.actionPlaying.nextBtnClicked();
                        }
                        Log.e(TAG, "POSITION_PLAY до - " + POSITION_PLAY);
                        POSITION_PLAY = POSITION_PLAY - 1 < 0 && nowPlay.size() > 1 ?
                                POSITION_PLAY = 0 : POSITION_PLAY - 1;
                        Log.e(TAG, "POSITION_PLAY после - " + POSITION_PLAY);
                    }
                    else if (nowPlay.indexOf(song) < POSITION_PLAY) {
                        Log.e(TAG, "POSITION_PLAY до - " + POSITION_PLAY);
                        POSITION_PLAY -= 1;
                        Log.e(TAG, "POSITION_PLAY после - " + POSITION_PLAY);
                    }
                    nowPlay.remove(song);
                    Log.e(TAG, "nowPlay.remove - " + song.getTitle());
                }
                while(musicFiles.contains(song)) {
                    musicFiles.remove(song);
                    Log.e(TAG, "musicFiles.remove - " + song.getTitle());
                }
                Log.i(TAG, "File deleted, " + song.getTitle());
            }
            else{
                Log.i(TAG, "Can't deleted, " + song.getTitle());
            }
        }

        MusicLab.get(context).newMusicFiles(ORDER);

        ArrayList<MusicFile> newNow = new ArrayList<>();
        for(MusicFile now: nowPlay){
            for(MusicFile musicFile: MusicLab.get(context).getMusicFiles()){
                if(musicFile.getPath().equals(now.getPath())) {
                    newNow.add(musicFile);
                    break;
                }
            }
        }
        nowPlay = new ArrayList<>();
        nowPlay.addAll(newNow);
        MusicLab.get(context).setNowPlaying(newNow);

        MiniPlayer.get(context).updateMini(!nowPlay.isEmpty() ?
                        nowPlay : MusicLab.get(context).getMusicFiles(), nowPlay.isEmpty());
        SongFragment.updateSongFiles(MusicLab.get(context).getSongFiles());
        AlbumFragment.updateAlbumFiles(MusicLab.get(context).getAlbumFiles());
        ArtistFragment.updateArtistFiles(MusicLab.get(context).getArtistFiles());
        Log.e(TAG, "DeleteAll, успешно удалено!");
    }

    public void share(MusicFile song, Context context) {
        Log.e(TAG, "Share, start!");
        File file = new File(song.getPath());
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, "Demo Title");
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri path = FileProvider.getUriForFile(this, "com.mygame.music_alpha.fileprovider",
                    file);
            intent.putExtra(Intent.EXTRA_STREAM, path);
        }
        else {
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        }
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.setType("*audio/*");
        intent = Intent.createChooser(intent, "Share File");
        context.startActivity(intent);
        Log.e(TAG, "Share, успешно отправлено!");
    }

    public void shareMulti(ArrayList<MusicFile> musicFiles, Context context) {
        ArrayList<Uri> listPath = new ArrayList<>();
        Log.i(TAG, "count share - " + musicFiles.size());
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.putExtra(Intent.EXTRA_TEXT, "Demo Title");
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            for(MusicFile single: musicFiles) {
                File file1 = new File(single.getPath());
                Uri path1 = FileProvider.getUriForFile(this, "com.mygame.music_alpha.fileprovider",
                        file1);
                listPath.add(path1);
            }
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, listPath);
        }
        else {

            for(MusicFile single: musicFiles) {
                File file2 = new File(single.getPath());
                Uri path2 = Uri.fromFile(file2);
                listPath.add(path2);
            }
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, listPath);
        }
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.setType("*audio/*");
        intent = Intent.createChooser(intent, "Share File");
        context.startActivity(intent);
        Log.e(TAG, "ShareMulti, успешно отправлено!");
    }

    public void addToPlaylist(MusicFile musicFile, FragmentActivity fragmentActivity, String key) {
        FragmentManager manager = fragmentActivity.getSupportFragmentManager();
        DialogArrayPlaylist dialog = DialogArrayPlaylist.newInstance();
        manager.setFragmentResultListener(FOR_RESULT, fragmentActivity, new FragmentResultListener() {
            @Override
            public void onFragmentResult(String requestKey, Bundle result) {
                ArrayList<String> pl = result.getStringArrayList(DIALOG_ARRAY_ADD);
                Log.e(TAG, "addToPlaylist pl.size - " + pl.size());
                asd(musicFile, pl, key);
            }
        });
        dialog.show(manager, DIALOG_ADD_ARRAY_PLAYLIST);
    }

    private void asd(MusicFile musicFile, ArrayList<String> pl, String key) {
        Log.i(TAG, "Save song - " + musicFile.getTitle());
        ArrayList<MusicFile> list = new ArrayList<>();
        if(key.equals(ALBUM)) {
            list = MusicLab.get(context).createPlaylist(key, musicFile.getAlbum());
        }
        else if (key.equals(ARTIST)) {
            list = MusicLab.get(context).createPlaylist(key, musicFile.getArtist());
        }
        else if (key.equals(SINGLE)) {
            list.add(musicFile);
        }
        else if (key.equals("Garbage")) {
            list.addAll(getGarbage());
        }
        Log.i(TAG, key + " list - " + list.size());
        ArrayList<PL> playlist = MusicLab.get(context).getPlaylistString();
        ArrayList<String> paths = MusicLab.get(context).fromToString(list);
        Log.i(TAG, "paths - " + paths.size());
        Log.i(TAG, "playlist - " + playlist.size());
        for(PL play: playlist) {
            Log.i(TAG, play.getTitle() + " name");
            if(pl.contains(play.getTitle())) {
                ArrayList<String> playlistString = tinyDB.getListString(play.getTitle());
                Log.i(TAG, play.getTitle() + " size - " + playlistString.size());
                playlistString.addAll(paths);
                if(play.getAlbumId() == -1) {
                    play.setAlbumId(list.get(0).getAlbumId());
                }
                Log.i(TAG, play.getTitle() + " size - " + playlistString.size());
                tinyDB.putListString(play.getTitle(), playlistString);
            }
        }
        PlaylistFragment.updatePlaylistFiles(playlist);
    }

    public void saveGarbage(ArrayList<MusicFile> musicFiles) {
        garbage = new ArrayList<>();
        garbage.addAll(musicFiles);
    }

    private ArrayList<MusicFile> getGarbage() {
        return garbage;
    }

    public void deletePL(int position) {
        ArrayList<PL> playlist = MusicLab.get(context).getPlaylistString();
        tinyDB.remove(playlist.get(position).getTitle());
        playlist.remove(position);
        MusicLab.get(context).putPL();
    }

    public void deletePLforever(int position) {
        ArrayList<PL> playlist = MusicLab.get(context).getPlaylistString();
        ArrayList<MusicFile> musicFiles =
                MusicLab.get(context).createPlaylist(PLAYLIST, playlist.get(position).getTitle());
        deleteAll(musicFiles);
        playlist.remove(position);
        MusicLab.get(context).putPL();
        Log.i(TAG, "PL - " + playlist.size());
    }

    public void newName(String name, String ex) {
        ArrayList<String> paths = MusicLab.get(context).getPlaylistName(ex);
        tinyDB.putListString(name, paths);
        tinyDB.remove(ex);
    }

    public static int calculateNoOfColumns(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int noOfColumns = (int) (dpWidth / 180);
        return noOfColumns;
    }
}

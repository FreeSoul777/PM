package com.mygame.music_alpha;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaMetadata;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;

import static android.provider.Settings.Global.getString;
import static com.mygame.music_alpha.MiniPlayer.musicService;
import static com.mygame.music_alpha.MusicFragment.KEY_LIST_PLAYLIST_ALBUM_ID;
import static com.mygame.music_alpha.MusicFragment.KEY_LIST_PLAYLIST_PATH;
import static com.mygame.music_alpha.MusicFragment.KEY_LIST_PLAYLIST_TITLE;


public class MusicLab {

    private static final String TAG = "MusicLab";

    public static final String POSITION_PLAY_NOW = "position_play_now";
    public static String SORT_SONGS = "sortByDate";
    public static final String WHAT = "what";
    public static final String WHAT_2 = "what_2";
    public static final String ARTIST = "artists";
    public static final String ALBUM = "albums";
    public static final String SINGLE = "singles";
    public static final String PLAYLIST = "playlist";
    public static boolean BORDER_ALBUM; // = true;
    public static boolean BORDER_ARTIST; // = false;
    public static boolean BORDER_PLAYLIST; // = true;
    public static boolean SHUFFLE; // = false;
    public static boolean REPEAT; // = false;
    public static boolean FLAG; // = true;
    public static String ORDER; // = MediaStore.MediaColumns.DATE_MODIFIED + " DESC";
    public static int POSITION_PLAY; // = -1;


    private ArrayList<MusicFile> MusicFiles = new ArrayList<>();
    private ArrayList<MusicFile> SongFiles = new ArrayList<>();
    private ArrayList<MusicFile> AlbumFiles = new ArrayList<>();
    private ArrayList<MusicFile> ArtistFiles = new ArrayList<>();
    private ArrayList<MusicFile> NowPlaying = new ArrayList<>();
    private ArrayList<MusicFile> PlaylistFiles = new ArrayList<>();
    private ArrayList<MusicFile> GarbageFiles = new ArrayList<>();
    private ArrayList<MusicFile> FavoriteFiles = new ArrayList<>();
    private ArrayList<PL> PlaylistString = new ArrayList<>();
    private static MusicLab mMusicLab;
    private Context mContext;
    private TinyDB tinyDB;
    private String songPath;

    public static MusicLab get(Context context) {
        if (mMusicLab == null) {
             mMusicLab = new MusicLab(context);
        }
        return mMusicLab;
    }

    private MusicLab(Context context) {
        mContext = context.getApplicationContext();
        tinyDB = new TinyDB(mContext);
    }

    public void setMusicFiles(ArrayList<MusicFile> musicFiles) {
        MusicFiles = new ArrayList<>();
        MusicFiles.addAll(musicFiles);
        Log.e(TAG, MusicFiles.size() + " - MusicFiles.size()");
    }
    public void setNowPlaying(ArrayList<MusicFile> nowPlaying) {
        NowPlaying = new ArrayList<>();
        NowPlaying.addAll(nowPlaying);
//        NowPlaying = nowPlaying;
        Log.e(TAG, NowPlaying.size() + " - NowPlaying.size()");
    }
    public void setSongFiles(ArrayList<MusicFile> songFiles) {
        SongFiles = new ArrayList<>();
        SongFiles.addAll(songFiles);
        Log.e(TAG, SongFiles.size() + " - SongFiles.size()");
    }
    public void setAlbumFiles(ArrayList<MusicFile> albumFiles) {
        AlbumFiles = new ArrayList<>();
        AlbumFiles.addAll(albumFiles);
        Log.e(TAG, AlbumFiles.size() + " - AlbumFiles.size()");
    }
    public void setArtistFiles(ArrayList<MusicFile> artistFiles) {
        ArtistFiles = new ArrayList<>();
        ArtistFiles.addAll(artistFiles);
        Log.e(TAG, ArtistFiles.size() + " - ArtistFiles.size()");
    }
    public void setFavoriteFiles(ArrayList<MusicFile> favoriteFiles) {
        FavoriteFiles = new ArrayList<>();
        FavoriteFiles.addAll(favoriteFiles);
        Log.e(TAG, FavoriteFiles.size() + " - FavoriteFiles.size()");
    }
    public void setPlaylistFiles(ArrayList<MusicFile> playlistFiles) {
        PlaylistFiles = new ArrayList<>();
        PlaylistFiles.addAll(playlistFiles);
        Log.i(TAG, PlaylistFiles.size() + " - PlaylistFiles.size()");
    }
    public void setGarbageFiles(){
        GarbageFiles = new ArrayList<>();
        Log.i(TAG, GarbageFiles.size() + " - GarbageFiles.size()");
    }

    public com.mygame.music_alpha.MusicFile getMusicFile(int position) { return NowPlaying.get(position); }


    public ArrayList<MusicFile> getMusicFiles() {
        return MusicFiles;
    }

    public ArrayList<MusicFile> getSongFiles() {
        return SongFiles;
    }

    public ArrayList<MusicFile> getNowPlaying() {
        return NowPlaying;
    }

    public ArrayList<MusicFile> getAlbumFiles() { return AlbumFiles; }

    public ArrayList<MusicFile> getArtistFiles() { return ArtistFiles; }

    public ArrayList<MusicFile> createPlaylist(String what, String name) {
        ArrayList<MusicFile> tempAudioList = new ArrayList<>();
        if(what.equals(SINGLE)) {
            tempAudioList.addAll(SongFiles);
        }
        else if(what.equals(ALBUM)) {
            for(MusicFile musicFile: MusicFiles) {
                if(musicFile.getAlbum().equals(name)) {
                    tempAudioList.add(musicFile);
                }
            }
        }
        else if(what.equals(ARTIST)) {
            for(MusicFile musicFile: MusicFiles) {
                if(musicFile.getArtist().equals(name)) {
                    tempAudioList.add(musicFile);
                }
            }
        }
        else if(what.equals(PLAYLIST)) {
            ArrayList<String> paths = tinyDB.getListString(name);
            tempAudioList = getSavePlaylist(paths, name);
        }
        return tempAudioList;
    }

    public ArrayList<MusicFile> createPlaylist(String what) {
        ArrayList<MusicFile> tempAudioList = new ArrayList<>();
        if(what.equals("SongFragment")) {
            tempAudioList.addAll(SongFragment.getSongs());
        }
        else if(what.equals("AlbumFragment")) {
            tempAudioList.addAll(AlbumFragment.getAlbums());
        }
        else if(what.equals("ArtistFragment")) {
            tempAudioList.addAll(ArtistFragment.getArtists());
        }
        else if(what.equals("ArtistAlbumDetails")) {
            tempAudioList.addAll(PlaylistFiles);
        }
        return tempAudioList;
    }

    public ArrayList<MusicFile> getPlaylistFiles() { return PlaylistFiles; }

    public ArrayList<MusicFile> getGarbageFiles() { return GarbageFiles; }

    public ArrayList<MusicFile> getFavoriteFiles() { return FavoriteFiles; }

    public ArrayList<PL> getPlaylistString() { return PlaylistString; }

    public void newMusicFiles(String order) {
        Log.i(TAG, "newMusicFiles");
        ArrayList<MusicFile> tempAudioList = new ArrayList<>();
        ArrayList<MusicFile> tempAlbumList = new ArrayList<>();
        ArrayList<MusicFile> tempArtistList = new ArrayList<>();
        ArrayList<String> duplicateAlbum = new ArrayList<>();
        ArrayList<String> duplicateArtist = new ArrayList<>();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ARTIST_ID,
                MediaStore.Audio.Media.IS_MUSIC
        };
        Cursor cursor = mContext.getContentResolver().query(uri, projection, null, null, order);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                String duration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                String id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                Long albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                Long artistId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID));
                Long is_music = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_MUSIC));
                if (is_music == 0) continue;
                MusicFile musicFile = new MusicFile(path, title, artist, album,
                        duration, id, albumId, artistId);

                if (!duplicateAlbum.contains(album)) {
                    tempAlbumList.add(musicFile);
                    duplicateAlbum.add(album);
                }
                if (!duplicateArtist.contains(artist)) {
                    tempArtistList.add(musicFile);
                    duplicateArtist.add(artist);
                }
                tempAudioList.add(musicFile);
            }
            cursor.close();
        }

        setSongFiles(tempAudioList);
        setAlbumFiles(tempAlbumList);
        setArtistFiles(tempArtistList);
        setMusicFiles(tempAudioList);

    }


    //SAVE QUEUE AND FAVORITE____________________________________________________________________________________
    public ArrayList<String> putSaveQueue() {
        ArrayList<String> paths = new ArrayList<>();
        for(MusicFile song: NowPlaying) {
            paths.add(song.getPath());
        }
        return paths;
    }
    public ArrayList<String> putSaveFavorite() {
        ArrayList<String> paths = new ArrayList<>();
        for(MusicFile song: FavoriteFiles) {
            paths.add(song.getPath());
        }
        return paths;
    }

    public void getSaveQueueAndFavorite(ArrayList<String> pathsQueue, ArrayList<String> pathsFavorites) {
        ArrayList<MusicFile> songsQueue = new ArrayList<>();
        ArrayList<MusicFile> songsFavorite = new ArrayList<>();
        for(MusicFile single: MusicFiles) {
            if(pathsQueue.contains(single.getPath())){
                songsQueue.add(single);
            }
            if(pathsFavorites.contains(single.getPath())) {
                songsFavorite.add(single);
                single.setFavorite(true);
            } else {
                single.setFavorite(false);
            }
        }
        setNowPlaying(songsQueue);
        setFavoriteFiles(songsFavorite);
    }

    public int Qsize() { return NowPlaying.size();}
    //SAVE QUEUE____________________________________________________________________________________}



    public ArrayList<MusicFile> getSavePlaylist(ArrayList<String> paths, String name) {
        ArrayList<MusicFile> songs = new ArrayList<>();
        ArrayList<String> paths_2 = new ArrayList<>();
        paths_2.addAll(paths);
        boolean flag = false;
        for(String path: paths) {
            flag = false;
            for(MusicFile song: MusicFiles) {
                if(song.getPath().equals(path)) {
                    songs.add(song);
                    flag = true;
                    break;
                }
            }
            if(!flag) {
                paths_2.remove(path);
            }
        }
        if(paths.size() != paths_2.size()) {
            tinyDB.putListString(name, paths_2);
        }
        return songs;
    }

    public void savePathName(ArrayList<String> paths, String name){
        tinyDB.putListString(name, paths);
    }

    public void createPL(ArrayList<String> title, ArrayList<String> path, ArrayList<Long> albumId) {
        PlaylistString = new ArrayList<>();
        for(int i = 0; i < title.size(); i++) {
            PL pl = new PL(title.get(i), path.get(i), albumId.get(i));
            Log.i(TAG, "createPL - " + pl.getTitle());
            PlaylistString.add(pl);
        }
    }

    public void putPL() {
        ArrayList<String> paths = new ArrayList<>();
        ArrayList<String> titles = new ArrayList<>();
        ArrayList<Long> albumIds = new ArrayList<>();
        for(com.mygame.music_alpha.PL pl: PlaylistString) {
            titles.add(pl.getTitle());
            paths.add(pl.getPath());
            albumIds.add(pl.getAlbumId());
            Log.e(TAG, "PL: - " + pl.getTitle());
        }
        tinyDB.putListString(KEY_LIST_PLAYLIST_TITLE, titles);
        tinyDB.putListString(KEY_LIST_PLAYLIST_PATH, paths);
        tinyDB.putListLong(KEY_LIST_PLAYLIST_ALBUM_ID, albumIds);
    }

    public void putNewPlaylist(String name) {
        ArrayList<String> paths = new ArrayList<>();
        tinyDB.putListString(name, paths);
        PlaylistString.add(new PL(name));
        putPL();
    }

    public void renewPL(int position, String name) {
        ArrayList<String> paths = new ArrayList<>();
        tinyDB.putListString(name, paths);
        PlaylistString.set(position, new PL(name));
        putPL();
    }

    public ArrayList<String> fromToString(ArrayList<MusicFile> musicFiles) {
        ArrayList<String> paths = new ArrayList<>();
        for(MusicFile song: musicFiles) {
            paths.add(song.getPath());
        }
        return paths;
    }

    public ArrayList<String> getPlaylistName(String name) {
        return tinyDB.getListString(name);
    }

    public int PLsize() { return PlaylistString.size();}

    public void setSongPath(String path) {
        songPath = path;
    }

    public String getSongPath() {
        return songPath;
    }

    public void removePL(PL song) {
        tinyDB.remove(song.getTitle());
        PlaylistString.remove(song);
        putPL();
    }

    public int convertDpToPixel(int dp, Context context){
        return dp * ((int) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public void tabFavorite(MusicFile musicFile) {
        if(musicFile.getFavorite()) {
            musicFile.setFavorite(false);
            FavoriteFiles.remove(musicFile);
        } else {
            musicFile.setFavorite(true);
            FavoriteFiles.add(musicFile);
        }
    }
}

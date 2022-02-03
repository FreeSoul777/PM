package com.mygame.music_alpha;

import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

public class MusicFragment extends Fragment {

    private static final String TAG = "MusicFragment";

    private static final String KEY_K = "KEY_K";
    public static final String KEY_BORDER_ALBUM = "KEY_BORDER_ALBUM";
    public static final String KEY_BORDER_ARTIST = "KEY_BORDER_ARTIST";
    public static final String KEY_BORDER_PLAYLIST = "KEY_BORDER_PLAYLIST";
    public static final String KEY_ORDER = "KEY_ORDER";
    public static final String KEY_POSITION_PLAY = "KEY_POSITION_PLAY";
    public static final String KEY_QUEUE = "KEY_QUEUE";
    public static final String KEY_FAVORITE = "KEY_FAVORITE";
    public static final String KEY_LIST_PLAYLIST_TITLE = "KEY_LIST_PLAYLIST_TITLE";
    public static final String KEY_LIST_PLAYLIST_PATH = "KEY_LIST_PLAYLIST_PATH";
    public static final String KEY_LIST_PLAYLIST_ALBUM_ID = "KEY_LIST_PLAYLIST_ALBUM_ID";
    public static final String KEY_SHUFFLE = "KEY_SHUFFLE";
    public static final String KEY_REPEAT = "KEY_REPEAT";
    public static final String KEY_FLAG = "KEY_FLAG";
    private TinyDB tinyDB;


    private TabLayout tabLayout;
    private ViewPager2 viewPager2;
    private ViewPagerAdapter mAdapter;

    public static MusicFragment newInstance() {
        return new MusicFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");
        tinyDB = new TinyDB(getContext());
        saveData();
        FragmentManager fm = getChildFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.bottom_mini_player);
        if (fragment == null) {
            fragment =  MiniPlayer.newInstance();
            fm.beginTransaction().add(R.id.bottom_mini_player, fragment).commit();
        }

        Log.i(TAG, "mini_fragment - ok");

        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView()");
        View v = inflater.inflate(R.layout.fragment_music, container, false);
        tabLayout = (TabLayout) v.findViewById(R.id.music_tab_layout);
        viewPager2 = (ViewPager2) v.findViewById(R.id.music_view_pager2);

        mAdapter = new ViewPagerAdapter(getChildFragmentManager(), getLifecycle());
        mAdapter.addFragment(new SongFragment(), "Songs");
        mAdapter.addFragment(new AlbumFragment(), "Albums");
        mAdapter.addFragment(new ArtistFragment(), "Artists");
        mAdapter.addFragment(new PlaylistFragment(), "Playlists");
        viewPager2.setOffscreenPageLimit(4);
        viewPager2.setAdapter(mAdapter);
        new TabLayoutMediator(tabLayout, viewPager2,(tab, position) ->
                tab.setText(mAdapter.getPageTitle(position))).attach();

        return v;
    }

    private class ViewPagerAdapter extends FragmentStateAdapter {

        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        public ViewPagerAdapter(FragmentManager fragmentManager, Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        void addFragment(Fragment fragment, String title) {
            fragments.add(fragment);
            titles.add(title);
        }

        @Override
        public Fragment createFragment(int position) {
            return fragments.get(position);
        }

        @Override
        public int getItemCount() {
            return fragments.size();
        }

        @Nullable
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
        menuInflater.inflate(R.menu.fragment_menu_search, menu);
        MenuItem menuItem = menu.findItem(R.id.search_option);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint(getString(R.string.search));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String userInput = newText.toLowerCase();
                ArrayList<MusicFile> mySongFiles = new ArrayList<>();
                ArrayList<MusicFile> myAlbumFiles = new ArrayList<>();
                ArrayList<MusicFile> myArtistFiles = new ArrayList<>();
                for(MusicFile song: MusicLab.get(getContext()).getSongFiles()){
                    if(song.getTitle().toLowerCase().contains(userInput)){
                        mySongFiles.add(song);
                    }
                }
                for(MusicFile album: MusicLab.get(getContext()).getAlbumFiles()){
                    if(album.getAlbum().toLowerCase().contains(userInput)){
                        myAlbumFiles.add(album);
                    }
                }
                for(MusicFile artist: MusicLab.get(getContext()).getArtistFiles()){
                    if(artist.getArtist().toLowerCase().contains(userInput)){
                        myArtistFiles.add(artist);
                    }
                }
                SongFragment.updateSongFiles(mySongFiles);
                AlbumFragment.updateAlbumFiles(myAlbumFiles);
                ArtistFragment.updateArtistFiles(myArtistFiles);
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }



    private void saveData() {
        ArrayList<String> pathsQ = new ArrayList<>();
        ArrayList<String> pathsF = new ArrayList<>();
        ArrayList<String> playlist_title = new ArrayList<>();
        ArrayList<String> playlist_paths = new ArrayList<>();
        ArrayList<Long> playlist_id = new ArrayList<>();
//        tinyDB.clear();
        boolean key = tinyDB.getBoolean(KEY_K);
        if(!key) {
            Log.e(TAG, "key = false");
            tinyDB.putBoolean(KEY_K, true);
            tinyDB.putBoolean(KEY_BORDER_ALBUM, true);
            tinyDB.putBoolean(KEY_BORDER_ARTIST, false);
            tinyDB.putBoolean(KEY_BORDER_PLAYLIST, true);
            tinyDB.putBoolean(KEY_SHUFFLE, false);
            tinyDB.putBoolean(KEY_REPEAT, false);
            tinyDB.putBoolean(KEY_FLAG, true);
            tinyDB.putInt(KEY_POSITION_PLAY, -1);
            tinyDB.putString(KEY_ORDER, MediaStore.MediaColumns.DATE_MODIFIED + " DESC");
            tinyDB.putListString(KEY_QUEUE, pathsQ);
            tinyDB.putListString(KEY_FAVORITE, pathsF);
            tinyDB.putListString(KEY_LIST_PLAYLIST_TITLE, playlist_title);
            tinyDB.putListString(KEY_LIST_PLAYLIST_PATH, playlist_paths);
            tinyDB.putListLong(KEY_LIST_PLAYLIST_ALBUM_ID, playlist_id);

        }
        MusicLab.BORDER_ALBUM = tinyDB.getBoolean(KEY_BORDER_ALBUM);
        MusicLab.BORDER_ARTIST = tinyDB.getBoolean(KEY_BORDER_ARTIST);
        MusicLab.BORDER_PLAYLIST = tinyDB.getBoolean(KEY_BORDER_PLAYLIST);
        MusicLab.SHUFFLE = tinyDB.getBoolean(KEY_SHUFFLE);
        MusicLab.REPEAT = tinyDB.getBoolean(KEY_REPEAT);
        MusicLab.FLAG = tinyDB.getBoolean(KEY_FLAG);
        MusicLab.POSITION_PLAY = tinyDB.getInt(KEY_POSITION_PLAY);
        MusicLab.ORDER = tinyDB.getString(KEY_ORDER);
        switch (MusicLab.ORDER) {
            case MediaStore.Audio.Media.DATE_MODIFIED + " DESC":
                MusicLab.SORT_SONGS = "sortByDate";
                break;
            case MediaStore.Audio.Media.TITLE + " COLLATE LOCALIZED ASC":
                MusicLab.SORT_SONGS = "sortByName";
                break;
            case MediaStore.Audio.Media.ALBUM + " COLLATE LOCALIZED ASC":
                MusicLab.SORT_SONGS = "sortByAlbum";
                break;
            case MediaStore.Audio.Media.ARTIST + " COLLATE LOCALIZED ASC":
                MusicLab.SORT_SONGS = "sortByArtist";
                break;
            default:
                MusicLab.SORT_SONGS = "sortByDate";
        }
        MusicLab.get(getContext()).newMusicFiles(MusicLab.ORDER);
        pathsQ = tinyDB.getListString(KEY_QUEUE);
        pathsF = tinyDB.getListString(KEY_FAVORITE);
        MusicLab.get(getContext()).getSaveQueueAndFavorite(pathsQ, pathsF);
        if(MusicLab.get(getContext()).Qsize() == 0) MusicLab.POSITION_PLAY = -1;
        Log.e(TAG, "position - " + MusicLab.POSITION_PLAY + " Queue.size() - "
                + MusicLab.get(getContext()).Qsize());

        playlist_title = tinyDB.getListString(KEY_LIST_PLAYLIST_TITLE);
        playlist_paths = tinyDB.getListString(KEY_LIST_PLAYLIST_PATH);
        playlist_id = tinyDB.getListLong(KEY_LIST_PLAYLIST_ALBUM_ID);
        MusicLab.get(getContext()).createPL(playlist_title, playlist_paths, playlist_id);
        MusicLab.get(getContext()).setSongPath(MusicLab.POSITION_PLAY != -1 ?
                pathsQ.get(MusicLab.POSITION_PLAY) : "");
        Log.e(TAG, "PL.size() - " + MusicLab.get(getContext()).PLsize());
    }


    @Override
    public void onStart(){
        super.onStart();
        Log.i(TAG, "onStart()");
    }
    @Override
    public void onResume(){
        super.onResume();
        Log.i(TAG, "onResume()");
    }
    @Override
    public void onPause(){
        super.onPause();
        Log.i(TAG, "onPause()");

        tinyDB.putBoolean(KEY_BORDER_ALBUM, MusicLab.BORDER_ALBUM);
        tinyDB.putBoolean(KEY_BORDER_ARTIST, MusicLab.BORDER_ARTIST);
        tinyDB.putBoolean(KEY_SHUFFLE, MusicLab.SHUFFLE);
        tinyDB.putBoolean(KEY_REPEAT, MusicLab.REPEAT);
        tinyDB.putBoolean(KEY_FLAG, MusicLab.FLAG);
        tinyDB.putInt(KEY_POSITION_PLAY, MusicLab.POSITION_PLAY);
        tinyDB.putString(KEY_ORDER, MusicLab.ORDER);
        tinyDB.putListString(KEY_QUEUE, MusicLab.get(getContext()).putSaveQueue());
        tinyDB.putListString(KEY_FAVORITE, MusicLab.get(getContext()).putSaveFavorite());
        MusicLab.get(getContext()).putPL();
    }
    @Override
    public void onStop(){
        super.onStop();
        Log.i(TAG, "onStop()");
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.i(TAG, "onDestroy()");
    }
}


























package com.mygame.music_alpha;

import android.content.ContentUris;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.Arrays;

import static com.mygame.music_alpha.DialogMenuMore.RES;
import static com.mygame.music_alpha.MusicLab.ALBUM;
import static com.mygame.music_alpha.MusicLab.ARTIST;
import static com.mygame.music_alpha.MusicLab.SINGLE;
import static com.mygame.music_alpha.SongFragment.DIALOG_MENU_MORE;
import static com.mygame.music_alpha.SongFragment.FOR_RESULT;

public class GarbageDetails extends AppCompatActivity {

    private final String TAG = "GarbageDetails";

    private RecyclerView recyclerView;
    private GarbageAdapter garbageAdapter;
    private ImageView back, choice_all;
    private ImageButton delete, queue, add_playlist, more;
    private TextView count_selected;
    private ArrayList<MusicFile> PlaylistSongs;
    private ArrayList<MusicFile> GarbageSongs;
    private boolean[] Choice;
    private String what;
    private boolean choiceBoole;
    private View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");
        getSupportActionBar().hide();
        setContentView(R.layout.activity_deleted_details);

        recyclerView = findViewById(R.id.recyclerView);
        back = findViewById(R.id.back);
        delete = findViewById(R.id.delete);
        choice_all = findViewById(R.id.choice_all);
        queue = findViewById(R.id.queue);
        add_playlist = findViewById(R.id.add_playlist);
        more = findViewById(R.id.more);
        count_selected = findViewById(R.id.count_selected);

        choiceBoole = false;
        choice_all.setImageResource(R.drawable.ic_unchecked);
        count_selected.setText(getString(R.string.selected) + " 0");

        what = getIntent().getStringExtra(MusicLab.WHAT_2);
        ArrayList<MusicFile> playlist = MusicLab.get(getApplicationContext()).createPlaylist(what);
        MusicLab.get(this).setPlaylistFiles(playlist);
        PlaylistSongs = MusicLab.get(getApplicationContext()).getPlaylistFiles();

        MusicLab.get(this).setGarbageFiles();
        GarbageSongs = MusicLab.get(this).getGarbageFiles();
        Choice = new boolean[PlaylistSongs.size()];
        Arrays.fill(Choice, false);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(false);
        recyclerView.setNestedScrollingEnabled(false);
        garbageAdapter = new GarbageAdapter(PlaylistSongs);
        recyclerView.setAdapter(garbageAdapter);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        queue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queueDialog();
            }
        });
        add_playlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPlaylistDialog();
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDialog();
            }
        });
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreDialog();
            }
        });
        choice_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(choiceBoole) {
                    choiceBoole = false;
                    choice_all.setImageResource(R.drawable.ic_unchecked);
                    GarbageSongs.clear();
                    count_selected.setText(getString(R.string.selected)+ " " + GarbageSongs.size());
                    Arrays.fill(Choice, false);
                    Log.i(TAG, GarbageSongs.size() + " GarbageSongs.size()");
                    garbageAdapter.notifyDataSetChanged();
                }
                else {
                    choiceBoole = true;
                    choice_all.setImageResource(R.drawable.ic_checked_2);
                    GarbageSongs.clear();
                    GarbageSongs.addAll(PlaylistSongs);
                    Arrays.fill(Choice, true);
                    count_selected.setText(getString(R.string.selected)+ " " + GarbageSongs.size());
                    Log.i(TAG, GarbageSongs.size() + " GarbageSongs.size()");
                    garbageAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private class GarbageHolder extends RecyclerView.ViewHolder {

        private TextView file_name, file_artist_name;
        private ImageView album_art, choiceImg;

        public GarbageHolder(@NonNull View itemView){
            super(itemView);
            file_name = itemView.findViewById(R.id.music_file_name);
            file_artist_name = itemView.findViewById(R.id.music_artist_name);
            album_art = itemView.findViewById(R.id.music_img);
            choiceImg = itemView.findViewById(R.id.choice);
        }

        public void bind(MusicFile musicFile, int positon) {
            if(what.equals("SongFragment")) {
                file_name.setText(musicFile.getTitle());
                file_artist_name.setText((musicFile.getArtist() + " | " + musicFile.getAlbum()));
            }
            else if(what.equals("AlbumFragment")) {
                file_name.setText(musicFile.getAlbum());
                file_artist_name.setText("");
            }
            else if(what.equals("ArtistFragment")) {
                file_name.setText(musicFile.getArtist());
                file_artist_name.setText("");
            }
            else if(what.equals("ArtistAlbumDetails")) {
                file_name.setText(musicFile.getTitle());
                file_artist_name.setText((musicFile.getArtist() + " | " + musicFile.getAlbum()));
            }
            ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(getApplicationContext()));
            ImageLoader.getInstance().displayImage(ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"),
                    musicFile.getAlbumId()).toString(), album_art,
                    new DisplayImageOptions.Builder().cacheInMemory(true).showImageOnLoading(R.drawable.cover_art_2).
                            resetViewBeforeLoading(true).build());

            if(!Choice[positon]) {
                choiceImg.setImageResource(R.drawable.ic_unchecked);
            } else {
                choiceImg.setImageResource(R.drawable.ic_checked_2);
            }
        }
    }

    private class GarbageAdapter extends RecyclerView.Adapter<GarbageHolder> {

        private ArrayList<MusicFile> playlistFiles;

        public GarbageAdapter(ArrayList<MusicFile> playlistFiles) {
            this.playlistFiles = playlistFiles;
        }

        @Override
        public GarbageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            view = LayoutInflater.from(GarbageDetails.this).inflate(R.layout.delete_item, parent, false);
            return new GarbageHolder(view);
        }

        @Override
        public void onBindViewHolder(GarbageHolder holder, int position) {
            MusicFile musicFile = playlistFiles.get(position);
            holder.bind(musicFile, position);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(Choice[position]) {
                        GarbageSongs.remove(musicFile);
                        holder.choiceImg.setImageResource(R.drawable.ic_unchecked);
                        Choice[position] = false;
                        count_selected.setText(getString(R.string.selected)+ " " + GarbageSongs.size());
                        if(GarbageSongs.size() == 0 && choiceBoole) {
                            choiceBoole = false;
                            choice_all.setImageResource(R.drawable.ic_unchecked);
                        }
                    }
                    else {
                        GarbageSongs.add(musicFile);
                        holder.choiceImg.setImageResource(R.drawable.ic_checked_2);
                        Choice[position] = true;
                        count_selected.setText(getString(R.string.selected)+ " " + GarbageSongs.size());
                        if(GarbageSongs.size() == PlaylistSongs.size() && !choiceBoole) {
                            choiceBoole = true;
                            choice_all.setImageResource(R.drawable.ic_checked_2);
                        }
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return playlistFiles.size();
        }

        public void setMusic(ArrayList<MusicFile> songs) {
            playlistFiles = songs;
        }
    }


    private void queueDialog() {
        FragmentManager manager = this.getSupportFragmentManager();
        DialogMenuMore dialog = DialogMenuMore.newInstance(getSQ(), getString(R.string.queue));
        manager.setFragmentResultListener(FOR_RESULT, this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(String requestKey, Bundle result) {
                String res = (String) result.getSerializable(RES);
                if(res.equals(getString(R.string.play_next))) {
                    if(what.equals("SongFragment")) {
                        ALL(GarbageSongs, SINGLE, getString(R.string.play_next));
                    }
                    else if(what.equals("AlbumFragment")) {
                        ALL(GarbageSongs, ALBUM, getString(R.string.play_next));
                    }
                    else if(what.equals("ArtistFragment")) {
                        ALL(GarbageSongs, ARTIST, getString(R.string.play_next));
                    }
                    else if(what.equals("ArtistAlbumDetails")) {
                        ALL(GarbageSongs, SINGLE, getString(R.string.play_next));
                    }
                }
                else if(res.equals(getString(R.string.add_to_queue))) {
                    if(what.equals("SongFragment")) {
                        ALL(GarbageSongs, SINGLE, getString(R.string.add_to_queue));
                    }
                    else if(what.equals("AlbumFragment")) {
                        ALL(GarbageSongs, ALBUM, getString(R.string.add_to_queue));
                    }
                    else if(what.equals("ArtistFragment")) {
                        ALL(GarbageSongs, ARTIST, getString(R.string.add_to_queue));
                    }
                    else if(what.equals("ArtistAlbumDetails")) {
                        ALL(GarbageSongs, SINGLE, getString(R.string.add_to_queue));
                    }
                }
            }
        });
        dialog.show(manager, DIALOG_MENU_MORE);
    }

    private void addPlaylistDialog() {
        if(what.equals("SongFragment")) {
            ALL(GarbageSongs, SINGLE, getString(R.string.add));
        }
        else if(what.equals("AlbumFragment")) {
            ALL(GarbageSongs, ALBUM, getString(R.string.add));
        }
        else if(what.equals("ArtistFragment")) {
            ALL(GarbageSongs, ARTIST, getString(R.string.add));
        }
        else if(what.equals("ArtistAlbumDetails")) {
            ALL(GarbageSongs, SINGLE, getString(R.string.add));
        }
    }

    private void deleteDialog() {
        FragmentManager manager = this.getSupportFragmentManager();
        DialogMenuMore dialog = DialogMenuMore.newInstance(getSD(), getString(R.string.delete));
        manager.setFragmentResultListener(FOR_RESULT, this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(String requestKey, Bundle result) {
                String res = (String) result.getSerializable(RES);
                if(res.equals(getString(R.string.delete_forever))) {
                    if(what.equals("SongFragment")) {
                        ALL(GarbageSongs, SINGLE, getString(R.string.delete_forever));
                    }
                    else if(what.equals("AlbumFragment")) {
                        ALL(GarbageSongs, ALBUM, getString(R.string.delete_forever));
                    }
                    else if(what.equals("ArtistFragment")) {
                        ALL(GarbageSongs, ARTIST, getString(R.string.delete_forever));
                    }
                    else if(what.equals("ArtistAlbumDetails")) {
                        ALL(GarbageSongs, SINGLE, getString(R.string.delete_forever));
                    }
                }
            }
        });
        dialog.show(manager, DIALOG_MENU_MORE);
    }

    private void moreDialog() {
        FragmentManager manager = this.getSupportFragmentManager();
        DialogMenuMore dialog = DialogMenuMore.newInstance(getSS(), getString(R.string.share));
        manager.setFragmentResultListener(FOR_RESULT, this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(String requestKey, Bundle result) {
                String res = (String) result.getSerializable(RES);
                if(res.equals(getString(R.string.to_share))) {
                    if(what.equals("SongFragment")) {
                        ALL(GarbageSongs, SINGLE, getString(R.string.to_share));
                    }
                    else if(what.equals("AlbumFragment")) {
                        ALL(GarbageSongs, ALBUM, getString(R.string.to_share));
                    }
                    else if(what.equals("ArtistFragment")) {
                        ALL(GarbageSongs, ARTIST, getString(R.string.to_share));
                    }
                    else if(what.equals("ArtistAlbumDetails")) {
                        ALL(GarbageSongs, SINGLE, getString(R.string.to_share));
                    }
                }
            }
        });
        dialog.show(manager, DIALOG_MENU_MORE);
    }

    private String[] getSQ() {
        String[] s = {getString(R.string.play_next), getString(R.string.add_to_queue)};
        return s;
    }

    private String[] getSD() {
        String[] s = {getString(R.string.delete_forever)};
        return s;
    }

    private String[] getSS() {
        String[] s = {getString(R.string.to_share)};
        return s;
    }

    private void ALL(ArrayList<MusicFile> songs, String ORDER, String key) {
        Log.i(TAG, "key: " + key + " ORDER: " + ORDER);
        ArrayList<MusicFile> musicFiles = new ArrayList<>();
        if(ORDER.equals(SINGLE)) {
            musicFiles.addAll(songs);
        }
        else if(ORDER.equals(ALBUM)) {
            for (MusicFile song : songs) {
                musicFiles.addAll(MusicLab.get(this).createPlaylist(ORDER, song.getAlbum()));
            }
        }
        else if(ORDER.equals(ARTIST)) {
            for (MusicFile song : songs) {
                musicFiles.addAll(MusicLab.get(this).createPlaylist(ORDER, song.getArtist()));
            }
        }
        Log.i(TAG, " songs.size() - " + songs.size() + " musicFiles.size() - " + musicFiles.size());
        if(key.equals(getString(R.string.delete_forever))) {
            Log.i(TAG, getString(R.string.delete_forever) + "");
            com.mygame.music_alpha.ForAll.get(this).deleteAll(musicFiles);
            updateGarbageAdapter(GarbageSongs);
        }
        else if(key.equals(getString(R.string.play_next))) {
            Log.i(TAG, getString(R.string.play_next) + "");
            com.mygame.music_alpha.ForAll.get(this).playNextAll(musicFiles);
        }
        else if(key.equals(getString(R.string.add_to_queue))) {
            Log.i(TAG, getString(R.string.add_to_queue) + "");
            com.mygame.music_alpha.ForAll.get(this).addToQueueAll(musicFiles);
        }
        else if(key.equals(getString(R.string.add))) {
            Log.i(TAG, getString(R.string.add) + "");
            com.mygame.music_alpha.ForAll.get(this).saveGarbage(musicFiles);
            com.mygame.music_alpha.ForAll.get(this).addToPlaylist(musicFiles.get(0), this, "Garbage");
        }
        else if(key.equals(getString(R.string.to_share))) {
            Log.i(TAG, getString(R.string.to_share) + "");
            com.mygame.music_alpha.ForAll.get(this).shareMulti(musicFiles, GarbageDetails.this);
        }
    }

    private void updateGarbageAdapter(ArrayList<MusicFile> garbageSongs) {
        PlaylistSongs.removeAll(garbageSongs);
        Choice = new boolean[PlaylistSongs.size()];
        count_selected.setText(getString(R.string.selected) + "0");
        Arrays.fill(Choice, false);
        garbageAdapter.setMusic(PlaylistSongs);
        garbageAdapter.notifyDataSetChanged();
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

package com.mygame.music_alpha;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.request.RequestOptions;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.gpu.BrightnessFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.ContrastFilterTransformation;

import static com.mygame.music_alpha.DialogMenuMore.RES;
import static com.mygame.music_alpha.ForAll.*;
import static com.mygame.music_alpha.MusicLab.ALBUM;
import static com.mygame.music_alpha.MusicLab.ARTIST;
import static com.mygame.music_alpha.MusicLab.POSITION_PLAY;
import static com.mygame.music_alpha.MusicLab.POSITION_PLAY_NOW;
import static com.mygame.music_alpha.MusicLab.SINGLE;
import static com.mygame.music_alpha.MusicLab.WHAT;
import static com.mygame.music_alpha.MusicLab.WHAT_2;
import static com.mygame.music_alpha.SongFragment.DIALOG_MENU_MORE;
import static com.mygame.music_alpha.SongFragment.FOR_RESULT;

public class ArtistAlbumDetails extends AppCompatActivity {

    private static final String TAG = "ArtistAlbumDetails";

    private RecyclerView recyclerView;
    private static PlaylistSongsAdapter playlistSongsAdapter;
    private ImageView playlistPhoto, back, menu, background_player;
    private TextView name_playlist;
    private ArrayList<MusicFile> PlaylistSongs = new ArrayList<>();
    private Context context;
    private static String oldPath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");
        getSupportActionBar().hide();
        setContentView(R.layout.activity_album_and_artist_details);

        recyclerView = findViewById(R.id.recyclerView);
        playlistPhoto = findViewById(R.id.image_view_playlist);
        name_playlist = findViewById(R.id.playlist_name);
        background_player = findViewById(R.id.background_player);
        back = findViewById(R.id.back_btn);
        menu = findViewById(R.id.menu_btn);

        String what = getIntent().getStringExtra(WHAT);
        String name = getIntent().getStringExtra(what);
        ArrayList<MusicFile> playlist = MusicLab.get(getApplicationContext()).createPlaylist(what, name);
        MusicLab.get(getApplicationContext()).setPlaylistFiles(playlist);
        PlaylistSongs = MusicLab.get(getApplicationContext()).getPlaylistFiles();
        oldPath = MusicLab.get(getApplicationContext()).getSongPath();

        name_playlist.setText(name);
        context = ArtistAlbumDetails.this;
        metaData();


        recyclerView.setLayoutManager(new LinearLayoutManager(ArtistAlbumDetails.this));
        recyclerView.setHasFixedSize(false);
        recyclerView.setNestedScrollingEnabled(false);
        playlistSongsAdapter = new PlaylistSongsAdapter(PlaylistSongs);
        recyclerView.setAdapter(playlistSongsAdapter);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicFile musicFile = PlaylistSongs.get(0);
                FragmentManager manager = getSupportFragmentManager();
                DialogMenuMore dialog = DialogMenuMore.newInstance(getSD(),
                        what.equals(ALBUM) ? musicFile.getAlbum() : musicFile.getArtist());
                manager.setFragmentResultListener(FOR_RESULT, ArtistAlbumDetails.this, new FragmentResultListener() {
                    @Override
                    public void onFragmentResult(String requestKey, Bundle result) {
                        String res = (String) result.getSerializable(RES);
                        if(res.equals(getString(R.string.add_to_playlist))) {
                            get(context).addToPlaylist(musicFile, ArtistAlbumDetails.this,
                                    what.equals(ALBUM) ? ALBUM : ARTIST);
                        }
                        else if(res.equals(getString(R.string.delete_forever))) {
                            Log.i(TAG, "до PlaylistSongs.size() - " + PlaylistSongs.size());
                            ForAll.get(context).deleteAll(PlaylistSongs);
                            PlaylistSongs = new ArrayList<>();
                            Log.i(TAG, "после PlaylistSongs.size() - " + PlaylistSongs.size());
                            if(PlaylistSongs.isEmpty()) finish();
                        }
                        else if(res.equals(getString(R.string.to_share))) {
                            ForAll.get(context).shareMulti(PlaylistSongs, context);
                        }
                    }
                });
                dialog.show(manager, DIALOG_MENU_MORE);
            }
        });
    }

    private void metaData() {
        if (!PlaylistSongs.isEmpty()) {
            Uri uri = Uri.parse(PlaylistSongs.get(0).getPath());
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(uri.toString());
            byte[] art = retriever.getEmbeddedPicture();
            if (art != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
                Glide.with(this).load(bitmap).into(playlistPhoto);
                Glide.with(context)
                        .load(bitmap)
                        .apply(RequestOptions.bitmapTransform(new MultiTransformation(
                                new BlurTransformation(10, 3),
                                new ContrastFilterTransformation(0.5F),
                                new BrightnessFilterTransformation(-0.3F))))
                        .into(background_player);
            } else {
                Glide.with(this).asBitmap().load(R.drawable.cover_art_2).into(playlistPhoto);
                background_player.setImageResource(android.R.color.transparent);
            }
        }
        else {
            Glide.with(this).asBitmap().load(R.drawable.cover_art_2).into(playlistPhoto);
            background_player.setImageResource(android.R.color.transparent);
        }
    }

    private class PlaylistSongsHolder extends RecyclerView.ViewHolder {

        private TextView file_name, file_artist_name;
        private ImageView album_art, menuMore;

        public PlaylistSongsHolder(LayoutInflater inflater, ViewGroup parent){
            super(inflater.inflate(R.layout.music_items, parent, false));
            file_name = itemView.findViewById(R.id.music_file_name);
            file_artist_name = itemView.findViewById(R.id.music_artist_name);
            album_art = itemView.findViewById(R.id.music_img);
            menuMore = itemView.findViewById(R.id.menuMore);
        }

        public void bind(MusicFile musicFile) {
            file_name.setText(musicFile.getTitle());
            file_artist_name.setText((musicFile.getArtist() + " | " + musicFile.getAlbum()));
            ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(getApplicationContext()));
            ImageLoader.getInstance().displayImage(ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"),
                    musicFile.getAlbumId()).toString(), album_art,
                    new DisplayImageOptions.Builder().cacheInMemory(true).showImageOnLoading(R.drawable.cover_art_2).
                            resetViewBeforeLoading(true).build());
        }
    }

    private class PlaylistSongsAdapter extends RecyclerView.Adapter<PlaylistSongsHolder> {

        private ArrayList<MusicFile> playSongsFiles;

        public PlaylistSongsAdapter(ArrayList<MusicFile> playSongsFiles) {
            this.playSongsFiles = playSongsFiles;
        }

        @Override
        public PlaylistSongsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(ArtistAlbumDetails.this);
            return new PlaylistSongsHolder(layoutInflater, parent);
        }


        @Override
        public void onBindViewHolder(PlaylistSongsHolder holder, int position) {
            MusicFile musicFile = playSongsFiles.get(position);
            holder.bind(musicFile);
            if (musicFile.getPath().equals(MusicLab.get(ArtistAlbumDetails.this).getSongPath())) {
                holder.itemView.setBackgroundResource(R.color.primary_dark);
            }
            else {
                holder.itemView.setBackgroundResource(R.color.black_and_gray);
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MusicLab.get(getApplicationContext()).setNowPlaying(PlaylistSongs);
                    Intent intent = new Intent(getApplicationContext(), Player.class);
                    intent.putExtra(POSITION_PLAY_NOW, position);
                    intent.putExtra(Player.TAG, TAG);
                    POSITION_PLAY = position;
                    startActivity(intent);
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
//                    Toast.makeText(context, "CLICK LONG", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(context, com.mygame.music_alpha.GarbageDetails.class);
                    intent.putExtra(WHAT_2, "ArtistAlbumDetails");
                    startActivity(intent);
                    return true;
                }
            });
            holder.menuMore.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(final View v){
                    FragmentManager manager = getSupportFragmentManager();
                    DialogMenuMore dialog = DialogMenuMore.newInstance(getS(), musicFile.getTitle());
                    manager.setFragmentResultListener(FOR_RESULT, ArtistAlbumDetails.this, new FragmentResultListener() {
                        @Override
                        public void onFragmentResult(String requestKey, Bundle result) {
                            String res = (String) result.getSerializable(RES);
                            if(res.equals(getString(R.string.play_next))) {
                                get(context).playNext(musicFile);
                            }
                            else if(res.equals(getString(R.string.add_to_queue))) {
                                get(context).addToQueue(musicFile);
                            }
                            else if(res.equals(getString(R.string.add_to_playlist))) {
                                get(context).addToPlaylist(musicFile, ArtistAlbumDetails.this, SINGLE);
                            }
                            else if(res.equals(getString(R.string.delete_forever))) {
                                get(context).delete(musicFile);
                                playSongsFiles.remove(musicFile);
                                playlistSongsAdapter.notifyItemRemoved(position);
                                if(PlaylistSongs.isEmpty()) finish();
                            }
                            else if(res.equals(getString(R.string.to_share))) {
                                get(context).share(musicFile, context);
                            }
                        }
                    });
                    dialog.show(manager, DIALOG_MENU_MORE);
                }
            });
        }

        @Override
        public int getItemCount() {
            return playSongsFiles.size();
        }

        public void setMusic(ArrayList<MusicFile> songs) {
            playSongsFiles = songs;
        }
    }

    private String[] getS() {
        String[] s = {getString(R.string.play_next), getString(R.string.add_to_queue),
                getString(R.string.add_to_playlist), getString(R.string.delete_forever), getString(R.string.to_share)};
        return s;
    }
    private String[] getSD() {
        String[] s = {getString(R.string.add_to_playlist), getString(R.string.delete_forever),
                getString(R.string.to_share)};
        return s;
    }

    private void updateArtistAlbumDetails() {
        PlaylistSongs = MusicLab.get(context).getPlaylistFiles();
        Log.d(TAG, PlaylistSongs.size() + " - songs, updateAdapter");
        playlistSongsAdapter.setMusic(PlaylistSongs);
        playlistSongsAdapter.notifyDataSetChanged();
    }

    public static void updateNotifyItem(String newPath) {
        if(playlistSongsAdapter != null) {
            for (int i = 0; i < playlistSongsAdapter.playSongsFiles.size(); i++) {
                if (playlistSongsAdapter.playSongsFiles.get(i).getPath().equals(newPath) ||
                        playlistSongsAdapter.playSongsFiles.get(i).getPath().equals(oldPath)) {
                    playlistSongsAdapter.notifyItemChanged(i);
                }
            }
        }
        oldPath = newPath;
        Log.i(TAG, "updateNotifyItem");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart()");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()");
        if(playlistSongsAdapter != null) {
            updateArtistAlbumDetails();
        }
        metaData();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("MyLog", "ArtistAlbumDetails, onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "onStop()");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy()");
    }

}

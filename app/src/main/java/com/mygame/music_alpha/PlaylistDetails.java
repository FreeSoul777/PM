package com.mygame.music_alpha;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.Collections;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

import static com.mygame.music_alpha.DialogAddPlaylist.DIALOG_ADD;
import static com.mygame.music_alpha.DialogMenuMore.RES;
import static com.mygame.music_alpha.MusicLab.POSITION_PLAY;
import static com.mygame.music_alpha.MusicLab.POSITION_PLAY_NOW;
import static com.mygame.music_alpha.MusicLab.WHAT;
import static com.mygame.music_alpha.SongFragment.DIALOG_ADD_PLAYLIST;
import static com.mygame.music_alpha.SongFragment.DIALOG_MENU_MORE;
import static com.mygame.music_alpha.SongFragment.FOR_RESULT;
import static com.mygame.music_alpha.SongFragment.FOR_RESULT_ADD;

public class PlaylistDetails extends AppCompatActivity {

    private static final String TAG = "PlaylistDetails";

    private RecyclerView recyclerView;
    private static PlaylistSongsAdapter playlistSongsAdapter;
    private ImageView playlistPhoto, back, menu;
    private ImageButton delete_in_playlist;
    private TextView name_playlist, count_singles;
    private ArrayList<MusicFile> PlaylistSongs = new ArrayList<>();
    private Context context;
    private String name;
    private int pl_position;
    private static String oldPath = "";
    private TinyDB tinyDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");
        getSupportActionBar().hide();
        setContentView(R.layout.activity_playlist_details);

        recyclerView = findViewById(R.id.recyclerView);
        playlistPhoto = findViewById(R.id.image_view_playlist);
        name_playlist = findViewById(R.id.playlist_name);
        back = findViewById(R.id.back_btn);
        delete_in_playlist = findViewById(R.id.delete_in_playlist);
        count_singles = findViewById(R.id.count_singles);
        menu = findViewById(R.id.menu_btn);

        String what = getIntent().getStringExtra(WHAT);
        name = getIntent().getStringExtra(what);
        pl_position = getIntent().getIntExtra("pl_position", -1);
        ArrayList<MusicFile> playlist = MusicLab.get(getApplicationContext()).createPlaylist(what, name);
        MusicLab.get(getApplicationContext()).setPlaylistFiles(playlist);
        PlaylistSongs = MusicLab.get(getApplicationContext()).getPlaylistFiles();
        oldPath = MusicLab.get(getApplicationContext()).getSongPath();

        name_playlist.setText(name);
        metaData();
        count_singles.setText(PlaylistSongs.size() + " " + getString(R.string.singles));

        context = PlaylistDetails.this;
        tinyDB = new TinyDB(context);

        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setHasFixedSize(false);
        recyclerView.setNestedScrollingEnabled(false);
        playlistSongsAdapter = new PlaylistSongsAdapter(PlaylistSongs);
        recyclerView.setAdapter(playlistSongsAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(PlaylistSongs.isEmpty()) {
                    Toast.makeText(context, R.string.isEmptyPL, Toast.LENGTH_SHORT).show();
                }
                else {
                    com.mygame.music_alpha.MusicFile musicFile = PlaylistSongs.get(0);
                    FragmentManager manager = getSupportFragmentManager();
                    DialogMenuMore dialog = DialogMenuMore.newInstance(getSS(), name);
                    manager.setFragmentResultListener(FOR_RESULT, PlaylistDetails.this, new FragmentResultListener() {
                        @Override
                        public void onFragmentResult(String requestKey, Bundle result) {
                            String res = (String) result.getSerializable(RES);
                            if (res.equals(getString(R.string.add_to_playlist))) {
                                ForAll.get(context).saveGarbage(PlaylistSongs);
                                ForAll.get(context).addToPlaylist(musicFile, PlaylistDetails.this, "Garbage");
                                ArrayList<MusicFile> playlist =
                                        MusicLab.get(getApplicationContext()).createPlaylist(what, name);
                                MusicLab.get(getApplicationContext()).setPlaylistFiles(playlist);
                                updatePlaylistDetails(playlist);
                            } else if (res.equals(getString(R.string.to_share))) {
                                ForAll.get(context).shareMulti(PlaylistSongs, context);
                            }
                        }
                    });
                    dialog.show(manager, DIALOG_MENU_MORE);
                }
            }
        });
        delete_in_playlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getSupportFragmentManager();
                DialogMenuMore dialog = DialogMenuMore.newInstance(getSD(), getString(R.string.delete));
                manager.setFragmentResultListener(FOR_RESULT, PlaylistDetails.this, new FragmentResultListener() {
                    @Override
                    public void onFragmentResult(String requestKey, Bundle result) {
                        String res = (String) result.getSerializable(RES);
                        if(res.equals(getString(R.string.delete))) {
                            PlaylistSongs.clear();
                            ForAll.get(context).deletePL(pl_position);
                            finish();
                        }
                        else if(res.equals(getString(R.string.delete_forever))) {
                            PlaylistSongs.clear();
                            ForAll.get(context).deletePLforever(pl_position);
                            finish();
                        }
                        else if(res.equals(getString(R.string.clear_playlist))) {
                            MusicLab.get(context).renewPL(pl_position, name);
                            updatePlaylistDetails(new ArrayList<>());
                        }
                    }
                });
                dialog.show(manager, DIALOG_MENU_MORE);
            }
        });
        name_playlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getSupportFragmentManager();
                DialogAddPlaylist dialog = DialogAddPlaylist.newInstance();
                manager.setFragmentResultListener(FOR_RESULT_ADD, PlaylistDetails.this, new FragmentResultListener() {
                    @Override
                    public void onFragmentResult(String requestKey, Bundle result) {
                        String new_name = (String) result.getSerializable(DIALOG_ADD);
                        Log.e(TAG, name_playlist + " " + pl_position);
                        ForAll.get(context).newName(new_name, name_playlist.getText().toString());
                        MusicLab.get(context).getPlaylistString().get(pl_position).setTitle(new_name);
                        name_playlist.setText(new_name);
                        name = new_name;
                    }
                });
                dialog.show(manager, DIALOG_ADD_PLAYLIST);
            }
        });
    }

    private void metaData() {
        if(!PlaylistSongs.isEmpty()) {
            Uri uri = Uri.parse(PlaylistSongs.get(0).getPath());
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(uri.toString());
            byte[] art = retriever.getEmbeddedPicture();
            if (art != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
                Glide.with(this).load(bitmap).into(playlistPhoto);
            } else {
                Glide.with(this).asBitmap().load(R.drawable.cover_art_2).into(playlistPhoto);
            }
        }
        else {
            Glide.with(this).asBitmap().load(R.drawable.cover_art_2).into(playlistPhoto);
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

        public void bind(com.mygame.music_alpha.MusicFile musicFile) {
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
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            return new PlaylistSongsHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(PlaylistSongsHolder holder, int position) {
            MusicFile musicFile = playSongsFiles.get(holder.getAbsoluteAdapterPosition());
            holder.bind(musicFile);
            if (musicFile.getPath().equals(MusicLab.get(context).getSongPath())) {
                holder.itemView.setBackgroundResource(R.color.primary_dark);
            }
            else {
                holder.itemView.setBackgroundResource(R.color.black_and_gray);
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MusicLab.get(context).setNowPlaying(PlaylistSongs);
                    Intent intent = new Intent(context, Player.class);
                    intent.putExtra(POSITION_PLAY_NOW, holder.getAbsoluteAdapterPosition());
                    intent.putExtra(Player.TAG, TAG);
                    POSITION_PLAY = holder.getAbsoluteAdapterPosition();
                    startActivity(intent);
                }
            });
            holder.menuMore.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(final View v){
                    FragmentManager manager = getSupportFragmentManager();
                    DialogMenuMore dialog = DialogMenuMore.newInstance(getS(), musicFile.getTitle());
                    manager.setFragmentResultListener(FOR_RESULT, PlaylistDetails.this, new FragmentResultListener() {
                        @Override
                        public void onFragmentResult(String requestKey, Bundle result) {
                            String res = (String) result.getSerializable(RES);
                            if(res.equals(getString(R.string.play_next))) {
                                ForAll.get(context).playNext(musicFile);
                            }
                            else if(res.equals(getString(R.string.add_to_queue))) {
                                ForAll.get(context).addToQueue(musicFile);
                            }
                            else if(res.equals(getString(R.string.add_to_playlist))) {

                            }
                            else if(res.equals(getString(R.string.delete))) {
                                playSongsFiles.remove(musicFile);
                                playlistSongsAdapter.notifyItemRemoved(position);
                                metaData();
                            }
                            else if(res.equals(getString(R.string.delete_forever))) {
                                while(playSongsFiles.contains(musicFile)) {
                                    playSongsFiles.remove(musicFile);
                                }
                                updatePlaylistDetails(playSongsFiles);
                                ForAll.get(context).delete(musicFile);
                            }
                            else if(res.equals(getString(R.string.to_share))) {
                                ForAll.get(context).share(musicFile, PlaylistDetails.this);
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
                getString(R.string.add_to_playlist), getString(R.string.delete),
                getString(R.string.delete_forever), getString(R.string.to_share)};
        return s;
    }
    private String[] getSD() {
        String[] s = {getString(R.string.delete), getString(R.string.delete_forever), getString(R.string.clear_playlist)};
        return s;
    }
    private String[] getSS() {
        String[] s = {getString(R.string.add_to_playlist), getString(R.string.to_share)};
        return s;
    }

    private void updatePlaylistDetails(ArrayList<MusicFile> playlistSongs) {
        PlaylistSongs = new ArrayList<>();
        PlaylistSongs.addAll(playlistSongs);
        Log.d(TAG, PlaylistSongs.size() + " - songs, updateAdapter");
        count_singles.setText(PlaylistSongs.size() + " singles");
        playlistSongsAdapter.setMusic(PlaylistSongs);
        playlistSongsAdapter.notifyDataSetChanged();
        metaData();
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
        Log.i(TAG, "onResume(), PlaylistSongs.size() - " + PlaylistSongs.size());
    }

    @Override
    public void onPause() {
        com.mygame.music_alpha.PlaylistFragment.updateNotify();
        super.onPause();
        Log.i(TAG, pl_position + " pl_position, " + PlaylistSongs.size() + " PlaylistSize");
        if(pl_position != -1 && !PlaylistSongs.isEmpty()) {
            MusicLab.get(context).getPlaylistString().get(pl_position).setAlbumId(PlaylistSongs.get(0).getAlbumId());
            MusicLab.get(context).getPlaylistString().get(pl_position).setPath(PlaylistSongs.get(0).getPath());
        }
        ArrayList<String> paths = MusicLab.get(context).fromToString(PlaylistSongs);
        MusicLab.get(context).savePathName(paths, name);
        Log.i(TAG, "onPause()");
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










    ItemTouchHelper.SimpleCallback itemTouchHelperCallback =
            new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.RIGHT) {

                MusicFile deletedSong = new com.mygame.music_alpha.MusicFile();

                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                      @NonNull RecyclerView.ViewHolder target) {

                    int fromPosition = viewHolder.getAbsoluteAdapterPosition();
                    int toPosition = target.getAbsoluteAdapterPosition();

                    Collections.swap(playlistSongsAdapter.playSongsFiles, fromPosition, toPosition);
                    playlistSongsAdapter.notifyItemMoved(fromPosition, toPosition);
                    if(toPosition == 0) {
                        metaData();
                    }
                    return false;
                }

                @Override
                public void onSwiped(@NonNull  RecyclerView.ViewHolder viewHolder, int direction) {

                    int position = viewHolder.getAbsoluteAdapterPosition();
                    deletedSong = playlistSongsAdapter.playSongsFiles.get(position);

                    playlistSongsAdapter.playSongsFiles.remove(position);
                    playlistSongsAdapter.notifyItemRemoved(position);
                    count_singles.setText(playlistSongsAdapter.playSongsFiles.size() + " singles");
                    if(position == 0) {
                        metaData();
                    }

                    Log.e(TAG, PlaylistSongs.size() + " PlaylistSongs.size() " +
                            playlistSongsAdapter.playSongsFiles.size() + " playSongsFiles in adapter");

                    Snackbar.make(recyclerView, deletedSong.getTitle(), Snackbar.LENGTH_SHORT)
                            .setAction("Undo", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(position <= POSITION_PLAY) {
                                        POSITION_PLAY += 1;
                                    }
                                    playlistSongsAdapter.playSongsFiles.add(position, deletedSong);
                                    Toast.makeText(context, "UNDO", Toast.LENGTH_LONG).show();
                                    playlistSongsAdapter.notifyItemInserted(position);
                                    count_singles.setText(playlistSongsAdapter.playSongsFiles.size() + " singles");

                                    metaData();
                                }
                            }).show();
                }



                @Override
                public void onChildDraw (Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                         float dX, float dY, int actionState, boolean isCurrentlyActive) {

                    new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                            .addBackgroundColor(ContextCompat.getColor(context, R.color.red))
                            .addActionIcon(R.drawable.ic_delete_item)
                            .create()
                            .decorate();

                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            };
}

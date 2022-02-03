package com.mygame.music_alpha;

import android.content.ContentUris;
import android.content.Intent;
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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.Collections;

import static com.mygame.music_alpha.DialogAddPlaylist.DIALOG_ADD;
import static com.mygame.music_alpha.DialogMenuMore.RES;
import static com.mygame.music_alpha.MusicLab.BORDER_PLAYLIST;
import static com.mygame.music_alpha.MusicLab.PLAYLIST;
import static com.mygame.music_alpha.MusicLab.WHAT;
import static com.mygame.music_alpha.SongFragment.DIALOG_ADD_PLAYLIST;
import static com.mygame.music_alpha.SongFragment.DIALOG_MENU_MORE;
import static com.mygame.music_alpha.SongFragment.FOR_RESULT;
import static com.mygame.music_alpha.SongFragment.FOR_RESULT_ADD;

public class PlaylistFragment extends Fragment {

    private static final String TAG = "PlaylistFragment";

    private RecyclerView recyclerView;
    private static MusicAdapter musicAdapter;
    private static ArrayList<PL> Playlists;
    private ImageButton select, border, list, sort, add_playlist;
    private View view;

    public PlaylistFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_playlist, container, false);

        Playlists = MusicLab.get(getContext()).getPlaylistString();

        select = (ImageButton) v.findViewById(R.id.select);
        border = (ImageButton) v.findViewById(R.id.border);
        list = (ImageButton) v.findViewById(R.id.list);
        sort = (ImageButton) v.findViewById(R.id.sort);
        add_playlist = (ImageButton) v.findViewById(R.id.add_playlist);
        recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);

        updateAdapter();
        musicAdapter = new MusicAdapter(Playlists);
        recyclerView.setAdapter(musicAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        if(MusicLab.get(getContext()).getMusicFiles().size() == 0) {
            sort.setEnabled(false);
            select.setEnabled(false);
        }

        sort.setVisibility(View.INVISIBLE);
        border.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!BORDER_PLAYLIST) {
                    BORDER_PLAYLIST = true;
                }
                updateAdapter();
                recyclerView.setAdapter(musicAdapter);
            }
        });
        list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(BORDER_PLAYLIST) {
                    BORDER_PLAYLIST = false;
                }
                updateAdapter();
                recyclerView.setAdapter(musicAdapter);
            }
        });

        add_playlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getActivity().getSupportFragmentManager();
                DialogAddPlaylist dialog = DialogAddPlaylist.newInstance();
                manager.setFragmentResultListener(FOR_RESULT_ADD, getActivity(), new FragmentResultListener() {
                    @Override
                    public void onFragmentResult(String requestKey, Bundle result) {
                        String new_name = (String) result.getSerializable(DIALOG_ADD);
                        add_new_playlist(new_name);
                    }
                });
                dialog.show(manager, DIALOG_ADD_PLAYLIST);
            }
        });

        return v;
    }


    private class PlaylistHolder extends RecyclerView.ViewHolder {

        private ImageView album_image, menuMore;
        private TextView album_name;

        public PlaylistHolder(@NonNull View itemView){
            super(itemView);
            album_image = itemView.findViewById(R.id.album_image);
            album_name = itemView.findViewById(R.id.album_name);
            menuMore = itemView.findViewById(R.id.menuMore);
        }

        public void bind(com.mygame.music_alpha.PL pl) {
            album_name.setText(pl.getTitle());
            ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(getActivity()));
            ImageLoader.getInstance().displayImage(ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"),
                    pl.getAlbumId()).toString(), album_image,
                    new DisplayImageOptions.Builder().cacheInMemory(true).showImageOnLoading(R.drawable.cover_art_2).
                            resetViewBeforeLoading(true).build());
        }
    }

    private class MusicAdapter extends RecyclerView.Adapter<PlaylistHolder> {

        private ArrayList<PL> playlistFiles;

        public MusicAdapter(ArrayList<PL> playlistFiles) {
            this.playlistFiles = playlistFiles;
        }

        @NonNull
        @Override
        public PlaylistHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if(BORDER_PLAYLIST) {
                view = LayoutInflater.from(getContext()).inflate(R.layout.album_item, parent, false);
            }
            else{
                view = LayoutInflater.from(getContext()).inflate(R.layout.artist_item, parent, false);
            }
            return new PlaylistHolder(view);
        }

        @Override
        public void onBindViewHolder(PlaylistHolder holder, int position) {
            PL musicFile = playlistFiles.get(position);
            holder.bind(musicFile);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), PlaylistDetails.class);
                    intent.putExtra(PLAYLIST, musicFile.getTitle());
                    intent.putExtra(WHAT, PLAYLIST);
                    intent.putExtra("pl_position", holder.getAbsoluteAdapterPosition());
                    startActivity(intent);
                }
            });
            holder.menuMore.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(final View v){
                    FragmentManager manager = getActivity().getSupportFragmentManager();
                    DialogMenuMore dialog = DialogMenuMore.newInstance(getS(), musicFile.getTitle());
                    manager.setFragmentResultListener(FOR_RESULT, getActivity(), new FragmentResultListener() {
                        @Override
                        public void onFragmentResult(String requestKey, Bundle result) {
                            String res = (String) result.getSerializable(RES);
                            if(res.equals(getString(R.string.play_next))) {
                                nextPlay(musicFile);
                            }
                            else if(res.equals(getString(R.string.add_to_queue))) {
                                queueAdd(musicFile);
                            }
                            else if(res.equals(getString(R.string.delete))) {
                                allDelete(musicFile, holder.getAbsoluteAdapterPosition());
                            }
                            else if(res.equals(getString(R.string.delete_forever))) {
                                allDeleteForever(musicFile);
                                allDelete(musicFile, holder.getAbsoluteAdapterPosition());
                            }
                            else if(res.equals(getString(R.string.to_share))) {
                                share(musicFile);
                            }
                        }
                    });
                    dialog.show(manager, DIALOG_MENU_MORE);
                }
            });
        }

        @Override
        public int getItemCount() {
            return playlistFiles.size();
        }

        public void setMusic(ArrayList<PL> playlists) {
            playlistFiles = playlists;
        }
    }

    private void nextPlay(PL song) {
        ArrayList<MusicFile> pl = MusicLab.get(getContext()).createPlaylist(PLAYLIST, song.getTitle());
        ForAll.get(getContext()).playNextAll(pl);
    }

    private void queueAdd(PL song) {
        ArrayList<MusicFile> pl = MusicLab.get(getContext()).createPlaylist(PLAYLIST, song.getTitle());
        ForAll.get(getContext()).addToQueueAll(pl);
    }

    private void allDelete(PL song, int position) {
        MusicLab.get(getContext()).removePL(song);
        Playlists.remove(song);
        musicAdapter.playlistFiles.remove(song);
        musicAdapter.notifyItemRemoved(position);
    }

    private void allDeleteForever(PL song) {
        ArrayList<MusicFile> pl = MusicLab.get(getContext()).createPlaylist(PLAYLIST, song.getTitle());
        ForAll.get(getContext()).deleteAll(pl);
    }

    private void share(PL song) {
        ArrayList<MusicFile> pl = MusicLab.get(getContext()).createPlaylist(PLAYLIST, song.getTitle());
        ForAll.get(getContext()).shareMulti(pl, getContext());
    }

    private String[] getS() {
        String[] s = {getString(R.string.play_next), getString(R.string.add_to_queue), getString(R.string.delete),
                getString(R.string.delete_forever), getString(R.string.to_share)};
        return s;
    }


    private void add_new_playlist(String name) {
        PL playlist = new PL(name);
        MusicLab.get(getContext()).putNewPlaylist(name);
        updatePlaylistFiles(MusicLab.get(getContext()).getPlaylistString());
        Log.i(TAG, "Playlists.size() - " + Playlists.size() +
                ", PL - " + MusicLab.get(getContext()).getPlaylistString().size());
        Intent intent = new Intent(getContext(), PlaylistDetails.class);
        intent.putExtra(PLAYLIST, playlist.getTitle());
        intent.putExtra(WHAT, PLAYLIST);
        intent.putExtra("pl_position", Playlists.size() == 0 ? 0 : Playlists.size() - 1);
        startActivity(intent);
    }

    private void updateAdapter() {
        if(BORDER_PLAYLIST) {
            recyclerView.setLayoutManager(new GridLayoutManager(getActivity(),
                    ForAll.calculateNoOfColumns(getActivity())));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        }
        recyclerView.setHasFixedSize(false);
        recyclerView.setNestedScrollingEnabled(false);
    }

    public static void updatePlaylistFiles(ArrayList<PL> playlists) {
        Log.i(TAG, "updatePlaylistFiles");
        Playlists = new ArrayList<>();
        Playlists.addAll(playlists);
        musicAdapter.setMusic(playlists);
        musicAdapter.notifyDataSetChanged();
        Log.e(TAG, "Playlists.size() - " + Playlists.size());
    }

    public static void updateNotify() {
        if(musicAdapter != null) {
            musicAdapter.notifyDataSetChanged();
        }
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
//        if(musicAdapter != null) {
//            musicAdapter.notifyDataSetChanged();
//        }
    }
    @Override
    public void onPause(){
        super.onPause();
        MusicLab.get(getContext()).putPL();
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
        MusicLab.get(getContext()).putPL();
        Log.i(TAG, "onDestroy()");
    }




    ItemTouchHelper.SimpleCallback itemTouchHelperCallback =
            new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN |
                    ItemTouchHelper.START | ItemTouchHelper.END, -1) {

                MusicFile deletedSong = new MusicFile();

                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView,  RecyclerView.ViewHolder viewHolder,
                                      @NonNull RecyclerView.ViewHolder target) {

                    int fromPosition = viewHolder.getAbsoluteAdapterPosition();
                    int toPosition = target.getAbsoluteAdapterPosition();

                    Collections.swap(musicAdapter.playlistFiles, fromPosition, toPosition);
                    musicAdapter.notifyItemMoved(fromPosition, toPosition);
                    return false;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                }

                @Override
                public int getMovementFlags(RecyclerView recyclerView,
                                            RecyclerView.ViewHolder viewHolder) {
                    int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN |
                            ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
                    int swipeFlags = 0;
                    return makeMovementFlags(dragFlags, swipeFlags);
                }
            };
}

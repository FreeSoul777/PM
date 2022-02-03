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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;

import static com.mygame.music_alpha.DialogMenuMore.RES;
import static com.mygame.music_alpha.DialogSorting.ORDER_DIALOG;
import static com.mygame.music_alpha.MusicLab.ALBUM;
import static com.mygame.music_alpha.MusicLab.BORDER_ALBUM;
import static com.mygame.music_alpha.MusicLab.SORT_SONGS;
import static com.mygame.music_alpha.MusicLab.WHAT;
import static com.mygame.music_alpha.MusicLab.WHAT_2;
import static com.mygame.music_alpha.SongFragment.DIALOG_CHOICE_SORT;
import static com.mygame.music_alpha.SongFragment.DIALOG_MENU_MORE;
import static com.mygame.music_alpha.SongFragment.FOR_RESULT;

public class AlbumFragment extends Fragment {

    private static final String TAG = "AlbumFragment";

    private RecyclerView recyclerView;
    private static MusicAdapter musicAdapter;
    private static ArrayList<MusicFile> Albums;
    private ImageButton select, border, list, sort;
    private View view;

    public AlbumFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView()");
        View v = inflater.inflate(R.layout.fragment_album_artist, container, false);
        Albums = MusicLab.get(getContext()).getAlbumFiles();

        select = (ImageButton) v.findViewById(R.id.select);
        border = (ImageButton) v.findViewById(R.id.border);
        list = (ImageButton) v.findViewById(R.id.list);
        sort = (ImageButton) v.findViewById(R.id.sort);
        recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);

        updateAdapter();
        musicAdapter = new MusicAdapter(Albums);
        recyclerView.setAdapter(musicAdapter);

        if(MusicLab.get(getContext()).getMusicFiles().size() == 0) {
            sort.setEnabled(false);
            select.setEnabled(false);
        }

        sort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getActivity().getSupportFragmentManager();
                DialogSorting dialog = DialogSorting.newInstance(SORT_SONGS);
                manager.setFragmentResultListener(FOR_RESULT, getActivity(), new FragmentResultListener() {
                    @Override
                    public void onFragmentResult(String requestKey, Bundle result) {
                        String order = (String) result.getSerializable(ORDER_DIALOG);
                        MusicLab.get(getContext()).newMusicFiles(order);
                        SongFragment.updateSongFiles(MusicLab.get(getContext()).getSongFiles());
                        AlbumFragment.updateAlbumFiles(MusicLab.get(getContext()).getAlbumFiles());
                        ArtistFragment.updateArtistFiles(MusicLab.get(getContext()).getArtistFiles());
                    }
                });
                dialog.show(manager, DIALOG_CHOICE_SORT);
            }
        });
        border.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!BORDER_ALBUM) {
                    BORDER_ALBUM = true;
                }
                updateAdapter();
                recyclerView.setAdapter(musicAdapter);
            }
        });
        list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(BORDER_ALBUM) {
                    BORDER_ALBUM = false;
                }
                updateAdapter();
                recyclerView.setAdapter(musicAdapter);
            }
        });
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), GarbageDetails.class);
                intent.putExtra(WHAT_2, "AlbumFragment");
                startActivity(intent);
            }
        });
        return v;
    }

    private class AlbumHolder extends RecyclerView.ViewHolder {

        private ImageView album_image, menuMore;
        private TextView album_name;

        public AlbumHolder(@NonNull View itemView){
            super(itemView);
            album_image = itemView.findViewById(R.id.album_image);
            album_name = itemView.findViewById(R.id.album_name);
            menuMore = itemView.findViewById(R.id.menuMore);
        }

        public void bind(com.mygame.music_alpha.MusicFile albumFile) {
            album_name.setText(albumFile.getAlbum());
            ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(getActivity()));
            ImageLoader.getInstance().displayImage(ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"),
                    albumFile.getAlbumId()).toString(), album_image,
                    new DisplayImageOptions.Builder().cacheInMemory(true).showImageOnLoading(R.drawable.cover_art_2).
                            resetViewBeforeLoading(true).build());
        }
    }

    private class MusicAdapter extends RecyclerView.Adapter<AlbumHolder> {

        private ArrayList<MusicFile> albumFiles;

        public MusicAdapter(ArrayList<MusicFile> albumFiles) {
            this.albumFiles = albumFiles;
        }

        @NonNull
        @Override
        public AlbumHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if(BORDER_ALBUM) {
                view = LayoutInflater.from(getContext()).inflate(R.layout.album_item, parent, false);
            }
            else{
                view = LayoutInflater.from(getContext()).inflate(R.layout.artist_item, parent, false);
            }
            return new AlbumHolder(view);
        }

        @Override
        public void onBindViewHolder(AlbumHolder holder, int position) {
            MusicFile musicFile = albumFiles.get(position);
            holder.bind(musicFile);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), ArtistAlbumDetails.class);
                    intent.putExtra(ALBUM, musicFile.getAlbum());
                    intent.putExtra(WHAT, ALBUM);
                    startActivity(intent);
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
//                    Toast.makeText(getContext(), "CLICK LONG", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getContext(), GarbageDetails.class);
                    intent.putExtra(WHAT_2, "AlbumFragment");
                    startActivity(intent);
                    return true;
                }
            });
            holder.menuMore.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(final View v){
                    FragmentManager manager = getActivity().getSupportFragmentManager();
                    DialogMenuMore dialog = DialogMenuMore.newInstance(getS(), musicFile.getAlbum());
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
                            else if(res.equals(getString(R.string.add_to_playlist))) {
                                ForAll.get(getContext()).addToPlaylist(musicFile, getActivity(), ALBUM);
                            }
                            else if(res.equals(getString(R.string.delete_forever))) {
                                allDelete(musicFile);
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
            return albumFiles.size();
        }

        public void setMusic(ArrayList<MusicFile> albums) {
            albumFiles = albums;
        }
    }

    private void nextPlay(com.mygame.music_alpha.MusicFile song) {
        ArrayList<MusicFile> pl = MusicLab.get(getContext()).createPlaylist(ALBUM, song.getAlbum());
        ForAll.get(getContext()).playNextAll(pl);
    }

    private void queueAdd(MusicFile song) {
        ArrayList<com.mygame.music_alpha.MusicFile> pl = MusicLab.get(getContext()).createPlaylist(ALBUM, song.getAlbum());
        ForAll.get(getContext()).addToQueueAll(pl);
    }

    private void allDelete(MusicFile song) {
        ArrayList<com.mygame.music_alpha.MusicFile> pl = MusicLab.get(getContext()).createPlaylist(ALBUM, song.getAlbum());
        ForAll.get(getContext()).deleteAll(pl);
    }

    private void share(MusicFile song) {
        ArrayList<MusicFile> pl = MusicLab.get(getContext()).createPlaylist(ALBUM, song.getAlbum());
        ForAll.get(getContext()).shareMulti(pl, getContext());
    }

    private String[] getS() {
        String[] s = {getString(R.string.play_next), getString(R.string.add_to_queue),
                getString(R.string.add_to_playlist), getString(R.string.delete_forever), getString(R.string.to_share)};
        return s;
    }

    private void updateAdapter() {
        if(BORDER_ALBUM) {
            recyclerView.setLayoutManager(new GridLayoutManager(getActivity(),
                    ForAll.calculateNoOfColumns(getActivity())));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        }
        recyclerView.setHasFixedSize(false);
        recyclerView.setNestedScrollingEnabled(false);
    }


    public static void updateAlbumFiles(ArrayList<MusicFile> albums) {
        Albums = new ArrayList<>();
        Albums.addAll(albums);
        musicAdapter.setMusic(albums);
        musicAdapter.notifyDataSetChanged();
    }

    public static ArrayList<MusicFile> getAlbums(){
        return Albums;
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

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
import static com.mygame.music_alpha.MusicLab.ARTIST;
import static com.mygame.music_alpha.MusicLab.BORDER_ARTIST;
import static com.mygame.music_alpha.MusicLab.SORT_SONGS;
import static com.mygame.music_alpha.MusicLab.WHAT;
import static com.mygame.music_alpha.MusicLab.WHAT_2;
import static com.mygame.music_alpha.SongFragment.DIALOG_CHOICE_SORT;
import static com.mygame.music_alpha.SongFragment.DIALOG_MENU_MORE;
import static com.mygame.music_alpha.SongFragment.FOR_RESULT;

public class ArtistFragment extends Fragment {

    private static final String TAG = "ArtistFragment";

    private RecyclerView recyclerView;
    private static MusicAdapter musicAdapter;
    private static ArrayList<MusicFile> Artists;
    private ImageButton select, border, list, sort;
    private View view;

    public ArtistFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView()");
        View v = inflater.inflate(R.layout.fragment_album_artist, container, false);
        Artists = MusicLab.get(getContext()).getArtistFiles();

        select = (ImageButton) v.findViewById(R.id.select);
        border = (ImageButton) v.findViewById(R.id.border);
        list = (ImageButton) v.findViewById(R.id.list);
        sort = (ImageButton) v.findViewById(R.id.sort);
        recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);

        recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);

        updateAdapter();
        musicAdapter = new MusicAdapter(Artists);
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
                if(!BORDER_ARTIST) {
                    BORDER_ARTIST = true;
                }
                updateAdapter();
                recyclerView.setAdapter(musicAdapter);
            }
        });
        list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(BORDER_ARTIST) {
                    BORDER_ARTIST = false;
                }
                updateAdapter();
                recyclerView.setAdapter(musicAdapter);
            }
        });
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), GarbageDetails.class);
                intent.putExtra(WHAT_2, "ArtistFragment");
                startActivity(intent);
            }
        });

        return v;
    }

    private class ArtistHolder extends RecyclerView.ViewHolder {

        private ImageView artist_image, menuMore;
        private TextView artist_name;

        public ArtistHolder(@NonNull View itemView){
            super(itemView);
            artist_image = itemView.findViewById(R.id.album_image);
            artist_name = itemView.findViewById(R.id.album_name);
            menuMore = itemView.findViewById(R.id.menuMore);
        }

        public void bind(MusicFile albumFile) {
            artist_name.setText(albumFile.getArtist());
            ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(getActivity()));
            ImageLoader.getInstance().displayImage(ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"),
                    albumFile.getAlbumId()).toString(), artist_image,
                    new DisplayImageOptions.Builder().cacheInMemory(true).showImageOnLoading(R.drawable.cover_art_2).
                            resetViewBeforeLoading(true).build());
        }
    }

    private class MusicAdapter extends RecyclerView.Adapter<ArtistHolder> {

        private ArrayList<MusicFile> artistFiles;

        public MusicAdapter(ArrayList<MusicFile> artistFiles) {
            this.artistFiles = artistFiles;
        }

        @NonNull
        @Override
        public ArtistHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if(BORDER_ARTIST) {
                view = LayoutInflater.from(getContext()).inflate(R.layout.album_item, parent, false);
            }
            else{
                view = LayoutInflater.from(getContext()).inflate(R.layout.artist_item, parent, false);
            }
            return new ArtistHolder(view);
        }

        @Override
        public void onBindViewHolder(ArtistHolder holder, int position) {
            MusicFile musicFile = artistFiles.get(position);
            holder.bind(musicFile);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), ArtistAlbumDetails.class);
                    intent.putExtra(ARTIST, musicFile.getArtist());
                    intent.putExtra(WHAT, ARTIST);
                    startActivity(intent);
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Intent intent = new Intent(getContext(), GarbageDetails.class);
                    intent.putExtra(WHAT_2, "ArtistFragment");
                    startActivity(intent);
                    return true;
                }
            });
            holder.menuMore.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(final View v){
                    FragmentManager manager = getActivity().getSupportFragmentManager();
                    DialogMenuMore dialog = DialogMenuMore.newInstance(getS(), musicFile.getArtist());
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
                                ForAll.get(getContext()).addToPlaylist(musicFile, getActivity(), ARTIST);
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
            return artistFiles.size();
        }

        public void setMusic(ArrayList<MusicFile> artists) {
            artistFiles = artists;
        }
    }

    private void nextPlay(MusicFile song) {
        ArrayList<MusicFile> pl = MusicLab.get(getContext()).createPlaylist(ARTIST, song.getArtist());
        ForAll.get(getContext()).playNextAll(pl);
    }

    private void queueAdd(MusicFile song) {
        ArrayList<MusicFile> pl = MusicLab.get(getContext()).createPlaylist(ARTIST, song.getArtist());
        ForAll.get(getContext()).addToQueueAll(pl);
    }

    private void allDelete(MusicFile song) {
        ArrayList<MusicFile> pl = MusicLab.get(getContext()).createPlaylist(ARTIST, song.getArtist());
        ForAll.get(getContext()).deleteAll(pl);
    }

    private void share(MusicFile song) {
        ArrayList<MusicFile> pl = MusicLab.get(getContext()).createPlaylist(ARTIST, song.getArtist());
        ForAll.get(getContext()).shareMulti(pl, getContext());
    }

    private String[] getS() {
        String[] s = {getString(R.string.play_next), getString(R.string.add_to_queue),
                getString(R.string.add_to_playlist), getString(R.string.delete_forever), getString(R.string.to_share)};
        return s;
    }

    private void updateAdapter() {
        if(BORDER_ARTIST) {
            recyclerView.setLayoutManager(new GridLayoutManager(getActivity(),
                    ForAll.calculateNoOfColumns(getActivity())));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        }
        recyclerView.setHasFixedSize(false);
        recyclerView.setNestedScrollingEnabled(false);
    }

    public static void updateArtistFiles(ArrayList<MusicFile> artists) {
        Artists = new ArrayList<>();
        Artists.addAll(artists);
        musicAdapter.setMusic(artists);
        musicAdapter.notifyDataSetChanged();
    }

    public static ArrayList<MusicFile> getArtists(){
        return Artists;
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

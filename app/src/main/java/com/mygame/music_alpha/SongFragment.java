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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;

import static com.mygame.music_alpha.DialogMenuMore.RES;
import static com.mygame.music_alpha.DialogSorting.ORDER_DIALOG;
import static com.mygame.music_alpha.MusicLab.POSITION_PLAY;
import static com.mygame.music_alpha.MusicLab.POSITION_PLAY_NOW;
import static com.mygame.music_alpha.MusicLab.SINGLE;
import static com.mygame.music_alpha.MusicLab.SORT_SONGS;
import static com.mygame.music_alpha.MusicLab.WHAT_2;

public class SongFragment extends Fragment {

    private static final String TAG = "SongFragment";
    public static final String FOR_RESULT = "resultForSongFragment";
    public static final String DIALOG_CHOICE_SORT = "DialogSorting";
    public static final String DIALOG_MENU_MORE = "DialogMenuMore";
    public static final String DIALOG_ADD_PLAYLIST = "DialogAddPlaylist";
    public static final String DIALOG_ADD_ARRAY_PLAYLIST = "DialogAddArrayPlaylist";
    public static final String FOR_RESULT_ADD = "forResultAdd";

    private RecyclerView recyclerView;
    private static SongAdapter musicAdapter;
    private static ArrayList<MusicFile> Songs;
    private ImageButton select, sort, queue;
    private View view;
    private static String oldPath = "";

    public SongFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView()");
        View v = inflater.inflate(R.layout.fragment_song, container, false);
        Songs = MusicLab.get(getContext()).getSongFiles();
        oldPath = MusicLab.get(getContext()).getSongPath();

        select = (ImageButton) v.findViewById(R.id.select);
        sort = (ImageButton) v.findViewById(R.id.sort);
        queue = (ImageButton) v.findViewById(R.id.queue);
        recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(false);
        recyclerView.setNestedScrollingEnabled(false);
        musicAdapter = new SongAdapter(Songs);
        recyclerView.setAdapter(musicAdapter);

        if(MusicLab.get(getContext()).getMusicFiles().size() == 0) {
            sort.setEnabled(false);
            queue.setEnabled(false);
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
        queue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), Queue.class);
                startActivity(intent);
            }
        });
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), GarbageDetails.class);
                intent.putExtra(WHAT_2, "SongFragment");
                startActivity(intent);
            }
        });
        return v;
    }

    private class SongHolder extends RecyclerView.ViewHolder {

        private TextView file_name, file_artist_name;
        private ImageView album_art, menuMore, favorite;

        public SongHolder(@NonNull View itemView){
            super(itemView);
            file_name = itemView.findViewById(R.id.music_file_name);
            file_artist_name = itemView.findViewById(R.id.music_artist_name);
            album_art = itemView.findViewById(R.id.music_img);
            menuMore = itemView.findViewById(R.id.menuMore);
        }

        public void bind(MusicFile musicFile) {
            file_name.setText(musicFile.getTitle());
            file_artist_name.setText((musicFile.getArtist() + " | " + musicFile.getAlbum()));
            ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(getActivity()));
            ImageLoader.getInstance()
                    .displayImage(ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"),
                    musicFile.getAlbumId()).toString(), album_art,
                    new DisplayImageOptions.Builder()
                            .cacheInMemory(true)
                            .showImageOnLoading(R.drawable.cover_art_2).
                            resetViewBeforeLoading(true).build());

        }
    }

    private class SongAdapter extends RecyclerView.Adapter<SongHolder> {

        private ArrayList<MusicFile> musicFiles;

        public SongAdapter(ArrayList<MusicFile> musicFiles) {
            this.musicFiles = musicFiles;
        }

        @NonNull
        @Override
        public SongHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.music_items, parent, false);
            return new SongHolder(view);
        }

        @Override
        public void onBindViewHolder(SongHolder holder, int position) {
            MusicFile musicFile = musicFiles.get(position);
            holder.bind(musicFile);
            if (musicFile.getPath().equals(MusicLab.get(getContext()).getSongPath())) {
                holder.itemView.setBackgroundResource(R.color.primary_dark);
            }
            else {
                holder.itemView.setBackgroundResource(R.color.black_and_gray);
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MusicLab.get(getContext()).setNowPlaying(Songs);
                    Intent intent = new Intent(getContext(), Player.class);
                    intent.putExtra(POSITION_PLAY_NOW, position);
                    intent.putExtra(Player.TAG, TAG);
                    POSITION_PLAY = position;
                    startActivity(intent);
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
//                    Toast.makeText(getContext(), "CLICK LONG", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getContext(), GarbageDetails.class);
                    intent.putExtra(WHAT_2, "SongFragment");
                    startActivity(intent);
                    return true;
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
                                ForAll.get(getContext()).playNext(musicFile);
                            }
                            else if(res.equals(getString(R.string.add_to_queue))) {
                                ForAll.get(getContext()).addToQueue(musicFile);
                            }
                            else if(res.equals(getString(R.string.add_to_playlist))) {
                                ForAll.get(getContext()).addToPlaylist(musicFile, getActivity(), SINGLE);
                            }
                            else if(res.equals(getString(R.string.delete_forever))) {
                                ForAll.get(getContext()).delete(musicFile);
                            }
                            else if(res.equals(getString(R.string.to_share))) {
                                ForAll.get(getContext()).share(musicFile, getContext());
                            }
                        }
                    });
                    dialog.show(manager, DIALOG_MENU_MORE);
                }
            });
        }

        @Override
        public int getItemCount() {
            return musicFiles.size();
        }

        public void setMusic(ArrayList<MusicFile> songs) {
            musicFiles = songs;
        }
    }

    private String[] getS() {
        String[] s = {getString(R.string.play_next), getString(R.string.add_to_queue),
                getString(R.string.add_to_playlist), getString(R.string.delete_forever), getString(R.string.to_share)};
        return s;
    }

    public static void updateSongFiles(ArrayList<MusicFile> songs) {
        Songs = new ArrayList<>();
        Songs.addAll(songs);
        musicAdapter.setMusic(songs);
        musicAdapter.notifyDataSetChanged();
        Log.i(TAG, "Songs - " + Songs.size());
    }

    public static void updateNotifyItem(String newPath) {
        if(musicAdapter != null) {
            for (int i = 0; i < musicAdapter.musicFiles.size(); i++) {
                if (musicAdapter.musicFiles.get(i).getPath().equals(newPath) ||
                        musicAdapter.musicFiles.get(i).getPath().equals(oldPath)) {
                    musicAdapter.notifyItemChanged(i);
                }
            }
        }
        oldPath = newPath;
        Log.i(TAG, "updateNotifyItem");
    }

    public static ArrayList<MusicFile> getSongs(){
        return Songs;
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

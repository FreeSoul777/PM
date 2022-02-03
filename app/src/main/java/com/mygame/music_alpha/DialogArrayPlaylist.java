package com.mygame.music_alpha;

import android.app.Dialog;
import android.content.ContentUris;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.Arrays;

import static com.mygame.music_alpha.DialogAddPlaylist.DIALOG_ADD;
import static com.mygame.music_alpha.SongFragment.DIALOG_ADD_PLAYLIST;
import static com.mygame.music_alpha.SongFragment.FOR_RESULT;
import static com.mygame.music_alpha.SongFragment.FOR_RESULT_ADD;

public class DialogArrayPlaylist extends DialogFragment {

    private static final String TAG = "DialogArrayPlaylist";
    public static final String DIALOG_ARRAY_ADD = "com.mygame.music_alpha.dialogarrayplaylist";

    private Button btn_ok, btn_cancel;
    private TextView btn_add;
    private boolean[] Choice;
    private ArrayList<com.mygame.music_alpha.PL> Playlists;
    private ArrayList<com.mygame.music_alpha.PL> GarbageSongs;
    private GarbageAdapter garbageAdapter;
    private View view;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.i(TAG, "onCreateDialog()");
        Dialog dialog  = new Dialog(getActivity(), R.style.AlertDialogTheme);
        dialog.setContentView(R.layout.dialog_add_in_pl);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        btn_ok = (Button) dialog.findViewById(R.id.btn_ok);
        btn_cancel = (Button) dialog.findViewById(R.id.btn_cancel);
        btn_add = (TextView) dialog.findViewById(R.id.btn_add);
        btn_ok.setText(getString(R.string.add));
        btn_cancel.setText(getString(R.string.cancel));
        btn_add.setText("+");

        RecyclerView recyclerView = (RecyclerView) dialog.findViewById(R.id.recyclerView);
        Playlists = MusicLab.get(getContext()).getPlaylistString();
        GarbageSongs = new ArrayList<>();
        Choice = new boolean[Playlists.size()];
        Arrays.fill(Choice, false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setNestedScrollingEnabled(false);
        garbageAdapter = new GarbageAdapter(Playlists);
        recyclerView.setAdapter(garbageAdapter);



        TextView textView = (TextView) dialog.findViewById(R.id.title_dialog);
        textView.setText(getString(R.string.add_to_playlist));


        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Toast.makeText(getContext(), getString(R.string.cancel), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "DialogArrayPlaylist: Dialog -> Cancel ");
            }
        });
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Toast.makeText(getContext(), getString(R.string.ok), Toast.LENGTH_SHORT).show();
                ArrayList<String> result = new ArrayList<>();
                for(PL pl: GarbageSongs) {
                    result.add(pl.getTitle());
                    Log.e(TAG, pl.getTitle() + " Title");
                }
                setResult(result);
                Log.e(TAG, "DialogArrayPlaylist: Dialog -> OK, result.size() " + result.size());
            }
        });

        btn_add.setOnClickListener(new View.OnClickListener() {
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

        return dialog;
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

        public void bind(com.mygame.music_alpha.PL musicFile, int position) {
            file_name.setText(musicFile.getTitle());
            file_artist_name.setText("");
            ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(getContext()));
            ImageLoader.getInstance().displayImage(ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"),
                    musicFile.getAlbumId()).toString(), album_art,
                    new DisplayImageOptions.Builder().cacheInMemory(true).showImageOnLoading(R.drawable.cover_art_2).
                            resetViewBeforeLoading(true).build());

            if(!Choice[position]) {
                choiceImg.setImageResource(R.drawable.ic_unchecked);
            } else {
                choiceImg.setImageResource(R.drawable.ic_checked_2);
            }
        }
    }

    private class GarbageAdapter extends RecyclerView.Adapter<GarbageHolder> {

        private ArrayList<PL> playlistFiles;

        public GarbageAdapter(ArrayList<PL> playlistFiles) {
            this.playlistFiles = playlistFiles;
        }

        @NonNull
        @Override
        public GarbageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.delete_item, parent, false);
            return new GarbageHolder(view);
        }

        @Override
        public void onBindViewHolder(GarbageHolder holder, int position) {
            com.mygame.music_alpha.PL musicFile = playlistFiles.get(position);
            holder.bind(musicFile, position);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(Choice[position]) {
                        GarbageSongs.remove(musicFile);
                        holder.choiceImg.setImageResource(R.drawable.ic_unchecked);
                        Choice[position] = false;
                    }
                    else {
                        GarbageSongs.add(musicFile);
                        holder.choiceImg.setImageResource(R.drawable.ic_checked_2);
                        Choice[position] = true;
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return playlistFiles.size();
        }

        public void setMusic(ArrayList<PL> songs) {
            playlistFiles = songs;
        }
    }


    private void add_new_playlist(String name) {
        MusicLab.get(getContext()).putNewPlaylist(name);
        GarbageSongs = new ArrayList<>();
        Choice = new boolean[Playlists.size()];
        Arrays.fill(Choice, false);
        garbageAdapter.setMusic(Playlists);
        garbageAdapter.notifyDataSetChanged();
        Log.i(TAG, "Playlists.size() - " + Playlists.size() +
                ", PL - " + MusicLab.get(getContext()).getPlaylistString().size() +
                ", Choice.size() - " + Choice.length);
    }


    private void setResult(ArrayList<String> order){
        Bundle result = new Bundle();
        result.putStringArrayList(DIALOG_ARRAY_ADD, order);
        getParentFragmentManager().setFragmentResult(FOR_RESULT, result);
    }

    public static DialogArrayPlaylist newInstance(){
        DialogArrayPlaylist fragment = new DialogArrayPlaylist();
        return fragment;
    }
}

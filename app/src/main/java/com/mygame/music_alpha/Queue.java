package com.mygame.music_alpha;

import android.content.ContentUris;
import android.content.Intent;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.Collections;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

import static com.mygame.music_alpha.MusicLab.POSITION_PLAY;
import static com.mygame.music_alpha.MusicLab.POSITION_PLAY_NOW;

public class Queue extends AppCompatActivity {

    private static final String TAG = "Queue";

    private RecyclerView recyclerView;
    private static QueueAdapter queueAdapter;
    private ImageView back;
    private ArrayList<MusicFile> QueueSongs;
    private static String oldPath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");
        getSupportActionBar().hide();
        setContentView(R.layout.activity_queue);

        recyclerView = findViewById(R.id.recyclerView);
        back = findViewById(R.id.back_btn);

        QueueSongs = MusicLab.get(getApplicationContext()).getNowPlaying();
        oldPath = MusicLab.get(getApplicationContext()).getSongPath();

        recyclerView.setLayoutManager(new LinearLayoutManager(Queue.this));
        recyclerView.setHasFixedSize(false);
        recyclerView.setNestedScrollingEnabled(false);
        queueAdapter = new QueueAdapter(QueueSongs);
        recyclerView.setAdapter(queueAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private class QueueHolder extends RecyclerView.ViewHolder {

        private TextView file_name, file_artist_name;
        private ImageView album_art, menuMore;

        public QueueHolder(LayoutInflater inflater, ViewGroup parent){
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

    private class QueueAdapter extends RecyclerView.Adapter<QueueHolder> {

        private ArrayList<MusicFile> queueFiles;

        public QueueAdapter(ArrayList<MusicFile> queueFiles) {
            this.queueFiles = queueFiles;
        }

        @Override
        public QueueHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(Queue.this);
            return new QueueHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(QueueHolder holder, int position) {
            MusicFile musicFile = queueFiles.get(position);
            holder.bind(musicFile);
            if (musicFile.getPath().equals(MusicLab.get(Queue.this).getSongPath())) {
                holder.itemView.setBackgroundResource(R.color.primary_dark);
            }
            else {
                holder.itemView.setBackgroundResource(R.color.black_and_gray);
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), Player.class);
                    intent.putExtra(POSITION_PLAY_NOW, holder.getAbsoluteAdapterPosition());
                    intent.putExtra(Player.TAG, TAG);
                    POSITION_PLAY = holder.getAbsoluteAdapterPosition();
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return queueFiles.size();
        }
    }

    public static void updateNotifyItem(String newPath) {
        if(queueAdapter != null) {
            for (int i = 0; i < queueAdapter.queueFiles.size(); i++) {
                if (queueAdapter.queueFiles.get(i).getPath().equals(newPath) ||
                        queueAdapter.queueFiles.get(i).getPath().equals(oldPath)) {
                    queueAdapter.notifyItemChanged(i);
                }
            }
        }
        oldPath = newPath;
    }


    ItemTouchHelper.SimpleCallback itemTouchHelperCallback =
            new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.RIGHT) {

        MusicFile deletedSong = new MusicFile();

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView,  RecyclerView.ViewHolder viewHolder,
                              @NonNull RecyclerView.ViewHolder target) {

            int fromPosition = viewHolder.getAbsoluteAdapterPosition();
            int toPosition = target.getAbsoluteAdapterPosition();

            Log.i(TAG, " from and to - " + fromPosition + " " + toPosition + ", now - " + POSITION_PLAY);

            Collections.swap(queueAdapter.queueFiles, fromPosition, toPosition);
            queueAdapter.notifyItemMoved(fromPosition, toPosition);
            POSITION_PLAY = toPosition;

            Log.i(TAG, "now - " + POSITION_PLAY);
            return false;
        }

        @Override
        public void onSwiped(@NonNull  RecyclerView.ViewHolder viewHolder, int direction) {

            int position = viewHolder.getAbsoluteAdapterPosition();
            deletedSong = queueAdapter.queueFiles.get(position);
            final boolean[] flag = {true};

            if (MusicLab.get(Queue.this).getSongPath().equals(deletedSong.getPath())) {
                if(QueueSongs.size() > 1) {
                    MusicService.actionPlaying.nextBtnClicked();
                }
                Log.e(TAG, "POSITION_PLAY до - " + POSITION_PLAY);
                POSITION_PLAY = POSITION_PLAY - 1 < 0 && QueueSongs.size() > 1 ?
                        POSITION_PLAY = 0 : POSITION_PLAY - 1;
                Log.e(TAG, "POSITION_PLAY после - " + POSITION_PLAY);
            }
            else if (position < POSITION_PLAY) {
                Log.e(TAG, "POSITION_PLAY до - " + POSITION_PLAY);
                POSITION_PLAY -= 1;
                Log.e(TAG, "POSITION_PLAY после - " + POSITION_PLAY);
            }
            queueAdapter.queueFiles.remove(position);
            queueAdapter.notifyItemRemoved(position);
            Log.e(TAG, QueueSongs.size() + " QueueSongs.size() " + queueAdapter.queueFiles.size()
                    + " queueFiles in adapter");


            Snackbar.make(recyclerView, deletedSong.getTitle(), Snackbar.LENGTH_SHORT)
                    .setAction("Undo", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(position <= POSITION_PLAY) {
                                    POSITION_PLAY += 1;
                                }
                                flag[0] = false;
                                queueAdapter.queueFiles.add(position, deletedSong);
                                Toast.makeText(Queue.this, "UNDO", Toast.LENGTH_LONG).show();
                                queueAdapter.notifyItemInserted(position);
                            }
                        }).show();

            Log.i(TAG, "flag[0] " + flag[0]);
            if(flag[0] && QueueSongs.isEmpty()) {
                flag[0] = true;
                MiniPlayer.get(Queue.this).updateMini(!QueueSongs.isEmpty() ?
                        QueueSongs : MusicLab.get(Queue.this).getMusicFiles(), QueueSongs.isEmpty());

            }

        }

        @Override
        public void onChildDraw (Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                 float dX, float dY, int actionState, boolean isCurrentlyActive) {

            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addBackgroundColor(ContextCompat.getColor(Queue.this, R.color.red))
                    .addActionIcon(R.drawable.ic_delete_item)
                    .create()
                    .decorate();


            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };


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

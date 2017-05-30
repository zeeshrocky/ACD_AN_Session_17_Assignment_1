package com.example.zeeshan.musicplayer;

import android.app.NotificationManager;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

public class PlayingMusic extends AppCompatActivity {

    private TextView mSelectedTrackTitle,mSelectedTrackArtist,mSelectedTrackAlbum;
    private ImageView mSelectedTrackImage;
    private Button playPause, shuffle, search, repeat,next,prev;
    private SeekBar seekBar;
    private Handler myHandler = new Handler();
    private MusicService musicService;
    private Intent playIntent;
    private ArrayList<Song> songList;
    private int mediaMax, mediaPos;
    boolean wasPlaying;
    private Bundle bundle;
    private NotificationManager notificationManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playing_music);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout);

        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }

        songList = getIntent().getParcelableArrayListExtra("song_list");
        bundle = getIntent().getExtras();
        wasPlaying = bundle.getBoolean("playPause");

        seekBar = (SeekBar) findViewById(R.id.songSeekBar);
        playPause = (Button) findViewById(R.id.playPauseButton);
        shuffle = (Button) findViewById(R.id.shuffleButton);
        repeat = (Button) findViewById(R.id.replayButton);
        prev = (Button) findViewById(R.id.prevButton);
        next = (Button) findViewById(R.id.nextButton);
        search = (Button) findViewById(R.id.searchButton);

        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePlayPause();
            }
        });

        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicService.prevSong();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicService.nextSong();
            }
        });

        shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicService.shuffle();
                if(musicService.isShuffle())
                    shuffle.getBackground().setTint(getResources().getColor(R.color.colorAccent));
                else
                    shuffle.getBackground().setTint(getResources().getColor(R.color.colorPrimaryDark));
            }
        });

        repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicService.repeat();
                if(musicService.isRepeat())
                    repeat.getBackground().setTint(getResources().getColor(R.color.colorAccent));
                else
                    repeat.getBackground().setTint(getResources().getColor(R.color.colorPrimaryDark));
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                intent.putExtra(SearchManager.QUERY,songList.get(musicService.currentSongPosition()).getSongTitle());
                startActivity(intent);
            }
        });


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                musicService.moveTo(seekBar.getProgress());

            }
        });

    }

    private Runnable updateSeekBar = new Runnable() {
        @Override
        public void run() {
            Log.d("boobs", "run: in the run functions"+mediaPos+mediaMax);
            mediaPos = musicService.getPosition();
            mediaMax = musicService.getDuration();
            seekBar.setProgress(mediaPos);
            mSelectedTrackTitle.setText(songList.get(musicService.currentSongPosition()).getSongTitle());
            mSelectedTrackArtist.setText(songList.get(musicService.currentSongPosition()).getSongArtist());
            mSelectedTrackAlbum.setText(songList.get(musicService.currentSongPosition()).getSongAlbum());
            /*Uri AlbumArtUri = Uri.parse("content://media/external/audio/albumart");
            Uri uri = ContentUris.withAppendedId(AlbumArtUri, songList.get(musicService.currentSongPosition()).getAlbumId());
            Picasso.with(getApplicationContext()).load(uri).into(mSelectedTrackImage);*/
            myHandler.postDelayed(updateSeekBar,1000);
        }
    };


    private ServiceConnection musicConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;

            musicService=binder.getService();
            musicService.setList(songList);

            musicService.playSong();

            //mSelectedTrackImage = (ImageView) findViewById(R.id.albumImage);

            mSelectedTrackTitle = (TextView) findViewById(R.id.ActionBarText);
            mSelectedTrackAlbum = (TextView) findViewById(R.id.AlbumTextView);
            mSelectedTrackArtist = (TextView) findViewById(R.id.ArtistTextView);

           /* Uri AlbumArtUri = Uri.parse("content://media/external/audio/albumart");
            Uri uri = ContentUris.withAppendedId(AlbumArtUri, songList.get(musicService.currentSongPosition()).getAlbumId());
            Picasso.with(getApplicationContext()).load(uri).into(mSelectedTrackImage);*/

            mSelectedTrackAlbum.setText(songList.get(musicService.currentSongPosition()).getSongAlbum());
            mSelectedTrackArtist.setText(songList.get(musicService.currentSongPosition()).getSongArtist());
            mSelectedTrackAlbum.setText(songList.get(musicService.currentSongPosition()).getSongAlbum());

            mediaPos = musicService.getPosition();
            mediaMax = musicService.getDuration();

            Log.d("boobs", "onServiceConnected: "+mediaPos+ " "+mediaMax);

            seekBar.setMax(mediaMax);
            seekBar.setProgress(mediaPos);

            myHandler.postDelayed(updateSeekBar,1000);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(001);
        }
    };

    @Override
    public void onStart() {
        super.onStart();
    }

    private void togglePlayPause() {
        if (musicService.isPlaying()) {
            musicService.onPause();
            playPause.setBackgroundResource(R.drawable.ic_play_arrow_black_36dp);
        } else {
            musicService.onResume();
            playPause.setBackgroundResource(R.drawable.ic_pause_black_36dp);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

package com.example.zeeshan.musicplayer;

import android.Manifest;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Song> songList;
    private MusicService musicService;
    private Intent playIntent;
    private static final String TAG = "Main Activity";
    private TextView actionBarText,toolbarTextView;
    private Toolbar toolbar;
    private Handler myHandler = new Handler();
    private Button playPause;
    private NotificationManager notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout);

        actionBarText = (TextView) findViewById(R.id.ActionBarText);

        toolbar = (Toolbar) findViewById(R.id.mainToolbar);
        toolbarTextView = (TextView) findViewById(R.id.toolBarTitle);

        playPause = (Button) findViewById(R.id.toolbarPlayPause);

        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePlayPause();
            }
        });

        if(playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }

        isStoragePermissionGranted();

        songList=new ArrayList<>();
        getSongList();
        Collections.sort(songList, new Comparator<Song>() {
            @Override
            public int compare(Song o1, Song o2) {
                return o1.getSongTitle().compareTo(o2.getSongTitle());
            }
        });
        SongAdapter songAdapter = new SongAdapter(this,songList);
        ListView songListView = (ListView) findViewById(R.id.songListView);
        songListView.setAdapter(songAdapter);
        songListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                musicService.setSong(position);
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(),PlayingMusic.class);
                intent.putParcelableArrayListExtra("song_list",songList);
                intent.putExtra("playPause",true);
                startActivity(intent);
            }
        });
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(),PlayingMusic.class);
                intent.putParcelableArrayListExtra("song_list",songList);
                intent.putExtra("playPause",true);
                startActivity(intent);
            }
        });
    }

    private Runnable updateToolbar = new Runnable() {
        @Override
        public void run() {
            if(musicService.isPlaying())
                toolbar.setVisibility(View.VISIBLE);
            toolbarTextView.setText(songList.get(musicService.currentSongPosition()).getSongTitle());
            actionBarText.setText("Music Player");
            /*Uri AlbumArtUri = Uri.parse("content://media/external/audio/albumart");
            Uri uri = ContentUris.withAppendedId(AlbumArtUri, songList.get(musicService.currentSongPosition()).getAlbumId());
            Picasso.with(getApplicationContext()).load(uri).into(mSelectedTrackImage);*/
            myHandler.postDelayed(updateToolbar,1000);
        }
    };

    @Override
    public void onStart() {
        super.onStart();
    }

    ServiceConnection musicConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService=binder.getService();
            musicService.setList(songList);
            if(musicService.isPlaying())
                toolbar.setVisibility(View.VISIBLE);
            else
                toolbar.setVisibility(View.GONE);
            actionBarText.setText(songList.get(musicService.currentSongPosition()).getSongTitle());
            if (musicService.isPlaying()) {
                playPause.setBackgroundResource(R.drawable.ic_play_arrow_white_36dp);
            } else {
                playPause.setBackgroundResource(R.drawable.ic_pause_white_36dp);
            }
            myHandler.postDelayed(updateToolbar,1000);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(001);
        }
    };

    public void getSongList() {
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri,null,null,null,null);
        //get columns
        int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
        int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
        int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
        int albumColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
        int albumArt = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
        if(musicCursor !=null && musicCursor.moveToFirst()) {
            while(!musicCursor.isLast()) {
                String thisPath = musicCursor.getString(musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisAlbum = musicCursor.getString(albumColumn);
                long thisAlbumArt = musicCursor.getLong(albumArt);
                songList.add(new Song(thisId, thisTitle, thisArtist,thisPath,thisAlbum,thisAlbumArt));
                musicCursor.moveToNext();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
        }
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT > 22) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }

    private void togglePlayPause() {
        if (musicService.isPlaying()) {
            musicService.onPause();
            playPause.setBackgroundResource(R.drawable.ic_play_arrow_white_36dp);
        } else {
            musicService.onResume();
            playPause.setBackgroundResource(R.drawable.ic_pause_white_36dp);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        musicService.stopSelf();
    }
}

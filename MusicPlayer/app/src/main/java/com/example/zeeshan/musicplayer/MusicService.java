package com.example.zeeshan.musicplayer;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener{

    private static final String TAG = "Music Service";
    public static final int notifierId = 1;
    private MediaPlayer mMediaPlayer;
    private ArrayList<Song> songs;
    private final IBinder musicBind = new MusicBinder();
    private int songPosn;
    private boolean repeat,shuffle;
    private Random r;
    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotifyMgr;
    private int mNotificationId = 001;


    public IBinder onBind(Intent arg0) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mMediaPlayer.stop();
        mMediaPlayer.release();
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        songPosn = 0;
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setOnCompletionListener(this);

    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        //noinspection WrongConstant
        return 1;
    }

    @Override
    public void onDestroy() {
        mBuilder.setOngoing(false);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(001);
        mNotifyMgr.cancel(001);
    }

    @Override
    public void onLowMemory() {
        mBuilder.setOngoing(false);
        mNotifyMgr.cancel(001);
    }

    public void setList(ArrayList<Song> songList) {
        songs=songList;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d("boobs", "onCompletion:");
        nextSong();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.d("boots", "onError: err");
        return false;
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    public void playSong() {
        mMediaPlayer.reset();
        Song playSong = songs.get(songPosn);
        long currSong = playSong.getSongId();
        Uri trackUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,currSong);
        try{
            mMediaPlayer.setDataSource(getApplicationContext(), trackUri);
        }
        catch(Exception e){
            Log.e(TAG , "Error setting data source", e);
        }
        try {
            mMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mBuilder = new NotificationCompat.Builder(getApplicationContext());
        mBuilder.setSmallIcon(R.drawable.ic_music_note_black_24dp);
        mBuilder.setContentTitle("Now Playing");
        mBuilder.setContentText(playSong.getSongTitle());
        Intent resultIntent = new Intent(getApplicationContext(),MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(getApplicationContext(),0,resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setOngoing(true);
        mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId,mBuilder.build());
        mMediaPlayer.start();

    }

    public void onPause() {
        mMediaPlayer.pause();
        mBuilder.setOngoing(false);
    }

    public  void onResume() {
        mMediaPlayer.start();
        mBuilder.setOngoing(true);
    }

    public void nextSong() {
        if(shuffle) {
            r = new Random();
            songPosn = r.nextInt(songs.size()) + 0;
        }
        else if(!repeat)
            songPosn=(songPosn+1)%songs.size();
        playSong();
    }

    public void prevSong() {
        songPosn=(songPosn-1)%songs.size();
        playSong();
    }

    public void moveTo(int progress) {
        mMediaPlayer.seekTo(progress);
    }

    public int getDuration() {
        return mMediaPlayer.getDuration();
    }

    public int getPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    public void setSong(int songIndex){
        songPosn=songIndex;
    }

    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    public int currentSongPosition() {
        return songPosn;
    }

    public void repeat() {
        if(repeat)
            repeat=false;
        else
            repeat=true;
    }

    public void shuffle(){
        if(shuffle)
            shuffle=false;
        else
            shuffle=true;
    }

    public boolean isShuffle() { return shuffle; }
    public boolean isRepeat() { return repeat; }

}
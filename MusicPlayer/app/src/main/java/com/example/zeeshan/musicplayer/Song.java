package com.example.zeeshan.musicplayer;

import android.os.Parcel;
import android.os.Parcelable;

public class Song implements Parcelable {
    private long id,albumid;
    private String title,artist,path,album;
    public Song(long songID,String songTitle, String songArtist,String songPath,String songAlbum,long songAlbumId) {
        id=songID;
        artist=songArtist;
        title=songTitle;
        path=songPath;
        album=songAlbum;
        albumid=songAlbumId;
    }

    public Song(Parcel in) {
        id=in.readLong();
        artist=in.readString();
        title=in.readString();
        path=in.readString();
        album=in.readString();
    }

    public long getSongId() {
        return id;
    }
    public String getSongArtist() {
        return artist;
    }
    public String getSongTitle() { return title; }
    public String getSongPath() { return path;  }
    public String getSongAlbum() { return album; }
    public long getAlbumId() { return albumid; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(artist);
        dest.writeString(title);
        dest.writeString(path);
        dest.writeString(album);
    }

    public static final Parcelable.Creator<Song> CREATOR
            = new Parcelable.Creator<Song>() {

        // This simply calls our new constructor (typically private) and
        // passes along the unmarshalled `Parcel`, and then returns the new object!
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        // We just need to copy this and change the type to match our class.
        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };
}

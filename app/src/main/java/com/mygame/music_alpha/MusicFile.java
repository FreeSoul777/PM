package com.mygame.music_alpha;

public class MusicFile {
    private String path;
    private String title;
    private String artist;
    private String album;
    private String duration;
    private String id;
    private long albumId;
    private long artistId;
    private boolean is_favorite;

    public MusicFile(String path, String title, String artist, String album, String duration,
                     String id, long albumId, long artistId) {
        this.path = path;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.id = id;
        this.albumId = albumId;
        this.artistId = artistId;
        this.is_favorite = false;
    }

    public MusicFile() {
        this.path = "";
        this.title = "";
        this.artist = "";
        this.album = "";
        this.duration = "";
        this.id = "";
        this.albumId = 0;
        this.artistId = 0;
        this.is_favorite = false;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(long id) {
        this.albumId = albumId;
    }

    public long getArtistId() {
        return artistId;
    }

    public void setArtistId(long id) {
        this.artistId = artistId;
    }

    public void setFavorite(boolean is_favorite) {
        this.is_favorite = is_favorite;
    }

    public boolean getFavorite() {
        return is_favorite;
    }
}

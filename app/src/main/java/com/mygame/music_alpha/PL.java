package com.mygame.music_alpha;

public class PL {

    private String path;
    private String title;
    private long albumId;
    private boolean special;

    public PL(String title, String path, long albumId) {
        this.title = title;
        this.path = path;
        this.albumId = albumId;
        this.special = false;
    }

    public PL(String title) {
        this.title = title;
        this.path = "";
        this.albumId = -1;
        this.special = false;
    }

    public String getPath() {
        return path;
    }

    public String getTitle() {
        return title;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    public boolean getSpecial() {
        return special;
    }

    public void setSpecial(boolean special) {
        this.special = special;
    }
}

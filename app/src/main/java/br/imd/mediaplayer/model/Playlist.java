package br.imd.mediaplayer.model;

import br.imd.mediaplayer.model.annotation.Model;
import br.imd.mediaplayer.model.annotation.Persist;

import java.util.Arrays;

@Model("playlist_${owner}_${id}.txt")
public class Playlist extends AbstractModel {
    @Persist
    private String title;

    @Persist
    private User owner;

    @Persist
    private Song[] songs;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Song[] getSongs() {
        return songs;
    }

    public void setSongs(Song[] songs) {
        this.songs = songs;
    }

    public boolean addSong(Song song) {
        if (song == null) return false;
        if (songs == null) songs = new Song[] {song};
        if (Arrays.stream(songs).anyMatch(s -> s.equals(song))) {
            return false;
        }
        songs = Arrays.copyOf(songs, songs.length + 1);
        songs[songs.length] = song;
        return true;
    }

    public boolean removeSong(Song song) {
        if (song == null) return false;
        for (int i = 0; i < songs.length; i++) {
            if (songs[i].equals(song)) {
                System.arraycopy(songs, i+1, songs, i, songs.length - i - 1);
                songs = Arrays.copyOf(songs, songs.length - 1);
                return true;
            }
        }
        return false;
    }
}

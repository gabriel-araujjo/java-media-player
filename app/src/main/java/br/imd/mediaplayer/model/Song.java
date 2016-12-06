package br.imd.mediaplayer.model;

import br.imd.mediaplayer.dao.DAO;
import br.imd.mediaplayer.model.annotation.Model;
import br.imd.mediaplayer.model.annotation.Persist;
import com.mpatric.mp3agic.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by gabriel on 02/12/16.
 */
@Model("musicas.txt")
public class Song extends AbstractModel {

    @Persist
    private String title;

    @Persist
    private String artist;

    @Persist
    private int diskNumber;

    @Persist
    private int trackNumber;

    @Persist
    private String filePath;

    @Persist
    private Album album;



    public Song() {
        super();
    }

    public Song(File file) {
        this(file.getAbsolutePath());
    }

    public Song(Path filePath) {
        this(filePath.toString());
    }

    private Song(String filePath) {
        super();

        this.filePath = filePath;
        try {
            Mp3File mp3File = new Mp3File(this.filePath);

            if (mp3File.hasId3v2Tag()) {
                readId3v2Tag(mp3File.getId3v2Tag());
            } else if (mp3File.hasId3v1Tag()) {
                readId3v1Tag(mp3File.getId3v1Tag());
            }
        } catch (IOException | UnsupportedTagException | InvalidDataException e) {
            e.printStackTrace();
        }

        if (getTitle() == null) setTitle(new File(filePath).getName());
        if (getAlbum() == null) album = Album.getUnknownAlbum();
        album.addTrack(this);
    }

    private void readId3v2Tag(ID3v2 tag) {
        readId3v1Tag(tag);

        if (tag.getPartOfSet() != null) {
            try {
                diskNumber = Integer.parseInt(tag.getPartOfSet().replaceAll("(^[0]+|/\\d*$)", ""));
            } catch (NumberFormatException e) {
                diskNumber = 0;
            }
        }

        if (tag.getAlbumArtist() != null) {
            album.setArtist(tag.getAlbumArtist());
        }

        Album persistedAlbum = DAO.forModel(Album.class).find(album);

        if (persistedAlbum != null) {
            album = persistedAlbum;
        } else {
            DAO.forModel(Album.class).insert(album);
        }

        boolean albumModified = false;

        if (album.getCover() == null) {
            byte[] albumCover = tag.getAlbumImage();
            if (albumCover != null) {
                album.setCover(albumCover);
                albumModified = true;
            }
        }

        if (album.getArtist() != null) {
            String albumArtist = tag.getAlbumArtist();
            if (albumArtist != null) {
                album.setArtist(albumArtist);
                albumModified = true;
            }
        }

        if (albumModified) {
            DAO.forModel(Album.class).update(album);
        }
    }

    private void readId3v1Tag(ID3v1 tag) {
        title = tag.getTitle();
        artist = tag.getArtist();
        if (tag.getTrack() != null) {
            try {
                trackNumber = Integer.parseInt(tag.getTrack().replaceAll("(^[0]+|/\\d*$)", ""));
            } catch (NumberFormatException e) {
                trackNumber = 0;
            }
        }

        album = new Album();
        album.setTitle(tag.getAlbum());
        album.setArtist(artist);
        try {
            album.setYear(Integer.parseInt(tag.getYear()));
        } catch (NumberFormatException ignore) {
        }
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

    public int getDiskNumber() {
        return diskNumber;
    }

    public void setDiskNumber(int diskNumber) {
        this.diskNumber = diskNumber;
    }

    public int getTrackNumber() {
        return trackNumber;
    }

    public void setTrackNumber(int trackNumber) {
        this.trackNumber = trackNumber;
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        if (this.album == album) return;
        if (this.album != null) {
            this.album.removeTrack(this);
        }
        this.album = album;
        this.album.addTrack(this);
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public int compareTo(AbstractModel model) {
        if (!(model instanceof Song)) return 1;
        Song song = (Song) model;
        if (equals(song)) return 0;
        if (getAlbum() != null) {
            if (!getAlbum().equals(song.getAlbum())) {
                return getAlbum().compareTo(song.getAlbum());
            }
        } else if (((Song) model).getAlbum() != null) {
            return 1;
        }
        if (getDiskNumber() != song.getDiskNumber()) {
            return getDiskNumber() - song.getDiskNumber();
        }

        if (getTrackNumber() != song.getTrackNumber()) {
            return getTrackNumber() - song.getTrackNumber();
        }
        return getFilePath().compareTo(((Song) model).getFilePath());
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) || o instanceof Song && getFilePath() != null && getFilePath().equals(((Song) o).getFilePath());
    }

    @Override
    public String toString() {
        return String.format("[%s - %s] %d %d - %s", getArtist(), getAlbum() != null ? getAlbum().getTitle() : "NO ALBUM", getDiskNumber(), getTrackNumber(), getTitle());
    }

    private boolean isPlaying;

    public void setIsPlaying(boolean isPlaying) {
        if (this.isPlaying != isPlaying) {
            this.isPlaying = isPlaying;
            notifyPlayingStatusChange();
        }
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    List<PlayingStatusObserver> observers = new LinkedList<>();

    public void addPlayingStatusObserver(PlayingStatusObserver observer) {
        if (!observers.contains(observer)) observers.add(observer);
    }

    public void removePlayingStatusObserver(PlayingStatusObserver observer) {
        observers.remove(observer);
    }

    public void notifyPlayingStatusChange() {
        for (PlayingStatusObserver observer: observers) {
            observer.onPlayingStatusChange();
        }
    }

    public interface PlayingStatusObserver {
        void onPlayingStatusChange();
    }
}

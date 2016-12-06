package br.imd.mediaplayer.model;

import br.imd.mediaplayer.DaoConfig;
import br.imd.mediaplayer.dao.DAO;
import br.imd.mediaplayer.model.annotation.Model;
import br.imd.mediaplayer.model.annotation.Persist;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Created by gabriel on 02/12/16.
 */
@Model("albums.txt")
public class Album extends AbstractModel {

    @Persist
    private String title;

    @Persist
    private String artist;

    @Persist
    private int year;

    @Persist
    private String cover;

    private Image coverImage;

    private Map<Integer, SortedSet<Song>> disks = new TreeMap<>();

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

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Map<Integer, SortedSet<Song>> getDisks() {
        return disks;
    }

    public void setDisks(Map<Integer, SortedSet<Song>> disks) {
        this.disks = disks;
    }

    void addTrack(Song song) {
        if (song == null || song.getAlbum() != this) return;

        int diskNumber = song.getDiskNumber() > 0 ? song.getDiskNumber() - 1 : 0;

        SortedSet<Song> songs;
        if ((songs = disks.get(diskNumber)) == null) {
            disks.put(diskNumber, songs = new TreeSet<>());
        }

        try {
            if (songs.add(song)) {
                DAO.forModel(Album.class).update(this);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    void removeTrack(Song song) {
        if (song == null || song.getAlbum() != this) return;
        int diskNumber = song.getDiskNumber() > 0 ? song.getDiskNumber() - 1 : 0;
        if (disks.size() <= diskNumber) return;

        SortedSet<Song> disk = disks.get(diskNumber);
        if (disk.remove(song)) {
            if (disk.isEmpty()) disks.remove(diskNumber);
            DAO.forModel(Album.class).update(this);
        }

    }

    public Image getCoverImage() {
        if (coverImage == null) {
            if (cover != null) {
                try {
                    coverImage = new Image(new FileInputStream(cover));
                } catch (FileNotFoundException e) {
                    coverImage = new Image("/image/dafault-cover.png");
                }
            } else {
                coverImage = new Image("/image/default-cover.png");
            }
        }
        return coverImage;
    }

    public String getCover() {
        return cover;
    }

    private void setCover(String cover) {
        this.cover = cover;
    }

    @Override
    public int compareTo(AbstractModel model) {
        if (model == null && title == null && artist == null) return 0;
        if (!(model instanceof Album)) return 1;
        Album album = (Album) model;
        return (""+getArtist()+getTitle()).compareTo("" +album.getArtist()+album.getTitle());
    }

    void setCover(byte[] albumCover) {
        try {
            InputStream in = new ByteArrayInputStream(albumCover);
            BufferedImage img = ImageIO.read(in);

            java.awt.Image coverImage = img.getScaledInstance(150, -1, java.awt.Image.SCALE_SMOOTH);

            BufferedImage bufThumb = new BufferedImage(
                    coverImage.getWidth(null),
                    coverImage.getHeight(null),
                    BufferedImage.TYPE_INT_RGB);

            bufThumb.getGraphics().drawImage(coverImage, 0, 0, null);
            File destFile = new File(DaoConfig.ALBUM_COVERS, "" + getId() + ".png");
            if (ImageIO.write(bufThumb, "png", destFile)) {
                setCover(destFile.getAbsolutePath());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "Album{" +
                "title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", year=" + year +
                ", cover='" + cover + '\'' +
                ", disks=" + disks +
                '}';
    }

    private static Album UNKNOWN_ALBUM;

    public static Album getUnknownAlbum() {
        if (UNKNOWN_ALBUM != null) {
            return UNKNOWN_ALBUM;
        }
        UNKNOWN_ALBUM = DAO.forModel(Album.class).find(new Album());
        if (UNKNOWN_ALBUM == null) {
            UNKNOWN_ALBUM = new Album();
            DAO.forModel(Album.class).insert(UNKNOWN_ALBUM);
        }
        return UNKNOWN_ALBUM;
    }
}

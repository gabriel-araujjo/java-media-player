package br.imd.mediaplayer.adapter;

import br.imd.mediaplayer.layout.RecyclerView;
import br.imd.mediaplayer.model.Album;
import br.imd.mediaplayer.model.Song;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.SortedSet;

/**
 * Created by gabriel on 06/12/16.
 */
public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.Holder> {

    private LinkedList<TrackAdapter.Holder> trackHoldersCache = new LinkedList<>();
    private TrackAdapter trackAdapter = new TrackAdapter();

    @Override
    public Holder onCreateViewHolder(FXMLLoader loader) {
        loader.setLocation(getClass().getResource("/layout/album.fxml"));
        return new Holder(loader, trackHoldersCache, trackAdapter);
    }

    @Override
    public void onBindViewHolder(Holder holder, Object item) {
        holder.bind((Album) item);
    }

    static final class Holder extends RecyclerView.ViewHolder {

        Album album;

        ImageView cover;
        Label title;
        VBox tracks;

        LinkedList<TrackAdapter.Holder> trackAdapters = new LinkedList<>();
        LinkedList<TrackAdapter.Holder> trackCache;
        TrackAdapter trackAdapter;

        Holder(FXMLLoader loader, LinkedList<TrackAdapter.Holder> trackAdaptersCache, TrackAdapter trackAdapter) {
            super(loader);
            trackCache = trackAdaptersCache;
            this.trackAdapter = trackAdapter;

            cover = (ImageView) getView().lookup("#albumCover");
            title = (Label) getView().lookup("#albumTItle");
            tracks = (VBox) getView().lookup("#albumTracks");
        }

        void bind(Album item) {
            album = item;
            cover.setImage(album.getCoverImage());
            title.setText(album.getTitle());
            tracks.getChildren().clear();
            trackCache.addAll(trackAdapters);
            trackAdapters.clear();

            int diskCount = album.getDisks().size();
            for (int i = 0; i < diskCount; i++) {
                SortedSet<Song> songs = album.getDisks().get(i);
                try {
                    for (Song s : songs) {
                        TrackAdapter.Holder holder = getTrackHolder();
                        trackAdapter.onBindViewHolder(holder, s);
                        tracks.getChildren().add(holder.getView());
                    }
                } catch (ConcurrentModificationException e) {
                    break;
                }
            }
        }

        TrackAdapter.Holder getTrackHolder() {
            TrackAdapter.Holder holder = trackCache.poll();
            if (holder == null) {
                holder = trackAdapter.onCreateViewHolder(new FXMLLoader());
            }
            return holder;
        }
    }
}

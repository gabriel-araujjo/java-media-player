package br.imd.mediaplayer.adapter;

import br.imd.mediaplayer.layout.RecyclerView;
import br.imd.mediaplayer.model.Song;
import br.imd.mediaplayer.service.PlayService;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

/**
 * Created by gabriel on 06/12/16.
 */
public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.Holder> {

    @Override
    public Holder onCreateViewHolder(FXMLLoader loader) {
        loader.setLocation(getClass().getResource("/layout/track.fxml"));
        return new Holder(loader);
    }

    @Override
    public void onBindViewHolder(Holder holder, Object item) {
        holder.bind((Song) item);
    }

    static final class Holder extends RecyclerView.ViewHolder implements EventHandler<MouseEvent>,Song.PlayingStatusObserver {
        Song song;

        Label trackNumber;
        Label trackName;
        ImageView playingStatus;
        Holder(FXMLLoader loader) {
            super(loader);

            trackNumber = (Label) getView().lookup("#trackNumber");
            trackName = (Label) getView().lookup("#trackName");
            playingStatus = (ImageView) getView().lookup("#playingStatus");

            getView().setOnMouseClicked(this);
        }

        void bind(Song item) {
            if (song != null) {
                song.removePlayingStatusObserver(this);
            }
            song = item;

            trackNumber.setText(song.getTrackNumber() > 0 ? Integer.toString(song.getTrackNumber()) : "");
            trackName.setText(song.getTitle());
            if (song.isPlaying()) {
                playingStatus.getStyleClass().add("trackplaying");
            } else {
                playingStatus.getStyleClass().remove("trackplaying");
            }
            song.addPlayingStatusObserver(this);
        }

        @Override
        public void handle(MouseEvent mouseEvent) {
            if (song != null) {
                PlayService.getInstance().playSong(song);
            }
        }

        @Override
        public void onPlayingStatusChange() {
            if (song.isPlaying()) {
                playingStatus.getStyleClass().add("trackplaying");
            } else {
                playingStatus.getStyleClass().remove("trackplaying");
            }
        }
    }

}

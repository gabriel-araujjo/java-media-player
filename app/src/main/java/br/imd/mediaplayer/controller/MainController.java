package br.imd.mediaplayer.controller;

import br.imd.mediaplayer.adapter.AlbumAdapter;
import br.imd.mediaplayer.dao.DAO;
import br.imd.mediaplayer.layout.RecyclerView;
import br.imd.mediaplayer.model.Album;
import br.imd.mediaplayer.model.Directory;
import br.imd.mediaplayer.service.PlayService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import uk.co.caprica.vlcj.player.MediaPlayer;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by gabriel on 01/12/16.
 */
public class MainController extends AbstractController implements Initializable, PlayService.MediaPlayerObserver {

    @FXML
    RecyclerView albums;

    @FXML
    ProgressBar musicProgressBar;

    ImageView playPauseButton;

    @FXML
    Slider musicProgressSlider;

    @Override
    protected void onAttached() {
        super.onAttached();

        playPauseButton = (ImageView) getScene().lookup("#playPauseButton");

        musicProgressBar.setProgress(0);

        musicProgressSlider.valueProperty().addListener((ov, oldValue, newValue) -> {
            musicProgressBar.setProgress(newValue.doubleValue() / 100);
        });
        PlayService.getInstance().addObserver(this);
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        PlayService.getInstance().removeObserver(this);
    }

    @Override
    public String getTitle() {
        return "Main Page";
    }

    public void showAddFolderDialog(MouseEvent mouseEvent) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose directory to scan songs");
        File file = chooser.showDialog(getStage());
        if (file != null) {
            Directory d = new Directory();
            d.setPath(file.getAbsolutePath());
            DAO.forModel(Directory.class).insert(d);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        albums.setItems(DAO.forModel(Album.class).getObservableListVersion());
        albums.setAdapter(new AlbumAdapter());
    }

    @Override
    public void onProgressChange(float progress) {
        if (!seeking)
        musicProgressSlider.setValue(progress * 100);
    }

//    private static final Image PLAYING_BUTTON = new Image("/image/play.png");
//    private static final Image PAUSED_BUTTON = new Image("/image/pause.png");

    @Override
    public void onPlayingStatusChange(boolean isPlaying) {
        System.out.println("isPlaying = " + isPlaying);
        if (isPlaying) {
            playPauseButton.getStyleClass().add("playing");
        } else {
            playPauseButton.getStyleClass().remove("playing");
        }
    }

    public void toggleMedia(MouseEvent mouseEvent) {
        if (PlayService.getInstance().getMediaPlayer().isPlaying()) {
            PlayService.getInstance().pause();
        } else {
            PlayService.getInstance().resume();
        }
    }

    private boolean seeking;
    public void startSeeking(MouseEvent mouseEvent) {
        seeking = true;
    }

    public void seekMedia(MouseEvent mouseEvent) {
        MediaPlayer player = PlayService.getInstance().getMediaPlayer();
        if (player.isPlaying() && player.isSeekable()) {
            player.setTime((long) (musicProgressSlider.getValue() / 100 * player.getLength()));
        }
        seeking = false;
    }
}

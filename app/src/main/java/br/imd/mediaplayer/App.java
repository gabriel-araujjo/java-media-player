package br.imd.mediaplayer; /**
 * Created by gabriel on 02/12/16.
 */

import br.imd.mediaplayer.controller.AbstractController;
import br.imd.mediaplayer.dao.DAO;
import br.imd.mediaplayer.model.Song;
import br.imd.mediaplayer.service.SongScannerService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    public static int DEFAULT_WINDOW_WIDTH = 1000;
    public static int DEFAULT_WINDOW_HEIGHT = 600;

    private Stage stage;
    private AbstractController currentController;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        SongScannerService.getInstance().start();
        stage = primaryStage;
        setLayout("login");
        primaryStage.show();
        DAO.forModel(Song.class).list();
        stage.setOnCloseRequest(event -> {
            try {
                DAO.persistData();
            } catch (IOException e) {
                e.printStackTrace();
            }
            SongScannerService.getInstance().stop();
            System.exit(0);
        });
    }

    public void setLayout(String layout) {
        if (layout == null) throw new IllegalArgumentException("Can't set a null layout");
        if (currentController != null) currentController.detach();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/" + layout + ".fxml"));
            Parent root = loader.load();
            currentController = loader.getController();
            Scene scene = new Scene(root, DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT);
            currentController.setScene(scene);
            currentController.attach(this, stage);
        } catch (IOException e) {
            e.printStackTrace();
            currentController = null;
        }
    }
}

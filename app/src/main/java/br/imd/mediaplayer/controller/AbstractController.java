package br.imd.mediaplayer.controller;

import br.imd.mediaplayer.App;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Created by gabriel on 01/12/16.
 */
public abstract class AbstractController {

    private static final String DEFAULT_TITLE =  "G. A. Player";

    private Scene scene;
    private Stage stage;
    private App app;

    /**
     * Returns the scene associated with this page, if no scene is set, #getFxmlResource() method is called
     * @return the scene associated with this page
     */
    protected Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    /**
     * Returns the app this page belongs to
     * @return the app this page belongs to
     */
    protected App getApp() {
        return app;
    }

    /**
     * Returns the stage this page belongs to
     * @return the stage this page belongs to
     */
    protected Stage getStage() {
        return stage;
    }

    public String getTitle() {
        return DEFAULT_TITLE;
    }

    /**
     * Attach this page to a stage. If this page is already attached to another stage, do nothing.
     * @param app the main app
     * @param stage the stage to attach this page on
     * @return whether the attachment was successful
     */
    public boolean attach(App app, Stage stage) {
        if (this.stage != null) return false;
        this.app = app;
        this.stage = stage;
        stage.setScene(getScene());
        stage.setTitle(getTitle());
        onAttached();
        return true;
    }

    /**
     * Lazy stage detach, it must be called before an attached in another class
     */
    public void detach() {
        this.app = null;
        this.stage = null;
        onDetach();
    }

    public boolean isAttached() {
        return stage != null;
    }

    protected void onAttached() {

    }

    protected void onDetach() {

    }
}

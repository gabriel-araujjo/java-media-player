package br.imd.mediaplayer;

import javafx.scene.image.Image;

/**
 * Created by gabriel on 06/12/16.
 */
public class Consts {
    public static Image DEFAULT_COVER;

    static {
        try {
            DEFAULT_COVER = new Image(Consts.class.getResourceAsStream("/image/default-cover.png"));
        } catch (Exception ex) {
            DEFAULT_COVER = null;
        }
    }
}

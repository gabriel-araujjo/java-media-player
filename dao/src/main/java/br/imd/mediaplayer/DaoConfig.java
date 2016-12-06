package br.imd.mediaplayer;

import java.io.File;

/**
 * Created by gabriel on 03/12/16.
 */
public class DaoConfig {


    private static final String DATA_DIRNAME = "jmediaplayer";
    private static final String ALBUM_COVERS_DIRNAME = "covers";

    /** Directory where configuration files are stored */
    public static final File DATA_DIRECTORY;

    /** Directory where thumbs are stored */
    public static final File ALBUM_COVERS;

    /** It indicates whether the configuration data is new */
    public static final boolean FRESH_CONFIG;

    /** Indicates whether directory config hierarchy has error*/
    public static final boolean ERROR;

    static {
        File userHome = new File(System.getProperty("user.home"));
        DATA_DIRECTORY = new File(userHome, DATA_DIRNAME);

        FRESH_CONFIG = DATA_DIRECTORY.mkdirs();

        ALBUM_COVERS = new File(DATA_DIRECTORY, ALBUM_COVERS_DIRNAME);

        ERROR = !(ALBUM_COVERS.exists() || ALBUM_COVERS.mkdir());
    }
}

package br.imd.mediaplayer.model;

import br.imd.mediaplayer.model.annotation.Model;
import br.imd.mediaplayer.model.annotation.Persist;

/**
 * Directory model
 */
@Model("diretorios.txt")
public class Directory extends AbstractModel {
    @Persist
    private String path;

    @Persist
    private long lastModified;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }
}

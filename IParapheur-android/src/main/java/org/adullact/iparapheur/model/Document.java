package org.adullact.iparapheur.model;

/**
 * Created by jmaire on 03/11/2013.
 */
public class Document {

    private final String id;
    private final String name;
    /**
     * URL of the document (its content)
     */
    private final String url;
    /**
     * path of the file (if downloaded) on the device's storage
     */
    private String path;

    public Document(String id, String name, String url) {
        this.id = id;
        this.name = name;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}

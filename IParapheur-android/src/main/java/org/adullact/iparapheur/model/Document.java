package org.adullact.iparapheur.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jmaire on 03/11/2013.
 */
public class Document {

    private final String id;
    private final String name;
    private final Map<Integer, List<Annotation>> annotations;
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
        this.annotations = new HashMap<Integer, List<Annotation>>();
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

    public Map<Integer, List<Annotation>> getAnnotations() {
        return annotations;
    }
}

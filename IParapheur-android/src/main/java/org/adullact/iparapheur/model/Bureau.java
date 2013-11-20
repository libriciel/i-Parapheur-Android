package org.adullact.iparapheur.model;

/**
 * Created by jmaire on 19/11/2013.
 */
public class Bureau {

    private String id;
    private String title;

    public Bureau(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return title;
    }
}

package com.company.art_and_culture.myarts.pojo;

import java.util.Collection;

public class ServerResponse {
    private String result;
    private String message;
    private Collection<Art> listArts;
    private Art art;


    public Collection<Art> getListArts() {
        return listArts;
    }

    public String getResult() {
        return result;
    }

    public String getMessage() {
        return message;
    }

    public Art getArt() {
        return art;
    }
}

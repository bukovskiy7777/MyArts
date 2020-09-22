package com.company.art_and_culture.myarts.pojo;

import java.util.ArrayList;
import java.util.Collection;

public class ServerResponse {
    private String result;
    private String message;
    private ArrayList<Art> listArts;
    private Art art;
    private ArrayList<Suggest> listSuggests;
    private ArrayList<ExploreObject> listExplore;
    private Maker artMaker;

    public ArrayList<Art> getListArts() {
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

    public ArrayList<Suggest> getListSuggests() {
        return listSuggests;
    }

    public ArrayList<ExploreObject> getListExplore() {
        return listExplore;
    }

    public Maker getArtMaker() {
        return artMaker;
    }

}

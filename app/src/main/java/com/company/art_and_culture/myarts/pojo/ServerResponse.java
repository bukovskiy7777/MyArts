package com.company.art_and_culture.myarts.pojo;

import java.util.ArrayList;
import java.util.Collection;

public class ServerResponse {
    private String result;
    private String message;
    private ArrayList<Art> listArts;
    private Art art;
    private ArrayList<Suggest> listSuggests;
    private ArrayList<ExploreObject> listMaker;
    private ArrayList<ExploreObject> listCulture;
    private ArrayList<ExploreObject> listMedium;
    private ArrayList<ExploreObject> listCentury;
    private Maker artMaker;
    private ArrayList<Maker> listMakers;

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

    public Maker getArtMaker() {
        return artMaker;
    }

    public ArrayList<Maker> getListMakers() {
        return listMakers;
    }

    public ArrayList<ExploreObject> getListMaker() {
        return listMaker;
    }

    public ArrayList<ExploreObject> getListCulture() {
        return listCulture;
    }

    public ArrayList<ExploreObject> getListMedium() {
        return listMedium;
    }

    public ArrayList<ExploreObject> getListCentury() {
        return listCentury;
    }

}

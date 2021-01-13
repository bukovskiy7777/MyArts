package com.company.art_and_culture.myarts.pojo;

import java.util.ArrayList;

public class ServerResponse {
    private String result;
    private String message;
    private ArrayList<Art> listArts;
    private Art art;
    private ArrayList<Suggest> listSuggests;
    private ArrayList<ExploreObject> listExplore;
    private Maker artMaker;
    private ArrayList<Maker> listMakers;
    private String filter;
    private String century;
    private ArrayList<Attribute> listAttribute;
    private String suggestQuery;
    private ArrayList<Folder> listFolders;
    private ArrayList<String> listMakerFilters;

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

    public ArrayList<ExploreObject> getListExplore() {
        return listExplore;
    }

    public String getFilter() {
        return filter;
    }

    public String getCentury() {
        return century;
    }

    public ArrayList<Attribute> getListAttribute() {
        return listAttribute;
    }

    public String getSuggestQuery() {
        return suggestQuery;
    }

    public ArrayList<Folder> getListFolders() {
        return listFolders;
    }

    public ArrayList<String> getListMakerFilters() {
        return listMakerFilters;
    }

}

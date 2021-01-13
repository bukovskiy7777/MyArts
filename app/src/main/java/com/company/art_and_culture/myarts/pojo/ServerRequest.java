package com.company.art_and_culture.myarts.pojo;

import java.util.ArrayList;

public class ServerRequest {
    private String operation;
    private int pageNumber;
    private String userUniqueId;

    private String suggestQuery;
    private String searchQuery;

    private Art art;
    private ArrayList<Art> oldList;
    private Maker maker;
    private Folder folder;

    private String charFilter;
    private String centuryFilter;
    private String makerFilter;
    private String keywordFilter;
    private String keywordType;

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public void setUserUniqueId(String userUniqueId) {
        this.userUniqueId = userUniqueId;
    }

    public void setArt(Art art) {
        this.art = art;
    }

    public void setOldList(ArrayList<Art> oldList) {
        this.oldList = oldList;
    }

    public void setMaker(Maker maker) {
        this.maker = maker;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }

    public void setSuggestQuery(String suggestQuery) {
        this.suggestQuery = suggestQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public void setCharFilter(String charFilter) {
        this.charFilter = charFilter;
    }

    public void setCenturyFilter(String centuryFilter) {
        this.centuryFilter = centuryFilter;
    }

    public void setMakerFilter(String makerFilter) {
        this.makerFilter = makerFilter;
    }

    public void setKeywordFilter(String keywordFilter) {
        this.keywordFilter = keywordFilter;
    }

    public void setKeywordType(String keywordType) {
        this.keywordType = keywordType;
    }
}

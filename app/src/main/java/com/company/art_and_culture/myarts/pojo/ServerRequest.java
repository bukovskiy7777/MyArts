package com.company.art_and_culture.myarts.pojo;

import java.util.ArrayList;

public class ServerRequest {
    private String operation;
    private int pageNumber;
    private String userUniqueId;
    private Art art;
    private String suggestQuery;
    private String searchQuery;
    private String artQuery;
    private ArrayList<Art> oldList;

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

    public void setSuggestQuery(String suggestQuery) {
        this.suggestQuery = suggestQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public void setArtQuery(String artQuery) {
        this.artQuery = artQuery;
    }

    public void setOldList(ArrayList<Art> oldList) {
        this.oldList = oldList;
    }

}

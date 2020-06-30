package com.company.art_and_culture.myarts.pojo;

public class ServerRequest {
    private String operation;
    private int pageNumber;
    private String userUniqueId;
    private Art art;
    private String searchString;
    private String searchQuery;
    private String artMaker;

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

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public void setArtMaker(String artMaker) {
        this.artMaker = artMaker;
    }

}

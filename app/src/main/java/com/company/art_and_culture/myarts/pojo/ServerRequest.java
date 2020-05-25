package com.company.art_and_culture.myarts.pojo;

public class ServerRequest {
    private String operation;
    private int pageNumber;
    private String userUniqueId;
    private Art art;

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
}

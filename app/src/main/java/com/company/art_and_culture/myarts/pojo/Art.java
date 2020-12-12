package com.company.art_and_culture.myarts.pojo;

public class Art {

    private String artLink;
    private String artId;
    private String artTitle;
    private String artMaker;
    private String artLongTitle;
    private String artImgUrl;
    private String artImgUrlSmall;
    private String artProviderId;
    private String artProvider;
    private String artProviderCountry;
    private int artWidth;
    private int artHeight;
    private String artLogoUrl;
    private String artClassification;
    private boolean isLiked;
    private String century;
    private String artistBio;

    private boolean isChosenForAddToFolder = false;


    public void setArtWidth(int artWidth) {
        this.artWidth = artWidth;
    }

    public void setArtHeight(int artHeight) {
        this.artHeight = artHeight;
    }

    public String getArtLink() {
        return artLink;
    }

    public String getArtId() {
        return artId;
    }

    public String getArtTitle() {
        return artTitle;
    }

    public String getArtMaker() {
        return artMaker;
    }

    public String getArtLongTitle() {
        return artLongTitle;
    }

    public String getArtImgUrl() {
        return artImgUrl;
    }

    public String getArtImgUrlSmall() {
        return artImgUrlSmall;
    }

    public int getArtWidth() {
        return artWidth;
    }

    public int getArtHeight() {
        return artHeight;
    }

    public String getArtProvider() {
        return artProvider;
    }

    public String getArtProviderCountry() {
        return artProviderCountry;
    }

    public String getArtLogoUrl() {
        return artLogoUrl;
    }

    public String getArtClassification() {
        return artClassification;
    }

    public boolean getIsLiked() {
        return isLiked;
    }

    public String getArtProviderId() {
        return artProviderId;
    }

    public String getCentury() {
        return century;
    }

    public String getArtistBio() {
        return artistBio;
    }

    public boolean isChosenForAddToFolder() {
        return isChosenForAddToFolder;
    }

    public void setChosenForAddToFolder(boolean chosenForAddToFolder) {
        isChosenForAddToFolder = chosenForAddToFolder;
    }
}

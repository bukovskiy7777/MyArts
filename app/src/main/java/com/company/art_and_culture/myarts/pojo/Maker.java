package com.company.art_and_culture.myarts.pojo;

public class Maker {

    private String artMaker;
    private String artistBio;
    private String artistImageUrl;
    private int artCount;
    private boolean isLiked;

    private String artHeaderImageUrl;
    private int artWidth;
    private int artHeight;

    private String makerWikiDescription;
    private String makerWikiImageUrl;
    private String makerWikiPageUrl;

    public Maker(String artMaker, String artistBio, String artistImageUrl, String artHeaderImageUrl, int artWidth, int artHeight) {
        this.artMaker = artMaker;
        this.artistBio = artistBio;
        this.artistImageUrl = artistImageUrl;
        this.artHeaderImageUrl = artHeaderImageUrl;
        this.artWidth = artWidth;
        this.artHeight = artHeight;
    }

    public Maker(String artMaker, String artistBio, String artistImageUrl, String artHeaderImageUrl, int artWidth, int artHeight, String makerWikiDescription) {
        this.artMaker = artMaker;
        this.artistBio = artistBio;
        this.artistImageUrl = artistImageUrl;
        this.artHeaderImageUrl = artHeaderImageUrl;
        this.artWidth = artWidth;
        this.artHeight = artHeight;
        this.makerWikiDescription = makerWikiDescription;
    }

    public String getArtMaker() {
        return artMaker;
    }

    public void setArtMaker(String artMaker) {
        this.artMaker = artMaker;
    }

    public String getArtistBio() {
        return artistBio;
    }

    public void setArtistBio(String artistBio) {
        this.artistBio = artistBio;
    }

    public String getArtistImageUrl() {
        return artistImageUrl;
    }

    public void setArtistImageUrl(String artistImageUrl) {
        this.artistImageUrl = artistImageUrl;
    }

    public int getArtCount() {
        return artCount;
    }

    public void setArtCount(int artCount) {
        this.artCount = artCount;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public String getArtHeaderImageUrl() {
        return artHeaderImageUrl;
    }

    public void setArtHeaderImageUrl(String artHeaderImageUrl) {
        this.artHeaderImageUrl = artHeaderImageUrl;
    }

    public int getArtWidth() {
        return artWidth;
    }

    public void setArtWidth(int artWidth) {
        this.artWidth = artWidth;
    }

    public int getArtHeight() {
        return artHeight;
    }

    public void setArtHeight(int artHeight) {
        this.artHeight = artHeight;
    }

    public String getMakerWikiDescription() {
        return makerWikiDescription;
    }

    public void setMakerWikiDescription(String makerWikiDescription) {
        this.makerWikiDescription = makerWikiDescription;
    }

    public String getMakerWikiImageUrl() {
        return makerWikiImageUrl;
    }

    public void setMakerWikiImageUrl(String makerWikiImageUrl) {
        this.makerWikiImageUrl = makerWikiImageUrl;
    }

    public String getMakerWikiPageUrl() {
        return makerWikiPageUrl;
    }

    public void setMakerWikiPageUrl(String makerWikiPageUrl) {
        this.makerWikiPageUrl = makerWikiPageUrl;
    }

}



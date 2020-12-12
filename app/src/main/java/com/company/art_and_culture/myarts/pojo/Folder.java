package com.company.art_and_culture.myarts.pojo;

import java.util.ArrayList;

public class Folder {

    private String artImageUrl;
    private int artWidth;
    private int artHeight;
    private String artId;
    private String artProviderId;
    private int itemsCount;

    private String title;
    private String folderUniqueId;
    private String userUniqueId;
    private String createdAt;
    private boolean isPublic;
    private ArrayList<Art> folderListArts;
    public enum folderState {active, deleted}

    public Folder(String title, String folderUniqueId, String userUniqueId, boolean isPublic, ArrayList<Art> folderListArts) {
        this.title = title;
        this.folderUniqueId = folderUniqueId;
        this.userUniqueId = userUniqueId;
        this.isPublic = isPublic;
        this.folderListArts = folderListArts;
    }

    public String getArtImageUrl() {
        return artImageUrl;
    }

    public int getArtWidth() {
        return artWidth;
    }

    public int getArtHeight() {
        return artHeight;
    }

    public String getArtId() {
        return artId;
    }

    public String getArtProviderId() {
        return artProviderId;
    }

    public int getItemsCount() {
        return itemsCount;
    }

    public String getTitle() {
        return title;
    }

    public String getFolderUniqueId() {
        return folderUniqueId;
    }

    public String getUserUniqueId() {
        return userUniqueId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public ArrayList<Art> getFolderListArts() {
        return folderListArts;
    }
}

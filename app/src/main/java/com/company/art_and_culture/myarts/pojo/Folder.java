package com.company.art_and_culture.myarts.pojo;

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
    public enum folderState {active, deleted}

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
}

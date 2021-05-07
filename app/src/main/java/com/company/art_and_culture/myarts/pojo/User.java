package com.company.art_and_culture.myarts.pojo;

public class User {

    private String userDisplayName;
    private String userGivenName;
    private String userFamilyName;
    private String userEmail;
    private String userProviderId;
    private String userImageUrl;
    private String userAccountProvider;
    private String userUniqueId;

    public User(String userDisplayName, String userGivenName, String userFamilyName, String userEmail,
                String userProviderId, String userImageUrl, String userAccountProvider, String userUniqueId) {
        this.userDisplayName = userDisplayName;
        this.userGivenName = userGivenName;
        this.userFamilyName = userFamilyName;
        this.userEmail = userEmail;
        this.userProviderId = userProviderId;
        this.userImageUrl = userImageUrl;
        this.userAccountProvider = userAccountProvider;
        this.userUniqueId = userUniqueId;
    }

    public String getUserDisplayName() {
        return userDisplayName;
    }

    public String getUserGivenName() {
        return userGivenName;
    }

    public String getUserFamilyName() {
        return userFamilyName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserProviderId() {
        return userProviderId;
    }

    public String getUserImageUrl() {
        return userImageUrl;
    }

    public String getUserAccountProvider() {
        return userAccountProvider;
    }

    public String getUserUniqueId() {
        return userUniqueId;
    }
}

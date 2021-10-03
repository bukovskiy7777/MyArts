package com.company.art_and_culture.myarts.museum_fragment;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;

import com.company.art_and_culture.myarts.pojo.Art;

public class ArtDataSourceFactory extends DataSource.Factory<Integer, Art> {

    private Application application;
    private ArtDataSource artDataSource;
    private String artProviderId;

    public ArtDataSourceFactory(Application application, String artProviderId) {
        this.application = application;
        this.artProviderId = artProviderId;
        artDataSource = new ArtDataSource(application, artProviderId);
    }

    @NonNull
    @Override
    public DataSource<Integer, Art> create() {
        if (artDataSource.isInvalid()) artDataSource = new ArtDataSource(application, artProviderId);
        return artDataSource;
    }

    public boolean refresh() {
        boolean isConnected = artDataSource.isNetworkAvailable();
        if (isConnected){
            artDataSource.refresh();
        }
        return isConnected;
    }

    public void setArtProviderId(String artProviderId) {
        this.artProviderId = artProviderId;

        artDataSource.refresh();
    }

    public void restoreDataInMemory() {
        artProviderId = "";
        artDataSource.restoreDataInMemory();
    }
}

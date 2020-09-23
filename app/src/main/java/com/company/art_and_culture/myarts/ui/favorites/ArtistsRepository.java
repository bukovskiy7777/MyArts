package com.company.art_and_culture.myarts.ui.favorites;

import android.app.Application;

import com.company.art_and_culture.myarts.pojo.Maker;

import java.util.ArrayList;

import androidx.lifecycle.LiveData;

public class ArtistsRepository {

    private static ArtistsRepository instance;
    private ArtistsDataSource artistsDataSource;


    public static ArtistsRepository getInstance(Application application){

        if(instance == null){
            instance = new ArtistsRepository(application);
        }
        return instance;
    }

    public ArtistsRepository(Application application) {

        artistsDataSource = new ArtistsDataSource(application);

    }

    public LiveData<ArrayList<Maker>> getMakerList(){
        return artistsDataSource.getMakerList();
    }

    public LiveData<Boolean> getIsLoading() {
        return artistsDataSource.getIsLoading();
    }

    public LiveData<Boolean> getIsListEmpty() {
        return artistsDataSource.getIsListEmpty();
    }

    public boolean refresh() {
        boolean isConnected = artistsDataSource.isNetworkAvailable();
        if (isConnected){
            artistsDataSource.refresh();
        }
        return isConnected;
    }
}

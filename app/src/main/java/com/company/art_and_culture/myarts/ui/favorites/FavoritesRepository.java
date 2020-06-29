package com.company.art_and_culture.myarts.ui.favorites;

import android.app.Application;

import com.company.art_and_culture.myarts.pojo.Art;

import java.util.ArrayList;
import java.util.Collection;

import androidx.lifecycle.LiveData;

public class FavoritesRepository {

    private static FavoritesRepository instance;
    private FavoritesDataSource favoritesDataSource;

    public static FavoritesRepository getInstance(Application application){

        if(instance == null){
            instance = new FavoritesRepository(application);
        }
        return instance;
    }

    public FavoritesRepository(Application application) {

        favoritesDataSource = new FavoritesDataSource(application);

    }

    public LiveData<ArrayList<Art>> getArtList(){
        return favoritesDataSource.getArtList();
    }

    public LiveData<Boolean> getIsLoading() {
        return favoritesDataSource.getIsLoading();
    }

    public LiveData<Boolean> getIsListEmpty() {
        return favoritesDataSource.getIsListEmpty();
    }

    public boolean refresh() {
        boolean isConnected = favoritesDataSource.isNetworkAvailable();
        if (isConnected){
            favoritesDataSource.refresh();
        }
        return isConnected;
    }

}

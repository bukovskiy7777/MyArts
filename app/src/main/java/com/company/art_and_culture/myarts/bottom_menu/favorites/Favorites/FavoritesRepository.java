package com.company.art_and_culture.myarts.bottom_menu.favorites.Favorites;

import android.app.Application;

import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.pojo.Art;

import java.util.ArrayList;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

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

    public void setActivity(MainActivity activity) {
        activity.getArt().observe(activity, new Observer<Art>() {
            @Override
            public void onChanged(Art art) {
                refresh();
            }
        });
    }
}

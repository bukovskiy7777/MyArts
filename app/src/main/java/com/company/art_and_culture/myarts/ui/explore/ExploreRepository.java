package com.company.art_and_culture.myarts.ui.explore;

import android.app.Application;

import com.company.art_and_culture.myarts.pojo.ExploreObject;

import java.util.ArrayList;

import androidx.lifecycle.LiveData;

class ExploreRepository {

    private static ExploreRepository instance;
    private ExploreDataSource exploreDataSource;

    public static ExploreRepository getInstance(Application application){

        if(instance == null){
            instance = new ExploreRepository(application);
        }
        return instance;
    }

    public ExploreRepository(Application application) {

        exploreDataSource = new ExploreDataSource(application);

    }

    public LiveData<Boolean> getIsLoading() {
        return exploreDataSource.getIsLoading();
    }

    public LiveData<Boolean> getIsListEmpty() {
        return exploreDataSource.getIsListEmpty();
    }

    public boolean refresh() {
        boolean isConnected = exploreDataSource.isNetworkAvailable();
        if (isConnected){
            exploreDataSource.refresh();
        }
        return isConnected;
    }

    public LiveData<ArrayList<ExploreObject>> getExploreList() {
        return exploreDataSource.getExploreList();
    }
}

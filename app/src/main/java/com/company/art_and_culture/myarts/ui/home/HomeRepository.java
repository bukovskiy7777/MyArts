package com.company.art_and_culture.myarts.ui.home;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.pojo.Art;

import java.util.ArrayList;

import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

public class HomeRepository {

    private static HomeRepository instance;
    private LiveData<PagedList<Art>> artList;
    private LiveData<Art> art;
    private LiveData<Boolean> isLoading;
    private LiveData<Boolean> isListEmpty;
    private HomeDataSource homeDataSource;
    private HomeDataSourceFactory homeDataSourceFactory;


    public static HomeRepository getInstance(Application application){
        if(instance == null){
            instance = new HomeRepository(application);
        }
        return instance;
    }

    public HomeRepository(Application application) {

        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(Constants.PAGE_SIZE)
                .setInitialLoadSizeHint(Constants.PAGE_SIZE)
                .build();
        homeDataSourceFactory = new HomeDataSourceFactory(application);
        artList = new LivePagedListBuilder<>(homeDataSourceFactory, config).build();

        homeDataSource = (HomeDataSource) homeDataSourceFactory.create();//if remove this line artLike will not working after refresh

        art = homeDataSource.getArt();
        isLoading = homeDataSource.getIsLoading();
        isListEmpty = homeDataSource.getIsListEmpty();
    }

    public LiveData<PagedList<Art>> getArtList(){
        return artList;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getIsListEmpty() {
        return isListEmpty;
    }

    public LiveData<Art> getArt() {
        return art;
    }

    public boolean likeArt(Art art, int position, String userUniqueId) {

        boolean isConnected = homeDataSource.isNetworkAvailable();
        if (isConnected){
            homeDataSource.likeArt(art, position, userUniqueId);
        }
        return isConnected;
    }

    public boolean refresh() {
        return homeDataSourceFactory.refresh();
    }

    public HomeDataSource getHomeDataSource() {
        return homeDataSource;
    }

    public void writeDimentionsOnServer(Art art) {
        homeDataSource.writeDimentionsOnServer(art);
    }
}

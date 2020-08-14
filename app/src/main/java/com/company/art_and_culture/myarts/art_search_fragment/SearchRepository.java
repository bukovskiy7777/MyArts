package com.company.art_and_culture.myarts.art_search_fragment;

import android.app.Application;
import android.util.Log;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.pojo.Art;

import java.util.ArrayList;

import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

public class SearchRepository {

    private static SearchRepository instance;
    private LiveData<PagedList<Art>> artList;
    private LiveData<Art> art;
    private SearchDataSource searchDataSource;
    private SearchDataSourceFactory searchDataSourceFactory;
    private String searchQuery;


    public static SearchRepository getInstance(Application application){
        if(instance == null){
            instance = new SearchRepository(application);
        }
        return instance;
    }

    public SearchRepository(Application application) {

        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(Constants.PAGE_SIZE)
                .setInitialLoadSizeHint(Constants.PAGE_SIZE)
                .build();
        searchDataSourceFactory = new SearchDataSourceFactory(application);
        artList = new LivePagedListBuilder<>(searchDataSourceFactory, config).build();

        searchDataSource = (SearchDataSource) searchDataSourceFactory.create();//if remove this line artLike will not working after refresh
        art = searchDataSource.getArt();
    }

    public LiveData<PagedList<Art>> getArtList(){
        return artList;
    }

    public LiveData<Boolean> getIsLoading() {
        return searchDataSource.getIsLoading();
    }

    public LiveData<Boolean> getIsListEmpty() {
        return searchDataSource.getIsListEmpty();
    }

    public LiveData<Art> getArt() {
        return art;
    }

    public boolean likeArt(Art art, int position, String userUniqueId) {

        boolean isConnected = searchDataSource.isNetworkAvailable();
        if (isConnected){
            searchDataSource.likeArt(art, position, userUniqueId);
        }
        return isConnected;
    }

    public boolean refresh() {
        return searchDataSourceFactory.refresh();
    }

    public void writeDimentionsOnServer(Art art) {
        searchDataSource.writeDimentionsOnServer(art);
    }

    public SearchRepository finish(Application application) {
        searchDataSource.refresh();
        instance = new SearchRepository(application);
        return instance;
    }

    public SearchDataSource getSearchDataSource() {
        return searchDataSource;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public String getSearchQuery() {
        return searchQuery;
    }
}

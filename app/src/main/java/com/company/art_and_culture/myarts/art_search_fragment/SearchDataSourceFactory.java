package com.company.art_and_culture.myarts.art_search_fragment;

import android.app.Application;

import com.company.art_and_culture.myarts.pojo.Art;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.PageKeyedDataSource;

public class SearchDataSourceFactory extends DataSource.Factory<Integer, Art> {

    private MutableLiveData<PageKeyedDataSource<Integer, Art>> searchDataSourceMutableLiveData = new MutableLiveData<>();
    private Application application;
    private SearchDataSource searchDataSource;
    private String searchQuery;


    public SearchDataSourceFactory(Application application, String searchQuery) {
        this.application = application;
        this.searchQuery = searchQuery;
        searchDataSource = new SearchDataSource(application, searchQuery);
    }

    @NonNull
    @Override
    public DataSource<Integer, Art> create() {

        if (searchDataSource.isInvalid()) searchDataSource = new SearchDataSource(application, searchQuery);
        searchDataSourceMutableLiveData.postValue(searchDataSource);

        return searchDataSource;
    }

    public boolean refresh() {
        boolean isConnected = searchDataSource.isNetworkAvailable();
        if (isConnected){
            searchDataSource.refresh();
        }
        return isConnected;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
        searchDataSource.refresh();
    }

}

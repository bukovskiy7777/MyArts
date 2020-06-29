package com.company.art_and_culture.myarts.search;

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


    public SearchDataSourceFactory(Application application) {
        this.application = application;
        searchDataSource = new SearchDataSource(application);
    }

    @NonNull
    @Override
    public DataSource<Integer, Art> create() {

        if (searchDataSource.isInvalid()) searchDataSource = new SearchDataSource(application);
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

}

package com.company.art_and_culture.myarts.art_medium_fragment;

import android.app.Application;

import com.company.art_and_culture.myarts.pojo.Art;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.PageKeyedDataSource;

public class MediumDataSourceFactory extends DataSource.Factory<Integer, Art> {

    private MutableLiveData<PageKeyedDataSource<Integer, Art>> mediumDataSourceMutableLiveData = new MutableLiveData<>();
    private Application application;
    private MediumDataSource mediumDataSource;
    private String artQuery, queryType;

    public MediumDataSourceFactory(Application application, String artQuery, String queryType) {
        this.application = application;
        this.artQuery = artQuery;
        this.queryType = queryType;
        mediumDataSource = new MediumDataSource(application, artQuery, queryType);
    }

    @NonNull
    @Override
    public DataSource<Integer, Art> create() {

        if (mediumDataSource.isInvalid()) mediumDataSource = new MediumDataSource(application, artQuery, queryType);
        mediumDataSourceMutableLiveData.postValue(mediumDataSource);

        return mediumDataSource;
    }

    public boolean refresh() {
        boolean isConnected = mediumDataSource.isNetworkAvailable();
        if (isConnected){
            mediumDataSource.refresh();
        }
        return isConnected;
    }

    public void setArtQueryAndType(String artQuery, String queryType) {
        this.artQuery = artQuery;
        this.queryType = queryType;
        mediumDataSource.refresh();
    }
}

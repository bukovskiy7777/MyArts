package com.company.art_and_culture.myarts.ui.home;

import android.app.Application;
import android.util.Log;

import com.company.art_and_culture.myarts.pojo.Art;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.PageKeyedDataSource;

public class HomeDataSourceFactory extends DataSource.Factory<Integer, Art> {

    private MutableLiveData<PageKeyedDataSource<Integer, Art>> homeDataSourceMutableLiveData = new MutableLiveData<>();
    private Application application;
    private HomeDataSource homeDataSource;


    public HomeDataSourceFactory(Application application) {
        this.application = application;
        homeDataSource = new HomeDataSource(application);
    }

    @NonNull
    @Override
    public DataSource<Integer, Art> create() {

        if (homeDataSource.isInvalid()) homeDataSource = new HomeDataSource(application);
        homeDataSourceMutableLiveData.postValue(homeDataSource);

        return homeDataSource;
    }

    public boolean refresh() {
        boolean isConnected = homeDataSource.isNetworkAvailable();
        if (isConnected){
            homeDataSource.refresh();
        }
        return isConnected;
    }

    public HomeDataSource getHomeDataSource() {
        return homeDataSource;
    }
}

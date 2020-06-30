package com.company.art_and_culture.myarts.art_maker_fragment;

import android.app.Application;

import com.company.art_and_culture.myarts.art_search_fragment.SearchDataSource;
import com.company.art_and_culture.myarts.pojo.Art;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.PageKeyedDataSource;

public class MakerDataSourceFactory extends DataSource.Factory<Integer, Art> {

    private MutableLiveData<PageKeyedDataSource<Integer, Art>> makerDataSourceMutableLiveData = new MutableLiveData<>();
    private Application application;
    private MakerDataSource makerDataSource;


    public MakerDataSourceFactory(Application application) {
        this.application = application;
        makerDataSource = new MakerDataSource(application);
    }

    @NonNull
    @Override
    public DataSource<Integer, Art> create() {

        if (makerDataSource.isInvalid()) makerDataSource = new MakerDataSource(application);
        makerDataSourceMutableLiveData.postValue(makerDataSource);

        return makerDataSource;
    }

    public boolean refresh() {
        boolean isConnected = makerDataSource.isNetworkAvailable();
        if (isConnected){
            makerDataSource.refresh();
        }
        return isConnected;
    }

}

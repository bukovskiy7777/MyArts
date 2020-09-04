package com.company.art_and_culture.myarts.art_medium_fragment;

import android.app.Application;

import com.company.art_and_culture.myarts.art_maker_fragment.MakerDataSource;
import com.company.art_and_culture.myarts.pojo.Art;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.PageKeyedDataSource;

public class MediumDataSourceFactory extends DataSource.Factory<Integer, Art> {

    private MutableLiveData<PageKeyedDataSource<Integer, Art>> mediumDataSourceMutableLiveData = new MutableLiveData<>();
    private Application application;
    private MediumDataSource mediumDataSource;


    public MediumDataSourceFactory(Application application) {
        this.application = application;
        mediumDataSource = new MediumDataSource(application);
    }

    @NonNull
    @Override
    public DataSource<Integer, Art> create() {

        if (mediumDataSource.isInvalid()) mediumDataSource = new MediumDataSource(application);
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

}

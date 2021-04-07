package com.company.art_and_culture.myarts.maker_fragment;

import android.app.Application;

import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.pojo.FilterObject;
import com.company.art_and_culture.myarts.pojo.Maker;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.PageKeyedDataSource;

public class MakerDataSourceFactory extends DataSource.Factory<Integer, Art> {

    private MutableLiveData<PageKeyedDataSource<Integer, Art>> makerDataSourceMutableLiveData = new MutableLiveData<>();
    private Application application;
    private Maker artMaker;
    private FilterObject filterObject;
    private MakerDataSource makerDataSource;


    public MakerDataSourceFactory(Application application, Maker artMaker, FilterObject filterObject) {
        this.application = application;
        this.artMaker = artMaker;
        this.filterObject = filterObject;
        makerDataSource = new MakerDataSource(application, artMaker, filterObject);
    }

    @NonNull
    @Override
    public DataSource<Integer, Art> create() {

        if (makerDataSource.isInvalid()) makerDataSource = new MakerDataSource(application, artMaker, filterObject);
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

    public void setArtMaker(Maker artMaker) {
        this.artMaker = artMaker;
        makerDataSource.refresh();
    }

    public void makerTagClick(FilterObject filterObject) {
        this.filterObject = filterObject;
        makerDataSource.refresh();
    }
}

package com.company.art_and_culture.myarts.maker_fragment;

import android.app.Application;

import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.pojo.Maker;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.PageKeyedDataSource;

public class MakerDataSourceFactory extends DataSource.Factory<Integer, Art> {

    private MutableLiveData<PageKeyedDataSource<Integer, Art>> makerDataSourceMutableLiveData = new MutableLiveData<>();
    private Application application;
    private Maker artMaker;
    private MakerDataSource makerDataSource;


    public MakerDataSourceFactory(Application application, Maker artMaker) {
        this.application = application;
        this.artMaker = artMaker;
        makerDataSource = new MakerDataSource(application, artMaker);
    }

    @NonNull
    @Override
    public DataSource<Integer, Art> create() {

        if (makerDataSource.isInvalid()) makerDataSource = new MakerDataSource(application, artMaker);
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

}

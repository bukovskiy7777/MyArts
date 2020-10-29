package com.company.art_and_culture.myarts.filter_maker_fragment;

import android.app.Application;

import com.company.art_and_culture.myarts.pojo.Maker;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.PageKeyedDataSource;

class FilterMakerDataSourceFactory extends DataSource.Factory<Integer, Maker> {

    private Application application;
    private String filter = "", date = "";
    private FilterMakerDataSource filterMakerDataSource;
    private MutableLiveData<PageKeyedDataSource<Integer, Maker>> filterMakerDataSourceMutableLiveData = new MutableLiveData<>();

    public FilterMakerDataSourceFactory(Application application, String filter, String date) {
        this.filter = filter;
        this.date = date;
        this.application = application;
        filterMakerDataSource = new FilterMakerDataSource(application, filter, date);
    }

    @NonNull
    @Override
    public DataSource<Integer, Maker> create() {
        if (filterMakerDataSource.isInvalid()) filterMakerDataSource = new FilterMakerDataSource(application, filter, date);
        filterMakerDataSourceMutableLiveData.postValue(filterMakerDataSource);

        return filterMakerDataSource;
    }

    public void setFilter(String filter, String date) {
        this.filter = filter;
        this.date = date;
        filterMakerDataSource.refresh();
    }
}

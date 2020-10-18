package com.company.art_and_culture.myarts.filter_explore_fragment;

import android.app.Application;

import com.company.art_and_culture.myarts.pojo.ExploreObject;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.PageKeyedDataSource;

class FilterExploreDataSourceFactory extends DataSource.Factory<Integer, ExploreObject> {

    private Application application;
    private FilterExploreDataSource filterExploreDataSource;
    private MutableLiveData<PageKeyedDataSource<Integer, ExploreObject>> filterExploreDataSourceMutableLiveData = new MutableLiveData<>();

    public FilterExploreDataSourceFactory(Application application) {
        this.application = application;
        filterExploreDataSource = new FilterExploreDataSource(application);
    }

    @NonNull
    @Override
    public DataSource<Integer, ExploreObject> create() {
        if (filterExploreDataSource.isInvalid()) filterExploreDataSource = new FilterExploreDataSource(application);
        filterExploreDataSourceMutableLiveData.postValue(filterExploreDataSource);

        return filterExploreDataSource;
    }

    public void refresh() {
        filterExploreDataSource.refresh();
    }

}

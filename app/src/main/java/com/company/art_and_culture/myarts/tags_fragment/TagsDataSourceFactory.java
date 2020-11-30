package com.company.art_and_culture.myarts.tags_fragment;

import android.app.Application;

import com.company.art_and_culture.myarts.pojo.Attribute;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.PageKeyedDataSource;

class TagsDataSourceFactory extends DataSource.Factory<Integer, Attribute> {

    private Application application;
    private String filter = "";
    private TagsDataSource tagsDataSource;
    private MutableLiveData<PageKeyedDataSource<Integer, Attribute>> tagsDataSourceMutableLiveData = new MutableLiveData<>();

    public TagsDataSourceFactory(Application application, String filter) {
        this.filter = filter;
        this.application = application;
        tagsDataSource = new TagsDataSource(application, filter);
    }

    @NonNull
    @Override
    public DataSource<Integer, Attribute> create() {
        if (tagsDataSource.isInvalid()) tagsDataSource = new TagsDataSource(application, filter);
        tagsDataSourceMutableLiveData.postValue(tagsDataSource);

        return tagsDataSource;
    }

    public void setFilter(String filter) {
        this.filter = filter;
        tagsDataSource.refresh();
    }

}

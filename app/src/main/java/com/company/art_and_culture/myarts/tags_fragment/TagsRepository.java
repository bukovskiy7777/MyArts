package com.company.art_and_culture.myarts.tags_fragment;

import android.app.Application;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.pojo.Attribute;

import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

public class TagsRepository {

    private static TagsRepository instance;
    private LiveData<PagedList<Attribute>> tagsList;
    private LiveData<Boolean> isLoading;
    private TagsDataSourceFactory tagsDataSourceFactory;
    private String filter = "";
    private Application application;
    private TagsDataSource tagsDataSource;

    public static TagsRepository getInstance(Application application, String filter){

        if(instance == null){
            instance = new TagsRepository(application, filter);
        }
        return instance;
    }

    public TagsRepository(Application application, String filter) {

        this.application = application;
        this.filter = filter;

        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(Constants.PAGE_SIZE)
                .setInitialLoadSizeHint(Constants.PAGE_SIZE)
                .build();

        tagsDataSourceFactory = new TagsDataSourceFactory(application, filter);
        tagsList = new LivePagedListBuilder<>(tagsDataSourceFactory, config).build();

        tagsDataSource = (TagsDataSource) tagsDataSourceFactory.create();//if remove this line artLike will not working after refresh
        isLoading = tagsDataSource.getIsLoading();
    }


    public LiveData<PagedList<Attribute>> getTagsList(){
        return tagsList;
    }

    public TagsRepository setFilter(String filter) {

        if (!this.filter.equals(filter)) tagsDataSourceFactory.setFilter(filter);
        this.filter = filter;

        return instance;
    }

    public String getFilter() {
        return filter;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
}

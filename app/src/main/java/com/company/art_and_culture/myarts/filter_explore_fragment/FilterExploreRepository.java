package com.company.art_and_culture.myarts.filter_explore_fragment;

import android.app.Application;
import android.util.Log;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.pojo.ExploreObject;

import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

public class FilterExploreRepository {

    private static FilterExploreRepository instance;
    private LiveData<PagedList<ExploreObject>> exploreList;
    private FilterExploreDataSourceFactory filterExploreDataSourceFactory;
    private String filter = "";

    public static FilterExploreRepository getInstance(Application application){
        if(instance == null){
            instance = new FilterExploreRepository(application);
        }
        return instance;
    }

    public FilterExploreRepository(Application application) {

        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(Constants.PAGE_SIZE_SMALL)
                .setInitialLoadSizeHint(Constants.PAGE_SIZE_SMALL)
                .build();

        filterExploreDataSourceFactory = new FilterExploreDataSourceFactory(application);
        exploreList = new LivePagedListBuilder<>(filterExploreDataSourceFactory, config).build();

    }

    public LiveData<PagedList<ExploreObject>> getExploreList(){
        return exploreList;
    }

    public void setFilter(String filter) {

        if (!this.filter.equals(filter)) {
            filterExploreDataSourceFactory.refresh();
        }
        this.filter = filter;
    }

    public String getFilter() {
        return filter;
    }

    public FilterExploreRepository finish(Application application) {
        FilterExploreDataInMemory.getInstance().refresh();
        instance = new FilterExploreRepository(application);
        return instance;
    }
}

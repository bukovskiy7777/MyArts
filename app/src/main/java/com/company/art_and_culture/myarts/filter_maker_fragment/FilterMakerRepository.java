package com.company.art_and_culture.myarts.filter_maker_fragment;

import android.app.Application;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.pojo.Maker;

import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

public class FilterMakerRepository {

    private static FilterMakerRepository instance;
    private LiveData<PagedList<Maker>> makerList;
    private FilterMakerDataSourceFactory filterMakerDataSourceFactory;
    private String filter = "", date = "";
    private Application application;
    private FilterMakerDataSource filterMakerDataSource;
    private LiveData<Boolean> isInitialLoaded;

    public static FilterMakerRepository getInstance(Application application, String filter, String date){

        if(instance == null){
            instance = new FilterMakerRepository(application, filter, date);
        }
        return instance;
    }

    public FilterMakerRepository(Application application, String filter, String date) {

        this.application = application;
        this.filter = filter;
        this.date = date;

        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(Constants.PAGE_SIZE_SMALL)
                .setInitialLoadSizeHint(Constants.PAGE_SIZE_SMALL)
                .build();

        filterMakerDataSourceFactory = new FilterMakerDataSourceFactory(application, filter, date);
        makerList = new LivePagedListBuilder<>(filterMakerDataSourceFactory, config).build();

        filterMakerDataSource = (FilterMakerDataSource) filterMakerDataSourceFactory.create();//if remove this line artLike will not working after refresh

    }

    public LiveData<Boolean> getIsInitialLoaded() {
        return filterMakerDataSource.getIsInitialLoaded();
    }

    public LiveData<PagedList<Maker>> getExploreList(){
        return makerList;
    }

    public FilterMakerRepository setFilter(String filter, String date) {
        if (!this.filter.equals(filter) || !this.date.equals(date)) {
            filterMakerDataSourceFactory.setFilter(filter, date);
        }
        this.filter = filter;
        this.date = date;

        return instance;
    }

}

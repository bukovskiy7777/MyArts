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
    private LiveData<Boolean> isLoading;

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
                .setPageSize(Constants.PAGE_SIZE)
                .setInitialLoadSizeHint(Constants.PAGE_SIZE)
                .build();

        filterMakerDataSourceFactory = new FilterMakerDataSourceFactory(application, filter, date);
        makerList = new LivePagedListBuilder<>(filterMakerDataSourceFactory, config).build();

        filterMakerDataSource = filterMakerDataSourceFactory.getFilterMakerDataSource();
        isLoading = filterMakerDataSource.getIsLoading();
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<PagedList<Maker>> getMakerList(){
        return makerList;
    }

    public void setFilter(String filter, String date) {
        if (!this.filter.equals(filter) || !this.date.equals(date)) {
            this.filter = filter;
            this.date = date;
            filterMakerDataSourceFactory.setFilter(filter, date);
        }
    }

}

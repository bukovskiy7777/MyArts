package com.company.art_and_culture.myarts.filter_maker_fragment;

import android.app.Application;


import com.company.art_and_culture.myarts.pojo.Maker;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

public class FilterMakerViewModel extends AndroidViewModel {

    private android.content.res.Resources res;
    private Application application;
    private FilterMakerRepository filterMakerRepository;
    private LiveData<PagedList<Maker>> makerList;
    private LiveData<Boolean> isInitialLoaded;

    public FilterMakerViewModel(@NonNull Application application) {
        super(application);

        this.application = application;
        //isInitialLoaded = filterExploreRepository.getIsInitialLoaded();
    }

    public LiveData<Boolean> getIsInitialLoaded() {
        return filterMakerRepository.getIsInitialLoaded();
    }

    public LiveData<PagedList<Maker>> getMakerList(){
        return makerList;
    }

    public void setFilter(String filter, String date) {
        filterMakerRepository = FilterMakerRepository.getInstance(application, filter, date).setFilter(filter, date);
        makerList = filterMakerRepository.getExploreList();
    }

}

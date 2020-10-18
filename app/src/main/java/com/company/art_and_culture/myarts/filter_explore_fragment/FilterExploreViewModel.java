package com.company.art_and_culture.myarts.filter_explore_fragment;

import android.app.Application;

import com.company.art_and_culture.myarts.pojo.ExploreObject;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

public class FilterExploreViewModel extends AndroidViewModel {

    private android.content.res.Resources res;
    private Application application;
    private FilterExploreRepository filterExploreRepository;
    private LiveData<PagedList<ExploreObject>> exploreList;

    public FilterExploreViewModel(@NonNull Application application) {
        super(application);

        this.application = application;
        filterExploreRepository = FilterExploreRepository.getInstance(application);
        exploreList = filterExploreRepository.getExploreList();
    }

    public LiveData<PagedList<ExploreObject>> getExploreList(){
        return exploreList;
    }

    public void setFilter(String filter) {
        filterExploreRepository.setFilter(filter);
    }

    public void finish() {
        filterExploreRepository = filterExploreRepository.finish (application);
    }
}

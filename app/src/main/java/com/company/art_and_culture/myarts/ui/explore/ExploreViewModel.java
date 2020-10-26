package com.company.art_and_culture.myarts.ui.explore;

import android.app.Application;

import com.company.art_and_culture.myarts.pojo.ExploreObject;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class ExploreViewModel extends AndroidViewModel {

    private LiveData<ArrayList<ExploreObject>> exploreList;
    private LiveData<Boolean> isLoading;
    private LiveData<Boolean> isListEmpty;
    private ExploreRepository exploreRepository;

    public ExploreViewModel(@NonNull Application application) {
        super(application);

        exploreRepository = ExploreRepository.getInstance(application);
        exploreList = exploreRepository.getExploreList();
        isLoading = exploreRepository.getIsLoading();
        isListEmpty = exploreRepository.getIsListEmpty();
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getIsListEmpty() {
        return isListEmpty;
    }

    public boolean refresh() {
        return exploreRepository.refresh();
    }

    public LiveData<ArrayList<ExploreObject>> getExploreList() {
        return exploreList;
    }

}
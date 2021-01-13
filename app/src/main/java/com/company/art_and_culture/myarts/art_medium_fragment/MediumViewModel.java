package com.company.art_and_culture.myarts.art_medium_fragment;

import android.app.Application;

import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.pojo.Art;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;


public class MediumViewModel extends AndroidViewModel {

    private LiveData<PagedList<Art>> artList;
    private LiveData<ArrayList<String>> listMakerFilters;
    private LiveData<Boolean> isLoading;
    private LiveData<Boolean> isListEmpty;
    private MediumRepository mediumRepository;
    private android.content.res.Resources res;
    private Application application;

    public MediumViewModel(@NonNull Application application) {
        super(application);

        this.application = application;

    }

    public LiveData<PagedList<Art>> getArtList(){
        return artList;
    }

    public LiveData<ArrayList<String>> getListMakerFilters() {
        return listMakerFilters;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getIsListEmpty() {
        return isListEmpty;
    }

    public void writeDimentionsOnServer(Art art) {
        mediumRepository.writeDimentionsOnServer(art);
    }

    public boolean refresh() {
        return mediumRepository.refresh();
    }

    public void setFilters(String keyword, String makerFilter, String centuryFilter, String keywordType) {

        if(mediumRepository == null) {
            mediumRepository = new MediumRepository(application, keyword, makerFilter, centuryFilter, keywordType);
            artList = mediumRepository.getArtList();
            listMakerFilters = mediumRepository.getListMakerFilters();
            isLoading = mediumRepository.getIsLoading();
            isListEmpty = mediumRepository.getIsListEmpty();
        } else
            mediumRepository.setFilters(keyword, makerFilter, centuryFilter, keywordType);


    }

    public void setActivity(MainActivity activity) {
        mediumRepository.setActivity(activity);
    }
}
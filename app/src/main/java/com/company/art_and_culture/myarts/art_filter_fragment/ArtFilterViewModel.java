package com.company.art_and_culture.myarts.art_filter_fragment;

import android.app.Application;

import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.pojo.FilterObject;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;


public class ArtFilterViewModel extends AndroidViewModel {

    private LiveData<PagedList<Art>> artList;
    private LiveData<ArrayList<String>> listMakerFilters;
    private LiveData<ArrayList<String>> listCenturyFilters;
    private LiveData<ArrayList<FilterObject>> listKeywordFilters;
    private LiveData<Integer> artCount;
    private LiveData<Boolean> isLoading;
    private LiveData<Boolean> isListEmpty;
    private ArtFilterRepository artFilterRepository;
    private android.content.res.Resources res;
    private Application application;

    public ArtFilterViewModel(@NonNull Application application) {
        super(application);

        this.application = application;

    }

    public LiveData<PagedList<Art>> getArtList(){
        return artList;
    }

    public LiveData<ArrayList<String>> getListMakerFilters() {
        return listMakerFilters;
    }

    public LiveData<ArrayList<String>> getListCenturyFilters() {
        return listCenturyFilters;
    }

    public LiveData<ArrayList<FilterObject>> getListKeywordFilters() {
        return listKeywordFilters;
    }

    public LiveData<Integer> getArtCount() {
        return artCount;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getIsListEmpty() {
        return isListEmpty;
    }

    public void writeDimentionsOnServer(Art art) {
        artFilterRepository.writeDimentionsOnServer(art);
    }

    public boolean refresh() {
        return artFilterRepository.refresh();
    }

    public void setFilters(String keyword, String makerFilter, String centuryFilter, String keywordType) {

        if(artFilterRepository == null) {
            artFilterRepository = new ArtFilterRepository(application, keyword, makerFilter, centuryFilter, keywordType);
            artList = artFilterRepository.getArtList();
            listMakerFilters = artFilterRepository.getListMakerFilters();
            listCenturyFilters = artFilterRepository.getListCenturyFilters();
            listKeywordFilters = artFilterRepository.getListKeywordFilters();
            artCount = artFilterRepository.getArtCount();
            isLoading = artFilterRepository.getIsLoading();
            isListEmpty = artFilterRepository.getIsListEmpty();
        } else
            artFilterRepository.setFilters(keyword, makerFilter, centuryFilter, keywordType);


    }

    public void setActivity(MainActivity activity) {
        artFilterRepository.setActivity(activity);
    }
}
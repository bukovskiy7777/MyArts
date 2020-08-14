package com.company.art_and_culture.myarts.art_search_fragment;

import android.app.Application;

import com.company.art_and_culture.myarts.pojo.Art;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;


public class SearchViewModel extends AndroidViewModel {

    private LiveData<PagedList<Art>> artList;
    private LiveData<Boolean> isLoading;
    private LiveData<Boolean> isListEmpty;
    private LiveData<Art> art;
    private SearchRepository searchRepository;
    private android.content.res.Resources res;
    private Application application;

    public SearchViewModel(@NonNull Application application) {
        super(application);

        this.application = application;
        searchRepository = SearchRepository.getInstance(application);
        artList = searchRepository.getArtList();
        isLoading = searchRepository.getIsLoading();
        isListEmpty = searchRepository.getIsListEmpty();
        art = searchRepository.getArt();
    }

    public LiveData<PagedList<Art>> getArtList(){
        return artList;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getIsListEmpty() {
        return isListEmpty;
    }

    public LiveData<Art> getArt() {
        return art;
    }

    public boolean likeArt(Art art, int position, String userUniqueId) {
        return searchRepository.likeArt (art, position, userUniqueId);
    }

    public boolean refresh() {
        return searchRepository.refresh();
    }

    public void writeDimentionsOnServer(Art art) {
        searchRepository.writeDimentionsOnServer(art);
    }

    public void finish() {
        searchRepository = searchRepository.finish (application);
    }

    public void setSearchQuery(String searchQuery) {
        searchRepository.setSearchQuery(searchQuery);
    }
}
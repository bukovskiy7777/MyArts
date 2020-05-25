package com.company.art_and_culture.myarts.ui.home;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

import com.company.art_and_culture.myarts.pojo.Art;


public class HomeViewModel extends AndroidViewModel {

    private LiveData<PagedList<Art>> artList;
    private LiveData<Boolean> isLoading;
    private LiveData<Boolean> isListEmpty;
    private LiveData<Art> art;
    private HomeRepository homeRepository;
    private android.content.res.Resources res;

    public HomeViewModel(@NonNull Application application) {
        super(application);

        homeRepository = HomeRepository.getInstance(application);
        artList = homeRepository.getArtList();
        isLoading = homeRepository.getIsLoading();
        isListEmpty = homeRepository.getIsListEmpty();
        Log.i("updateIsListEmptyState", isListEmpty.getValue()+" HomeViewModel constructor");
        art = homeRepository.getArt();
    }

    public LiveData<PagedList<Art>> getArtList(){
        return artList;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getIsListEmpty() {
        Log.i("updateIsListEmptyState", isListEmpty.getValue()+" HomeViewModel getIsListEmpty");
        return isListEmpty;
    }

    public LiveData<Art> getArt() {
        return art;
    }

    public void likeArt(Art art, int position, String userUniqueId) {
        homeRepository.likeArt (art, position, userUniqueId);
    }

    public boolean refresh() {

        return homeRepository.refresh();
    }
}
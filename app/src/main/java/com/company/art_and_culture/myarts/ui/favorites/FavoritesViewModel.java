package com.company.art_and_culture.myarts.ui.favorites;

import android.app.Application;
import android.util.Log;

import com.company.art_and_culture.myarts.pojo.Art;

import java.util.ArrayList;
import java.util.Collection;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.PagedList;

public class FavoritesViewModel extends AndroidViewModel {

    private LiveData<ArrayList<Art>> artList;
    private LiveData<Boolean> isLoading;
    private LiveData<Boolean> isListEmpty;
    private  FavoritesRepository favoritesRepository;

    public FavoritesViewModel(@NonNull Application application) {
        super(application);

        favoritesRepository = FavoritesRepository.getInstance(application);
        artList = favoritesRepository.getArtList();
        isLoading = favoritesRepository.getIsLoading();
        isListEmpty = favoritesRepository.getIsListEmpty();

    }

    public LiveData<ArrayList<Art>> getArtList() {
        return artList;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getIsListEmpty() {
        return isListEmpty;
    }

    public boolean refresh() {
        return favoritesRepository.refresh();
    }
}
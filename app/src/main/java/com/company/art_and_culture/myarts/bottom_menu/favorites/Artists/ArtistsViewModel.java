package com.company.art_and_culture.myarts.bottom_menu.favorites.Artists;

import android.app.Application;

import com.company.art_and_culture.myarts.pojo.Maker;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class ArtistsViewModel extends AndroidViewModel {

    private LiveData<ArrayList<Maker>> makerList;
    private LiveData<Boolean> isLoading;
    private LiveData<Boolean> isListEmpty;
    private ArtistsRepository artistsRepository;


    public ArtistsViewModel(@NonNull Application application) {
        super(application);

        artistsRepository = ArtistsRepository.getInstance(application);
        makerList = artistsRepository.getMakerList();
        isLoading = artistsRepository.getIsLoading();
        isListEmpty = artistsRepository.getIsListEmpty();
    }

    public boolean refresh() {
        return artistsRepository.refresh();
    }

    public LiveData<ArrayList<Maker>> getMakerList() {
        return makerList;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getIsListEmpty() {
        return isListEmpty;
    }

}

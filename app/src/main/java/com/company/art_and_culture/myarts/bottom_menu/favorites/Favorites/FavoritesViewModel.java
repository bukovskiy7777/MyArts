package com.company.art_and_culture.myarts.bottom_menu.favorites.Favorites;

import android.app.Application;

import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.pojo.Art;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class FavoritesViewModel extends AndroidViewModel {

    private LiveData<ArrayList<Art>> artList;
    private LiveData<Boolean> isLoading;
    private LiveData<Boolean> isListEmpty;
    private FavoritesRepository favoritesRepository;
    private int scrollPosition = 0;
    private FavoritesFragment.Sort sort_type = FavoritesFragment.Sort.by_date;


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

    public void setActivity(MainActivity activity) {
        favoritesRepository.setActivity(activity);
    }

    public int getScrollPosition() {
        return scrollPosition;
    }

    public void setScrollPosition(int scrollPosition) {
        this.scrollPosition = scrollPosition;
    }

    public FavoritesFragment.Sort getSort_type() {
        return sort_type;
    }

    public void setSort_type(FavoritesFragment.Sort sort_type) {
        this.sort_type = sort_type;
    }
}
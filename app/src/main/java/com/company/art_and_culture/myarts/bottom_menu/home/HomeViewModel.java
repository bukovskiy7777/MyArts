package com.company.art_and_culture.myarts.bottom_menu.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.pojo.Art;


public class HomeViewModel extends AndroidViewModel {

    private LiveData<PagedList<Art>> artList;
    private LiveData<Boolean> isLoading;
    private LiveData<Boolean> isListEmpty;
    private HomeRepository homeRepository;
    private int scrollPosition = 0;

    public HomeViewModel(@NonNull Application application) {
        super(application);

        homeRepository = HomeRepository.getInstance(application);
        artList = homeRepository.getArtList();
        isLoading = homeRepository.getIsLoading();
        isListEmpty = homeRepository.getIsListEmpty();
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

    public boolean likeArt(Art art, int position, String userUniqueId) {
        return homeRepository.likeArt (art, position, userUniqueId);
    }

    public boolean refresh() {
        return homeRepository.refresh();
    }

    public void writeDimentionsOnServer(Art art) {
        homeRepository.writeDimentionsOnServer(art);
    }

    public void setActivity(MainActivity activity) {
        homeRepository.setActivity(activity);
    }

    public int getScrollPosition() {
        return scrollPosition;
    }

    public void setScrollPosition(int scrollPosition) {
        this.scrollPosition = scrollPosition;
    }
}
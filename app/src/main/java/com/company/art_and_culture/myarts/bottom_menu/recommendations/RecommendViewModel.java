package com.company.art_and_culture.myarts.bottom_menu.recommendations;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.pojo.Art;

import java.util.ArrayList;

public class RecommendViewModel extends AndroidViewModel {

    private LiveData<ArrayList<Art>> artList;
    private LiveData<Boolean> isLoading;
    private LiveData<Boolean> isListEmpty;
    private RecommendRepository recommendRepository;

    public RecommendViewModel(@NonNull Application application) {
        super(application);
        recommendRepository = RecommendRepository.getInstance(application);
        artList = recommendRepository.getArtList();
        isLoading = recommendRepository.getIsLoading();
        isListEmpty = recommendRepository.getIsListEmpty();
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
        return recommendRepository.refresh();
    }

    public void writeDimentionsOnServer(Art art) {
        recommendRepository.writeDimentionsOnServer(art);
    }

    public boolean likeArt(Art art, int position, String userUniqueId) {
        return recommendRepository.likeArt (art, position, userUniqueId);
    }

    public void setActivity(MainActivity activity) {
        recommendRepository.setActivity(activity);
    }
}
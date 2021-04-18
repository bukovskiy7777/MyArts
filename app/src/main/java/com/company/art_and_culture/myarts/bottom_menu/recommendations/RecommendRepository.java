package com.company.art_and_culture.myarts.bottom_menu.recommendations;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.pojo.Art;

import java.util.ArrayList;

public class RecommendRepository {

    private static RecommendRepository instance;
    private RecommendDataSource recommendDataSource;

    private RecommendRepository(Application application) {
        recommendDataSource = new RecommendDataSource(application);
    }

    public static RecommendRepository getInstance(Application application) {
        if(instance == null){
            instance = new RecommendRepository(application);
        }
        return instance;
    }

    public LiveData<ArrayList<Art>> getArtList(){
        return recommendDataSource.getArtList();
    }

    public LiveData<Boolean> getIsLoading() {
        return recommendDataSource.getIsLoading();
    }

    public LiveData<Boolean> getIsListEmpty() {
        return recommendDataSource.getIsListEmpty();
    }

    public boolean refresh() {
        boolean isConnected = recommendDataSource.isNetworkAvailable();
        if (isConnected){
            recommendDataSource.refresh();
        }
        return isConnected;
    }

    public void writeDimentionsOnServer(Art art) {
        recommendDataSource.writeDimentionsOnServer(art);
    }

    public boolean likeArt(Art art, int position, String userUniqueId) {
        boolean isConnected = recommendDataSource.isNetworkAvailable();
        if (isConnected){
            recommendDataSource.likeArt(art, position, userUniqueId);
        }
        return isConnected;
    }

    public void setActivity(MainActivity activity) {
        recommendDataSource.setActivity(activity);
    }
}

package com.company.art_and_culture.myarts.arts_show_fragment;

import android.app.Application;

import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.pojo.Art;

import androidx.lifecycle.LiveData;

public class ArtShowRepository {

    private static ArtShowRepository instance;
    private ArtShowDataSource artShowDataSource;

    public static ArtShowRepository getInstance(Application application){

        if(instance == null){
            instance = new ArtShowRepository(application);
        }
        return instance;
    }

    public ArtShowRepository(Application application) {

        artShowDataSource = ArtShowDataSource.getInstance(application);
    }

    public boolean likeArt(Art art, int position, String userUniqueId) {

        boolean isConnected = artShowDataSource.isNetworkAvailable();
        if (isConnected){
            artShowDataSource.likeArt(art, position, userUniqueId);
        }
        return isConnected;
    }


    public ArtShowRepository finish(Application application) {
        ArtShowDataSource.getInstance(application).finish();
        instance = new ArtShowRepository(application);
        return instance;
    }

    public void setActivity(MainActivity activity) {
        artShowDataSource.setActivity(activity);
    }
}

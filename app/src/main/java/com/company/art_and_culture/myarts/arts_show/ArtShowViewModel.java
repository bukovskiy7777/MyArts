package com.company.art_and_culture.myarts.arts_show;

import android.app.Application;

import com.company.art_and_culture.myarts.pojo.Art;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class ArtShowViewModel extends AndroidViewModel {

    private LiveData<Art> art;
    private ArtShowRepository artShowRepository;
    private Application application;

    public ArtShowViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
        artShowRepository = ArtShowRepository.getInstance(application);
        art = artShowRepository.getArt();
    }

    public LiveData<Art> getArt() {
        return art;
    }

    public boolean likeArt(Art art, int position, String userUniqueId) {
        return artShowRepository.likeArt (art, position, userUniqueId);
    }

    public void finish() {
        artShowRepository = artShowRepository.finish (application);
    }
}

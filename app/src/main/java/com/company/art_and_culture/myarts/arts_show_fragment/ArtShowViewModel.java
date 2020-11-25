package com.company.art_and_culture.myarts.arts_show_fragment;

import android.app.Application;

import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.pojo.Art;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class ArtShowViewModel extends AndroidViewModel {

    private ArtShowRepository artShowRepository;
    private Application application;

    public ArtShowViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
        artShowRepository = ArtShowRepository.getInstance(application);
    }

    public boolean likeArt(Art art, int position, String userUniqueId) {
        return artShowRepository.likeArt (art, position, userUniqueId);
    }

    public void finish() {
        artShowRepository = artShowRepository.finish (application);
    }

    public void setActivity(MainActivity activity) {
        artShowRepository.setActivity(activity);
    }
}

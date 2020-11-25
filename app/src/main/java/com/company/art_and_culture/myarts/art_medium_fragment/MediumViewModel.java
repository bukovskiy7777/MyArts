package com.company.art_and_culture.myarts.art_medium_fragment;

import android.app.Application;

import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.pojo.Art;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;


public class MediumViewModel extends AndroidViewModel {

    private LiveData<PagedList<Art>> artList;
    private LiveData<Boolean> isLoading;
    private LiveData<Boolean> isListEmpty;
    private MediumRepository mediumRepository;
    private android.content.res.Resources res;
    private Application application;

    public MediumViewModel(@NonNull Application application) {
        super(application);

        this.application = application;
        mediumRepository = MediumRepository.getInstance(application);
        artList = mediumRepository.getArtList();
        isLoading = mediumRepository.getIsLoading();
        isListEmpty = mediumRepository.getIsListEmpty();
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

    public boolean refresh() {
        return mediumRepository.refresh();
    }

    public void writeDimentionsOnServer(Art art) {
        mediumRepository.writeDimentionsOnServer(art);
    }

    public void finish() {
        mediumRepository = mediumRepository.finish (application);
    }

    public void setArtQueryAndType(String artQuery, String queryType) {
        mediumRepository.setArtQueryAndType(artQuery, queryType);
    }

    public void setActivity(MainActivity activity) {
        mediumRepository.setActivity(activity);
    }
}
package com.company.art_and_culture.myarts.art_maker_fragment;

import android.app.Application;

import com.company.art_and_culture.myarts.art_search_fragment.SearchRepository;
import com.company.art_and_culture.myarts.pojo.Art;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;


public class MakerViewModel extends AndroidViewModel {

    private LiveData<PagedList<Art>> artList;
    private LiveData<Boolean> isLoading;
    private LiveData<Boolean> isListEmpty;
    private MakerRepository makerRepository;
    private android.content.res.Resources res;
    private Application application;

    public MakerViewModel(@NonNull Application application) {
        super(application);

        this.application = application;
        makerRepository = MakerRepository.getInstance(application);
        artList = makerRepository.getArtList();
        isLoading = makerRepository.getIsLoading();
        isListEmpty = makerRepository.getIsListEmpty();
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
        return makerRepository.refresh();
    }

    public void writeDimentionsOnServer(Art art) {
        makerRepository.writeDimentionsOnServer(art);
    }

    public void finish() {
        makerRepository = makerRepository.finish (application);
    }

}
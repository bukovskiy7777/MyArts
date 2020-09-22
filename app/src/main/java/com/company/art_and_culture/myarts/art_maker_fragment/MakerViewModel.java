package com.company.art_and_culture.myarts.art_maker_fragment;

import android.app.Application;

import com.company.art_and_culture.myarts.art_search_fragment.SearchRepository;
import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.pojo.Maker;

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
    private LiveData<Maker> maker;
    private LiveData<Art> art;

    public MakerViewModel(@NonNull Application application) {
        super(application);

        this.application = application;
        makerRepository = MakerRepository.getInstance(application);
        artList = makerRepository.getArtList();
        isLoading = makerRepository.getIsLoading();
        isListEmpty = makerRepository.getIsListEmpty();
        maker = makerRepository.getMaker();
        art = makerRepository.getArt();
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

    public LiveData<Maker> getMaker() {
        return maker;
    }

    public LiveData<Art> getArt() {
        return art;
    }

    public boolean likeArt(Art art, int position, String userUniqueId) {
        return makerRepository.likeArt (art, position, userUniqueId);
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

    public void setArtMaker(Maker artMaker) {
        makerRepository.setArtMaker(artMaker);
    }

    public boolean likeMaker(Maker maker, String userUniqueId) {
        return makerRepository.likeMaker(maker, userUniqueId);
    }
}
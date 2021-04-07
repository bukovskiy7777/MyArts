package com.company.art_and_culture.myarts.maker_fragment;

import android.app.Application;

import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.pojo.FilterObject;
import com.company.art_and_culture.myarts.pojo.Maker;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;


public class MakerViewModel extends AndroidViewModel {

    private LiveData<PagedList<Art>> artList;
    private LiveData<Boolean> isLoading;
    private LiveData<Boolean> isListEmpty;
    private LiveData<Boolean> isMakerLiked;
    private MakerRepository makerRepository;
    private android.content.res.Resources res;
    private Application application;
    private LiveData<Maker> maker;
    private LiveData<ArrayList<FilterObject>> keywords;
    private LiveData<Integer> artCountMaker;

    public MakerViewModel(@NonNull Application application) {
        super(application);

        this.application = application;

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

    public LiveData<Boolean> getIsMakerLiked() {
        return isMakerLiked;
    }

    public LiveData<Maker> getMaker() {
        return maker;
    }

    public LiveData<ArrayList<FilterObject>> getMakerKeywords() {
        return keywords;
    }

    public LiveData<Integer> getArtCountMaker() {
        return artCountMaker;
    }

    public boolean likeArt(Art art, int position, String userUniqueId) {
        return makerRepository.likeArt (art, position, userUniqueId);
    }

    public boolean makerTagClick(FilterObject filterObject) {
        return makerRepository.makerTagClick (filterObject);
    }


    public FilterObject getFilterObject() {
        return makerRepository.getFilterObject();
    }


    public boolean refresh() {
        return makerRepository.refresh();
    }

    public void writeDimentionsOnServer(Art art) {
        makerRepository.writeDimentionsOnServer(art);
    }

    public void setArtMaker(Maker artMaker) {
        makerRepository = MakerRepository.getInstance(application, artMaker);
        if(!makerRepository.getArtMaker().getArtMaker().equals(artMaker.getArtMaker())) {
            makerRepository = makerRepository.setArtMaker(artMaker);
        }
        artList = makerRepository.getArtList();
        isLoading = makerRepository.getIsLoading();
        isListEmpty = makerRepository.getIsListEmpty();
        isMakerLiked = makerRepository.getIsMakerLiked();
        maker = makerRepository.getMaker();
        keywords = makerRepository.getMakerKeywords();
        artCountMaker = makerRepository.getArtCountMaker();
    }

    public boolean likeMaker(Maker maker, String userUniqueId) {
        return makerRepository.likeMaker(maker, userUniqueId);
    }

    public void setActivity(MainActivity activity) {
        makerRepository.setActivity(activity);
    }
}
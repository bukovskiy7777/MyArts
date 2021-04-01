package com.company.art_and_culture.myarts.museum_fragment;

import android.app.Application;

import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.pojo.ArtProvider;
import com.company.art_and_culture.myarts.pojo.Maker;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;


public class MuseumViewModel extends AndroidViewModel {

    private LiveData<PagedList<Art>> artList;
    private LiveData<Boolean> isLoading;
    private LiveData<Boolean> isListEmpty;
    private LiveData<ArtProvider> artProvider;
    private LiveData<Boolean> isLiked;
    private LiveData<ArrayList<Maker>> listMakers;
    private MuseumRepository museumRepository;
    private android.content.res.Resources res;
    private Application application;

    public MuseumViewModel(@NonNull Application application) {
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

    public LiveData<ArtProvider> getArtProvider() {
        return artProvider;
    }

    public LiveData<ArrayList<Maker>> getListMakers() {
        return listMakers;
    }

    public LiveData<Boolean> getArtProviderLike() {
        return isLiked;
    }

    public void writeDimentionsOnServer(Art art) {
        museumRepository.writeDimentionsOnServer(art);
    }

    public boolean refresh() {
        return museumRepository.refresh();
    }

    public void setArtProviderId(String artProviderId) {

        if(museumRepository == null) {
            museumRepository = new MuseumRepository(application, artProviderId);
            artList = museumRepository.getArtList();
            isLoading = museumRepository.getIsLoading();
            isListEmpty = museumRepository.getIsListEmpty();
            artProvider = museumRepository.getArtProvider();
            listMakers = museumRepository.getListMakers();
            isLiked = museumRepository.getArtProviderLike();
        } else
            museumRepository.setArtProviderId(artProviderId);
    }

    public void setActivity(MainActivity activity) {
        museumRepository.setActivity(activity);
    }

    public boolean likeMuseum(ArtProvider museum, String userUniqueId) {
        return museumRepository.likeMuseum(museum, userUniqueId);
    }
}
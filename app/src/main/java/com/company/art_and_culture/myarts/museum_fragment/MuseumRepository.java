package com.company.art_and_culture.myarts.museum_fragment;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.pojo.ArtProvider;
import com.company.art_and_culture.myarts.pojo.Maker;

import java.util.ArrayList;

public class MuseumRepository {

    private LiveData<PagedList<Art>> artList;
    private ArtDataSource artDataSource;
    private MuseumInfoDataSource museumInfoDataSource;
    private ArtDataSourceFactory artDataSourceFactory;
    private Application application;

    private String artProviderId;

    public MuseumRepository(Application application, String artProviderId) {

        this.application = application;
        this.artProviderId = artProviderId;

        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(Constants.PAGE_SIZE)
                .setInitialLoadSizeHint(Constants.PAGE_SIZE)
                .build();
        artDataSourceFactory = new ArtDataSourceFactory(application, artProviderId);
        artList = new LivePagedListBuilder<>(artDataSourceFactory, config).build();

        artDataSource = (ArtDataSource) artDataSourceFactory.create();//if remove this line artLike will not working after refresh

        museumInfoDataSource = new MuseumInfoDataSource(application, artProviderId);
    }

    public LiveData<PagedList<Art>> getArtList(){
        return artList;
    }

    public LiveData<Boolean> getIsLoading() {
        return museumInfoDataSource.getIsLoading();
    }

    public LiveData<Boolean> getIsListEmpty() {
        return artDataSource.getIsListEmpty();
    }

    public LiveData<ArtProvider> getArtProvider() {
        return museumInfoDataSource.getArtProvider();
    }

    public LiveData<Boolean> getArtProviderLike() {
        return museumInfoDataSource.getArtProviderLike();
    }

    public LiveData<ArrayList<Maker>> getListMakers() {
        return museumInfoDataSource.getListMakers();
    }

    public void writeDimentionsOnServer(Art art) {
        artDataSource.writeDimentionsOnServer(art);
    }

    public boolean refresh() {
        museumInfoDataSource.refresh();
        return artDataSourceFactory.refresh();
    }

    public void setArtProviderId(String artProviderId) {

        if (!this.artProviderId.equals(artProviderId)) {
            this.artProviderId = artProviderId;
            artDataSourceFactory.setArtProviderId(artProviderId);
            museumInfoDataSource.setArtProviderId(artProviderId);
        }
    }

    public boolean likeMuseum(ArtProvider museum, String userUniqueId) {
        boolean isConnected = artDataSource.isNetworkAvailable();
        if (isConnected){
            museumInfoDataSource.likeMuseum(museum, userUniqueId);
        }
        return isConnected;
    }
}

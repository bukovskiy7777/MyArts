package com.company.art_and_culture.myarts.art_maker_fragment;

import android.app.Application;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.pojo.Maker;

import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

public class MakerRepository {

    private static MakerRepository instance;
    private LiveData<PagedList<Art>> artList;
    private MakerDataSource makerDataSource;
    private MakerDataSourceFactory makerDataSourceFactory;
    private Maker artMaker;
    private Application application;


    public static MakerRepository getInstance(Application application, Maker artMaker){
        if(instance == null){
            instance = new MakerRepository(application, artMaker);
        }
        return instance;
    }

    public MakerRepository(Application application, Maker artMaker) {

        this.application = application;
        this.artMaker = artMaker;

        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(Constants.PAGE_SIZE)
                .setInitialLoadSizeHint(Constants.PAGE_SIZE)
                .build();
        makerDataSourceFactory = new MakerDataSourceFactory(application, artMaker);
        artList = new LivePagedListBuilder<>(makerDataSourceFactory, config).build();

        makerDataSource = (MakerDataSource) makerDataSourceFactory.create();//if remove this line artLike will not working after refresh
    }

    public LiveData<PagedList<Art>> getArtList(){
        return artList;
    }

    public LiveData<Boolean> getIsLoading() {
        return makerDataSource.getIsLoading();
    }

    public LiveData<Boolean> getIsListEmpty() {
        return makerDataSource.getIsListEmpty();
    }

    public LiveData<Maker> getMaker() {
        return makerDataSource.getMaker();
    }

    public LiveData<Maker> getMakerFirstTime() {
        return makerDataSource.getMakerFirstTime();
    }

    public boolean likeArt(Art art, int position, String userUniqueId) {

        boolean isConnected = makerDataSource.isNetworkAvailable();
        if (isConnected){
            makerDataSource.likeArt(art, position, userUniqueId);
        }
        return isConnected;
    }

    public boolean refresh() {
        return makerDataSourceFactory.refresh();
    }

    public void writeDimentionsOnServer(Art art) {
        makerDataSource.writeDimentionsOnServer(art);
    }

    public MakerRepository setArtMaker(Maker artMaker) {
        makerDataSourceFactory.setArtMaker(artMaker);
        instance = new MakerRepository(application, artMaker);

        return instance;
    }

    public Maker getArtMaker() {
        return artMaker;
    }

    public boolean likeMaker(Maker maker, String userUniqueId) {

        boolean isConnected = makerDataSource.isNetworkAvailable();
        if (isConnected){
            makerDataSource.likeMaker(maker, userUniqueId);
        }
        return isConnected;
    }

    public void setActivity(MainActivity activity) {
        makerDataSource.setActivity(activity);
    }
}

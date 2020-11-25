package com.company.art_and_culture.myarts.art_medium_fragment;

import android.app.Application;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.art_maker_fragment.MakerDataSource;
import com.company.art_and_culture.myarts.art_maker_fragment.MakerDataSourceFactory;
import com.company.art_and_culture.myarts.pojo.Art;

import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

public class MediumRepository {

    private static MediumRepository instance;
    private LiveData<PagedList<Art>> artList;
    private MediumDataSource mediumDataSource;
    private MediumDataSourceFactory mediumDataSourceFactory;

    private String artQuery, queryType;


    public static MediumRepository getInstance(Application application){
        if(instance == null){
            instance = new MediumRepository(application);
        }
        return instance;
    }

    public MediumRepository(Application application) {

        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(Constants.PAGE_SIZE)
                .setInitialLoadSizeHint(Constants.PAGE_SIZE)
                .build();
        mediumDataSourceFactory = new MediumDataSourceFactory(application);
        artList = new LivePagedListBuilder<>(mediumDataSourceFactory, config).build();

        mediumDataSource = (MediumDataSource) mediumDataSourceFactory.create();//if remove this line artLike will not working after refresh
    }

    public LiveData<PagedList<Art>> getArtList(){
        return artList;
    }

    public LiveData<Boolean> getIsLoading() {
        return mediumDataSource.getIsLoading();
    }

    public LiveData<Boolean> getIsListEmpty() {
        return mediumDataSource.getIsListEmpty();
    }

    public boolean refresh() {
        return mediumDataSourceFactory.refresh();
    }

    public void writeDimentionsOnServer(Art art) {
        mediumDataSource.writeDimentionsOnServer(art);
    }

    public MediumRepository finish(Application application) {
        MediumDataInMemory.getInstance().refresh();
        instance = new MediumRepository(application);
        return instance;
    }

    public void setArtQueryAndType(String artQuery, String queryType) {
        this.artQuery = artQuery;
        this.queryType = queryType;
    }

    public String getArtQuery() {
        return artQuery;
    }

    public String getQueryType() {
        return queryType;
    }

    public void setActivity(MainActivity activity) {
        mediumDataSource.setActivity(activity);
    }
}

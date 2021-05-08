package com.company.art_and_culture.myarts.art_filter_fragment;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.pojo.FilterObject;

import java.util.ArrayList;

public class ArtFilterRepository {

    private LiveData<PagedList<Art>> artList;
    private ArtFilterDataSource artFilterDataSource;
    private FiltersDataSource filtersDataSource;
    private ArtFilterDataSourceFactory artFilterDataSourceFactory;
    private Application application;

    private String keyword, makerFilter, centuryFilter, keywordType;

    public ArtFilterRepository(Application application, String keyword, String makerFilter, String centuryFilter, String keywordType) {

        this.application = application;
        this.keyword = keyword;
        this.makerFilter = makerFilter;
        this.centuryFilter = centuryFilter;
        this.keywordType = keywordType;

        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(Constants.PAGE_SIZE)
                .setInitialLoadSizeHint(Constants.PAGE_SIZE)
                .build();
        artFilterDataSourceFactory = new ArtFilterDataSourceFactory(application, keyword, makerFilter, centuryFilter, keywordType);
        artList = new LivePagedListBuilder<>(artFilterDataSourceFactory, config).build();

        artFilterDataSource = (ArtFilterDataSource) artFilterDataSourceFactory.create();//if remove this line artLike will not working after refresh

        filtersDataSource = new FiltersDataSource(application, keyword, makerFilter, centuryFilter, keywordType);
    }

    public LiveData<PagedList<Art>> getArtList(){
        return artList;
    }

    public LiveData<ArrayList<String>> getListMakerFilters() {
        return filtersDataSource.getListMakerFilters();
    }

    public LiveData<ArrayList<String>> getListCenturyFilters() {
        return filtersDataSource.getListCenturyFilters();
    }

    public LiveData<ArrayList<FilterObject>> getListKeywordFilters() {
        return filtersDataSource.getListKeywordFilters();
    }

    public LiveData<Integer> getArtCount() {
        return filtersDataSource.getArtCount();
    }

    public LiveData<Boolean> getIsLoading() {
        return artFilterDataSource.getIsLoading();
    }

    public LiveData<Boolean> getIsListEmpty() {
        return artFilterDataSource.getIsListEmpty();
    }

    public void writeDimentionsOnServer(Art art) {
        artFilterDataSource.writeDimentionsOnServer(art);
    }

    public boolean refresh() {
        //filtersDataSource.refresh();
        return artFilterDataSourceFactory.refresh();
    }

    public void setFilters(String keyword, String makerFilter, String centuryFilter, String keywordType) {

        if (!this.keyword.equals(keyword) || !this.makerFilter.equals(makerFilter) ||
                !this.centuryFilter.equals(centuryFilter) || !this.keywordType.equals(keywordType)) {
            this.keyword = keyword;
            this.makerFilter = makerFilter;
            this.centuryFilter = centuryFilter;
            this.keywordType = keywordType;
            artFilterDataSourceFactory.setFilters(keyword, makerFilter, centuryFilter, keywordType);
            filtersDataSource.setFilters(keyword, makerFilter, centuryFilter, keywordType);
        }
    }

}

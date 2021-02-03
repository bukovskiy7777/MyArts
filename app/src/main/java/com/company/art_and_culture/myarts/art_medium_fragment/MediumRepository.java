package com.company.art_and_culture.myarts.art_medium_fragment;

import android.app.Application;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.pojo.FilterObject;

import java.util.ArrayList;

import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

public class MediumRepository {

    private LiveData<PagedList<Art>> artList;
    private MediumDataSource mediumDataSource;
    private FiltersDataSource filtersDataSource;
    private MediumDataSourceFactory mediumDataSourceFactory;
    private Application application;

    private String keyword, makerFilter, centuryFilter, keywordType;

    public MediumRepository(Application application, String keyword, String makerFilter, String centuryFilter, String keywordType) {

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
        mediumDataSourceFactory = new MediumDataSourceFactory(application, keyword, makerFilter, centuryFilter, keywordType);
        artList = new LivePagedListBuilder<>(mediumDataSourceFactory, config).build();

        mediumDataSource = (MediumDataSource) mediumDataSourceFactory.create();//if remove this line artLike will not working after refresh

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
        return mediumDataSource.getIsLoading();
    }

    public LiveData<Boolean> getIsListEmpty() {
        return mediumDataSource.getIsListEmpty();
    }

    public void writeDimentionsOnServer(Art art) {
        mediumDataSource.writeDimentionsOnServer(art);
    }

    public boolean refresh() {
        //filtersDataSource.refresh();
        return mediumDataSourceFactory.refresh();
    }

    public void setFilters(String keyword, String makerFilter, String centuryFilter, String keywordType) {

        if (!this.keyword.equals(keyword) || !this.makerFilter.equals(makerFilter)
                || !this.centuryFilter.equals(centuryFilter) || !this.keywordType.equals(keywordType)) {
            this.keyword = keyword;
            this.makerFilter = makerFilter;
            this.centuryFilter = centuryFilter;
            this.keywordType = keywordType;
            mediumDataSourceFactory.setFilters(keyword, makerFilter, centuryFilter, keywordType);
            filtersDataSource.setFilters(keyword, makerFilter, centuryFilter, keywordType);
        }
    }

    public void setActivity(MainActivity activity) {
        mediumDataSource.setActivity(activity);
    }

}

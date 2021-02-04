package com.company.art_and_culture.myarts.art_filter_fragment;

import android.app.Application;

import com.company.art_and_culture.myarts.pojo.Art;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;

public class ArtFilterDataSourceFactory extends DataSource.Factory<Integer, Art> {

    private Application application;
    private ArtFilterDataSource artFilterDataSource;
    private String keyword, makerFilter, centuryFilter, keywordType;

    public ArtFilterDataSourceFactory(Application application, String keyword, String makerFilter, String centuryFilter, String keywordType) {
        this.application = application;
        this.keyword = keyword;
        this.makerFilter = makerFilter;
        this.centuryFilter = centuryFilter;
        this.keywordType = keywordType;
        artFilterDataSource = new ArtFilterDataSource(application, keyword, makerFilter, centuryFilter, keywordType);
    }

    @NonNull
    @Override
    public DataSource<Integer, Art> create() {
        if (artFilterDataSource.isInvalid()) artFilterDataSource = new ArtFilterDataSource(application, keyword, makerFilter, centuryFilter, keywordType);
        return artFilterDataSource;
    }

    public boolean refresh() {
        boolean isConnected = artFilterDataSource.isNetworkAvailable();
        if (isConnected){
            artFilterDataSource.refresh();
        }
        return isConnected;
    }

    public void setFilters(String keyword, String makerFilter, String centuryFilter, String keywordType) {
        this.keyword = keyword;
        this.makerFilter = makerFilter;
        this.centuryFilter = centuryFilter;
        this.keywordType = keywordType;

        artFilterDataSource.refresh();
    }
}

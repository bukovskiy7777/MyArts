package com.company.art_and_culture.myarts.art_medium_fragment;

import android.app.Application;

import com.company.art_and_culture.myarts.pojo.Art;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;

public class MediumDataSourceFactory extends DataSource.Factory<Integer, Art> {

    private Application application;
    private MediumDataSource mediumDataSource;
    private String keyword, makerFilter, centuryFilter, keywordType;

    public MediumDataSourceFactory(Application application, String keyword, String makerFilter, String centuryFilter, String keywordType) {
        this.application = application;
        this.keyword = keyword;
        this.makerFilter = makerFilter;
        this.centuryFilter = centuryFilter;
        this.keywordType = keywordType;
        mediumDataSource = new MediumDataSource(application, keyword, makerFilter, centuryFilter, keywordType);
    }

    @NonNull
    @Override
    public DataSource<Integer, Art> create() {
        if (mediumDataSource.isInvalid()) mediumDataSource = new MediumDataSource(application, keyword, makerFilter, centuryFilter, keywordType);
        return mediumDataSource;
    }

    public MediumDataSource getMediumDataSource() {
        return mediumDataSource;
    }

    public boolean refresh() {
        boolean isConnected = mediumDataSource.isNetworkAvailable();
        if (isConnected){
            mediumDataSource.refresh();
        }
        return isConnected;
    }

    public void setFilters(String keyword, String makerFilter, String centuryFilter, String keywordType) {
        this.keyword = keyword;
        this.makerFilter = makerFilter;
        this.centuryFilter = centuryFilter;
        this.keywordType = keywordType;

        mediumDataSource.refresh();
    }
}

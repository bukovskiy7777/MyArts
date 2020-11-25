package com.company.art_and_culture.myarts.art_search_fragment;

import android.app.Application;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.pojo.Art;

import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

public class SearchRepository {

    private static SearchRepository instance;
    private LiveData<PagedList<Art>> artList;
    private SearchDataSource searchDataSource;
    private SearchDataSourceFactory searchDataSourceFactory;
    private String searchQuery;


    public static SearchRepository getInstance(Application application, String searchQuery){
        if(instance == null){
            instance = new SearchRepository(application, searchQuery);
        }
        return instance;
    }

    public SearchRepository(Application application, String searchQuery) {

        this.searchQuery = searchQuery;

        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(Constants.PAGE_SIZE)
                .setInitialLoadSizeHint(Constants.PAGE_SIZE)
                .build();
        searchDataSourceFactory = new SearchDataSourceFactory(application, searchQuery);
        artList = new LivePagedListBuilder<>(searchDataSourceFactory, config).build();

        searchDataSource = (SearchDataSource) searchDataSourceFactory.create();//if remove this line artLike will not working after refresh
    }

    public LiveData<PagedList<Art>> getArtList(){
        return artList;
    }

    public LiveData<Boolean> getIsLoading() {
        return searchDataSource.getIsLoading();
    }

    public LiveData<Boolean> getIsListEmpty() {
        return searchDataSource.getIsListEmpty();
    }

    public boolean likeArt(Art art, int position, String userUniqueId) {

        boolean isConnected = searchDataSource.isNetworkAvailable();
        if (isConnected){
            searchDataSource.likeArt(art, position, userUniqueId);
        }
        return isConnected;
    }

    public boolean refresh() {
        return searchDataSourceFactory.refresh();
    }

    public void writeDimentionsOnServer(Art art) {
        searchDataSource.writeDimentionsOnServer(art);
    }

    public SearchRepository setSearchQuery(String searchQuery) {
        if (!this.searchQuery.equals(searchQuery)) searchDataSourceFactory.setSearchQuery(searchQuery);
        this.searchQuery = searchQuery;
        return instance;
    }

    public void setActivity(MainActivity activity) {
        searchDataSource.setActivity(activity);
    }
}

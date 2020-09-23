package com.company.art_and_culture.myarts.art_search_fragment;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.network.NetworkQuery;
import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.pojo.ServerRequest;
import com.company.art_and_culture.myarts.pojo.ServerResponse;
import com.company.art_and_culture.myarts.ui.favorites.Favorites.FavoritesRepository;
import com.company.art_and_culture.myarts.ui.home.HomeDataInMemory;
import com.company.art_and_culture.myarts.ui.home.HomeRepository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.PageKeyedDataSource;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchDataSource extends PageKeyedDataSource<Integer, Art> {

    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<Boolean> isListEmpty = new MutableLiveData<>();
    private MutableLiveData<Art> art = new MutableLiveData<>();
    private Application application;

    public SearchDataSource(Application application) {
        this.application = application;
    }

    @Override
    public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Integer, Art> callback) { }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull final LoadInitialCallback<Integer, Art> callback) {

        updateIsLoadingState(true);
        updateIsListEmptyState(false);

        String userUniqueId = application.getSharedPreferences(Constants.TAG,0).getString(Constants.USER_UNIQUE_ID,"");
        String searchQuery = SearchRepository.getInstance(application).getSearchQuery();
        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.GET_ARTS_LIST_SEARCH_OPERATION);
        request.setSearchQuery(searchQuery);
        request.setPageNumber(1);
        request.setUserUniqueId(userUniqueId);

        Call<ServerResponse> response = NetworkQuery.getInstance().create(Constants.BASE_URL, request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                updateIsLoadingState(false);
                if (response.isSuccessful()) {
                    ServerResponse resp = response.body();
                    if(resp.getResult().equals(Constants.SUCCESS)) {

                        updateIsListEmptyState(false);
                        SearchDataInMemory.getInstance().addData(resp.getListArts());
                        callback.onResult(SearchDataInMemory.getInstance().getInitialData(),null, 2);
                    } else {
                        updateIsListEmptyState(true);
                    }
                } else {
                    updateIsListEmptyState(true);
                }
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                updateIsLoadingState(false);
                updateIsListEmptyState(true);
            }
        });

    }

    @Override
    public void loadAfter(@NonNull final LoadParams<Integer> params, @NonNull final LoadCallback<Integer, Art> callback) {

        String userUniqueId = application.getSharedPreferences(Constants.TAG,0).getString(Constants.USER_UNIQUE_ID,"");
        String searchQuery = SearchRepository.getInstance(application).getSearchQuery();
        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.GET_ARTS_LIST_SEARCH_OPERATION);
        request.setSearchQuery(searchQuery);
        request.setPageNumber(params.key);
        request.setUserUniqueId(userUniqueId);

        Call<ServerResponse> response = NetworkQuery.getInstance().create(Constants.BASE_URL, request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                updateIsLoadingState(false);
                if (response.isSuccessful()) {
                    ServerResponse resp = response.body();
                    if(resp.getResult().equals(Constants.SUCCESS)) {

                        updateIsListEmptyState(false);
                        SearchDataInMemory.getInstance().addData(resp.getListArts());
                        callback.onResult(SearchDataInMemory.getInstance().getAfterData(params.key), params.key + 1);
                    } else {
                        updateIsListEmptyState(false);
                    }
                } else {
                    updateIsListEmptyState(false);
                }
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                updateIsLoadingState(false);
                updateIsListEmptyState(false);
            }
        });

    }

    private void updateIsLoadingState(Boolean state) {
        isLoading.postValue(state);
    }

    private void updateIsListEmptyState(Boolean state) {
        isListEmpty.postValue(state);
    }

    public void updateArt(Art newArt) {
        art.postValue(newArt);
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getIsListEmpty() {
        return isListEmpty;
    }

    public LiveData<Art> getArt() {
        return art;
    }

    public void likeArt(Art art, final int position, String userUniqueId) {

        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.ART_LIKE_OPERATION);
        request.setUserUniqueId(userUniqueId);
        request.setArt(art);
        Call<ServerResponse> response = NetworkQuery.getInstance().create(Constants.BASE_URL, request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                if (response.isSuccessful()) {
                    ServerResponse resp = response.body();
                    if(resp.getResult().equals(Constants.SUCCESS)) {

                        updateArt(resp.getArt());
                        SearchDataInMemory.getInstance().updateSingleItem(resp.getArt());

                        HomeRepository.getInstance(application).getHomeDataSource().updateArt(resp.getArt());
                        HomeDataInMemory.getInstance().updateSingleItem(resp.getArt());

                        FavoritesRepository favoritesRepository = FavoritesRepository.getInstance(application);
                        favoritesRepository.refresh();

                    } else {

                    }
                } else {

                }
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {

            }
        });
    }

    public void refresh() {
        SearchDataInMemory.getInstance().refresh();
        invalidate();
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager manager =
                (ConnectivityManager) application.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            // Network is present and connected
            isAvailable = true;
        }
        return isAvailable;
    }

    public void writeDimentionsOnServer(Art art) {

        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.ART_WRITE_DIMENS_OPERATION);
        request.setArt(art);
        Call<ServerResponse> response = NetworkQuery.getInstance().create(Constants.BASE_URL, request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) { }
            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) { }
        });
    }
}

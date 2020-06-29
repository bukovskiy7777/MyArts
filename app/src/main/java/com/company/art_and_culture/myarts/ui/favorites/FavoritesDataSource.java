package com.company.art_and_culture.myarts.ui.favorites;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.network.NetworkQuery;
import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.pojo.ServerRequest;
import com.company.art_and_culture.myarts.pojo.ServerResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

class FavoritesDataSource {

    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<Boolean> isListEmpty = new MutableLiveData<>();
    private MutableLiveData<ArrayList<Art>> artList = new MutableLiveData<>();
    private Application application;

    public FavoritesDataSource(Application application) {
        this.application = application;
        updateIsLoadingState(true);
        updateIsListEmptyState(false);
        loadMyFavorites();
    }

    public void loadMyFavorites (){

        String userUniqueId = application.getSharedPreferences(Constants.TAG,0).getString(Constants.USER_UNIQUE_ID,"");
        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.GET_MY_FAVORITES_OPERATION);
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
                        updateArtList(resp.getListArts());
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

    private void updateIsLoadingState(Boolean state) {
        isLoading.postValue(state);
    }

    private void updateIsListEmptyState(Boolean state) {
        isListEmpty.postValue(state);
    }

    private void updateArtList (ArrayList<Art> arts) {
        artList.postValue(arts);
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getIsListEmpty() {
        return isListEmpty;
    }

    public LiveData<ArrayList<Art>> getArtList() {
        return artList;
    }

    public void refresh() {
        loadMyFavorites();
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

}

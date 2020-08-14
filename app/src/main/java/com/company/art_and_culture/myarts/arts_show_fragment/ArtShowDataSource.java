package com.company.art_and_culture.myarts.arts_show_fragment;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.art_maker_fragment.MakerDataInMemory;
import com.company.art_and_culture.myarts.network.NetworkQuery;
import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.pojo.ServerRequest;
import com.company.art_and_culture.myarts.pojo.ServerResponse;
import com.company.art_and_culture.myarts.art_search_fragment.SearchDataInMemory;
import com.company.art_and_culture.myarts.art_search_fragment.SearchRepository;
import com.company.art_and_culture.myarts.ui.favorites.FavoritesRepository;
import com.company.art_and_culture.myarts.ui.home.HomeDataInMemory;
import com.company.art_and_culture.myarts.ui.home.HomeRepository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

class ArtShowDataSource {

    private Application application;
    private MutableLiveData<Art> art = new MutableLiveData<>();

    private static ArtShowDataSource instance;

    public static ArtShowDataSource getInstance(Application application){
        if(instance == null){
            instance = new ArtShowDataSource(application);
        }
        return instance;
    }


    public ArtShowDataSource(Application application) {
        this.application = application;
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
                        ArtShowDataInMemory.getInstance().updateSingleItem(resp.getArt());

                        SearchRepository.getInstance(application).getSearchDataSource().updateArt(resp.getArt());
                        SearchDataInMemory.getInstance().updateSingleItem(resp.getArt());

                        HomeRepository.getInstance(application).getHomeDataSource().updateArt(resp.getArt());
                        HomeDataInMemory.getInstance().updateSingleItem(resp.getArt());

                        MakerDataInMemory.getInstance().updateSingleItem(resp.getArt());

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

    private void updateArt(Art newArt) {
        art.postValue(newArt);
    }

    public LiveData<Art> getArt() {
        return art;
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

    public void finish() {
        ArtShowDataInMemory.getInstance().finish();
        instance = null;
    }

}
package com.company.art_and_culture.myarts.museum_fragment;

import android.app.Application;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.network.NetworkQuery;
import com.company.art_and_culture.myarts.pojo.ArtProvider;
import com.company.art_and_culture.myarts.pojo.Maker;
import com.company.art_and_culture.myarts.pojo.ServerRequest;
import com.company.art_and_culture.myarts.pojo.ServerResponse;

import java.util.ArrayList;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MuseumInfoDataSource {

    private Application application;
    private String artProviderId;
    private MutableLiveData<ArtProvider> artProvider = new MutableLiveData<>();
    private MutableLiveData<ArrayList<Maker>> listMakers = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLiked = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public MuseumInfoDataSource(Application application, String museumId) {
        this.artProviderId = museumId;
        this.application = application;
        updateIsLoadingState(true);
        loadMuseumInfo();
        loadMakersList();
    }

    public void setArtProviderId(String artProviderId) {
        this.artProviderId = artProviderId;
        updateIsLoadingState(true);
        loadMuseumInfo();
        loadMakersList();
    }

    public void refresh() {
        updateIsLoadingState(true);
        loadMuseumInfo();
        loadMakersList();
    }

    public LiveData<ArtProvider> getArtProvider() {
        return artProvider;
    }

    public void updateArtProvider(ArtProvider artProvider) {
        this.artProvider.postValue(artProvider);
    }

    public LiveData<ArrayList<Maker>> getListMakers() {
        return listMakers;
    }

    private void updateMakersList(ArrayList<Maker> listMakers) {
        this.listMakers.postValue(listMakers);
    }

    private void updateArtProviderLike(boolean liked) {
        isLiked.postValue(liked);
    }

    public LiveData<Boolean> getArtProviderLike() {
        return isLiked;
    }

    private void updateIsLoadingState(Boolean state) {
        isLoading.postValue(state);
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    private void loadMuseumInfo() {

        String userUniqueId = application.getSharedPreferences(Constants.TAG,0).getString(Constants.USER_UNIQUE_ID,"");
        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.GET_MUSEUM_INFO_OPERATION);
        request.setUserUniqueId(userUniqueId);
        request.setArtProviderId(artProviderId);

        Call<ServerResponse> response = NetworkQuery.getInstance().create(Constants.BASE_URL, request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {

                updateIsLoadingState(false);

                if (response.isSuccessful()) {
                    ServerResponse resp = response.body();
                    if(resp.getResult().equals(Constants.SUCCESS)) {

                        updateArtProvider(resp.getArtProvider());

                    } else { }
                } else { }
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                updateIsLoadingState(false);
            }
        });
    }

    private void loadMakersList() {

        String userUniqueId = application.getSharedPreferences(Constants.TAG,0).getString(Constants.USER_UNIQUE_ID,"");
        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.GET_MUSEUM_MAKERS_LIST_OPERATION);
        request.setUserUniqueId(userUniqueId);
        request.setArtProviderId(artProviderId);

        Call<ServerResponse> response = NetworkQuery.getInstance().create(Constants.BASE_URL, request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {

                if (response.isSuccessful()) {
                    ServerResponse resp = response.body();
                    if(resp.getResult().equals(Constants.SUCCESS)) {

                        updateMakersList(resp.getListMakers());

                    } else { }
                } else { }
            }
            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) { }
        });
    }

    public void likeMuseum(ArtProvider museum, String userUniqueId) {
        ServerRequest request = new ServerRequest();
        request.setUserUniqueId(userUniqueId);
        request.setOperation(Constants.MUSEUM_LIKE_OPERATION);
        request.setArtProvider(museum);
        Call<ServerResponse> response = NetworkQuery.getInstance().create(Constants.BASE_URL, request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                if (response.isSuccessful()) {
                    ServerResponse resp = response.body();
                    if(resp.getResult().equals(Constants.SUCCESS)) {

                        updateArtProviderLike(resp.getArtProvider().isLiked());

                    } else { }
                } else { }
            }
            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) { }
        });
    }
}

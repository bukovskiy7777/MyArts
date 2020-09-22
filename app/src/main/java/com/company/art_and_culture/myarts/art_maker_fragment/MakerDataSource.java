package com.company.art_and_culture.myarts.art_maker_fragment;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.art_search_fragment.SearchDataInMemory;
import com.company.art_and_culture.myarts.art_search_fragment.SearchRepository;
import com.company.art_and_culture.myarts.network.NetworkQuery;
import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.pojo.Maker;
import com.company.art_and_culture.myarts.pojo.ServerRequest;
import com.company.art_and_culture.myarts.pojo.ServerResponse;
import com.company.art_and_culture.myarts.ui.favorites.FavoritesRepository;
import com.company.art_and_culture.myarts.ui.home.HomeDataInMemory;
import com.company.art_and_culture.myarts.ui.home.HomeRepository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.PageKeyedDataSource;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MakerDataSource extends PageKeyedDataSource<Integer, Art> {

    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<Boolean> isListEmpty = new MutableLiveData<>();
    private MutableLiveData<Maker> maker = new MutableLiveData<>();
    private MutableLiveData<Art> art = new MutableLiveData<>();
    private Application application;

    public MakerDataSource(Application application) {
        this.application = application;
    }

    @Override
    public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Integer, Art> callback) { }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull final LoadInitialCallback<Integer, Art> callback) {

        updateIsLoadingState(true);
        updateIsListEmptyState(false);
        getMakerObject();

        String userUniqueId = application.getSharedPreferences(Constants.TAG,0).getString(Constants.USER_UNIQUE_ID,"");
        String artMaker = MakerRepository.getInstance(application).getArtMaker().getArtMaker();

        ServerRequest request = new ServerRequest();
        request.setArtQuery(artMaker);
        request.setPageNumber(1);
        request.setUserUniqueId(userUniqueId);
        request.setOperation(Constants.GET_ARTS_LIST_MAKER_OPERATION);

        Call<ServerResponse> response = NetworkQuery.getInstance().create(Constants.BASE_URL, request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                updateIsLoadingState(false);
                if (response.isSuccessful()) {
                    ServerResponse resp = response.body();
                    if(resp.getResult().equals(Constants.SUCCESS)) {

                        updateIsListEmptyState(false);
                        MakerDataInMemory.getInstance().addData(resp.getListArts());
                        callback.onResult(MakerDataInMemory.getInstance().getInitialData(),null, 2);
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
        String artMaker = MakerRepository.getInstance(application).getArtMaker().getArtMaker();

        ServerRequest request = new ServerRequest();
        request.setArtQuery(artMaker);
        request.setPageNumber(params.key);
        request.setUserUniqueId(userUniqueId);
        request.setOperation(Constants.GET_ARTS_LIST_MAKER_OPERATION);

        Call<ServerResponse> response = NetworkQuery.getInstance().create(Constants.BASE_URL, request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                updateIsLoadingState(false);
                if (response.isSuccessful()) {
                    ServerResponse resp = response.body();
                    if(resp.getResult().equals(Constants.SUCCESS)) {

                        updateIsListEmptyState(false);
                        MakerDataInMemory.getInstance().addData(resp.getListArts());
                        callback.onResult(MakerDataInMemory.getInstance().getAfterData(params.key), params.key + 1);
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

    private void updateArtMaker(Maker artMaker) {
        maker.postValue(artMaker);
    }

    public MutableLiveData<Maker> getMaker() {
        return maker;
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
                        MakerDataInMemory.getInstance().updateSingleItem(resp.getArt());

                        SearchRepository.getInstance(application).getSearchDataSource().updateArt(resp.getArt());
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
        MakerDataInMemory.getInstance().refresh();
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

    private void getMakerObject() {

        String userUniqueId = application.getSharedPreferences(Constants.TAG,0).getString(Constants.USER_UNIQUE_ID,"");
        Maker artMaker = MakerRepository.getInstance(application).getArtMaker();
        ServerRequest request = new ServerRequest();
        request.setUserUniqueId(userUniqueId);
        request.setMaker(artMaker);
        request.setOperation(Constants.GET_MAKER_OBJECT);

        Call<ServerResponse> response = NetworkQuery.getInstance().create(Constants.BASE_URL, request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                if (response.isSuccessful()) {
                    ServerResponse resp = response.body();
                    if(resp.getResult().equals(Constants.SUCCESS)) {

                        updateArtMaker(resp.getArtMaker());
                    } else {

                    }
                } else {

                }
            }
            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) { }
        });
    }

    public void likeMaker(Maker maker, String userUniqueId) {

        ServerRequest request = new ServerRequest();
        request.setUserUniqueId(userUniqueId);
        request.setOperation(Constants.MAKER_LIKE_OPERATION);
        request.setMaker(maker);
        Call<ServerResponse> response = NetworkQuery.getInstance().create(Constants.BASE_URL, request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                if (response.isSuccessful()) {
                    ServerResponse resp = response.body();
                    Log.i("response", resp.getMessage());
                    if(resp.getResult().equals(Constants.SUCCESS)) {

                        updateArtMaker(resp.getArtMaker());
                        //MakerDataInMemory.getInstance().updateArtMaker(resp.getArtMaker());
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
}

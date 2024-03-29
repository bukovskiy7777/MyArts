package com.company.art_and_culture.myarts.bottom_menu.home;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.network.CallbackWithRetry;
import com.company.art_and_culture.myarts.network.NetworkQuery;
import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.pojo.ServerRequest;
import com.company.art_and_culture.myarts.pojo.ServerResponse;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.PageKeyedDataSource;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeDataSource extends PageKeyedDataSource<Integer, Art> {

    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<Boolean> isListEmpty = new MutableLiveData<>();
    private Application application;
    private MainActivity activity;

    public HomeDataSource(Application application) {
        this.application = application;
    }

    @Override
    public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Integer, Art> callback) { }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull final LoadInitialCallback<Integer, Art> callback) {

        updateIsLoadingState(true);
        updateIsListEmptyState(false);

        String userUniqueId = application.getSharedPreferences(Constants.TAG,0).getString(Constants.USER_UNIQUE_ID,"");
        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.GET_ARTS_LIST_OPERATION);
        request.setPageNumber(1);
        request.setUserUniqueId(userUniqueId);

        Call<ServerResponse> response = NetworkQuery.getInstance().homeFragmentGetArts(Constants.BASE_URL, request);
        response.enqueue(new CallbackWithRetry<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                updateIsLoadingState(false);
                if (response.isSuccessful()) {
                    ServerResponse resp = response.body();
                    if(resp.getResult().equals(Constants.SUCCESS)) {

                        updateIsListEmptyState(false);
                        HomeDataInMemory.getInstance().addData(resp.getListArts());
                        callback.onResult(HomeDataInMemory.getInstance().getInitialData(),null, 2);
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
        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.GET_ARTS_LIST_OPERATION);
        request.setPageNumber(params.key);
        request.setUserUniqueId(userUniqueId);

        Call<ServerResponse> response = NetworkQuery.getInstance().homeFragmentGetArts(Constants.BASE_URL, request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                updateIsLoadingState(false);
                if (response.isSuccessful()) {
                    ServerResponse resp = response.body();
                    if(resp.getResult().equals(Constants.SUCCESS)) {

                        updateIsListEmptyState(false);
                        HomeDataInMemory.getInstance().addData(resp.getListArts());
                        callback.onResult(HomeDataInMemory.getInstance().getAfterData(params.key), params.key + 1);
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

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getIsListEmpty() {
        return isListEmpty;
    }

    public void likeArt(final Art art, final int position, String userUniqueId) {

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

                        activity.postNewArt(resp.getArt());

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
        HomeDataInMemory.getInstance().refresh();
        invalidate();
        //updateIsLoadingState(true);
        updateIsListEmptyState(false);
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

    public void setActivity(MainActivity activity) {
        this.activity = activity;
        HomeDataInMemory.getInstance().setArtObserver(activity);
    }
}

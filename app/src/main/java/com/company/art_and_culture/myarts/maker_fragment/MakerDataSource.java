package com.company.art_and_culture.myarts.maker_fragment;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.network.NetworkQuery;
import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.pojo.FilterObject;
import com.company.art_and_culture.myarts.pojo.Maker;
import com.company.art_and_culture.myarts.pojo.ServerRequest;
import com.company.art_and_culture.myarts.pojo.ServerResponse;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.PageKeyedDataSource;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MakerDataSource extends PageKeyedDataSource<Integer, Art> {


    private MutableLiveData<Boolean> isListEmpty = new MutableLiveData<>();

    private Application application;
    private Maker artMaker;
    private MainActivity activity;
    private FilterObject filterObject;

    public MakerDataSource(Application application, Maker artMaker, FilterObject filterObject) {
        this.application = application;
        this.artMaker = artMaker;
        this.filterObject = filterObject;
    }

    @Override
    public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Integer, Art> callback) { }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull final LoadInitialCallback<Integer, Art> callback) {

        updateIsListEmptyState(false);

        String userUniqueId = application.getSharedPreferences(Constants.TAG,0).getString(Constants.USER_UNIQUE_ID,"");

        ServerRequest request = new ServerRequest();
        request.setMakerFilter(artMaker.getArtMaker());
        request.setPageNumber(1);
        request.setKeywordFilter(filterObject.getText());
        request.setKeywordType(filterObject.getType());
        request.setUserUniqueId(userUniqueId);
        request.setOperation(Constants.GET_ARTS_LIST_MAKER_OPERATION);

        Call<ServerResponse> response = NetworkQuery.getInstance().create(Constants.BASE_URL, request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {

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
                updateIsListEmptyState(true);
            }
        });

    }

    @Override
    public void loadAfter(@NonNull final LoadParams<Integer> params, @NonNull final LoadCallback<Integer, Art> callback) {

        String userUniqueId = application.getSharedPreferences(Constants.TAG,0).getString(Constants.USER_UNIQUE_ID,"");

        ServerRequest request = new ServerRequest();
        request.setMakerFilter(artMaker.getArtMaker());
        request.setPageNumber(params.key);
        request.setKeywordFilter(filterObject.getText());
        request.setKeywordType(filterObject.getType());
        request.setUserUniqueId(userUniqueId);
        request.setOperation(Constants.GET_ARTS_LIST_MAKER_OPERATION);

        Call<ServerResponse> response = NetworkQuery.getInstance().create(Constants.BASE_URL, request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {

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
                updateIsListEmptyState(false);
            }
        });

    }

    private void updateIsListEmptyState(Boolean state) {
        isListEmpty.postValue(state);
    }

    public LiveData<Boolean> getIsListEmpty() {
        return isListEmpty;
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

    public void setActivity(MainActivity activity) {
        this.activity = activity;
        MakerDataInMemory.getInstance().setArtObserver(activity);
    }
}

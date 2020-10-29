package com.company.art_and_culture.myarts.filter_maker_fragment;

import android.app.Application;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.network.NetworkQuery;
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

class FilterMakerDataSource extends PageKeyedDataSource<Integer, Maker> {

    private Application application;
    private String filter = "", date = "";
    private MutableLiveData<Boolean> isInitialLoaded = new MutableLiveData<>();

    public FilterMakerDataSource(Application application, String filter, String date) {
        this.filter = filter;
        this.date = date;
        this.application = application;
        FilterMakerDataInMemory.getInstance().setFilter(filter, date);
    }


    @Override
    public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Integer, Maker> callback) { }


    @Override
    public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull final LoadInitialCallback<Integer, Maker> callback) {

        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.GET_FILTER_MAKERS_OPERATION);
        request.setSearchQuery(filter);
        request.setArtQuery(date);
        request.setPageNumber(1);
        String userUniqueId = application.getSharedPreferences(Constants.TAG,0).getString(Constants.USER_UNIQUE_ID,"");
        request.setUserUniqueId(userUniqueId);

        Call<ServerResponse> response = NetworkQuery.getInstance().create(Constants.BASE_URL, request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                if (response.isSuccessful()) {
                    ServerResponse resp = response.body();
                    if(resp.getResult().equals(Constants.SUCCESS)) {

                        FilterMakerDataInMemory.getInstance().addData(resp.getListMakers(), resp.getFilter(), resp.getCentury());
                        callback.onResult(FilterMakerDataInMemory.getInstance().getInitialData(),null, 2);
                        updateIsInitialLoaded (true);

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

    private void updateIsInitialLoaded(boolean state) {
        //Log.i("filterExploreViewModel", state+"  DDSS");
        isInitialLoaded.postValue(state);
    }

    public LiveData<Boolean> getIsInitialLoaded() {
        //Log.i("filterExploreViewModel", isInitialLoaded+"  gettt DDSS");
        return isInitialLoaded;
    }

    @Override
    public void loadAfter(@NonNull final LoadParams<Integer> params, @NonNull final LoadCallback<Integer, Maker> callback) {

        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.GET_FILTER_MAKERS_OPERATION);
        request.setSearchQuery(filter);
        request.setArtQuery(date);
        request.setPageNumber(params.key);
        String userUniqueId = application.getSharedPreferences(Constants.TAG,0).getString(Constants.USER_UNIQUE_ID,"");
        request.setUserUniqueId(userUniqueId);

        Call<ServerResponse> response = NetworkQuery.getInstance().create(Constants.BASE_URL, request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                if (response.isSuccessful()) {
                    ServerResponse resp = response.body();
                    if(resp.getResult().equals(Constants.SUCCESS)) {

                        FilterMakerDataInMemory.getInstance().addData(resp.getListMakers(), resp.getFilter(), resp.getCentury());
                        callback.onResult(FilterMakerDataInMemory.getInstance().getAfterData(params.key), params.key + 1);
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
        FilterMakerDataInMemory.getInstance().refresh();
        updateIsInitialLoaded (false);
        invalidate();
    }
}

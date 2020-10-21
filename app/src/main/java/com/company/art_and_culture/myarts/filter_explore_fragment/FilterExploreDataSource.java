package com.company.art_and_culture.myarts.filter_explore_fragment;

import android.app.Application;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.network.NetworkQuery;
import com.company.art_and_culture.myarts.pojo.ExploreObject;
import com.company.art_and_culture.myarts.pojo.ServerRequest;
import com.company.art_and_culture.myarts.pojo.ServerResponse;

import androidx.annotation.NonNull;
import androidx.paging.PageKeyedDataSource;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

class FilterExploreDataSource extends PageKeyedDataSource<Integer, ExploreObject> {

    private Application application;

    public FilterExploreDataSource(Application application) {
        this.application = application;
    }


    @Override
    public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Integer, ExploreObject> callback) { }


    @Override
    public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull final LoadInitialCallback<Integer, ExploreObject> callback) {

        String filter = FilterExploreRepository.getInstance(application).getFilter();
        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.GET_FILTER_EXPLORE_MAKERS_OPERATION);
        request.setSearchQuery(filter);
        request.setPageNumber(1);

        Call<ServerResponse> response = NetworkQuery.getInstance().create(Constants.BASE_URL, request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                if (response.isSuccessful()) {
                    ServerResponse resp = response.body();
                    if(resp.getResult().equals(Constants.SUCCESS)) {
                        if (resp.getListMaker().get(0).getText().startsWith(FilterExploreRepository.getInstance(application).getFilter())) {
                            FilterExploreDataInMemory.getInstance().addData(resp.getListMaker());
                            callback.onResult(FilterExploreDataInMemory.getInstance().getInitialData(),null, 2);
                        }
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

    @Override
    public void loadAfter(@NonNull final LoadParams<Integer> params, @NonNull final LoadCallback<Integer, ExploreObject> callback) {

        String filter = FilterExploreRepository.getInstance(application).getFilter();
        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.GET_FILTER_EXPLORE_MAKERS_OPERATION);
        request.setSearchQuery(filter);
        request.setPageNumber(params.key);

        Call<ServerResponse> response = NetworkQuery.getInstance().create(Constants.BASE_URL, request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                if (response.isSuccessful()) {
                    ServerResponse resp = response.body();
                    if(resp.getResult().equals(Constants.SUCCESS)) {
                        if (resp.getListMaker().get(0).getText().startsWith(FilterExploreRepository.getInstance(application).getFilter())) {
                            FilterExploreDataInMemory.getInstance().addData(resp.getListMaker());
                            callback.onResult(FilterExploreDataInMemory.getInstance().getAfterData(params.key), params.key + 1);                    } else {
                        }
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
        FilterExploreDataInMemory.getInstance().refresh();
        invalidate();
    }
}

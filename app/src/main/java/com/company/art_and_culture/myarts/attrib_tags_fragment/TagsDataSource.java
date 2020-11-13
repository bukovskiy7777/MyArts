package com.company.art_and_culture.myarts.attrib_tags_fragment;

import android.app.Application;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.network.NetworkQuery;
import com.company.art_and_culture.myarts.pojo.Attribute;
import com.company.art_and_culture.myarts.pojo.ServerRequest;
import com.company.art_and_culture.myarts.pojo.ServerResponse;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.PageKeyedDataSource;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

class TagsDataSource extends PageKeyedDataSource<Integer, Attribute> {

    private Application application;
    private String filter = "";
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public TagsDataSource(Application application, String filter) {
        this.filter = filter;
        this.application = application;
        TagsDataInMemory.getInstance().setFilter(filter);
    }

    private void updateIsLoadingState(Boolean state) {
        isLoading.postValue(state);
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }


    @Override
    public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Integer, Attribute> callback) { }


    @Override
    public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull final LoadInitialCallback<Integer, Attribute> callback) {

        updateIsLoadingState (true);

        ServerRequest request = new ServerRequest();
        request.setPageNumber(1);
        request.setSearchQuery(filter);
        request.setOperation(Constants.GET_LIST_TAG_OPERATION);

        Call<ServerResponse> response = NetworkQuery.getInstance().create(Constants.BASE_URL, request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                if (response.isSuccessful()) {
                    ServerResponse resp = response.body();
                    if(resp.getResult().equals(Constants.SUCCESS)) {

                        updateIsLoadingState (false);
                        TagsDataInMemory.getInstance().addData(resp.getListAttribute(), resp.getFilter());
                        callback.onResult(TagsDataInMemory.getInstance().getInitialData(),null, 2);

                    } else {
                        updateIsLoadingState (false);
                    }
                } else {
                    updateIsLoadingState (false);
                }
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                updateIsLoadingState (false);
            }
        });
    }


    @Override
    public void loadAfter(@NonNull final LoadParams<Integer> params, @NonNull final LoadCallback<Integer, Attribute> callback) {

        updateIsLoadingState (true);

        ServerRequest request = new ServerRequest();
        request.setPageNumber(params.key);
        request.setSearchQuery(filter);
        request.setOperation(Constants.GET_LIST_TAG_OPERATION);

        Call<ServerResponse> response = NetworkQuery.getInstance().create(Constants.BASE_URL, request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                if (response.isSuccessful()) {
                    ServerResponse resp = response.body();
                    if(resp.getResult().equals(Constants.SUCCESS)) {

                        updateIsLoadingState (false);
                        TagsDataInMemory.getInstance().addData(resp.getListAttribute(), resp.getFilter());
                        callback.onResult(TagsDataInMemory.getInstance().getAfterData(params.key), params.key + 1);
                    } else {
                        updateIsLoadingState (false);
                    }
                } else {
                    updateIsLoadingState (false);
                }
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                updateIsLoadingState (false);
            }
        });
    }

    public void refresh() {
        TagsDataInMemory.getInstance().refresh();
        invalidate();
    }

}

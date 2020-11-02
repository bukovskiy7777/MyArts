package com.company.art_and_culture.myarts.attribute_fragment;

import android.app.Application;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.network.NetworkQuery;
import com.company.art_and_culture.myarts.pojo.Attribute;
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

class AttributeDataSource extends PageKeyedDataSource<Integer, Attribute> {

    private Application application;
    private String attributeType = "";

    public AttributeDataSource(Application application, String attributeType) {
        this.attributeType = attributeType;
        this.application = application;
        //AttributeDataInMemory.getInstance();
    }


    @Override
    public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Integer, Attribute> callback) { }


    @Override
    public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull final LoadInitialCallback<Integer, Attribute> callback) {

        ServerRequest request = new ServerRequest();
        request.setPageNumber(1);

        if (attributeType != null && attributeType.equals(Constants.ART_MEDIUM)) {
            request.setOperation(Constants.GET_LIST_MEDIUM_OPERATION);
        } else if (attributeType != null && attributeType.equals(Constants.ART_CLASSIFICATION)) {
            request.setOperation(Constants.GET_LIST_CLASSIFICATION_OPERATION);
        } else if (attributeType != null && attributeType.equals(Constants.ART_CULTURE)) {
            request.setOperation(Constants.GET_LIST_CULTURE_OPERATION);
        }

        Call<ServerResponse> response = NetworkQuery.getInstance().create(Constants.BASE_URL, request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                if (response.isSuccessful()) {
                    ServerResponse resp = response.body();
                    if(resp.getResult().equals(Constants.SUCCESS)) {

                        AttributeDataInMemory.getInstance().addData(resp.getListAttribute());
                        callback.onResult(AttributeDataInMemory.getInstance().getInitialData(),null, 2);

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
    public void loadAfter(@NonNull final LoadParams<Integer> params, @NonNull final LoadCallback<Integer, Attribute> callback) {

        ServerRequest request = new ServerRequest();
        request.setPageNumber(params.key);

        if (attributeType != null && attributeType.equals(Constants.ART_MEDIUM)) {
            request.setOperation(Constants.GET_LIST_MEDIUM_OPERATION);
        } else if (attributeType != null && attributeType.equals(Constants.ART_CLASSIFICATION)) {
            request.setOperation(Constants.GET_LIST_CLASSIFICATION_OPERATION);
        } else if (attributeType != null && attributeType.equals(Constants.ART_CULTURE)) {
            request.setOperation(Constants.GET_LIST_CULTURE_OPERATION);
        }

        Call<ServerResponse> response = NetworkQuery.getInstance().create(Constants.BASE_URL, request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                if (response.isSuccessful()) {
                    ServerResponse resp = response.body();
                    if(resp.getResult().equals(Constants.SUCCESS)) {

                        AttributeDataInMemory.getInstance().addData(resp.getListAttribute());
                        callback.onResult(AttributeDataInMemory.getInstance().getAfterData(params.key), params.key + 1);
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
        AttributeDataInMemory.getInstance().refresh();
        invalidate();
    }
}

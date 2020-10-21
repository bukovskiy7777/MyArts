package com.company.art_and_culture.myarts.ui.explore;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.network.NetworkQuery;
import com.company.art_and_culture.myarts.pojo.ExploreObject;
import com.company.art_and_culture.myarts.pojo.ServerRequest;
import com.company.art_and_culture.myarts.pojo.ServerResponse;

import java.util.ArrayList;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

class ExploreDataSource {

    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<Boolean> isListEmpty = new MutableLiveData<>();
    private MutableLiveData<ArrayList<ExploreObject>> makersList = new MutableLiveData<>();
    private MutableLiveData<ArrayList<ExploreObject>> cultureList = new MutableLiveData<>();
    private MutableLiveData<ArrayList<ExploreObject>> mediumList = new MutableLiveData<>();
    private MutableLiveData<ArrayList<ExploreObject>> centuryList = new MutableLiveData<>();
    private Application application;

    public ExploreDataSource(Application application) {
        this.application = application;
        updateIsLoadingState(true);
        updateIsListEmptyState(false);
        loadExploreListFirst();
    }

    private void updateIsLoadingState(Boolean state) {
        isLoading.postValue(state);
    }

    private void updateIsListEmptyState(Boolean state) {
        isListEmpty.postValue(state);
    }

    private void updateMakersList(ArrayList<ExploreObject> listExplore) {
        makersList.postValue(listExplore);
    }

    private void updateCultureList(ArrayList<ExploreObject> listExplore) {
        cultureList.postValue(listExplore);
    }

    private void updateMediumList(ArrayList<ExploreObject> listExplore) {
        mediumList.postValue(listExplore);
    }

    private void updateCenturyList(ArrayList<ExploreObject> listExplore) {
        centuryList.postValue(listExplore);
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getIsListEmpty() {
        return isListEmpty;
    }

    public void refresh() {
        updateIsListEmptyState(false);
        loadExploreListAll();
    }

    private void loadExploreListAll() {

        String userUniqueId = application.getSharedPreferences(Constants.TAG,0).getString(Constants.USER_UNIQUE_ID,"");
        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.GET_EXPLORE_LIST_OPERATION);
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
                        updateMakersList(resp.getListMaker());
                        updateCultureList(resp.getListCulture());
                        updateMediumList(resp.getListMedium());
                        updateCenturyList(resp.getListCentury());
                    } else {
                        updateIsListEmptyState(true);
                        updateMakersList(null);
                        updateCultureList(null);
                        updateMediumList(null);
                        updateCenturyList(null);
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

    public LiveData<ArrayList<ExploreObject>> getMakersList() {
        return makersList;
    }

    public LiveData<ArrayList<ExploreObject>> getCultureList() {
        return cultureList;
    }

    public LiveData<ArrayList<ExploreObject>> getMediumList() {
        return mediumList;
    }

    public LiveData<ArrayList<ExploreObject>> getCenturyList() {
        return centuryList;
    }

    public void loadExploreListFirst (){

        String userUniqueId = application.getSharedPreferences(Constants.TAG,0).getString(Constants.USER_UNIQUE_ID,"");
        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.GET_EXPLORE_LIST_MAKER);
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
                        updateMakersList(resp.getListMaker());

                        loadExploreListOther ();
                    } else {
                        updateIsListEmptyState(true);
                        updateMakersList(null);
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

    public void loadExploreListOther (){

        String userUniqueId = application.getSharedPreferences(Constants.TAG,0).getString(Constants.USER_UNIQUE_ID,"");
        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.GET_EXPLORE_LIST_OTHER);
        request.setUserUniqueId(userUniqueId);

        Call<ServerResponse> response = NetworkQuery.getInstance().create(Constants.BASE_URL, request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                //updateIsLoadingState(false);
                if (response.isSuccessful()) {
                    ServerResponse resp = response.body();
                    if(resp.getResult().equals(Constants.SUCCESS)) {

                        //updateIsListEmptyState(false);
                        updateCultureList(resp.getListCulture());
                        updateMediumList(resp.getListMedium());
                        updateCenturyList(resp.getListCentury());
                    } else {
                        //updateIsListEmptyState(true);
                        updateCultureList(null);
                        updateMediumList(null);
                        updateCenturyList(null);
                    }
                } else {
                    //updateIsListEmptyState(true);
                }
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                //updateIsLoadingState(false);
                //updateIsListEmptyState(true);
            }
        });

    }


}

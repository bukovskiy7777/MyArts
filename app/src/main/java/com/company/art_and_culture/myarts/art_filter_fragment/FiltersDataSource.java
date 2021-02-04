package com.company.art_and_culture.myarts.art_filter_fragment;

import android.app.Application;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.network.NetworkQuery;
import com.company.art_and_culture.myarts.pojo.FilterObject;
import com.company.art_and_culture.myarts.pojo.ServerRequest;
import com.company.art_and_culture.myarts.pojo.ServerResponse;

import java.util.ArrayList;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FiltersDataSource {

    private Application application;
    private MutableLiveData<ArrayList<String>> listMakerFilters = new MutableLiveData<>();
    private MutableLiveData<ArrayList<String>> listCenturyFilters = new MutableLiveData<>();
    private MutableLiveData<ArrayList<FilterObject>> listKeywordFilters = new MutableLiveData<>();
    private MutableLiveData<Integer> artCount = new MutableLiveData<>();
    private String keyword, makerFilter, centuryFilter, keywordType;

    public FiltersDataSource(Application application, String keyword, String makerFilter, String centuryFilter, String keywordType) {
        this.keyword = keyword;
        this.makerFilter = makerFilter;
        this.centuryFilter = centuryFilter;
        this.keywordType = keywordType;
        this.application = application;
        loadFiltersList();
        loadArtCount();
    }

    private void updateMakersList (ArrayList<String> makers) {
        listMakerFilters.postValue(makers);
    }

    private void updateCenturyList(ArrayList<String> centuries) {
        listCenturyFilters.postValue(centuries);
    }

    private void updateKeywordListList(ArrayList<FilterObject> keywords) {
        listKeywordFilters.postValue(keywords);
    }

    private void updateArtCount (int count) {
        artCount.postValue(count);
    }

    public LiveData<ArrayList<String>> getListMakerFilters() {
        return listMakerFilters;
    }

    public LiveData<ArrayList<String>> getListCenturyFilters() {
        return listCenturyFilters;
    }

    public LiveData<ArrayList<FilterObject>> getListKeywordFilters() {
        return listKeywordFilters;
    }

    public LiveData<Integer> getArtCount() {
        return artCount;
    }

    public void setFilters(String keyword, String makerFilter, String centuryFilter, String keywordType) {
        this.keyword = keyword;
        this.makerFilter = makerFilter;
        this.centuryFilter = centuryFilter;
        this.keywordType = keywordType;
        loadFiltersList();
        loadArtCount();
    }

    public void refresh() {
        //loadFiltersList();
        //loadArtCount();
    }

    private void loadFiltersList() {

        String userUniqueId = application.getSharedPreferences(Constants.TAG,0).getString(Constants.USER_UNIQUE_ID,"");
        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.GET_FILTERS_LIST_OPERATION);
        request.setUserUniqueId(userUniqueId);
        request.setKeywordFilter(keyword);
        request.setMakerFilter(makerFilter);
        request.setCenturyFilter(centuryFilter);
        request.setKeywordType(keywordType);

        Call<ServerResponse> response = NetworkQuery.getInstance().create(Constants.BASE_URL, request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {

                if (response.isSuccessful()) {
                    ServerResponse resp = response.body();
                    if(resp.getResult().equals(Constants.SUCCESS)) {

                        updateMakersList(resp.getListMakerFilters());
                        updateCenturyList(resp.getListCenturyFilters());
                        updateKeywordListList(resp.getListKeywordFilters());
                    } else { }
                } else { }
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) { }
        });
    }

    private void loadArtCount() {

        String userUniqueId = application.getSharedPreferences(Constants.TAG,0).getString(Constants.USER_UNIQUE_ID,"");
        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.GET_ART_COUNT_FILTER_FRAGMENT_OPERATION);
        request.setUserUniqueId(userUniqueId);
        request.setKeywordFilter(keyword);
        request.setMakerFilter(makerFilter);
        request.setCenturyFilter(centuryFilter);
        request.setKeywordType(keywordType);

        Call<ServerResponse> response = NetworkQuery.getInstance().create(Constants.BASE_URL, request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {

                if (response.isSuccessful()) {
                    ServerResponse resp = response.body();
                    if(resp.getResult().equals(Constants.SUCCESS)) {

                        updateArtCount(resp.getArtCount());
                    } else { }
                } else { }
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) { }
        });
    }

}

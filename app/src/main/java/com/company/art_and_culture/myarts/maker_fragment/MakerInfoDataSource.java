package com.company.art_and_culture.myarts.maker_fragment;

import android.app.Application;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.bottom_menu.favorites.Artists.ArtistsRepository;
import com.company.art_and_culture.myarts.network.NetworkQuery;
import com.company.art_and_culture.myarts.pojo.FilterObject;
import com.company.art_and_culture.myarts.pojo.Maker;
import com.company.art_and_culture.myarts.pojo.ServerRequest;
import com.company.art_and_culture.myarts.pojo.ServerResponse;

import java.util.ArrayList;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MakerInfoDataSource {

    private Application application;
    private Maker artMaker;
    private FilterObject filterObject;
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<Boolean> isMakerLiked = new MutableLiveData<>();
    private MutableLiveData<Maker> maker = new MutableLiveData<>();
    private MutableLiveData<ArrayList<FilterObject>> keywords = new MutableLiveData<>();
    private MutableLiveData<Integer> artCountMaker = new MutableLiveData<>();

    public MakerInfoDataSource(Application application, Maker artMaker, FilterObject filterObject) {
        this.artMaker = artMaker;
        this.application = application;
        this.filterObject = filterObject;
        updateIsLoadingState(true);
        loadMakerInfo();
        //loadMakerKeywords();
        //loadArtsCountMaker(filterObject);
    }

    public void refresh() {
        updateIsLoadingState(true);
        loadMakerInfo();
        //loadMakerKeywords();
        //loadArtsCountMaker(filterObject);
        loadArtsCountMakerByKeyword(filterObject);
    }
/*
    public void setArtMaker(Maker artMaker) {
        this.artMaker = artMaker;
        updateIsLoadingState(true);
        loadMakerInfo();
        //loadMakerKeywords();
        //loadArtsCountMaker(filterObject);
    }
*/
    public void makerTagClick(FilterObject filterObject) {
        this.filterObject = filterObject;
        loadArtsCountMakerByKeyword(filterObject);
    }

    private void loadArtsCountMakerByKeyword(FilterObject filterObject) {

        ServerRequest request = new ServerRequest();
        request.setMakerFilter(artMaker.getArtMaker());
        request.setKeywordFilter(filterObject.getText());
        request.setKeywordType(filterObject.getType());
        request.setOperation(Constants.GET_ARTS_COUNT_MAKER_BY_KEYWORD);

        Call<ServerResponse> response = NetworkQuery.getInstance().create(Constants.BASE_URL, request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {

                if (response.isSuccessful()) {
                    ServerResponse resp = response.body();
                    if(resp.getResult().equals(Constants.SUCCESS)) {

                        updateArtCountMaker(resp.getArtCount());
                    } else { }
                } else { }
            }
            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) { }
        });
    }

    private void loadMakerInfo() {

        String userUniqueId = application.getSharedPreferences(Constants.TAG,0).getString(Constants.USER_UNIQUE_ID,"");
        ServerRequest request = new ServerRequest();
        request.setUserUniqueId(userUniqueId);
        request.setMaker(artMaker);
        request.setOperation(Constants.GET_MAKER_OBJECT);

        Call<ServerResponse> response = NetworkQuery.getInstance().create(Constants.BASE_URL, request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {

                updateIsLoadingState(false);

                if (response.isSuccessful()) {
                    ServerResponse resp = response.body();
                    if(resp.getResult().equals(Constants.SUCCESS)) {

                        updateArtMaker(resp.getArtMaker());
                        updateMakerKeywords(resp.getListKeywordFilters());
                    } else { }
                } else { }
            }
            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                updateIsLoadingState(false);
            }
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
                    if(resp.getResult().equals(Constants.SUCCESS)) {

                        updateIsMakerLiked(resp.getArtMaker().isLiked());

                        ArtistsRepository artistsRepository = ArtistsRepository.getInstance(application);
                        artistsRepository.refresh();
                    } else { }
                } else { }
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) { }
        });
    }

    private void updateIsLoadingState(Boolean state) {
        isLoading.postValue(state);
    }

    private void updateIsMakerLiked(boolean liked) {
        isMakerLiked.postValue(liked);
    }

    private void updateArtMaker(Maker artMaker) {
        maker.postValue(artMaker);
    }

    private void updateMakerKeywords(ArrayList<FilterObject> listKeywordFilters) {
        keywords.postValue(listKeywordFilters);
    }

    private void updateArtCountMaker(int artCount) {
        artCountMaker.postValue(artCount);
    }

    public MutableLiveData<Maker> getMaker() {
        return maker;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getIsMakerLiked() {
        return isMakerLiked;
    }

    public LiveData<ArrayList<FilterObject>> getMakerKeywords() {
        return keywords;
    }

    public LiveData<Integer> getArtCountMaker() {
        return artCountMaker;
    }

}

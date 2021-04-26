package com.company.art_and_culture.myarts;

import android.widget.Toast;

import com.company.art_and_culture.myarts.network.NetworkQuery;
import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.pojo.Folder;
import com.company.art_and_culture.myarts.pojo.ServerRequest;
import com.company.art_and_culture.myarts.pojo.ServerResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivityDataSource {

    private MainActivity activity;

    public MainActivityDataSource(MainActivity activity) {
        this.activity = activity;
    }

    public void getFoldersList(String userUniqueId) {
        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.GET_FOLDERS_LIST_OPERATION);
        request.setUserUniqueId(userUniqueId);

        Call<ServerResponse> response = NetworkQuery.getInstance().create(Constants.BASE_URL, request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                if (response.isSuccessful()) {
                    ServerResponse resp = response.body();
                    if(resp.getResult().equals(Constants.SUCCESS)) {
                        activity.setListFolders(resp.getListFolders());
                    } else { }
                } else { }
            }
            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) { }
        });
    }

    public void saveArtToFolder(Art artToSaveInFolder, Folder folder, String userUniqueId) {
        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.SAVE_ART_TO_FOLDER_OPERATION);
        request.setArt(artToSaveInFolder);
        request.setFolder(folder);
        request.setUserUniqueId(userUniqueId);

        Call<ServerResponse> response = NetworkQuery.getInstance().create(Constants.BASE_URL, request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                if (response.isSuccessful()) {
                    ServerResponse resp = response.body();
                    if(resp.getResult().equals(Constants.SUCCESS)) {
                        activity.updateFolders(true);
                        Toast.makeText(activity, resp.getMessage(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(activity, resp.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else { }
            }
            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) { }
        });
    }

    public void createFolder(Folder folder) {
        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.CREATE_FOLDER_OPERATION);
        request.setFolder(folder);
        Call<ServerResponse> response = NetworkQuery.getInstance().create(Constants.BASE_URL, request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                if (response.isSuccessful()) {
                    ServerResponse resp = response.body();
                    if(resp.getResult().equals(Constants.SUCCESS)) {
                        activity.updateFolders(true);
                        activity.hideSaveToFolderView();
                        activity.hideCreateNewFolder();
                        Toast.makeText(activity, resp.getMessage(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(activity, resp.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else { }
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) { }
        });
    }

    public void getInitialSuggests(String userUniqueId) {

        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.GET_INITIAL_SUGGEST_OPERATION);
        request.setUserUniqueId(userUniqueId);

        Call<ServerResponse> response = NetworkQuery.getInstance().create(Constants.BASE_URL, request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                if (response.body() != null) {
                    if(response.body().getResult().equals(Constants.SUCCESS)) {
                        activity.postServerResponse(response.body());
                    }
                }
            }
            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) { }
        });
    }

    public void getSuggests(String suggestQuery, String userUniqueId) {

        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.GET_SUGGEST_OPERATION);
        request.setSuggestQuery(suggestQuery);
        request.setUserUniqueId(userUniqueId);

        Call<ServerResponse> response = NetworkQuery.getInstance().create(Constants.BASE_URL, request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                activity.postServerResponse(response.body());
            }
            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                activity.postServerResponse(null);
            }
        });
    }

    public void deleteSuggest(String suggestStr, String userUniqueId) {

        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.DELETE_SUGGEST_QUERY_OPERATION);
        request.setSuggestQuery(suggestStr);
        request.setUserUniqueId(userUniqueId);

        Call<ServerResponse> response = NetworkQuery.getInstance().create(Constants.BASE_URL, request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                if (response.body() != null) {
                    activity.postServerResponse(response.body());
                }
            }
            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) { }
        });

    }

}

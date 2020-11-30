package com.company.art_and_culture.myarts.bottom_menu.favorites.Folders;

import android.app.Application;

import com.company.art_and_culture.myarts.pojo.Folder;

import java.util.ArrayList;

import androidx.lifecycle.LiveData;

public class FoldersRepository {

    private static FoldersRepository instance;
    private FoldersDataSource foldersDataSource;

    public static FoldersRepository getInstance(Application application){

        if(instance == null){
            instance = new FoldersRepository(application);
        }
        return instance;
    }

    public FoldersRepository(Application application) {

        foldersDataSource = new FoldersDataSource(application);

    }

    public LiveData<Boolean> getIsLoading() {
        return foldersDataSource.getIsLoading();
    }

    public LiveData<Boolean> getIsListEmpty() {
        return foldersDataSource.getIsListEmpty();
    }

    public boolean refresh() {
        boolean isConnected = foldersDataSource.isNetworkAvailable();
        if (isConnected){
            foldersDataSource.refresh();
        }
        return isConnected;
    }

    public LiveData<ArrayList<Folder>> getFoldersList() {
        return foldersDataSource.getFoldersList();
    }
}

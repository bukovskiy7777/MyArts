package com.company.art_and_culture.myarts.show_folder_fragment;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.pojo.Folder;

import java.util.ArrayList;

public class ShowFolderRepository {

    private static ShowFolderRepository instance;
    private ShowFolderDataSource showFolderDataSource;

    public static ShowFolderRepository getInstance(Application application, Folder folder){

        if(instance == null){
            instance = new ShowFolderRepository(application, folder);
        }
        return instance;
    }

    public ShowFolderRepository(Application application, Folder folder) {

        showFolderDataSource = new ShowFolderDataSource(application, folder);
    }

    public LiveData<ArrayList<Art>> getArtsList(){
        return showFolderDataSource.getArtList();
    }

    public LiveData<Boolean> getIsLoading() {
        return showFolderDataSource.getIsLoading();
    }

    public LiveData<Boolean> getIsListEmpty() {
        return showFolderDataSource.getIsListEmpty();
    }

    public void detach() {
        instance = null;
    }

    public boolean refresh() {
        boolean isConnected = showFolderDataSource.isNetworkAvailable();
        if (isConnected){
            showFolderDataSource.refresh();
        }
        return isConnected;
    }

    public void deleteFolder(Folder currentFolder) {
        showFolderDataSource.deleteFolder(currentFolder);
    }

    public void setActivity(MainActivity activity) {
        showFolderDataSource.setActivity(activity);
    }
}

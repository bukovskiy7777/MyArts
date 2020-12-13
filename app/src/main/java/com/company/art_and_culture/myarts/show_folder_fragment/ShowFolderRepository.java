package com.company.art_and_culture.myarts.show_folder_fragment;

import android.app.Application;

import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.pojo.Folder;

import java.util.ArrayList;

import androidx.lifecycle.LiveData;

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

    public void refresh() {
        instance = null;
    }

}

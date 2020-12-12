package com.company.art_and_culture.myarts.create_folder_fragment;

import android.app.Application;

import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.pojo.Folder;

import java.util.ArrayList;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

public class CreateFolderRepository {

    private static CreateFolderRepository instance;
    private CreateFolderDataSource createFolderDataSource;

    public static CreateFolderRepository getInstance(Application application){

        if(instance == null){
            instance = new CreateFolderRepository(application);
        }
        return instance;
    }

    public CreateFolderRepository(Application application) {

        createFolderDataSource = new CreateFolderDataSource(application);
    }

    public LiveData<ArrayList<Art>> getArtsList(){
        return createFolderDataSource.getArtList();
    }

    public LiveData<Boolean> getIsLoading() {
        return createFolderDataSource.getIsLoading();
    }

    public LiveData<Boolean> getIsListEmpty() {
        return createFolderDataSource.getIsListEmpty();
    }

    public boolean refresh() {
        boolean isConnected = createFolderDataSource.isNetworkAvailable();
        if (isConnected){
            createFolderDataSource.refresh();
        }
        return isConnected;
    }

    public void setActivity(MainActivity activity) {
        activity.getArt().observe(activity, new Observer<Art>() {
            @Override
            public void onChanged(Art art) {
                refresh();
            }
        });
    }

    public void createFolder(Folder folder, MainActivity activity) {
        createFolderDataSource.createFolder(folder, activity);
    }
}

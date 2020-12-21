package com.company.art_and_culture.myarts.create_folder_fragment;

import android.app.Application;

import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.pojo.Folder;

import java.util.ArrayList;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

public class CreateFolderRepository {

    private CreateFolderDataSource createFolderDataSource;

    public CreateFolderRepository(Application application, Folder folderForEdit) {

        createFolderDataSource = new CreateFolderDataSource(application, folderForEdit);
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

    public void setActivity(MainActivity activity) {
        activity.getArt().observe(activity, new Observer<Art>() {
            @Override
            public void onChanged(Art art) {
                createFolderDataSource.refresh(); //refresh();
            }
        });
    }

    public void createFolder(Folder folder, MainActivity activity) {
        createFolderDataSource.createFolder(folder, activity);
    }
}

package com.company.art_and_culture.myarts.create_folder_fragment;

import android.app.Application;

import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.pojo.Folder;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class CreateFolderViewModel extends AndroidViewModel {

    private android.content.res.Resources res;
    private Application application;
    private CreateFolderRepository createFolderRepository;
    private LiveData<ArrayList<Art>> artsList;
    private LiveData<Boolean> isLoading;
    private LiveData<Boolean> isListEmpty;

    public CreateFolderViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
    }

    public void getFolderForEdit(Folder folderForEdit) {
        createFolderRepository = new CreateFolderRepository(application, folderForEdit); //.getInstance
        artsList = createFolderRepository.getArtsList();
        isLoading = createFolderRepository.getIsLoading();
        isListEmpty = createFolderRepository.getIsListEmpty();
    }

    public LiveData<ArrayList<Art>> getArtsList(){
        return artsList;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getIsListEmpty() {
        return isListEmpty;
    }

    public void setActivity(MainActivity activity) {
        createFolderRepository.setActivity(activity);
    }

    public void createFolder(Folder folder, MainActivity activity) {
        createFolderRepository.createFolder(folder, activity);
    }

}

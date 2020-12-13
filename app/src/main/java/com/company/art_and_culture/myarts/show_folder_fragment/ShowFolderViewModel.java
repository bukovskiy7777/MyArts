package com.company.art_and_culture.myarts.show_folder_fragment;

import android.app.Application;

import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.pojo.Folder;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class ShowFolderViewModel extends AndroidViewModel {

    private android.content.res.Resources res;
    private Application application;
    private ShowFolderRepository showFolderRepository;
    private LiveData<ArrayList<Art>> artsList;
    private LiveData<Boolean> isLoading;
    private LiveData<Boolean> isListEmpty;

    public ShowFolderViewModel(@NonNull Application application) {
        super(application);

        this.application = application;

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

    public void setFolder(Folder currentFolder) {
        showFolderRepository = ShowFolderRepository.getInstance(application, currentFolder);
        artsList = showFolderRepository.getArtsList();
        isLoading = showFolderRepository.getIsLoading();
        isListEmpty = showFolderRepository.getIsListEmpty();
    }

    public void refresh() {
        showFolderRepository.refresh();
    }
}

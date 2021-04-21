package com.company.art_and_culture.myarts.show_folder_fragment;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.pojo.Folder;

import java.util.ArrayList;

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

    public void detach() {
        showFolderRepository.detach();
    }

    public void deleteFolder(Folder currentFolder) {
        showFolderRepository.deleteFolder(currentFolder);
    }

    public void setActivity(MainActivity activity) {
        showFolderRepository.setActivity(activity);
    }

    public boolean refresh() {
        return showFolderRepository.refresh();
    }
}

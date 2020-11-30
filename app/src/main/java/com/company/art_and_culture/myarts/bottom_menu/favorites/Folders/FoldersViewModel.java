package com.company.art_and_culture.myarts.bottom_menu.favorites.Folders;

import android.app.Application;

import com.company.art_and_culture.myarts.pojo.Folder;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class FoldersViewModel extends AndroidViewModel {

    private LiveData<ArrayList<Folder>> foldersList;
    private LiveData<Boolean> isLoading;
    private LiveData<Boolean> isListEmpty;
    private FoldersRepository foldersRepository;

    public FoldersViewModel(@NonNull Application application) {
        super(application);

        foldersRepository = FoldersRepository.getInstance(application);
        foldersList = foldersRepository.getFoldersList();
        isLoading = foldersRepository.getIsLoading();
        isListEmpty = foldersRepository.getIsListEmpty();
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getIsListEmpty() {
        return isListEmpty;
    }

    public boolean refresh() {
        return foldersRepository.refresh();
    }

    public LiveData<ArrayList<Folder>> getFoldersList() {
        return foldersList;
    }


}

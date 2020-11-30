package com.company.art_and_culture.myarts.tags_fragment;

import android.app.Application;

import com.company.art_and_culture.myarts.pojo.Attribute;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

public class TagsViewModel extends AndroidViewModel {

    private android.content.res.Resources res;
    private Application application;
    private TagsRepository tagsRepository;
    private LiveData<PagedList<Attribute>> tagsList;
    private LiveData<Boolean> isLoading;

    public TagsViewModel(@NonNull Application application) {
        super(application);

        this.application = application;
    }

    public LiveData<PagedList<Attribute>> getTagsList(){
        return tagsList;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void setFilter(String filter) {

        tagsRepository = TagsRepository.getInstance(application, filter).setFilter(filter);
        tagsList = tagsRepository.getTagsList();
        isLoading = tagsRepository.getIsLoading();
    }
}

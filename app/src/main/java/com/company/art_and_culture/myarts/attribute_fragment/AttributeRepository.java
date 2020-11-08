package com.company.art_and_culture.myarts.attribute_fragment;

import android.app.Application;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.pojo.Attribute;

import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

public class AttributeRepository {

    private static AttributeRepository instance;
    private LiveData<PagedList<Attribute>> attributeList;
    private LiveData<Boolean> isLoading;
    private AttributeDataSourceFactory attributeDataSourceFactory;
    private String attributeType = "";
    private Application application;
    private AttributeDataSource attributeDataSource;

    public static AttributeRepository getInstance(Application application, String attributeType){

        if(instance == null){
            instance = new AttributeRepository(application, attributeType);
        }
        return instance;
    }

    public AttributeRepository(Application application, String attributeType) {

        this.application = application;
        this.attributeType = attributeType;

        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(Constants.PAGE_SIZE)
                .setInitialLoadSizeHint(Constants.PAGE_SIZE)
                .build();

        attributeDataSourceFactory = new AttributeDataSourceFactory(application, attributeType);
        attributeList = new LivePagedListBuilder<>(attributeDataSourceFactory, config).build();

        attributeDataSource = (AttributeDataSource) attributeDataSourceFactory.create();//if remove this line artLike will not working after refresh
        isLoading = attributeDataSource.getIsLoading();
    }


    public LiveData<PagedList<Attribute>> getAttributeList(){
        return attributeList;
    }

    public AttributeRepository setAttributeType(String attributeType) {

        attributeDataSourceFactory.setAttributeType(attributeType);
        instance = new AttributeRepository(application, attributeType);

        return instance;
    }

    public String getAttributeType() {
        return attributeType;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
}

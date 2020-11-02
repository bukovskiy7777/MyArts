package com.company.art_and_culture.myarts.attribute_fragment;

import android.app.Application;

import com.company.art_and_culture.myarts.pojo.Attribute;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.PageKeyedDataSource;

class AttributeDataSourceFactory extends DataSource.Factory<Integer, Attribute> {

    private Application application;
    private String attributeType = "";
    private AttributeDataSource attributeDataSource;
    private MutableLiveData<PageKeyedDataSource<Integer, Attribute>> attributeDataSourceMutableLiveData = new MutableLiveData<>();

    public AttributeDataSourceFactory(Application application, String attributeType) {
        this.attributeType = attributeType;
        this.application = application;
        attributeDataSource = new AttributeDataSource(application, attributeType);
    }

    @NonNull
    @Override
    public DataSource<Integer, Attribute> create() {
        if (attributeDataSource.isInvalid()) attributeDataSource = new AttributeDataSource(application, attributeType);
        attributeDataSourceMutableLiveData.postValue(attributeDataSource);

        return attributeDataSource;
    }

    public void setAttributeType(String attributeType) {
        this.attributeType = attributeType;
        attributeDataSource.refresh();
    }

}

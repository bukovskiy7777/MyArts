package com.company.art_and_culture.myarts.attribute_fragment;

import android.app.Application;
import android.util.Log;

import com.company.art_and_culture.myarts.filter_maker_fragment.FilterMakerRepository;
import com.company.art_and_culture.myarts.pojo.Attribute;
import com.company.art_and_culture.myarts.pojo.Maker;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

public class AttributeViewModel extends AndroidViewModel {

    private android.content.res.Resources res;
    private Application application;
    private AttributeRepository attributeRepository;
    private LiveData<PagedList<Attribute>> attributeList;

    public AttributeViewModel(@NonNull Application application) {
        super(application);

        this.application = application;
    }

    public LiveData<PagedList<Attribute>> getAttributeList(){
        return attributeList;
    }

    public void setAttributeType(String attributeType) {

        attributeRepository = AttributeRepository.getInstance(application, attributeType);
        if (!attributeRepository.getAttributeType().equals(attributeType)) {
            attributeRepository = attributeRepository.setAttributeType(attributeType);
        }
        attributeList = attributeRepository.getAttributeList();

    }

}

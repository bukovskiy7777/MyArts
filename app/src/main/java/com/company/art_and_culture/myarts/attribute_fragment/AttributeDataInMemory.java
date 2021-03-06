package com.company.art_and_culture.myarts.attribute_fragment;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.pojo.Attribute;
import com.company.art_and_culture.myarts.pojo.Maker;

import java.util.ArrayList;
import java.util.List;

class AttributeDataInMemory {

    private static AttributeDataInMemory instance;
    private ArrayList<Attribute> listAttribute = new ArrayList<>();

    public static AttributeDataInMemory getInstance(){
        if(instance == null){
            instance = new AttributeDataInMemory();
        }
        return instance;
    }


    public void refresh() {
        instance = null;
    }

    public void addData(ArrayList<Attribute> data) {

        listAttribute.addAll(data);

    }

    public List<Attribute> getInitialData() {

        ArrayList<Attribute> outputData = new ArrayList<>();

        int lastPosition;
        if (listAttribute.size() >= Constants.PAGE_SIZE) {
            lastPosition = Constants.PAGE_SIZE;
        } else if (listAttribute.size() > 0){
            lastPosition = listAttribute.size();
        } else {
            lastPosition = 0;
        }

        for(int i = 0; i < lastPosition; i++) {
            outputData.add(listAttribute.get(i));
        }
        return outputData;
    }

    public List<Attribute> getAfterData(Integer key) {

        int startPosition = (key - 1) * Constants.PAGE_SIZE;
        int lastPosition;
        if (listAttribute.size() >= key * Constants.PAGE_SIZE) {
            lastPosition = key * Constants.PAGE_SIZE;
        } else if (listAttribute.size() > (key - 1) * Constants.PAGE_SIZE){
            lastPosition = listAttribute.size();
        } else {
            lastPosition = (key - 1) * Constants.PAGE_SIZE;
        }

        ArrayList<Attribute> outputData = new ArrayList<>();

        for(int i = startPosition; i < lastPosition; i++) {
            outputData.add(listAttribute.get(i));
        }
        return outputData;
    }
}

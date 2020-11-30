package com.company.art_and_culture.myarts.tags_fragment;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.pojo.Attribute;

import java.util.ArrayList;
import java.util.List;

class TagsDataInMemory {

    private static TagsDataInMemory instance;
    private ArrayList<Attribute> listTags = new ArrayList<>();
    private String filter;

    public static TagsDataInMemory getInstance(){
        if(instance == null){
            instance = new TagsDataInMemory();
        }
        return instance;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public void refresh() {
        instance = null;
    }

    public void addData(ArrayList<Attribute> data, String filter) {

        if (filter.equals(this.filter)) listTags.addAll(data);

    }

    public List<Attribute> getInitialData() {

        ArrayList<Attribute> outputData = new ArrayList<>();

        int lastPosition;
        if (listTags.size() >= Constants.PAGE_SIZE) {
            lastPosition = Constants.PAGE_SIZE;
        } else if (listTags.size() > 0){
            lastPosition = listTags.size();
        } else {
            lastPosition = 0;
        }

        for(int i = 0; i < lastPosition; i++) {
            outputData.add(listTags.get(i));
        }
        return outputData;
    }

    public List<Attribute> getAfterData(Integer key) {

        int startPosition = (key - 1) * Constants.PAGE_SIZE;
        int lastPosition;
        if (listTags.size() >= key * Constants.PAGE_SIZE) {
            lastPosition = key * Constants.PAGE_SIZE;
        } else if (listTags.size() > (key - 1) * Constants.PAGE_SIZE){
            lastPosition = listTags.size();
        } else {
            lastPosition = (key - 1) * Constants.PAGE_SIZE;
        }

        ArrayList<Attribute> outputData = new ArrayList<>();

        for(int i = startPosition; i < lastPosition; i++) {
            outputData.add(listTags.get(i));
        }
        return outputData;
    }
}

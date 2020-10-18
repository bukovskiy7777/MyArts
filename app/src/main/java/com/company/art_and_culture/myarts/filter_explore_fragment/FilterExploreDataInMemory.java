package com.company.art_and_culture.myarts.filter_explore_fragment;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.pojo.ExploreObject;

import java.util.ArrayList;
import java.util.List;

class FilterExploreDataInMemory {

    private static FilterExploreDataInMemory instance;
    private ArrayList<ExploreObject> listExplore = new ArrayList<>();

    public static FilterExploreDataInMemory getInstance(){
        if(instance == null){
            instance = new FilterExploreDataInMemory();
        }
        return instance;
    }

    public void refresh() {
        instance = null;
    }

    public void addData(ArrayList<ExploreObject> data) {
        listExplore.addAll(data);
    }

    public List<ExploreObject> getInitialData() {

        ArrayList<ExploreObject> outputData = new ArrayList<>();

        int lastPosition;
        if (listExplore.size() >= Constants.PAGE_SIZE_SMALL) {
            lastPosition = Constants.PAGE_SIZE_SMALL;
        } else if (listExplore.size() > 0){
            lastPosition = listExplore.size();
        } else {
            lastPosition = 0;
        }

        for(int i = 0; i < lastPosition; i++) {
            outputData.add(listExplore.get(i));
        }
        return outputData;
    }

    public List<ExploreObject> getAfterData(Integer key) {

        int startPosition = (key - 1) * Constants.PAGE_SIZE_SMALL;
        int lastPosition;
        if (listExplore.size() >= key * Constants.PAGE_SIZE_SMALL) {
            lastPosition = key * Constants.PAGE_SIZE_SMALL;
        } else if (listExplore.size() > (key - 1) * Constants.PAGE_SIZE_SMALL){
            lastPosition = listExplore.size();
        } else {
            lastPosition = (key - 1) * Constants.PAGE_SIZE_SMALL;
        }

        ArrayList<ExploreObject> outputData = new ArrayList<>();

        for(int i = startPosition; i < lastPosition; i++) {
            outputData.add(listExplore.get(i));
        }
        return outputData;
    }
}

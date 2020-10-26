package com.company.art_and_culture.myarts.filter_maker_fragment;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.pojo.Maker;

import java.util.ArrayList;
import java.util.List;

class FilterMakerDataInMemory {

    private static FilterMakerDataInMemory instance;
    private ArrayList<Maker> listMakers = new ArrayList<>();
    private String filter;

    public static FilterMakerDataInMemory getInstance(){
        if(instance == null){
            instance = new FilterMakerDataInMemory();
        }
        return instance;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public void refresh() {
        instance = null;
    }

    public void addData(ArrayList<Maker> data) {
        if (data.get(0).getArtMaker().startsWith(filter)) listMakers.addAll(data);
    }

    public List<Maker> getInitialData() {

        ArrayList<Maker> outputData = new ArrayList<>();

        int lastPosition;
        if (listMakers.size() >= Constants.PAGE_SIZE_SMALL) {
            lastPosition = Constants.PAGE_SIZE_SMALL;
        } else if (listMakers.size() > 0){
            lastPosition = listMakers.size();
        } else {
            lastPosition = 0;
        }

        for(int i = 0; i < lastPosition; i++) {
            outputData.add(listMakers.get(i));
        }
        return outputData;
    }

    public List<Maker> getAfterData(Integer key) {

        int startPosition = (key - 1) * Constants.PAGE_SIZE_SMALL;
        int lastPosition;
        if (listMakers.size() >= key * Constants.PAGE_SIZE_SMALL) {
            lastPosition = key * Constants.PAGE_SIZE_SMALL;
        } else if (listMakers.size() > (key - 1) * Constants.PAGE_SIZE_SMALL){
            lastPosition = listMakers.size();
        } else {
            lastPosition = (key - 1) * Constants.PAGE_SIZE_SMALL;
        }

        ArrayList<Maker> outputData = new ArrayList<>();

        for(int i = startPosition; i < lastPosition; i++) {
            outputData.add(listMakers.get(i));
        }
        return outputData;
    }
}

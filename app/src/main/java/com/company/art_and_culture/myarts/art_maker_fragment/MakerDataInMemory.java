package com.company.art_and_culture.myarts.art_maker_fragment;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.pojo.Art;

import java.util.ArrayList;

import androidx.lifecycle.Observer;

public class MakerDataInMemory {

    private static MakerDataInMemory instance;
    private ArrayList<Art> listArts = new ArrayList<>();

    public static MakerDataInMemory getInstance(){
        if(instance == null){
            instance = new MakerDataInMemory();
        }
        return instance;
    }

    public void refresh() {
        instance = null;
    }

    public void addData(ArrayList<Art> data) {
        listArts.addAll(data);
    }

    public ArrayList<Art> getInitialData() {

        ArrayList<Art> outputData = new ArrayList<>();

        int lastPosition;
        if (listArts.size() >= Constants.PAGE_SIZE) {
            lastPosition = Constants.PAGE_SIZE;
        } else if (listArts.size() > 0){
            lastPosition = listArts.size();
        } else {
            lastPosition = 0;
        }

        for(int i = 0; i < lastPosition; i++) {
            outputData.add(listArts.get(i));
        }
        return outputData;
    }

    public ArrayList<Art> getAfterData(Integer key) {

        int startPosition = (key - 1) * Constants.PAGE_SIZE;
        int lastPosition;
        if (listArts.size() >= key * Constants.PAGE_SIZE) {
            lastPosition = key * Constants.PAGE_SIZE;
        } else if (listArts.size() > (key - 1) * Constants.PAGE_SIZE){
            lastPosition = listArts.size();
        } else {
            lastPosition = (key - 1) * Constants.PAGE_SIZE;
        }

        ArrayList<Art> outputData = new ArrayList<>();

        for(int i = startPosition; i < lastPosition; i++) {
            outputData.add(listArts.get(i));
        }
        return outputData;
    }

    public void updateSingleItem(Art art) {

        for (int i = 0; i < listArts.size(); i++) {
            if (listArts.get(i).getArtId().equals(art.getArtId()) && listArts.get(i).getArtProvider().equals(art.getArtProvider())) {
                listArts.remove(i);
                listArts.add(i, art);
            }
        }

    }

    public Art getSingleItem(int position) {
        return listArts.get(position);
    }

    public ArrayList<Art> getAllData() {
        return listArts;
    }

    public void setArtObserver(MainActivity activity) {
        activity.getArt().observe(activity, new Observer<Art>() {
            @Override
            public void onChanged(Art art) {
                updateSingleItem(art);
            }
        });
    }
}

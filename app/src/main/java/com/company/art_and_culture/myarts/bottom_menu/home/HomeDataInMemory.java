package com.company.art_and_culture.myarts.bottom_menu.home;

import androidx.lifecycle.Observer;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.pojo.Art;

import java.util.ArrayList;

public class HomeDataInMemory {

    private static HomeDataInMemory instance;
    private ArrayList<Art> listArts = new ArrayList<>();

    public static HomeDataInMemory getInstance(){
        if(instance == null){
            instance = new HomeDataInMemory();
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

        for(int i = 0; i < Constants.PAGE_SIZE; i++) {
            outputData.add(listArts.get(i));
        }
        return outputData;
    }

    public ArrayList<Art> getAfterData(Integer key) {

        int startPosition = (key - 1) * Constants.PAGE_SIZE;
        int lastPosition = key * Constants.PAGE_SIZE;
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
        if(listArts.size() - 1 >= position) return listArts.get(position);
        else return null;
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

package com.company.art_and_culture.myarts.arts_show_fragment;

import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.pojo.Art;

import java.util.ArrayList;

import androidx.lifecycle.Observer;

public class ArtShowDataInMemory {

    private static ArtShowDataInMemory instance;
    private ArrayList<Art> listArts = new ArrayList<>();

    public static ArtShowDataInMemory getInstance(){
        if(instance == null){
            instance = new ArtShowDataInMemory();
        }
        return instance;
    }

    public void finish() {
        instance = null;
    }

    public void addData(ArrayList<Art> data) {
        listArts.addAll(data);
    }

    public void updateSingleItem(Art art) {

        for (int i = 0; i < listArts.size(); i++) {
            if (listArts.get(i).getArtId().equals(art.getArtId()) && listArts.get(i).getArtProvider().equals(art.getArtProvider())) {
                listArts.remove(i);
                listArts.add(i, art);
            }
        }

    }

    public Art getSingleItem(int position){
        if (listArts.size() >= position) {
            return listArts.get(position);
        } else {
            return null;
        }

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

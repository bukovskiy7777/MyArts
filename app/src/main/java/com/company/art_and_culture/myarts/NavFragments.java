package com.company.art_and_culture.myarts;

import android.os.Handler;

import com.company.art_and_culture.myarts.art_maker_fragment.MakerFragment;
import com.company.art_and_culture.myarts.art_medium_fragment.MediumFragment;
import com.company.art_and_culture.myarts.art_search_fragment.SearchFragment;
import com.company.art_and_culture.myarts.arts_show_fragment.ArtShowFragment;
import com.company.art_and_culture.myarts.attribute_fragment.AttributeFragment;
import com.company.art_and_culture.myarts.filter_maker_fragment.FilterMakerFragment;
import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.pojo.Attribute;
import com.company.art_and_culture.myarts.pojo.Maker;
import com.company.art_and_culture.myarts.ui.explore.ExploreFragment;
import com.company.art_and_culture.myarts.ui.favorites.Artists.ArtistsFragment;
import com.company.art_and_culture.myarts.ui.favorites.Favorites.FavoritesFragment;
import com.company.art_and_culture.myarts.ui.home.HomeFragment;

import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class NavFragments implements
        HomeFragment.HomeEventListener, FavoritesFragment.FavoritesEventListener, SearchFragment.SearchEventListener,
        MakerFragment.MakerEventListener, ExploreFragment.ExploreEventListener, MediumFragment.MediumEventListener,
        ArtistsFragment.ArtistsEventListener, ArtShowFragment.ArtShowEventListener, FilterMakerFragment.FilterMakerEventListener,
        AttributeFragment.AttributeEventListener {

    private MainActivity activity;
    private ArtShowFragment artShowFragment;
    private MakerFragment makerFragment;
    private MediumFragment mediumFragment;
    private FilterMakerFragment filterMakerFragment;
    private AttributeFragment attributeFragment;
    private SearchFragment searchFragment;

    private Collection<Art> listArtsForArtShowFragment;
    private int clickPositionForArtShowFragment;

    private Maker makerForMakerFragment;

    private String artQueryForMediumFragment, queryTypeForMediumFragment;

    private String typeForAttributeFragment;

    private int favoritesPosition = 0;
    private FavoritesFragment.Sort sort_type = FavoritesFragment.Sort.by_date;

    private int filterMakerPosition = 0, dateMakerPosition = 0;

    private int homePosition = 0;



    public NavFragments(MainActivity mainActivity) {
        activity = mainActivity;
    }

    private void showArtFragment() {
        if (artShowFragment == null) {
            artShowFragment = new ArtShowFragment();
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right);
            //fragmentTransaction.addToBackStack("artShowFragment");
            fragmentTransaction.add(R.id.frame_container_common, artShowFragment, "artShowFragment").commit();
        }
    }

    private void showMakerFragment() {
        if (makerFragment == null) {
            makerFragment = new MakerFragment();
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.enter_from_bottom,R.anim.exit_to_bottom, R.anim.enter_from_bottom,R.anim.exit_to_bottom);
            //fragmentTransaction.addToBackStack("makerFragment");
            fragmentTransaction.add(R.id.frame_container_common, makerFragment, "makerFragment").commit();
        }
    }

    private void showMediumFragment() {
        if (mediumFragment == null) {
            mediumFragment = new MediumFragment();
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom);
            //fragmentTransaction.addToBackStack("mediumFragment");
            fragmentTransaction.add(R.id.frame_container_common, mediumFragment, "mediumFragment").commit();
        }
    }

    private void showFilterMakerFragment() {
        if (filterMakerFragment == null) {
            filterMakerFragment = new FilterMakerFragment();
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right);
            //fragmentTransaction.addToBackStack("filterMakerFragment");
            fragmentTransaction.add(R.id.frame_container_common, filterMakerFragment, "filterMakerFragment").commit();
        }
    }

    private void showAttributeFragment() {
        if (attributeFragment == null) {
            attributeFragment = new AttributeFragment();
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right);
            //fragmentTransaction.addToBackStack("attributeFragment");
            fragmentTransaction.add(R.id.frame_container_common, attributeFragment, "attributeFragment").commit();
        }
    }

    public void showSearchFragment() {
        if (searchFragment == null) {
            searchFragment = new SearchFragment();
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom);
            //fragmentTransaction.addToBackStack("searchFragment");
            fragmentTransaction.add(R.id.frame_container_search, searchFragment, "searchFragment").commit();
        }
    }

    public void finishSearchFragment() {
        if (searchFragment != null) {
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom);
            fragmentTransaction.remove(searchFragment).commit();
            searchFragment.finish();
            searchFragment = null;
        }
    }


    public boolean isFragmentsClosed() {
        if (artShowFragment != null) {
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right);
            fragmentTransaction.remove(artShowFragment).commit();
            artShowFragment = artShowFragment.finish();
            return false;

        } else if (makerFragment != null) {
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom);
            fragmentTransaction.remove(makerFragment).commit();
            makerFragment = null;
            return false;

        } else if (mediumFragment != null) {
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom);
            fragmentTransaction.remove(mediumFragment).commit();
            mediumFragment.finish();
            mediumFragment = null;
            return false;

        } else if (searchFragment != null) {
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom);
            fragmentTransaction.remove(searchFragment).commit();
            searchFragment.finish();
            searchFragment = null;
            return false;

        } else if (filterMakerFragment != null) {
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right);
            fragmentTransaction.remove(filterMakerFragment).commit();
            filterMakerFragment = null;
            return false;

        } else if (attributeFragment != null) {
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right);
            fragmentTransaction.remove(attributeFragment).commit();
            attributeFragment = null;
            return false;

        } else {
            return true;
        }

    }


    @Override
    public void makerArtClickEvent(Collection<Art> arts, int position) {
        this.listArtsForArtShowFragment = arts;
        this.clickPositionForArtShowFragment = position;
        showArtFragment();
    }



    @Override
    public void mediumArtClickEvent(Collection<Art> arts, int position) {
        this.listArtsForArtShowFragment = arts;
        this.clickPositionForArtShowFragment = position;
        showArtFragment();
    }



    @Override
    public void searchArtClickEvent(Collection<Art> arts, int position) {
        this.listArtsForArtShowFragment = arts;
        this.clickPositionForArtShowFragment = position;
        showArtFragment();
    }
    @Override
    public void searchMakerClickEvent(Maker maker) {
        this.makerForMakerFragment = maker;
        showMakerFragment();
    }
    @Override
    public void searchClassificationClickEvent(String artClassification, String queryType) {
        this.artQueryForMediumFragment = artClassification;
        this.queryTypeForMediumFragment = queryType;
        showMediumFragment();
    }



    @Override
    public void makerClickEvent(Maker maker) {
        this.makerForMakerFragment = maker;
        showMakerFragment();
    }



    @Override
    public void attributeClickEvent(Attribute attribute) {
        queryTypeForMediumFragment = attribute.getType();
        artQueryForMediumFragment = attribute.getText();
        showMediumFragment();
    }



    @Override
    public void filterMakerClickEvent(Maker maker) {
        this.makerForMakerFragment = maker;
        showMakerFragment();
    }
    @Override
    public void filterMakerOnPauseEvent(int filterPosition, int datePosition) {
        this.filterMakerPosition = filterPosition;
        this.dateMakerPosition = datePosition;
    }



    @Override
    public void exploreClick(final String type) {
        Timer timer=new Timer();
        final long DELAY = 500; // milliseconds
        final Handler handler = new Handler();
        timer.cancel();
        timer = new Timer();
        timer.schedule( new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(type.equals(Constants.ART_MAKER)) {
                            showFilterMakerFragment();
                        } else if (type.equals(Constants.ART_CULTURE)) {
                            typeForAttributeFragment = type;
                            showAttributeFragment();
                        } else if (type.equals(Constants.ART_MEDIUM)) {
                            typeForAttributeFragment = type;
                            showAttributeFragment();
                        } else if (type.equals(Constants.ART_CLASSIFICATION)) {
                            typeForAttributeFragment = type;
                            showAttributeFragment();
                        }  else if (type.equals(Constants.ART_TAG)) {
                            typeForAttributeFragment = type;
                            showAttributeFragment();
                        }
                    }
                });
            }
        }, DELAY);
    }



    @Override
    public void artistsClickEvent(Maker maker) {
        this.makerForMakerFragment = maker;
        showMakerFragment();
    }
    @Override
    public void favoritesScrollEvent(int position, FavoritesFragment.Sort sort_type) {
        this.favoritesPosition = position;
        this.sort_type = sort_type;
    }
    @Override
    public void favoritesClickEvent(Collection<Art> listArts, int position) {
        this.listArtsForArtShowFragment = listArts;
        this.clickPositionForArtShowFragment = position;
        showArtFragment();
    }



    @Override
    public void homeScrollEvent(int position) {
        this.homePosition = position;
    }
    @Override
    public void homeArtClickEvent(Collection<Art> arts, int position) {
        this.listArtsForArtShowFragment = arts;
        this.clickPositionForArtShowFragment = position;
        showArtFragment();
    }
    @Override
    public void homeMakerClickEvent(Maker maker) {
        this.makerForMakerFragment = maker;
        showMakerFragment();

    }
    @Override
    public void homeClassificationClickEvent(String artClassification, String queryType) {
        this.artQueryForMediumFragment = artClassification;
        this.queryTypeForMediumFragment = queryType;
        showMediumFragment();
    }



    public Collection<Art> getListArtsForArtShowFragment() {
        return listArtsForArtShowFragment;
    }

    public int getClickPositionForArtShowFragment() {
        return clickPositionForArtShowFragment;
    }

    public Maker getMakerForMakerFragment() {
        return makerForMakerFragment;
    }

    public String getArtQueryForMediumFragment() {
        return artQueryForMediumFragment;
    }

    public String getQueryTypeForMediumFragment() {
        return queryTypeForMediumFragment;
    }

    public String getTypeForAttributeFragment() {
        return typeForAttributeFragment;
    }

    public ArtShowFragment getArtShowFragment() {
        return artShowFragment;
    }

    public int getFavoritesPosition() {
        return favoritesPosition;
    }

    public FavoritesFragment.Sort getSort_type() {
        return sort_type;
    }

    public int getFilterMakerPosition() {
        return filterMakerPosition;
    }

    public int getDateMakerPosition() {
        return dateMakerPosition;
    }

    public int getHomePosition() {
        return homePosition;
    }


}

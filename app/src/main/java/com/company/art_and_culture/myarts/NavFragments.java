package com.company.art_and_culture.myarts;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;

import com.company.art_and_culture.myarts.art_maker_fragment.MakerFragment;
import com.company.art_and_culture.myarts.art_medium_fragment.MediumFragment;
import com.company.art_and_culture.myarts.art_search_fragment.SearchFragment;
import com.company.art_and_culture.myarts.arts_show_fragment.ArtShowFragment;
import com.company.art_and_culture.myarts.attrib_tags_fragment.TagsFragment;
import com.company.art_and_culture.myarts.attribute_fragment.AttributeFragment;
import com.company.art_and_culture.myarts.filter_maker_fragment.FilterMakerFragment;
import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.pojo.Attribute;
import com.company.art_and_culture.myarts.pojo.Maker;
import com.company.art_and_culture.myarts.ui.explore.ExploreFragment;
import com.company.art_and_culture.myarts.ui.favorites.Artists.ArtistsFragment;
import com.company.art_and_culture.myarts.ui.favorites.BlankFragment;
import com.company.art_and_culture.myarts.ui.favorites.Favorites.FavoritesFragment;
import com.company.art_and_culture.myarts.ui.home.HomeFragment;
import com.company.art_and_culture.myarts.web_view_fragment.WebViewFragment;

import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;

public class NavFragments implements
        HomeFragment.HomeEventListener, FavoritesFragment.FavoritesEventListener, SearchFragment.SearchEventListener,
        MakerFragment.MakerEventListener, ExploreFragment.ExploreEventListener, MediumFragment.MediumEventListener,
        ArtistsFragment.ArtistsEventListener, ArtShowFragment.ArtShowEventListener, FilterMakerFragment.FilterMakerEventListener,
        AttributeFragment.AttributeEventListener, TagsFragment.TagsEventListener, BlankFragment.BlankEventListener {

    private MainActivity activity;
    private NavController navController;
    private ArtShowFragment artShowFragment;
    private MakerFragment makerFragment;
    private MediumFragment mediumFragment;
    private FilterMakerFragment filterMakerFragment;
    private AttributeFragment attributeFragment;
    private SearchFragment searchFragment;
    private TagsFragment tagsFragment;
    private WebViewFragment webViewFragment;

    private Collection<Art> listArtsForArtShowFragment;
    private int clickPositionForArtShowFragment;

    private Maker makerForMakerFragment;

    private String artQueryForMediumFragment, queryTypeForMediumFragment;

    private String typeForAttributeFragment;

    private int favoritesPosition = 0;
    private FavoritesFragment.Sort sort_type = FavoritesFragment.Sort.by_date;

    private int filterMakerPosition = 0, dateMakerPosition = 0;

    private int homePosition = 0;

    private int filterPositionForTagsFragment= 0;

    private String urlForWebFragment;


    public NavFragments(MainActivity mainActivity, NavController navController) {
        activity = mainActivity;
        this.navController = navController;
        visibilityNavElements(navController);
    }

    private void visibilityNavElements(NavController navController) {
        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                if(destination.getId() == R.id.navigation_home || destination.getId() == R.id.navigation_explore ||
                        destination.getId() == R.id.navigation_favorites || destination.getId() == R.id.navigation_notifications) {
                    activity.setNavViewVisible();
                } else {
                    activity.goneNavView();
                }
            }
        });
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

    private void showTagsFragment() {
        if (tagsFragment == null) {
            tagsFragment = new TagsFragment();
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right);
            //fragmentTransaction.addToBackStack("attributeFragment");
            fragmentTransaction.add(R.id.frame_container_common, tagsFragment, "tagsFragment").commit();
        }
    }




    public void popBackStack() {
        navController.popBackStack();
    }



    public boolean isFragmentsClosed() {

        /*  if (webViewFragment != null) {
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right);
            fragmentTransaction.remove(webViewFragment).commit();
            webViewFragment = null;
            return false;

        } else if (artShowFragment != null) {
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

        } else */ if (mediumFragment != null) {
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
            //searchFragment.finish();
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

        } else if (tagsFragment != null) {
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right);
            fragmentTransaction.remove(tagsFragment).commit();
            tagsFragment = null;
            return false;
        } else {
            return true;
        }

    }


    @Override
    public void makerArtClickEvent(Collection<Art> arts, int position) {
        this.listArtsForArtShowFragment = arts;
        this.clickPositionForArtShowFragment = position;
        //showArtFragment();
        navController.navigate(R.id.action_makerFragment_to_artShowFragment);
    }
    @Override
    public void makerWikiClick(String makerWikiPageUrl) {
        this.urlForWebFragment = makerWikiPageUrl;
        //showWebFragment();
        navController.navigate(R.id.action_makerFragment_to_webViewFragment);
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
        //showArtFragment();
        navController.navigate(R.id.action_searchFragment_to_artShowFragment);
    }
    @Override
    public void searchMakerClickEvent(Maker maker) {
        this.makerForMakerFragment = maker;
        //showMakerFragment();
        navController.navigate(R.id.action_searchFragment_to_makerFragment);
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
        //showMakerFragment();
        navController.navigate(R.id.action_artShowFragment_to_makerFragment);
    }
    @Override
    public void logoClickEvent(String artLink) {
        this.urlForWebFragment = artLink;
        //showWebFragment();
        navController.navigate(R.id.action_artShowFragment_to_webViewFragment);
    }



    @Override
    public void attributeClickEvent(Attribute attribute) {
        queryTypeForMediumFragment = attribute.getType();
        artQueryForMediumFragment = attribute.getText();
        showMediumFragment();
    }




    @Override
    public void tagClickEvent(Attribute attribute) {
        queryTypeForMediumFragment = attribute.getType();
        artQueryForMediumFragment = attribute.getText();
        showMediumFragment();
    }
    @Override
    public void tagFilterPositionEvent(int position) {
        filterPositionForTagsFragment = position;
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
                            showTagsFragment();
                        }
                    }
                });
            }
        }, DELAY);
    }
    @Override
    public void exploreSearchClickEvent() {
        navController.navigate(R.id.action_navigation_explore_to_searchFragment);
    }




    @Override
    public void blankSearchClickEvent() {
        navController.navigate(R.id.action_navigation_favorites_to_searchFragment);
    }





    @Override
    public void artistsClickEvent(Maker maker) {
        this.makerForMakerFragment = maker;
        //showMakerFragment();
        navController.navigate(R.id.action_navigation_favorites_to_makerFragment);
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
        //showArtFragment();
        navController.navigate(R.id.action_navigation_favorites_to_artShowFragment);
    }



    @Override
    public void homeScrollEvent(int position) {
        this.homePosition = position;
    }
    @Override
    public void homeArtClickEvent(Collection<Art> arts, int position) {
        this.listArtsForArtShowFragment = arts;
        this.clickPositionForArtShowFragment = position;
        //showArtFragment();
        navController.navigate(R.id.action_navigation_home_to_artShowFragment);
    }
    @Override
    public void homeMakerClickEvent(Maker maker) {
        this.makerForMakerFragment = maker;
        //showMakerFragment();
        navController.navigate(R.id.action_navigation_home_to_makerFragment);

    }
    @Override
    public void homeClassificationClickEvent(String artClassification, String queryType) {
        this.artQueryForMediumFragment = artClassification;
        this.queryTypeForMediumFragment = queryType;
        showMediumFragment();
    }
    @Override
    public void homeSearchClickEvent() {
        navController.navigate(R.id.action_navigation_home_to_searchFragment);
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

    public int getFilterPositionForTagsFragment() {
        return filterPositionForTagsFragment;
    }

    public String getUrlForWebFragment() {
        return urlForWebFragment;
    }

    public ArtShowFragment getArtShowFragment() {
        return artShowFragment;
    }


}

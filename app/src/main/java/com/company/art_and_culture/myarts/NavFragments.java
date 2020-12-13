package com.company.art_and_culture.myarts;

import android.os.Bundle;
import android.os.Handler;

import com.company.art_and_culture.myarts.art_maker_fragment.MakerFragment;
import com.company.art_and_culture.myarts.art_medium_fragment.MediumFragment;
import com.company.art_and_culture.myarts.art_search_fragment.SearchFragment;
import com.company.art_and_culture.myarts.arts_show_fragment.ArtShowFragment;
import com.company.art_and_culture.myarts.pojo.Folder;
import com.company.art_and_culture.myarts.show_folder_fragment.ShowFolderFragment;
import com.company.art_and_culture.myarts.tags_fragment.TagsFragment;
import com.company.art_and_culture.myarts.attribute_fragment.AttributeFragment;
import com.company.art_and_culture.myarts.filter_maker_fragment.FilterMakerFragment;
import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.pojo.Attribute;
import com.company.art_and_culture.myarts.pojo.Maker;
import com.company.art_and_culture.myarts.bottom_menu.explore.ExploreFragment;
import com.company.art_and_culture.myarts.bottom_menu.favorites.Artists.ArtistsFragment;
import com.company.art_and_culture.myarts.bottom_menu.favorites.BlankFragment;
import com.company.art_and_culture.myarts.bottom_menu.favorites.Favorites.FavoritesFragment;
import com.company.art_and_culture.myarts.bottom_menu.favorites.Folders.FoldersFragment;
import com.company.art_and_culture.myarts.bottom_menu.home.HomeFragment;

import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;

public class NavFragments implements
        HomeFragment.HomeEventListener, FavoritesFragment.FavoritesEventListener, SearchFragment.SearchEventListener,
        MakerFragment.MakerEventListener, ExploreFragment.ExploreEventListener, MediumFragment.MediumEventListener,
        ArtistsFragment.ArtistsEventListener, ArtShowFragment.ArtShowEventListener, FilterMakerFragment.FilterMakerEventListener,
        AttributeFragment.AttributeEventListener, TagsFragment.TagsEventListener, BlankFragment.BlankEventListener, FoldersFragment.FoldersEventListener, ShowFolderFragment.ShowFolderEventListener {

    private MainActivity activity;
    private NavController navController;

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

    private Folder folderForShowFolderFragment;


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

    public void popBackStack() {
        navController.popBackStack();
    }





    @Override
    public void makerArtClickEvent(Collection<Art> arts, int position) {
        this.listArtsForArtShowFragment = arts;
        this.clickPositionForArtShowFragment = position;
        navController.navigate(R.id.action_makerFragment_to_artShowFragment);
    }
    @Override
    public void makerWikiClick(String makerWikiPageUrl) {
        this.urlForWebFragment = makerWikiPageUrl;
        navController.navigate(R.id.action_makerFragment_to_webViewFragment);
    }


    @Override
    public void mediumArtClickEvent(Collection<Art> arts, int position) {
        this.listArtsForArtShowFragment = arts;
        this.clickPositionForArtShowFragment = position;
        navController.navigate(R.id.action_mediumFragment_to_artShowFragment);
    }



    @Override
    public void searchArtClickEvent(Collection<Art> arts, int position) {
        this.listArtsForArtShowFragment = arts;
        this.clickPositionForArtShowFragment = position;
        navController.navigate(R.id.action_searchFragment_to_artShowFragment);
    }
    @Override
    public void searchMakerClickEvent(Maker maker) {
        this.makerForMakerFragment = maker;
        navController.navigate(R.id.action_searchFragment_to_makerFragment);
    }
    @Override
    public void searchClassificationClickEvent(String artClassification, String queryType) {
        this.artQueryForMediumFragment = artClassification;
        this.queryTypeForMediumFragment = queryType;
        navController.navigate(R.id.action_searchFragment_to_mediumFragment);
    }



    @Override
    public void makerClickEvent(Maker maker) {
        this.makerForMakerFragment = maker;
        navController.navigate(R.id.action_artShowFragment_to_makerFragment);
    }
    @Override
    public void logoClickEvent(String artLink) {
        this.urlForWebFragment = artLink;
        navController.navigate(R.id.action_artShowFragment_to_webViewFragment);
    }



    @Override
    public void attributeClickEvent(Attribute attribute) {
        queryTypeForMediumFragment = attribute.getType();
        artQueryForMediumFragment = attribute.getText();
        navController.navigate(R.id.action_attributeFragment_to_mediumFragment);
    }




    @Override
    public void tagClickEvent(Attribute attribute) {
        queryTypeForMediumFragment = attribute.getType();
        artQueryForMediumFragment = attribute.getText();
        navController.navigate(R.id.action_tagsFragment_to_mediumFragment);
    }
    @Override
    public void tagFilterPositionEvent(int position) {
        filterPositionForTagsFragment = position;
    }


    @Override
    public void filterMakerClickEvent(Maker maker) {
        this.makerForMakerFragment = maker;
        navController.navigate(R.id.action_filterMakerFragment_to_makerFragment);
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
                            navController.navigate(R.id.action_navigation_explore_to_filterMakerFragment);
                        } else if (type.equals(Constants.ART_CULTURE)) {
                            typeForAttributeFragment = type;
                            navController.navigate(R.id.action_navigation_explore_to_attributeFragment);
                        } else if (type.equals(Constants.ART_MEDIUM)) {
                            typeForAttributeFragment = type;
                            navController.navigate(R.id.action_navigation_explore_to_attributeFragment);
                        } else if (type.equals(Constants.ART_CLASSIFICATION)) {
                            typeForAttributeFragment = type;
                            navController.navigate(R.id.action_navigation_explore_to_attributeFragment);
                        }  else if (type.equals(Constants.ART_TAG)) {
                            navController.navigate(R.id.action_navigation_explore_to_tagsFragment);
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
        navController.navigate(R.id.action_navigation_favorites_to_artShowFragment);
    }
    @Override
    public void folderClick(Folder folder) {
        folderForShowFolderFragment = folder;
        navController.navigate(R.id.action_navigation_favorites_to_showFolderFragment);
    }
    @Override
    public void createFolderClick() {
        navController.navigate(R.id.action_navigation_favorites_to_createFolderFragment);
    }
    @Override
    public void artFolderClickEvent(Collection<Art> arts, int position) {
        this.listArtsForArtShowFragment = arts;
        this.clickPositionForArtShowFragment = position;
        navController.navigate(R.id.action_showFolderFragment_to_artShowFragment);
    }
    @Override
    public void folderEditClickListener(Folder folder) {

    }



    @Override
    public void homeScrollEvent(int position) {
        this.homePosition = position;
    }
    @Override
    public void homeArtClickEvent(Collection<Art> arts, int position) {
        this.listArtsForArtShowFragment = arts;
        this.clickPositionForArtShowFragment = position;
        navController.navigate(R.id.action_navigation_home_to_artShowFragment);
    }
    @Override
    public void homeMakerClickEvent(Maker maker) {
        this.makerForMakerFragment = maker;
        navController.navigate(R.id.action_navigation_home_to_makerFragment);

    }
    @Override
    public void homeClassificationClickEvent(String artClassification, String queryType) {
        this.artQueryForMediumFragment = artClassification;
        this.queryTypeForMediumFragment = queryType;
        navController.navigate(R.id.action_navigation_home_to_mediumFragment);
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

    public Folder getFolderForShowFolderFragment() {
        return folderForShowFolderFragment;
    }
}

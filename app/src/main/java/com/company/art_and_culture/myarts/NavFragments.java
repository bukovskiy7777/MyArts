package com.company.art_and_culture.myarts;

import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;

import com.company.art_and_culture.myarts.art_filter_fragment.ArtFilterFragment;
import com.company.art_and_culture.myarts.art_search_fragment.SearchFragment;
import com.company.art_and_culture.myarts.arts_show_fragment.ArtShowFragment;
import com.company.art_and_culture.myarts.attribute_fragment.AttributeFragment;
import com.company.art_and_culture.myarts.bottom_menu.explore.ExploreFragment;
import com.company.art_and_culture.myarts.bottom_menu.favorites.Artists.ArtistsFragment;
import com.company.art_and_culture.myarts.bottom_menu.favorites.BlankFragment;
import com.company.art_and_culture.myarts.bottom_menu.favorites.Favorites.FavoritesFragment;
import com.company.art_and_culture.myarts.bottom_menu.favorites.Folders.FoldersFragment;
import com.company.art_and_culture.myarts.bottom_menu.home.HomeFragment;
import com.company.art_and_culture.myarts.bottom_menu.recommendations.RecommendationsFragment;
import com.company.art_and_culture.myarts.create_folder_fragment.CreateFolderFragment;
import com.company.art_and_culture.myarts.filter_maker_fragment.FilterMakerFragment;
import com.company.art_and_culture.myarts.maker_fragment.MakerFragment;
import com.company.art_and_culture.myarts.museum_fragment.MuseumFragment;
import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.pojo.Attribute;
import com.company.art_and_culture.myarts.pojo.Folder;
import com.company.art_and_culture.myarts.pojo.Maker;
import com.company.art_and_culture.myarts.show_folder_fragment.ShowFolderFragment;
import com.company.art_and_culture.myarts.tags_fragment.TagsFragment;

import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

public class NavFragments implements
        HomeFragment.HomeEventListener, FavoritesFragment.FavoritesEventListener, SearchFragment.SearchEventListener,
        MakerFragment.MakerEventListener, ExploreFragment.ExploreEventListener, ArtFilterFragment.ArtFilterEventListener,
        ArtistsFragment.ArtistsEventListener, ArtShowFragment.ArtShowEventListener, FilterMakerFragment.FilterMakerEventListener,
        AttributeFragment.AttributeEventListener, TagsFragment.TagsEventListener, BlankFragment.BlankEventListener,
        FoldersFragment.FoldersEventListener, ShowFolderFragment.ShowFolderEventListener, CreateFolderFragment.CreateFolderEventListener,
        MuseumFragment.MuseumEventListener, RecommendationsFragment.RecommendationsEventListener{

    private NavController navController;

    private Collection<Art> listArtsForArtShowFragment;
    private int clickPositionForArtShowFragment;

    private Maker makerForMakerFragment;

    private String keywordForArtFilterFragment, keywordTypeForArtFilterFragment;

    private String typeForAttributeFragment;

    private int favoritesPosition = 0;
    private FavoritesFragment.Sort sort_type = FavoritesFragment.Sort.by_date;

    private int filterMakerPosition = 0, dateMakerPosition = 0;

    private int homePosition = 0;

    private int recommendPosition = 0;

    private int filterPositionForTagsFragment= 0;

    private Folder folderForShowFolderFragment, folderForEditFolderFragment;

    private String artProviderIdForMuseumFragment;


    public NavFragments(MainActivity mainActivity, NavController navController) {
        this.navController = navController;
        visibilityNavElements(mainActivity, navController);
    }

    private void visibilityNavElements(final MainActivity mainActivity, NavController navController) {
        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                if(destination.getId() == R.id.navigation_home || destination.getId() == R.id.navigation_explore ||
                        destination.getId() == R.id.navigation_favorites || destination.getId() == R.id.navigation_recommend) {
                    mainActivity.setNavViewVisible();
                } else {
                    mainActivity.goneNavView();
                }
            }
        });
    }

    public void popBackStack() {
        navController.popBackStack();
    }





    @Override
    public void onArtClickEvent(Collection<Art> arts, int position) {
        this.listArtsForArtShowFragment = arts;
        this.clickPositionForArtShowFragment = position;
        navController.navigate(R.id.action_museumFragment_to_artShowFragment);
    }
    @Override
    public void onArtistsClickEvent(Maker maker) {
        this.makerForMakerFragment = maker;
        navController.navigate(R.id.action_museumFragment_to_makerFragment);
    }


    @Override
    public void makerArtClickEvent(Collection<Art> arts, int position) {
        this.listArtsForArtShowFragment = arts;
        this.clickPositionForArtShowFragment = position;
        navController.navigate(R.id.action_makerFragment_to_artShowFragment);
    }



    @Override
    public void artFilterArtClickEvent(Collection<Art> arts, int position) {
        this.listArtsForArtShowFragment = arts;
        this.clickPositionForArtShowFragment = position;
        navController.navigate(R.id.action_artFilterFragment_to_artShowFragment);
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
        this.keywordForArtFilterFragment = artClassification;
        this.keywordTypeForArtFilterFragment = queryType;
        navController.navigate(R.id.action_searchFragment_to_artFilterFragment);
    }
    @Override
    public void searchMuseumClickEvent(String artProviderId) {
        artProviderIdForMuseumFragment = artProviderId;
        navController.navigate(R.id.action_searchFragment_to_museumFragment);
    }


    @Override
    public void makerClickEvent(Maker maker) {
        this.makerForMakerFragment = maker;
        navController.navigate(R.id.action_artShowFragment_to_makerFragment);
    }



    @Override
    public void attributeClickEvent(Attribute attribute) {
        keywordTypeForArtFilterFragment = attribute.getType();
        keywordForArtFilterFragment = attribute.getText();
        navController.navigate(R.id.action_attributeFragment_to_artFilterFragment);
    }




    @Override
    public void tagClickEvent(Attribute attribute) {
        keywordTypeForArtFilterFragment = attribute.getType();
        keywordForArtFilterFragment = attribute.getText();
        navController.navigate(R.id.action_tagsFragment_to_artFilterFragment);
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
        timer.schedule( new TimerTask() {
            @Override
            public void run() {
                handler.post(() -> {
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
                });
            }
        }, DELAY);
    }
    @Override
    public void exploreSearchClickEvent() {
        navController.navigate(R.id.action_navigation_explore_to_searchFragment);
    }
    @Override
    public void exploreProfileClickEvent(boolean isLoggedIn) {
        if(isLoggedIn)
            navController.navigate(R.id.action_navigation_explore_to_userFragment);
        else
            navController.navigate(R.id.action_navigation_explore_to_signInFragment);
    }


    @Override
    public void blankSearchClickEvent() {
        navController.navigate(R.id.action_navigation_favorites_to_searchFragment);
    }
    @Override
    public void blankProfileClickEvent(boolean isLoggedIn) {
        if(isLoggedIn)
            navController.navigate(R.id.action_navigation_favorites_to_userFragment);
        else
            navController.navigate(R.id.action_navigation_favorites_to_signInFragment);
    }


    @Override
    public void artistsClickEvent(Maker maker) {
        this.makerForMakerFragment = maker;
        navController.navigate(R.id.action_navigation_favorites_to_makerFragment);
    }
    @Override
    public void showArtistsClick() {
        navController.navigate(R.id.action_navigation_favorites_to_filterMakerFragment);
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
    public void onCreateFolderFragmentClose() {
        folderForEditFolderFragment = null;
    }
    @Override
    public void artFolderClickEvent(Collection<Art> arts, int position) {
        this.listArtsForArtShowFragment = arts;
        this.clickPositionForArtShowFragment = position;
        navController.navigate(R.id.action_showFolderFragment_to_artShowFragment);
    }
    @Override
    public void folderEditClickListener(Folder folder) {
        folderForEditFolderFragment = folder;
        navController.navigate(R.id.action_showFolderFragment_to_createFolderFragment);
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
        this.keywordForArtFilterFragment = artClassification;
        this.keywordTypeForArtFilterFragment = queryType;
        navController.navigate(R.id.action_navigation_home_to_artFilterFragment);
    }
    @Override
    public void homeSearchClickEvent() {
        navController.navigate(R.id.action_navigation_home_to_searchFragment);
    }
    @Override
    public void homeMuseumClickEvent(String artProviderId) {
        artProviderIdForMuseumFragment = artProviderId;
        navController.navigate(R.id.action_navigation_home_to_museumFragment);
    }
    @Override
    public void homeProfileClickEvent(boolean isLoggedIn) {
        if(isLoggedIn)
            navController.navigate(R.id.action_navigation_home_to_userFragment);
        else
            navController.navigate(R.id.action_navigation_home_to_signInFragment);
    }


    @Override
    public void recommendationsArtClickEvent(Collection<Art> arts, int position) {
        this.listArtsForArtShowFragment = arts;
        this.clickPositionForArtShowFragment = position;
        navController.navigate(R.id.action_navigation_recommend_to_artShowFragment);
    }
    @Override
    public void recommendationsMakerClickEvent(Maker maker) {
        this.makerForMakerFragment = maker;
        navController.navigate(R.id.action_navigation_recommend_to_makerFragment);
    }
    @Override
    public void recommendationsSearchClickEvent() {
        navController.navigate(R.id.action_navigation_recommend_to_searchFragment);
    }
    @Override
    public void recommendationsClassificationClickEvent(String artClassification, String queryType) {
        this.keywordForArtFilterFragment = artClassification;
        this.keywordTypeForArtFilterFragment = queryType;
        navController.navigate(R.id.action_navigation_recommend_to_artFilterFragment);
    }
    @Override
    public void recommendScrollEvent(int scrollPosition) {
        this.recommendPosition = scrollPosition;
    }
    @Override
    public void recommendProfileClickEvent(boolean isLoggedIn) {
        if(isLoggedIn)
            navController.navigate(R.id.action_navigation_recommend_to_userFragment);
        else
            navController.navigate(R.id.action_navigation_recommend_to_signInFragment);
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

    public String getKeywordForArtFilterFragment() {
        return keywordForArtFilterFragment;
    }

    public String getKeywordTypeForArtFilterFragment() {
        return keywordTypeForArtFilterFragment;
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

    public Folder getFolderForShowFolderFragment() {
        return folderForShowFolderFragment;
    }

    public Folder getFolderForEditFolderFragment() {
        return folderForEditFolderFragment;
    }

    public String getArtProviderIdForMuseumFragment() { return artProviderIdForMuseumFragment; }

    public int getRecommendPosition() {
        return recommendPosition;
    }
}

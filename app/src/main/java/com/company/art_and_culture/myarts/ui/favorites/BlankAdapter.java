package com.company.art_and_culture.myarts.ui.favorites;

import com.company.art_and_culture.myarts.ui.favorites.Artists.ArtistsFragment;
import com.company.art_and_culture.myarts.ui.favorites.Favorites.FavoritesFragment;
import com.company.art_and_culture.myarts.ui.favorites.Folders.FoldersFragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class BlankAdapter extends FragmentStateAdapter {

    private FavoritesFragment favoritesFragment;

    public BlankAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 2:
                return new FoldersFragment();
            case 1:
                return new ArtistsFragment();
            default:
                favoritesFragment = new FavoritesFragment();
                return favoritesFragment;
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    public FavoritesFragment getFavoritesFragment() {
        return favoritesFragment;
    }

}

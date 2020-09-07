package com.company.art_and_culture.myarts.ui.favorites;

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
            case 1:

                return new FoldersFragment();
            default:
                favoritesFragment = new FavoritesFragment();
                return favoritesFragment;
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public FavoritesFragment getFavoritesFragment() {
        return favoritesFragment;
    }

}

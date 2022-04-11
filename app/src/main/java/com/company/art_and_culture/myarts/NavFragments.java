package com.company.art_and_culture.myarts;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;


public class NavFragments {

    private NavController navController;

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


}

package com.company.art_and_culture.myarts.ui.favorites;


import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.pojo.Art;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.lang.reflect.Field;
import java.util.Collection;

public class BlankFragment extends Fragment {

    private MainActivity activity;
    private ViewPager2 viewPager;
    private BlankAdapter blankAdapter;
    private TabLayout tabLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_favorites_blank, container, false);

        viewPager = root.findViewById(R.id.favorite_view_pager);

        activity = (MainActivity) getActivity();

        blankAdapter = new BlankAdapter(activity);
        viewPager.setAdapter(blankAdapter);

        try {
            final Field recyclerViewField = ViewPager2.class.getDeclaredField("mRecyclerView");
            recyclerViewField.setAccessible(true);

            final RecyclerView recyclerView = (RecyclerView) recyclerViewField.get(viewPager);

            final Field touchSlopField = RecyclerView.class.getDeclaredField("mTouchSlop");
            touchSlopField.setAccessible(true);

            final int touchSlop = (int) touchSlopField.get(recyclerView);
            touchSlopField.set(recyclerView, touchSlop * 6);//6 is empirical value
        } catch (Exception ignore) {
        }

        tabLayout = root.findViewById(R.id.favorite_tab);
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position) {
                    case 0:
                        tab.setText(R.string.app_name);
                        break;
                    case 1:
                        tab.setText(R.string.folders);
                        break;
                }
            }
        });
        tabLayoutMediator.attach();

        setOnBackPressedListener(root);

        return root;
    }

    private void setOnBackPressedListener(final View root) {

        root.setFocusableInTouchMode(true);
        root.requestFocus();
        root.setOnKeyListener( new View.OnKeyListener() {
            @Override
            public boolean onKey( View v, int keyCode, KeyEvent event ) {
                Log.i("BlankScrollEvent", "");
                if( keyCode == KeyEvent.KEYCODE_BACK && activity.getArtShowFragment() == null && !activity.isSearchLayoutOpen()) {
                    int scrollPosition = 0;
                    int spanCount = 3;
                    scrollPosition = blankAdapter.getFavoritesFragment().getTargetScrollPosition();
                    spanCount = blankAdapter.getFavoritesFragment().getSpanCount();
                    if (scrollPosition > 4 * spanCount) {
                        if(viewPager.getCurrentItem() == 0) {
                            blankAdapter.getFavoritesFragment().scrollRecyclerViewToStart();
                            return true;
                        } else {
                            return false;
                        }
                    }
                    return false;
                }
                return false;
            }
        } );
    }

    @Override
    public void onPause() {
        super.onPause();
        blankAdapter.getFavoritesFragment().postScrollDataToMainActivity();
    }

}
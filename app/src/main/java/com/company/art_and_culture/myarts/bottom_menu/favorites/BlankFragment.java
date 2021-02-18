package com.company.art_and_culture.myarts.bottom_menu.favorites;


import android.animation.ValueAnimator;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.lang.reflect.Field;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

public class BlankFragment extends Fragment implements View.OnClickListener {

    private MainActivity activity;
    private ViewPager2 viewPager;
    private BlankAdapter blankAdapter;
    private TabLayout tabLayout;
    private ImageView search_btn;
    private BlankEventListener blankEventListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_favorites_blank, container, false);

        viewPager = root.findViewById(R.id.favorite_view_pager);
        search_btn = root.findViewById(R.id.search_btn);
        search_btn.setOnClickListener(this);

        activity = (MainActivity) getActivity();

        if (activity != null) blankEventListener = activity.getNavFragments();

        blankAdapter = new BlankAdapter(activity);
        viewPager.setAdapter(blankAdapter);

        try {
            final Field recyclerViewField = ViewPager2.class.getDeclaredField("mRecyclerView");
            recyclerViewField.setAccessible(true);

            final RecyclerView recyclerView = (RecyclerView) recyclerViewField.get(viewPager);

            final Field touchSlopField = RecyclerView.class.getDeclaredField("mTouchSlop");
            touchSlopField.setAccessible(true);

            final int touchSlop = (int) touchSlopField.get(recyclerView);
            touchSlopField.set(recyclerView, touchSlop * 3);//6 is empirical value
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
                        tab.setText(R.string.artists);
                        break;
                    case 2:
                        tab.setText(R.string.folders);
                        break;
                }
            }
        });
        tabLayoutMediator.attach();

        setOnBackPressedListener(root);

        subscribeCountObservers();

        return root;
    }

    private void subscribeCountObservers() {
        activity.getFavoritesArtsCount().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                animateTab(0, integer, 1500);
            }
        });
        activity.getArtistsCount().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                animateTab(1, integer, 1000);
            }
        });
        activity.getFoldersCount().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                animateTab(2, integer, 500);
            }
        });
    }

    private void animateTab(final int tabNumber, int value, int duration) {
        ValueAnimator animator = ValueAnimator.ofInt(0, value);
        animator.setDuration(duration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                tabLayout.getTabAt(tabNumber).getOrCreateBadge().setNumber((Integer) animation.getAnimatedValue());
            }
        });
        animator.start();
    }


    private void setOnBackPressedListener(final View root) {

        root.setFocusableInTouchMode(true);
        root.requestFocus();
        root.setOnKeyListener( new View.OnKeyListener() {
            @Override
            public boolean onKey( View v, int keyCode, KeyEvent event ) {
                Log.i("BlankScrollEvent", "");
                if( keyCode == KeyEvent.KEYCODE_BACK ) {

                    if(viewPager.getCurrentItem() == 0) {
                        if (blankAdapter.getFavoritesFragment()!=null) {
                            int scrollPosition = blankAdapter.getFavoritesFragment().getTargetScrollPosition();
                            int spanCount = blankAdapter.getFavoritesFragment().getSpanCount();
                            if (scrollPosition > 4 * spanCount) {
                                blankAdapter.getFavoritesFragment().scrollRecyclerViewToStart();
                                return true;
                            }
                        }
                    } else if(viewPager.getCurrentItem() == 1) {
                        if (blankAdapter.getArtistsFragment()!=null) {
                            int scrollPosition = blankAdapter.getArtistsFragment().getTargetScrollPosition();
                            if (scrollPosition > 4 ) {
                                blankAdapter.getArtistsFragment().scrollRecyclerViewToStart();
                                return true;
                            }
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
        if (blankAdapter.getFavoritesFragment()!=null) blankAdapter.getFavoritesFragment().postScrollDataToMainActivity();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == search_btn.getId()) {
            blankEventListener.blankSearchClickEvent();
        }
    }

    public interface BlankEventListener {
        void blankSearchClickEvent();
    }
}

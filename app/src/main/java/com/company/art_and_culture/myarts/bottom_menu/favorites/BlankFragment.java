package com.company.art_and_culture.myarts.bottom_menu.favorites;


import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Field;

public class BlankFragment extends Fragment implements View.OnClickListener {

    private MainActivity activity;
    private ViewPager2 viewPager;
    private BlankAdapter blankAdapter;
    private TabLayout tabLayout;
    private ImageView search_btn, profile_img;
    private BlankEventListener blankEventListener;
    private FloatingActionButton fab_favorites, fab_artists, fab_folders;
    private AppCompatEditText search_favorites, search_artists;
    private SharedPreferences preferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_favorites_blank, container, false);

        viewPager = root.findViewById(R.id.favorite_view_pager);
        search_btn = root.findViewById(R.id.search_btn);
        search_btn.setOnClickListener(this);
        profile_img = root.findViewById(R.id.profile_img);
        profile_img.setOnClickListener(this);
        fab_favorites = root.findViewById(R.id.fab_favorites);
        fab_favorites.setOnClickListener(this);
        fab_artists = root.findViewById(R.id.fab_artists);
        fab_artists.setOnClickListener(this);
        fab_folders = root.findViewById(R.id.fab_folders);
        fab_folders.setOnClickListener(this);
        search_favorites = root.findViewById(R.id.search_favorites);
        search_artists = root.findViewById(R.id.search_artists);

        activity = (MainActivity) getActivity();

        if (activity != null) blankEventListener = activity.getNavFragments();
        if (activity != null) preferences = activity.getSharedPreferences(Constants.TAG, 0);

        if(preferences.getBoolean(Constants.IS_LOGGED_IN,false)) Picasso.get().load(preferences.getString(Constants.USER_IMAGE_URL,getResources().getString(R.string.http))).into(profile_img);

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

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        fab_favorites.show(); fab_artists.hide(); fab_folders.hide();
                        if(search_favorites.getText().toString().length() == 0) search_favorites.setVisibility(View.GONE);
                        else search_favorites.setVisibility(View.VISIBLE);
                        search_artists.setVisibility(View.GONE);
                        break;
                    case 1:
                        fab_favorites.hide(); fab_artists.show(); fab_folders.hide();
                        if(search_artists.getText().toString().length() == 0) search_artists.setVisibility(View.GONE);
                        else search_artists.setVisibility(View.VISIBLE);
                        search_favorites.setVisibility(View.GONE);
                        break;
                    case 2:
                        fab_favorites.hide(); fab_artists.hide(); fab_folders.show();
                        hideEditTexts();
                        break;
                    default:
                        fab_favorites.hide(); fab_artists.hide(); fab_folders.hide();
                        hideEditTexts();
                        break;
                }
            }
        });

        tabLayoutMediator.attach();

        setOnBackPressedListener(root);

        subscribeCountObservers();

        initEditTexts();

        return root;
    }

    private void hideEditTexts() {
        search_favorites.setVisibility(View.GONE);
        search_artists.setVisibility(View.GONE);
    }

    private void initEditTexts() {
        search_favorites.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                search_favorites.clearFocus();
            }
            return false;
        });
        search_favorites.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void afterTextChanged(Editable editable) {
                activity.postFavoritesFilter(search_favorites.getText().toString());
            }
        });

        search_artists.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                search_artists.clearFocus();
            }
            return false;
        });
        search_artists.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void afterTextChanged(Editable editable) {
                activity.postArtistsFilter(search_artists.getText().toString());
            }
        });
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
        activity.getIsUpdateUserData().observe(getViewLifecycleOwner(), aBoolean -> {
            if(aBoolean) {
                if(preferences.getString(Constants.USER_IMAGE_URL,"").startsWith(getResources().getString(R.string.http))) {
                    Picasso.get().load(preferences.getString(Constants.USER_IMAGE_URL,getResources().getString(R.string.http))).into(profile_img);
                } else profile_img.setImageResource(R.drawable.ic_outline_account_circle_24);

                final Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(() -> activity.updateUserData(false), 1000);
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
    public void onDestroy() {
        super.onDestroy();
        activity.postArtistsFilter("");
        activity.postFavoritesFilter("");
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == search_btn.getId()) {
            blankEventListener.blankSearchClickEvent();

        } else if (v.getId() == profile_img.getId()) {
            blankEventListener.blankProfileClickEvent(preferences.getBoolean(Constants.IS_LOGGED_IN,false));

        }else if (v.getId() == fab_folders.getId()) {
            blankEventListener.createFolderClick();

        } else if (v.getId() == fab_favorites.getId()) {
            if (search_favorites.isShown()) search_favorites.setVisibility(View.GONE);
            else search_favorites.setVisibility(View.VISIBLE);

        } else if (v.getId() == fab_artists.getId()) {
            if (search_artists.isShown()) search_artists.setVisibility(View.GONE);
            else search_artists.setVisibility(View.VISIBLE);
        }
    }

    public interface BlankEventListener {
        void blankSearchClickEvent();
        void blankProfileClickEvent(boolean isLoggedIn);
        void createFolderClick();
    }
}

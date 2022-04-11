package com.company.art_and_culture.myarts.bottom_menu.explore;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.art_filter_fragment.ArtFilterFragment;
import com.company.art_and_culture.myarts.attribute_fragment.AttributeFragment;
import com.company.art_and_culture.myarts.bottom_menu.home.HomeFragment;
import com.company.art_and_culture.myarts.maker_fragment.MakerFragment;
import com.company.art_and_culture.myarts.pojo.ExploreObject;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ExploreFragment extends Fragment implements View.OnClickListener, View.OnTouchListener {

    private ExploreViewModel exploreViewModel;
    private TextView textView;
    private RecyclerView exploreRecyclerView;
    private ExploreAdapter exploreAdapter;
    private int spanCount = 1;
    private ProgressBar exploreProgressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private android.content.res.Resources res;
    private SharedPreferences preferences;
    private MainActivity activity;
    private ImageView search_btn, profile_img;


    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_explore, container, false);

        exploreViewModel = new ViewModelProvider(this).get(ExploreViewModel.class);

        textView = root.findViewById(R.id.text_explore);
        exploreRecyclerView = root.findViewById(R.id.exploreRecyclerView);
        exploreProgressBar = root.findViewById(R.id.progress_bar_explore);
        swipeRefreshLayout = root.findViewById(R.id.explore_swipeRefreshLayout);
        search_btn = root.findViewById(R.id.search_btn);
        search_btn.setOnClickListener(this);
        profile_img = root.findViewById(R.id.profile_img);
        profile_img.setOnClickListener(this);

        res = getResources();
        int displayWidth = res.getDisplayMetrics().widthPixels;
        int displayHeight = res.getDisplayMetrics().heightPixels;

        initRecyclerView(displayWidth, displayHeight);

        activity = (MainActivity) getActivity();
        if (activity != null) preferences = activity.getSharedPreferences(Constants.TAG, 0);

        String imageUrl = preferences.getString(Constants.USER_IMAGE_URL,"");
        if(preferences.getBoolean(Constants.IS_LOGGED_IN,false)) Picasso.get().load(imageUrl.isEmpty()? null : imageUrl).into(profile_img);

        initSwipeRefreshLayout();
        subscribeObservers();

        return root;
    }

    private void initRecyclerView(int displayWidth, int displayHeight) {

        ExploreAdapter.OnExploreClickListener onExploreClickListener = new ExploreAdapter.OnExploreClickListener() {
            @Override
            public void onExploreImageClick(ExploreObject exploreObject, int position) {
                Timer timer=new Timer();
                final long DELAY = 500; // milliseconds
                final Handler handler = new Handler();
                timer.schedule( new TimerTask() {
                    @Override
                    public void run() {
                        handler.post(() -> {
                            switch (exploreObject.getType()) {
                                case Constants.ART_MAKER:
                                    NavHostFragment.findNavController(ExploreFragment.this)
                                            .navigate(R.id.action_navigation_explore_to_filterMakerFragment);
                                    break;
                                case Constants.ART_CULTURE:
                                case Constants.ART_MEDIUM:
                                case Constants.ART_CLASSIFICATION:
                                    Bundle args = new Bundle();
                                    args.putString(AttributeFragment.ATTRIBUTE_TYPE, exploreObject.getType());
                                    NavHostFragment.findNavController(ExploreFragment.this)
                                            .navigate(R.id.action_navigation_explore_to_attributeFragment, args);
                                    break;
                                case Constants.ART_TAG:
                                    NavHostFragment.findNavController(ExploreFragment.this)
                                            .navigate(R.id.action_navigation_explore_to_tagsFragment);
                                    break;
                            }
                        });
                    }
                }, DELAY);
            }
        };
        exploreAdapter = new ExploreAdapter(getContext(), onExploreClickListener, displayWidth, displayHeight, spanCount);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), spanCount);
        exploreRecyclerView.setLayoutManager(layoutManager);
        exploreRecyclerView.setAdapter(exploreAdapter);
    }


    private void subscribeObservers() {

        exploreViewModel.getExploreList().observe(getViewLifecycleOwner(), new Observer<ArrayList<ExploreObject>>() {
            @Override
            public void onChanged(ArrayList<ExploreObject> objects) {

                if (objects == null) {
                    exploreAdapter.clearItems();
                } else {
                    setAnimationRecyclerView (objects, exploreAdapter, exploreRecyclerView);
                    exploreAdapter.clearItems();
                    exploreAdapter.setItems(objects);
                }
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        exploreViewModel.getIsLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) { showProgressBar(); } else { hideProgressBar(); }
            }
        });
        exploreViewModel.getIsListEmpty().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) { showText(); } else { hideText(); }
            }
        });
        activity.getIsUpdateUserData().observe(getViewLifecycleOwner(), aBoolean -> {
            if(aBoolean) {
                if(preferences.getString(Constants.USER_IMAGE_URL,"").startsWith(res.getString(R.string.http))) {
                    Picasso.get().load(preferences.getString(Constants.USER_IMAGE_URL,"")).into(profile_img);
                } else profile_img.setImageResource(R.drawable.ic_outline_account_circle_24);

                final Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(() -> activity.updateUserData(false), 1000);
            }
        });

    }

    private void initSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            boolean networkState = refresh();
            if (!networkState) {
                Toast.makeText(getContext(), R.string.network_is_unavailable, Toast.LENGTH_LONG).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorBlue
        );
    }

    private void showProgressBar(){
        exploreProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar(){
        exploreProgressBar.setVisibility(View.GONE);
    }

    private void showText(){
        textView.setVisibility(View.VISIBLE);
    }

    private void hideText(){
        textView.setVisibility(View.GONE);
    }

    public boolean refresh () {
        return exploreViewModel.refresh();
    }

    private void setAnimationRecyclerView(ArrayList<ExploreObject> objects, ExploreAdapter exploreAdapter, RecyclerView recyclerView) {

        LayoutAnimationController layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_fade_in);
        recyclerView.setLayoutAnimation(layoutAnimationController);
        //recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == search_btn.getId()) {
            NavHostFragment.findNavController(this).navigate(R.id.action_navigation_explore_to_searchFragment);

        } else if (v.getId() == profile_img.getId()) {
            if(preferences.getBoolean(Constants.IS_LOGGED_IN,false))
                NavHostFragment.findNavController(this).navigate(R.id.action_navigation_explore_to_userFragment);
            else
                NavHostFragment.findNavController(this).navigate(R.id.action_navigation_explore_to_signInFragment);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }


}
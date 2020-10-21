package com.company.art_and_culture.myarts.ui.explore;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.pojo.ExploreObject;
import com.company.art_and_culture.myarts.pojo.Maker;

import java.util.ArrayList;

public class ExploreFragment extends Fragment implements View.OnClickListener, View.OnTouchListener {

    private ExploreViewModel exploreViewModel;
    private TextView textView, explore_maker, explore_maker_more, explore_culture, explore_culture_more, explore_medium, explore_medium_more, explore_century, explore_century_more;
    private RecyclerView makerRecyclerView, cultureRecyclerView, mediumRecyclerView, centuryRecyclerView;
    private ExploreAdapter makerAdapter, cultureAdapter, mediumAdapter, centuryAdapter;
    private int spanCount = 3;
    private ProgressBar exploreProgressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private android.content.res.Resources res;
    private ExploreEventListener exploreEventListener;


    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_explore, container, false);

        exploreViewModel = new ViewModelProvider(this).get(ExploreViewModel.class);

        textView = root.findViewById(R.id.text_explore);
        explore_maker = root.findViewById(R.id.explore_maker);
        explore_maker.setVisibility(View.GONE);
        explore_maker_more = root.findViewById(R.id.explore_maker_more);
        explore_maker_more.setVisibility(View.GONE);
        explore_maker_more.setOnClickListener(this);
        explore_maker_more.setOnTouchListener(this);
        makerRecyclerView = root.findViewById(R.id.makerRecyclerView);
        explore_culture = root.findViewById(R.id.explore_culture);
        explore_culture.setVisibility(View.GONE);
        explore_culture_more = root.findViewById(R.id.explore_culture_more);
        explore_culture_more.setVisibility(View.GONE);
        explore_culture_more.setOnClickListener(this);
        explore_culture_more.setOnTouchListener(this);
        cultureRecyclerView = root.findViewById(R.id.cultureRecyclerView);
        explore_medium = root.findViewById(R.id.explore_medium);
        explore_medium.setVisibility(View.GONE);
        explore_medium_more = root.findViewById(R.id.explore_medium_more);
        explore_medium_more.setVisibility(View.GONE);
        explore_medium_more.setOnClickListener(this);
        explore_medium_more.setOnTouchListener(this);
        mediumRecyclerView = root.findViewById(R.id.mediumRecyclerView);
        explore_century = root.findViewById(R.id.explore_century);
        explore_century.setVisibility(View.GONE);
        explore_century_more = root.findViewById(R.id.explore_century_more);
        explore_century_more.setVisibility(View.GONE);
        explore_century_more.setOnClickListener(this);
        explore_century_more.setOnTouchListener(this);
        centuryRecyclerView = root.findViewById(R.id.centuryRecyclerView);
        exploreProgressBar = root.findViewById(R.id.progress_bar_explore);
        swipeRefreshLayout = root.findViewById(R.id.explore_swipeRefreshLayout);

        res = getResources();
        int displayWidth = res.getDisplayMetrics().widthPixels;
        int displayHeight = res.getDisplayMetrics().heightPixels;

        initMakerRecyclerView(displayWidth, displayHeight);
        initCultureRecyclerView(displayWidth, displayHeight);
        initMediumRecyclerView(displayWidth, displayHeight);
        initCenturyRecyclerView(displayWidth, displayHeight);

        initSwipeRefreshLayout();
        subscribeObservers();

        return root;
    }

    private void initMakerRecyclerView(int displayWidth, int displayHeight) {

        ExploreAdapter.OnExploreClickListener onExploreClickListener = new ExploreAdapter.OnExploreClickListener() {
            @Override
            public void onExploreImageClick(ExploreObject exploreObject, int position) {
                String imageUrl;
                if (!exploreObject.getImageUrlSmall().equals(" ") && exploreObject.getImageUrlSmall().startsWith(getResources().getString(R.string.http))) {
                    imageUrl = exploreObject.getImageUrlSmall();
                } else {
                    imageUrl= exploreObject.getImageUrl();
                }
                Maker maker = new Maker(exploreObject.getText(), exploreObject.getArtistBio(), imageUrl, exploreObject.getWidth(), exploreObject.getHeight());
                exploreEventListener.exploreMakerClickEvent(maker);
            }
        };
        makerAdapter = new ExploreAdapter(getContext(), onExploreClickListener, displayWidth, displayHeight, spanCount);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), spanCount);
        makerRecyclerView.setLayoutManager(layoutManager);
        makerRecyclerView.setAdapter(makerAdapter);
    }

    private void initCultureRecyclerView(int displayWidth, int displayHeight) {

        ExploreAdapter.OnExploreClickListener onExploreClickListener = new ExploreAdapter.OnExploreClickListener() {
            @Override
            public void onExploreImageClick(ExploreObject exploreObject, int position) {
                exploreEventListener.exploreCultureClickEvent(exploreObject.getText(), exploreObject.getType());
            }
        };
        cultureAdapter = new ExploreAdapter(getContext(), onExploreClickListener, displayWidth, displayHeight, spanCount);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), spanCount);
        cultureRecyclerView.setLayoutManager(layoutManager);
        cultureRecyclerView.setAdapter(cultureAdapter);
    }

    private void initMediumRecyclerView(int displayWidth, int displayHeight) {

        ExploreAdapter.OnExploreClickListener onExploreClickListener = new ExploreAdapter.OnExploreClickListener() {
            @Override
            public void onExploreImageClick(ExploreObject exploreObject, int position) {
                exploreEventListener.exploreCultureClickEvent(exploreObject.getText(), exploreObject.getType());
            }
        };
        mediumAdapter = new ExploreAdapter(getContext(), onExploreClickListener, displayWidth, displayHeight, spanCount);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), spanCount);
        mediumRecyclerView.setLayoutManager(layoutManager);
        mediumRecyclerView.setAdapter(mediumAdapter);
    }

    private void initCenturyRecyclerView(int displayWidth, int displayHeight) {

        ExploreAdapter.OnExploreClickListener onExploreClickListener = new ExploreAdapter.OnExploreClickListener() {
            @Override
            public void onExploreImageClick(ExploreObject exploreObject, int position) {
                exploreEventListener.exploreCultureClickEvent(exploreObject.getText(), exploreObject.getType());
            }
        };
        centuryAdapter = new ExploreAdapter(getContext(), onExploreClickListener, displayWidth, displayHeight, spanCount);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), spanCount);
        centuryRecyclerView.setLayoutManager(layoutManager);
        centuryRecyclerView.setAdapter(centuryAdapter);
    }

    private void subscribeObservers() {

        exploreViewModel.getMakersList().observe(getViewLifecycleOwner(), new Observer<ArrayList<ExploreObject>>() {
            @Override
            public void onChanged(ArrayList<ExploreObject> objects) {

                if (objects == null) {
                    makerAdapter.clearItems();
                } else {
                    setAnimationRecyclerView (objects, makerAdapter, makerRecyclerView);
                    makerAdapter.clearItems();
                    makerAdapter.setItems(objects);
                    if (objects.size() > 0) {
                        explore_maker.setVisibility(View.VISIBLE);
                        explore_maker_more.setVisibility(View.VISIBLE);
                    } else {
                        explore_maker.setVisibility(View.GONE);
                        explore_maker_more.setVisibility(View.GONE);
                    }
                }
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        exploreViewModel.getCultureList().observe(getViewLifecycleOwner(), new Observer<ArrayList<ExploreObject>>() {
            @Override
            public void onChanged(ArrayList<ExploreObject> objects) {

                if (objects == null) {
                    cultureAdapter.clearItems();
                } else {
                    setAnimationRecyclerView (objects, cultureAdapter, cultureRecyclerView);
                    cultureAdapter.clearItems();
                    cultureAdapter.setItems(objects);
                    if (objects.size() > 0) {
                        explore_culture.setVisibility(View.VISIBLE);
                        explore_culture_more.setVisibility(View.VISIBLE);
                    } else {
                        explore_culture.setVisibility(View.GONE);
                        explore_culture_more.setVisibility(View.GONE);
                    }
                }
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        exploreViewModel.getMediumList().observe(getViewLifecycleOwner(), new Observer<ArrayList<ExploreObject>>() {
            @Override
            public void onChanged(ArrayList<ExploreObject> objects) {

                if (objects == null) {
                    mediumAdapter.clearItems();
                } else {
                    setAnimationRecyclerView (objects, mediumAdapter, mediumRecyclerView);
                    mediumAdapter.clearItems();
                    mediumAdapter.setItems(objects);
                    if (objects.size() > 0) {
                        explore_medium.setVisibility(View.VISIBLE);
                        explore_medium_more.setVisibility(View.VISIBLE);
                    } else {
                        explore_medium.setVisibility(View.GONE);
                        explore_medium_more.setVisibility(View.GONE);
                    }
                }
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        exploreViewModel.getCenturyList().observe(getViewLifecycleOwner(), new Observer<ArrayList<ExploreObject>>() {
            @Override
            public void onChanged(ArrayList<ExploreObject> objects) {

                if (objects == null) {
                    centuryAdapter.clearItems();
                } else {
                    setAnimationRecyclerView (objects, centuryAdapter, centuryRecyclerView);
                    centuryAdapter.clearItems();
                    centuryAdapter.setItems(objects);
                    if (objects.size() > 0) {
                        explore_century.setVisibility(View.VISIBLE);
                        explore_century_more.setVisibility(View.VISIBLE);
                    } else {
                        explore_century.setVisibility(View.GONE);
                        explore_century_more.setVisibility(View.GONE);
                    }
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

    }

    private void initSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                boolean networkState = refresh();
                if (!networkState) {
                    Toast.makeText(getContext(), R.string.network_is_unavailable, Toast.LENGTH_LONG).show();
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light,
                android.R.color.holo_blue_bright
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
        if (v.getId() == explore_maker_more.getId()) {
            exploreEventListener.exploreMakerSearchClick();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == explore_maker_more.getId() || v.getId() == explore_culture_more.getId()
                || v.getId() == explore_medium_more.getId() || v.getId() == explore_century_more.getId()) {
            switch(event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    ((TextView)v).setTextColor(getContext().getResources().getColor(R.color.colorBlack));
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    ((TextView)v).setTextColor(getContext().getResources().getColor(R.color.colorBlueLight));
                    break;
            }
        }
        return false;
    }

    public interface ExploreEventListener {
        void exploreMakerSearchClick();
        void exploreMakerClickEvent(Maker maker);
        void exploreCultureClickEvent(String text, String type);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            exploreEventListener = (ExploreEventListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement onSomeEventListener");
        }
    }



}
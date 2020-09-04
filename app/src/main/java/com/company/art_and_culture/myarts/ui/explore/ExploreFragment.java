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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.pojo.ExploreObject;

import java.util.ArrayList;
import java.util.Collection;

public class ExploreFragment extends Fragment implements View.OnClickListener, View.OnTouchListener {

    private ExploreViewModel exploreViewModel;
    private TextView textView, explore_maker, explore_maker_more;
    private RecyclerView exploreRecyclerView;
    private ExploreAdapter exploreAdapter;
    private int spanCount = 3;
    private ProgressBar exploreProgressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private android.content.res.Resources res;
    private Collection<ExploreObject> listObjects;
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
        exploreRecyclerView = root.findViewById(R.id.recycler_view_explore);
        exploreProgressBar = root.findViewById(R.id.progress_bar_explore);
        swipeRefreshLayout = root.findViewById(R.id.explore_swipeRefreshLayout);

        res = getResources();
        int displayWidth = res.getDisplayMetrics().widthPixels;
        int displayHeight = res.getDisplayMetrics().heightPixels;

        initRecyclerView(displayWidth, displayHeight);

        initSwipeRefreshLayout();
        subscribeObservers();

        return root;
    }

    private void initRecyclerView(int displayWidth, int displayHeight) {

        ExploreAdapter.OnExploreClickListener onExploreClickListener = new ExploreAdapter.OnExploreClickListener() {
            @Override
            public void onExploreImageClick(ExploreObject exploreObject, int position) {
                exploreEventListener.exploreClickEvent(exploreObject, position);

            }
        };
        exploreAdapter = new ExploreAdapter(getContext(), onExploreClickListener, displayWidth, displayHeight, spanCount);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), spanCount);
        exploreRecyclerView.setLayoutManager(layoutManager);
        exploreRecyclerView.setAdapter(exploreAdapter);


    }

    private void subscribeObservers() {

        exploreViewModel.getExploreData().observe(getViewLifecycleOwner(), new Observer<ArrayList<ExploreObject>>() {
            @Override
            public void onChanged(ArrayList<ExploreObject> objects) {

                if (objects == null) {
                    exploreAdapter.clearItems();
                } else {
                    setAnimationRecyclerView (objects);
                    listObjects = objects;
                    exploreAdapter.clearItems();
                    exploreAdapter.setItems(listObjects);
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

    private void setAnimationRecyclerView(ArrayList<ExploreObject> objects) {

        boolean animate = false;
        if (exploreAdapter.getItemCount() > 0) {

            for (int i = 0; i < objects.size(); i++) {
                if (!objects.get(i).getImageUrl().equals(exploreAdapter.getItems().get(i).getImageUrl())) {
                    animate = true;
                }
            }
            if (animate) {
                LayoutAnimationController layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.favorites_fall_down);
                exploreRecyclerView.setLayoutAnimation(layoutAnimationController);
                exploreRecyclerView.getAdapter().notifyDataSetChanged();
                exploreRecyclerView.scheduleLayoutAnimation();
            }
        }
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
        if (v.getId() == explore_maker_more.getId()) {
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
        void exploreClickEvent(ExploreObject exploreObject, int position);
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
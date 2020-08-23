package com.company.art_and_culture.myarts.ui.favorites;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.pojo.Art;

import java.util.ArrayList;
import java.util.Collection;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class FavoritesFragment extends Fragment {

    private FavoritesViewModel favoritesViewModel;
    private TextView textView;
    private RecyclerView favoritesRecyclerView;
    private ProgressBar favoritesProgressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private android.content.res.Resources res;
    private FavoritesAdapter favoritesAdapter;
    private FavoritesEventListener favoritesEventListener;
    private int scrollPosition = 0;
    private int scrollPixels = 0;
    private int spanCount = 3;
    private Collection<Art> listArts;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_favorites, container, false);
        textView = root.findViewById(R.id.text_favorites);
        favoritesRecyclerView = root.findViewById(R.id.recycler_view_favorites);
        favoritesProgressBar = root.findViewById(R.id.progress_bar_favorites);
        swipeRefreshLayout = root.findViewById(R.id.favorites_swipeRefreshLayout);

        favoritesViewModel =new ViewModelProvider(this).get(FavoritesViewModel.class);

        res = getResources();
        int displayWidth = res.getDisplayMetrics().widthPixels;
        int displayHeight = res.getDisplayMetrics().heightPixels;

        initRecyclerView(displayWidth, displayHeight);

        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) scrollPosition = activity.getFavoritesPosition();
        if (scrollPosition >= 0) favoritesRecyclerView.scrollToPosition(scrollPosition);
        scrollPixels = (scrollPosition/spanCount) *(displayWidth/spanCount);

        initSwipeRefreshLayout();
        subscribeObservers();

        return root;
    }

    public boolean refresh () {
        return favoritesViewModel.refresh();
    }

    private void initRecyclerView(int displayWidth, int displayHeight) {

        FavoritesAdapter.OnArtClickListener onArtClickListener = new FavoritesAdapter.OnArtClickListener() {
            @Override
            public void onArtImageClick(Art art, int position) {
                favoritesEventListener.favoritesClickEvent(listArts, position);
            }
        };
        favoritesAdapter = new FavoritesAdapter(getContext(), onArtClickListener, displayWidth, displayHeight, spanCount);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), spanCount);
        favoritesRecyclerView.setLayoutManager(layoutManager);
        favoritesRecyclerView.setAdapter(favoritesAdapter);
        favoritesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                scrollPixels = scrollPixels + dy;
            }
        });
    }

    private void subscribeObservers() {

        favoritesViewModel.getArtList().observe(getViewLifecycleOwner(), new Observer<ArrayList<Art>>() {
            @Override
            public void onChanged(ArrayList<Art> arts) {

                if (arts == null) {
                    favoritesAdapter.clearItems();
                    //showText();
                } else {
                    setAnimationRecyclerView (arts);
                    listArts = arts;
                    favoritesAdapter.clearItems();
                    favoritesAdapter.setItems(listArts);
                    //hideText();
                }
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        favoritesViewModel.getIsLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) { showProgressBar(); } else { hideProgressBar(); }
            }
        });
        favoritesViewModel.getIsListEmpty().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) { showText(); } else { hideText(); }
            }
        });
    }

    private void setAnimationRecyclerView(ArrayList<Art> arts) {

        boolean animate = false;
        if (listArts != null) {
            if (arts.size() != favoritesAdapter.getItemCount()) {
                animate = true;
            } else {
                int count = 0;
                if (arts.size() > favoritesAdapter.getItemCount()) {
                    count = favoritesAdapter.getItemCount();
                } else {
                    count = arts.size();
                }
                for (int i = 0; i < count; i++) {
                    if (!arts.get(i).getArtId().equals(favoritesAdapter.getItems().get(i).getArtId())) {
                        animate = true;
                    }
                }
            }
            if (animate) {
                LayoutAnimationController layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.favorites_fall_down);
                favoritesRecyclerView.setLayoutAnimation(layoutAnimationController);
                favoritesRecyclerView.getAdapter().notifyDataSetChanged();
                favoritesRecyclerView.scheduleLayoutAnimation();
            }
        }
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
        favoritesProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar(){
        favoritesProgressBar.setVisibility(View.GONE);
    }

    private void showText(){
        textView.setVisibility(View.VISIBLE);
    }

    private void hideText(){
        textView.setVisibility(View.GONE);
    }

    public interface FavoritesEventListener {
        void favoritesScrollEvent(int position);
        void favoritesClickEvent(Collection<Art> art, int position);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            favoritesEventListener = (FavoritesEventListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement onSomeEventListener");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (favoritesAdapter.getItemCount() > 0) scrollPosition = getTargetScrollPosition();
        favoritesEventListener.favoritesScrollEvent(scrollPosition);
    }

    private int getTargetScrollPosition () {

        int displayWidth = res.getDisplayMetrics().widthPixels;
        int itemHeight = displayWidth/spanCount;

        int scrollPosition = (scrollPixels/itemHeight)* spanCount;

        Log.i("getTargetScrollPosition", scrollPosition+" "+scrollPixels);

        return scrollPosition;
    }

}
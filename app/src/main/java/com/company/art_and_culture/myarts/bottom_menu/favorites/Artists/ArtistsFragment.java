package com.company.art_and_culture.myarts.bottom_menu.favorites.Artists;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.pojo.Maker;

import java.util.ArrayList;

public class ArtistsFragment extends Fragment implements View.OnClickListener {

    private ArtistsViewModel artistsViewModel;
    private ConstraintLayout blank_artists;
    private TextView link_artists;
    private RecyclerView artistsRecyclerView;
    private ProgressBar artistsProgressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private android.content.res.Resources res;
    private int displayWidth, displayHeight;
    private MainActivity activity;
    private ArtistsAdapter artistsAdapter;
    private ArtistsEventListener artistsEventListener;
    private ArrayList<Maker> globalListMakers = new ArrayList<>();
    private String filter = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_favorites_artists, container, false);

        blank_artists = root.findViewById(R.id.blank_artists);
        link_artists = root.findViewById(R.id.link_artists);
        link_artists.setOnClickListener(this);
        artistsRecyclerView = root.findViewById(R.id.recycler_view_artists);
        artistsProgressBar = root.findViewById(R.id.progress_bar_artists);
        swipeRefreshLayout = root.findViewById(R.id.artists_swipeRefreshLayout);

        artistsViewModel =new ViewModelProvider(this).get(ArtistsViewModel.class);

        res = getResources();
        displayWidth = res.getDisplayMetrics().widthPixels;
        displayHeight = res.getDisplayMetrics().heightPixels;

        activity = (MainActivity) getActivity();
        if (activity != null) artistsEventListener = activity.getNavFragments();

        initRecyclerView(displayWidth, displayHeight);
        initSwipeRefreshLayout();
        subscribeObservers();
        hideText();

        return root;
    }

    private void subscribeObservers() {

        artistsViewModel.getMakerList().observe(getViewLifecycleOwner(), new Observer<ArrayList<Maker>>() {
            @Override
            public void onChanged(ArrayList<Maker> makers) {

                if(makers != null) globalListMakers = makers; else globalListMakers.clear();
                activity.postArtistsCount(globalListMakers.size());
                setListMakers(globalListMakers);
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        artistsViewModel.getIsLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) { showProgressBar(); } else { hideProgressBar(); }
            }
        });
        artistsViewModel.getIsListEmpty().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) { showText(); } else { hideText(); }
            }
        });
        if (activity != null) activity.getArtistsFilter().observe(getViewLifecycleOwner(), s -> {
            filter = s;
            setListMakers(globalListMakers);
        });
        activity.getIsUpdateAllAppData().observe(getViewLifecycleOwner(), aBoolean -> {
            if(aBoolean) artistsViewModel.refresh();
        });

    }

    private void setListMakers(ArrayList<Maker> makers) {

        ArrayList<Maker> partialListMakers = new ArrayList<>();
        if(filter.length() > 0) {
            for (Maker maker : makers) {
                if (maker.getArtMaker().toUpperCase().contains(filter.toUpperCase()))
                    partialListMakers.add(maker);
            }
        } else {
            partialListMakers = makers;
        }

        if (partialListMakers.size() == 0) {
            artistsAdapter.clearItems();
        } else {
            setAnimationRecyclerView (partialListMakers);
            artistsAdapter.clearItems();
            artistsAdapter.setItems(partialListMakers);
        }
    }

    private void setAnimationRecyclerView(ArrayList<Maker> makers) {

        boolean animate = false;
        if (artistsAdapter.getItemCount() > 0) {

            if (makers.size() != artistsAdapter.getItemCount()) {
                animate = true;
            } else {

                for (int i = 0; i < makers.size(); i++) {
                    if (!makers.get(i).getArtMaker().equals(artistsAdapter.getItems().get(i).getArtMaker())) {
                        animate = true;
                    }
                }
            }
            if (animate) {
                LayoutAnimationController layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_fall_down);
                artistsRecyclerView.setLayoutAnimation(layoutAnimationController);
                //artistsRecyclerView.getAdapter().notifyDataSetChanged();
                artistsRecyclerView.scheduleLayoutAnimation();
            }
        } else {
            LayoutAnimationController layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_fade_in);
            artistsRecyclerView.setLayoutAnimation(layoutAnimationController);
            //favoritesRecyclerView.getAdapter().notifyDataSetChanged();
            artistsRecyclerView.scheduleLayoutAnimation();
        }
    }

    private void initRecyclerView(int displayWidth, int displayHeight) {

        ArtistsAdapter.OnMakerClickListener onMakerClickListener = new ArtistsAdapter.OnMakerClickListener() {
            @Override
            public void onMakerImageClick(Maker maker, int position) {
                artistsEventListener.artistsClickEvent(maker);
            }
        };
        artistsAdapter = new ArtistsAdapter(getContext(), onMakerClickListener, displayWidth, displayHeight);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        artistsRecyclerView.setLayoutManager(layoutManager);
        artistsRecyclerView.setAdapter(artistsAdapter);
    }

    private void initSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                boolean networkState = artistsViewModel.refresh();
                if (!networkState) {
                    Toast.makeText(getContext(), R.string.network_is_unavailable, Toast.LENGTH_LONG).show();
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorBlue
        );

    }

    @Override
    public void onClick(View v) {
        if(v.getId() == link_artists.getId()) {
            artistsEventListener.showArtistsClick();
        }
    }

    public interface ArtistsEventListener {
        void artistsClickEvent(Maker maker);
        void showArtistsClick();
    }

    private void showProgressBar(){
        artistsProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar(){
        artistsProgressBar.setVisibility(View.GONE);
    }

    private void showText(){
        blank_artists.setVisibility(View.VISIBLE);
    }

    private void hideText(){
        blank_artists.setVisibility(View.GONE);
    }

    public void scrollRecyclerViewToStart () {
        artistsRecyclerView.smoothScrollToPosition(0);
    }

    public int getTargetScrollPosition () {

        return ((LinearLayoutManager) artistsRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
    }


}

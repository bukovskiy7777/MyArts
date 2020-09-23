package com.company.art_and_culture.myarts.ui.favorites;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.pojo.Maker;

import java.util.ArrayList;
import java.util.Collection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class ArtistsFragment extends Fragment {

    private ArtistsViewModel artistsViewModel;
    private TextView textView;
    private RecyclerView artistsRecyclerView;
    private ProgressBar artistsProgressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private android.content.res.Resources res;
    private int displayWidth, displayHeight;
    private MainActivity activity;
    private ArtistsAdapter artistsAdapter;
    private ArtistsEventListener artistsEventListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_favorites_artists, container, false);

        textView = root.findViewById(R.id.text_artists);
        artistsRecyclerView = root.findViewById(R.id.recycler_view_artists);
        artistsProgressBar = root.findViewById(R.id.progress_bar_artists);
        swipeRefreshLayout = root.findViewById(R.id.artists_swipeRefreshLayout);

        artistsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int offset = 0;
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                offset = offset + dy;
            }
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if(newState == 2) {
                    if (offset >= 10) {
                        artistsEventListener.artistsOnScroll(1);
                    } else if (offset < -10) {
                        artistsEventListener.artistsOnScroll(0);
                    }
                }
                offset = 0;
            }
        });

        artistsViewModel =new ViewModelProvider(this).get(ArtistsViewModel.class);

        res = getResources();
        displayWidth = res.getDisplayMetrics().widthPixels;
        displayHeight = res.getDisplayMetrics().heightPixels;

        activity = (MainActivity) getActivity();

        initRecyclerView(displayWidth, displayHeight);
        initSwipeRefreshLayout();
        subscribeObservers();

        return root;
    }

    private void subscribeObservers() {

        artistsViewModel.getMakerList().observe(getViewLifecycleOwner(), new Observer<ArrayList<Maker>>() {
            @Override
            public void onChanged(ArrayList<Maker> makers) {
                if (makers == null) {
                    artistsAdapter.clearItems();
                } else {
                    artistsAdapter.clearItems();
                    artistsAdapter.setItems(makers);
                }
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

    public boolean refresh () {
        return artistsViewModel.refresh();
    }

    public interface ArtistsEventListener {
        void artistsClickEvent(Maker maker);
        void artistsOnScroll(int direction);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            artistsEventListener = (ArtistsEventListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement onSomeEventListener");
        }
    }

    private void showProgressBar(){
        artistsProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar(){
        artistsProgressBar.setVisibility(View.GONE);
    }

    private void showText(){
        textView.setVisibility(View.VISIBLE);
    }

    private void hideText(){
        textView.setVisibility(View.GONE);
    }




}

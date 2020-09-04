package com.company.art_and_culture.myarts.art_medium_fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MediumFragment extends Fragment {

    private MediumViewModel mediumViewModel;
    private RecyclerView mediumRecyclerView;
    private MediumAdapter mediumAdapter;
    private ProgressBar mediumProgressBar;
    private TextView textView, appbar_medium;
    private MediumEventListener mediumEventListener;
    private SwipeRefreshLayout swipeRefreshLayout;
    private android.content.res.Resources res;
    private MainActivity activity;
    private String artQuery, queryType;
    private int spanCount = 2;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_art_medium, container, false);
        textView = root.findViewById(R.id.text_medium);
        mediumRecyclerView = root.findViewById(R.id.recycler_view_medium);
        mediumProgressBar = root.findViewById(R.id.progress_bar_medium);
        swipeRefreshLayout = root.findViewById(R.id.medium_swipeRefreshLayout);
        appbar_medium = root.findViewById(R.id.appbar_medium);

        mediumViewModel = new ViewModelProvider(this).get(MediumViewModel.class);

        res = getResources();
        int displayWidth = res.getDisplayMetrics().widthPixels;
        int displayHeight = res.getDisplayMetrics().heightPixels;

        initRecyclerView(mediumViewModel, displayWidth, displayHeight);

        activity = (MainActivity) getActivity();

        if (activity != null) artQuery = activity.getArtQueryForMediumFragment();
        if (activity != null) queryType = activity.getQueryTypeForMediumFragment();
        appbar_medium.setText(artQuery);

        mediumViewModel.setArtQueryAndType(artQuery, queryType);

        initSwipeRefreshLayout();
        subscribeObservers();
        setOnBackPressedListener(root);

        return root;
    }

    private void setOnBackPressedListener(final View root) {
        //You need to add the following line for this solution to work; thanks skayred
        root.setFocusableInTouchMode(true);
        root.requestFocus();
        root.setOnKeyListener( new View.OnKeyListener() {
            @Override
            public boolean onKey( View v, int keyCode, KeyEvent event ) {

                if( keyCode == KeyEvent.KEYCODE_BACK && activity.getArtShowFragment() == null ) {
                    int scrollPosition = 0;
                    if (mediumAdapter.getItemCount() > 0) scrollPosition = getTargetScrollPosition();
                    if (scrollPosition > 4) {
                        mediumRecyclerView.smoothScrollToPosition(0);
                        return true;
                    }
                    return false;
                }
                return false;
            }
        } );
    }

    private void initSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                boolean networkState = mediumViewModel.refresh();
                if (!networkState) {
                    Toast.makeText(getContext(), R.string.network_is_unavailable, Toast.LENGTH_LONG).show();
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );

    }

    private void subscribeObservers() {

        mediumViewModel.getArtList().observe(getViewLifecycleOwner(), new Observer<PagedList<Art>>() {
            @Override
            public void onChanged(PagedList<Art> arts) {
                mediumAdapter.submitList(arts);
                hideText();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        mediumViewModel.getIsLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) { showProgressBar(); } else { hideProgressBar(); }
            }
        });
        mediumViewModel.getIsListEmpty().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) { showText(); } else { hideText(); }
            }
        });
    }

    private void initRecyclerView(final MediumViewModel mediumViewModel, int displayWidth, int displayHeight){

        MediumAdapter.OnArtClickListener onArtClickListener = new MediumAdapter.OnArtClickListener() {

            @Override
            public void onArtImageClick(Art art, int position) {
                ArrayList<Art> artInMemory = MediumDataInMemory.getInstance().getAllData();
                mediumEventListener.mediumArtClickEvent(artInMemory, position);
            }

        };

        mediumAdapter = new MediumAdapter(mediumViewModel, getContext(), onArtClickListener, displayWidth, displayHeight, spanCount);
        RecyclerView.LayoutManager layoutManager = new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL);
        mediumRecyclerView.setLayoutManager(layoutManager);
        mediumRecyclerView.setAdapter(mediumAdapter);
    }

    private void showProgressBar(){
        mediumProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar(){
        mediumProgressBar.setVisibility(View.GONE);
    }

    private void showText(){
        textView.setVisibility(View.VISIBLE);
    }

    private void hideText(){
        textView.setVisibility(View.GONE);
    }

    public interface MediumEventListener {
        void mediumArtClickEvent(Collection<Art> arts, int position);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mediumEventListener = (MediumEventListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement onSomeEventListener");
        }
    }

    private int getTargetScrollPosition () {

        int scrollPosition = ((StaggeredGridLayoutManager) mediumRecyclerView.getLayoutManager()).findFirstVisibleItemPositions(null)[0];

        return scrollPosition;
    }

    public void finish() {
        mediumViewModel.finish ();
    }


}
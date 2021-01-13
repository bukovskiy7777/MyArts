package com.company.art_and_culture.myarts.art_medium_fragment;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.pojo.FilterObject;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collection;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MediumFragment extends Fragment implements View.OnClickListener {

    private MediumViewModel mediumViewModel;
    private RecyclerView mediumRecyclerView;
    private MediumAdapter mediumAdapter;
    private ProgressBar mediumProgressBar;
    private TextView textView, appbar_medium;
    private MediumEventListener mediumEventListener;
    private SwipeRefreshLayout swipeRefreshLayout;
    private android.content.res.Resources res;
    private MainActivity activity;
    private String keyword = "", makerFilter = "", centuryFilter = "";
    private String keywordType = "";
    private int spanCount = 2;
    private FloatingActionButton floatingActionButton;
    private FrameLayout black_layout;
    private FilterAdapter filterMakerAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_art_medium, container, false);
        textView = root.findViewById(R.id.text_medium);
        mediumRecyclerView = root.findViewById(R.id.recycler_view_medium);
        mediumProgressBar = root.findViewById(R.id.progress_bar_medium);
        swipeRefreshLayout = root.findViewById(R.id.medium_swipeRefreshLayout);
        appbar_medium = root.findViewById(R.id.appbar_medium);
        floatingActionButton = root.findViewById(R.id.floating_button);
        floatingActionButton.setOnClickListener(this);
        black_layout = root.findViewById(R.id.black_layout);
        black_layout.setOnClickListener(this);
        black_layout.setVisibility(View.GONE);

        mediumViewModel = new ViewModelProvider(this).get(MediumViewModel.class);

        res = getResources();
        int displayWidth = res.getDisplayMetrics().widthPixels;
        int displayHeight = res.getDisplayMetrics().heightPixels;

        initRecyclerView(mediumViewModel, displayWidth, displayHeight);
        initFiltersRecyclerView(root);

        activity = (MainActivity) getActivity();

        if (activity != null) keyword = activity.getNavFragments().getArtQueryForMediumFragment();
        if (activity != null) keywordType = activity.getNavFragments().getQueryTypeForMediumFragment();
        if (activity != null) mediumEventListener = activity.getNavFragments();
        appbar_medium.setText(keyword);

        mediumViewModel.setFilters(keyword, makerFilter, centuryFilter, keywordType);
        mediumViewModel.setActivity(activity);

        initSwipeRefreshLayout();
        subscribeObservers();
        setOnBackPressedListener(root);

        return root;
    }

    @Override
    public void onDestroy() {
        keyword = ""; makerFilter = ""; centuryFilter = ""; keywordType = "";
        mediumViewModel.setFilters(keyword, makerFilter, centuryFilter, keywordType);
        super.onDestroy();
    }

    private void initFiltersRecyclerView(View root) {
        FlexboxLayoutManager layoutManagerArtists = new FlexboxLayoutManager(getContext());
        layoutManagerArtists.setFlexDirection(FlexDirection.ROW);
        layoutManagerArtists.setJustifyContent(JustifyContent.CENTER);
        RecyclerView recycler_view_artists = root.findViewById(R.id.recycler_view_artists);
        recycler_view_artists.setLayoutManager(layoutManagerArtists);

        FlexboxLayoutManager layoutManagerCenturies = new FlexboxLayoutManager(getContext());
        layoutManagerCenturies.setFlexDirection(FlexDirection.ROW);
        layoutManagerCenturies.setJustifyContent(JustifyContent.CENTER);
        RecyclerView recycler_view_centuries = root.findViewById(R.id.recycler_view_centuries);
        recycler_view_centuries.setLayoutManager(layoutManagerCenturies);

        FlexboxLayoutManager layoutManagerKeywords = new FlexboxLayoutManager(getContext());
        layoutManagerKeywords.setFlexDirection(FlexDirection.ROW);
        layoutManagerKeywords.setJustifyContent(JustifyContent.CENTER);
        RecyclerView recycler_view_keywords = root.findViewById(R.id.recycler_view_keywords);
        recycler_view_keywords.setLayoutManager(layoutManagerKeywords);

        FilterAdapter.OnFilterClickListener onMakerClickListener = new FilterAdapter.OnFilterClickListener() {
            @Override
            public void onFilterClick(FilterObject filterObject, int position) {
                makerFilter = filterObject.getText();
                ArrayList<String> listString = new ArrayList<>();
                for(int i = 0; i < filterMakerAdapter.getItemCount(); i++) {
                    listString.add(filterMakerAdapter.getItems().get(i).getText());
                }
                setListMakerFilters(listString);
                mediumViewModel.setFilters(keyword, makerFilter, centuryFilter, keywordType);
            }
        };
        filterMakerAdapter = new FilterAdapter(getContext(), onMakerClickListener);
        recycler_view_artists.setAdapter(filterMakerAdapter);
    }

    private void setOnBackPressedListener(final View root) {
        //You need to add the following line for this solution to work; thanks skayred
        root.setFocusableInTouchMode(true);
        root.requestFocus();
        root.setOnKeyListener( new View.OnKeyListener() {
            @Override
            public boolean onKey( View v, int keyCode, KeyEvent event ) {

                if( keyCode == KeyEvent.KEYCODE_BACK ) {
                    if(black_layout.isShown()) {
                        goneFilterViews();
                        return true;
                    } else {
                        int scrollPosition = 0;
                        if (mediumAdapter.getItemCount() > 0) scrollPosition = getTargetScrollPosition();
                        if (scrollPosition > 4) {
                            mediumRecyclerView.smoothScrollToPosition(0);
                            return true;
                        }
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
        mediumViewModel.getListMakerFilters().observe(getViewLifecycleOwner(), new Observer<ArrayList<String>>() {
            @Override
            public void onChanged(ArrayList<String> listStrings) {
                if (listStrings == null) {
                    filterMakerAdapter.clearItems();
                } else {
                    //listStrings.add(0, res.getString(R.string.all));
                    setListMakerFilters(listStrings);
                }
            }
        });
    }

    private void setListMakerFilters(ArrayList<String> listStrings) {
        ArrayList<FilterObject> listMakerFilters = new ArrayList<>();
        for(int i = 0; i < listStrings.size(); i++) {
            if(listStrings.get(i).equals(makerFilter))
                listMakerFilters.add(new FilterObject(listStrings.get(i), true));
            else
                listMakerFilters.add(new FilterObject(listStrings.get(i), false));
        }
        filterMakerAdapter.clearItems();
        filterMakerAdapter.setItems(listMakerFilters);
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

    @Override
    public void onClick(View view) {
        if(view.getId() == floatingActionButton.getId()) {
            if(black_layout.isShown()) {
                goneFilterViews();
            } else {
                Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.enter_fade_in);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) { }
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        black_layout.setVisibility(View.VISIBLE);
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) { }
                });
                black_layout.startAnimation(animation);
                activity.getWindow().setStatusBarColor(res.getColor(R.color.colorText));
                black_layout.setVisibility(View.VISIBLE);
            }
        } else if (view.getId() == black_layout.getId()) {
            goneFilterViews();
        }
    }

    private void goneFilterViews() {
        black_layout.setVisibility(View.GONE);
        activity.getWindow().setStatusBarColor(res.getColor(R.color.colorPrimaryDark));
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.exit_fade_out);
        black_layout.startAnimation(animation);
    }

    public interface MediumEventListener {
        void mediumArtClickEvent(Collection<Art> arts, int position);
    }

    private int getTargetScrollPosition () {

        int scrollPosition = ((StaggeredGridLayoutManager) mediumRecyclerView.getLayoutManager()).findFirstVisibleItemPositions(null)[0];

        return scrollPosition;
    }

}
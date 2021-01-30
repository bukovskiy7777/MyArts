package com.company.art_and_culture.myarts.art_medium_fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.company.art_and_culture.myarts.Constants;
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
import java.util.List;

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
    private RecyclerView mediumRecyclerView, recycler_view_artists, recycler_view_keywords, recycler_view_centuries;
    private MediumAdapter mediumAdapter;
    private ProgressBar mediumProgressBar, progress_bar_filter, progress_bar_artist, progress_bar_century, progress_bar_keywords;
    private TextView textView, appbar_medium;
    private MediumEventListener mediumEventListener;
    private SwipeRefreshLayout swipeRefreshLayout;
    private android.content.res.Resources res;
    private MainActivity activity;
    private String keyword, makerFilter = "", centuryFilter = "";
    private String keywordType;
    private int spanCount = 2;
    private FloatingActionButton floatingActionButton;
    private FrameLayout black_layout;
    private FilterAdapter filterMakerAdapter, filterCenturyAdapter, filterKeywordAdapter;
    private FilterAdapter.OnFilterClickListener onCenturyClickListener, onMakerClickListener, onKeywordClickListener;
    private ImageView clear_artist_tv, clear_century_tv, clear_keywords_tv;

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

        clear_artist_tv = root.findViewById(R.id.clear_artist_tv);
        clear_artist_tv.setOnClickListener(this);
        clear_artist_tv.setVisibility(View.GONE);
        clear_century_tv = root.findViewById(R.id.clear_century_tv);
        clear_century_tv.setOnClickListener(this);
        clear_century_tv.setVisibility(View.GONE);
        clear_keywords_tv = root.findViewById(R.id.clear_keywords_tv);
        clear_keywords_tv.setOnClickListener(this);
        clear_keywords_tv.setVisibility(View.GONE);

        progress_bar_filter = root.findViewById(R.id.progress_bar_filter);
        progress_bar_artist = root.findViewById(R.id.progress_bar_artist);
        progress_bar_century = root.findViewById(R.id.progress_bar_century);
        progress_bar_keywords = root.findViewById(R.id.progress_bar_keywords);
        setFilterProgressesVisibility(View.GONE);

        mediumViewModel = new ViewModelProvider(this).get(MediumViewModel.class);

        res = getResources();
        int displayWidth = res.getDisplayMetrics().widthPixels;
        int displayHeight = res.getDisplayMetrics().heightPixels;

        initRecyclerView(mediumViewModel, displayWidth, displayHeight);
        initFiltersRecyclerView(root);

        activity = (MainActivity) getActivity();

        if (activity != null && keyword == null) keyword = activity.getNavFragments().getArtQueryForMediumFragment();
        if (activity != null && keywordType == null) keywordType = activity.getNavFragments().getQueryTypeForMediumFragment();
        if (activity != null) mediumEventListener = activity.getNavFragments();
        setAppBarText();

        mediumViewModel.setFilters(keyword, makerFilter, centuryFilter, keywordType);
        mediumViewModel.setActivity(activity);

        initSwipeRefreshLayout();
        subscribeObservers();
        setOnBackPressedListener(root);

        return root;
    }

    private void setAppBarText() {
        List<String> list = new ArrayList<>();
        list.add(makerFilter); list.add(keyword); if(centuryFilter.length() > 0) list.add(centuryFilter+res.getString(R.string.century));
        appbar_medium.setText(getJoinedString(list,", "));
    }

    private void setFilterProgressesVisibility(int visible) {
        progress_bar_filter.setVisibility(visible);
        progress_bar_artist.setVisibility(visible);
        progress_bar_century.setVisibility(visible);
        progress_bar_keywords.setVisibility(visible);
    }

    public static String getJoinedString(List<String> list, String delim) {
        StringBuilder sb = new StringBuilder();
        String loopDelim = "";
        for(String s : list) {
            if(s.length() > 0) {
                sb.append(loopDelim);
                sb.append(s);
                loopDelim = delim;
            }
        }
        return sb.toString();
    }

    @Override
    public void onDestroy() {
        keyword = ""; makerFilter = ""; centuryFilter = ""; keywordType = "";
        mediumViewModel.setFilters(keyword, makerFilter, centuryFilter, keywordType);
        super.onDestroy();
    }

    private void initFiltersRecyclerView(View root) {
        FlexboxLayoutManager layoutManagerArtists = new FlexboxLayoutManager(getContext());
        layoutManagerArtists.setFlexDirection(FlexDirection.ROW); layoutManagerArtists.setJustifyContent(JustifyContent.CENTER);
        recycler_view_artists = root.findViewById(R.id.recycler_view_artists);
        recycler_view_artists.setLayoutManager(layoutManagerArtists);

        FlexboxLayoutManager layoutManagerCenturies = new FlexboxLayoutManager(getContext());
        layoutManagerCenturies.setFlexDirection(FlexDirection.ROW); layoutManagerCenturies.setJustifyContent(JustifyContent.CENTER);
        recycler_view_centuries = root.findViewById(R.id.recycler_view_centuries);
        recycler_view_centuries.setLayoutManager(layoutManagerCenturies);

        FlexboxLayoutManager layoutManagerKeywords = new FlexboxLayoutManager(getContext());
        layoutManagerKeywords.setFlexDirection(FlexDirection.ROW); layoutManagerKeywords.setJustifyContent(JustifyContent.CENTER);
        recycler_view_keywords = root.findViewById(R.id.recycler_view_keywords);
        recycler_view_keywords.setLayoutManager(layoutManagerKeywords);

        onMakerClickListener = new FilterAdapter.OnFilterClickListener() {
            @Override
            public void onFilterClick(FilterObject filterObject, int position) {
                if(!makerFilter.equals(filterObject.getText())) {
                    makerFilter = filterObject.getText();
                    clear_artist_tv.setVisibility(View.VISIBLE);
                    setFilterProgressesVisibility(View.VISIBLE);
                    setAppBarText();
                }
                mediumViewModel.setFilters(keyword, makerFilter, centuryFilter, keywordType);
            }
        };

        onCenturyClickListener = new FilterAdapter.OnFilterClickListener() {
            @Override
            public void onFilterClick(FilterObject filterObject, int position) {
                if(!centuryFilter.equals(filterObject.getText())) {
                    centuryFilter = filterObject.getText();
                    clear_century_tv.setVisibility(View.VISIBLE);
                    setFilterProgressesVisibility(View.VISIBLE);
                    setAppBarText();
                }
                mediumViewModel.setFilters(keyword, makerFilter, centuryFilter, keywordType);
            }
        };

        onKeywordClickListener = new FilterAdapter.OnFilterClickListener() {
            @Override
            public void onFilterClick(FilterObject filterObject, int position) {
                if(!keyword.toUpperCase().equals(filterObject.getText().toUpperCase())) {
                    keyword = filterObject.getText();
                    keywordType = filterObject.getType();
                    clear_keywords_tv.setVisibility(View.VISIBLE);
                    setFilterProgressesVisibility(View.VISIBLE);
                    setAppBarText();
                }
                mediumViewModel.setFilters(keyword, makerFilter, centuryFilter, keywordType);
            }
        };
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
                if (listStrings == null)
                    filterMakerAdapter.clearItems();
                 else
                    setListMakerFilters(listStrings);
            }
        });
        mediumViewModel.getListCenturyFilters().observe(getViewLifecycleOwner(), new Observer<ArrayList<String>>() {
            @Override
            public void onChanged(ArrayList<String> listStrings) {
                if (listStrings == null)
                    filterCenturyAdapter.clearItems();
                 else
                    setListCenturyFilters(listStrings);
            }
        });
        mediumViewModel.getListKeywordFilters().observe(getViewLifecycleOwner(), new Observer<ArrayList<FilterObject>>() {
            @Override
            public void onChanged(ArrayList<FilterObject> listKeywords) {
                if (listKeywords == null)
                    filterKeywordAdapter.clearItems();
                else
                    setListKeywordFilters(listKeywords);
            }
        });
    }

    private void setListKeywordFilters(final List<FilterObject> listKeywords) {

        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final ArrayList<FilterObject> listKeywordFilters = new ArrayList<>();
                int position = -1;
                for (FilterObject filter : listKeywords) {
                    if (keyword.toUpperCase().equals(filter.getText().toUpperCase())) {
                        position = listKeywords.indexOf(filter);
                        listKeywordFilters.add(new FilterObject(filter.getText(), true, filter.getType()));
                    } else
                        listKeywordFilters.add(new FilterObject(filter.getText(), false, filter.getType()));
                }
                final int finalPosition = position;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        filterKeywordAdapter = new FilterAdapter(getContext(), onKeywordClickListener);
                        recycler_view_keywords.setAdapter(filterKeywordAdapter);
                        filterKeywordAdapter.setItems(listKeywordFilters);
                        if(finalPosition >= 0) recycler_view_keywords.scrollToPosition(finalPosition);
                        setFilterProgressesVisibility(View.GONE);
                    }
                });
            }
        }).start();
        if(keyword.length() ==0) clear_keywords_tv.setVisibility(View.GONE); else clear_keywords_tv.setVisibility(View.VISIBLE);
    }

    private void setListMakerFilters(final ArrayList<String> listStrings) {

        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final ArrayList<FilterObject> listMakerFilters = new ArrayList<>();
                int position = -1;
                for (String maker : listStrings) {
                    if (maker.equals(makerFilter)) {
                        position = listStrings.indexOf(maker);
                        listMakerFilters.add(new FilterObject(maker, true, Constants.ART_MAKER));
                    } else
                        listMakerFilters.add(new FilterObject(maker, false, Constants.ART_MAKER));
                }
                final int finalPosition = position;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        filterMakerAdapter = new FilterAdapter(getContext(), onMakerClickListener);
                        recycler_view_artists.setAdapter(filterMakerAdapter);
                        filterMakerAdapter.setItems(listMakerFilters);
                        if(finalPosition >= 0) recycler_view_artists.scrollToPosition(finalPosition);
                        setFilterProgressesVisibility(View.GONE);
                    }
                });
            }
        }).start();
        if(makerFilter.length() ==0) clear_artist_tv.setVisibility(View.GONE); else clear_artist_tv.setVisibility(View.VISIBLE);
    }

    private void setListCenturyFilters(ArrayList<String> listStrings) {
        ArrayList<FilterObject> listCenturyFilters = new ArrayList<>();
        for(String century : listStrings) {
            if(century.equals(centuryFilter))
                listCenturyFilters.add(new FilterObject(century, true, Constants.ART_CENTURY));
            else
                listCenturyFilters.add(new FilterObject(century, false, Constants.ART_CENTURY));
        }
        filterCenturyAdapter = new FilterAdapter(getContext(), onCenturyClickListener);
        recycler_view_centuries.setAdapter(filterCenturyAdapter);
        filterCenturyAdapter.setItems(listCenturyFilters);
        if(centuryFilter.length() ==0) clear_century_tv.setVisibility(View.GONE); else clear_century_tv.setVisibility(View.VISIBLE);
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
        } else if (view.getId() == clear_artist_tv.getId()) {
            makerFilter = "";
            setFilterProgressesVisibility(View.VISIBLE);
            setAppBarText();

            ArrayList<String> listString = new ArrayList<>();
            for(FilterObject filter : filterMakerAdapter.getItems()) {
                listString.add(filter.getText());
            }
            setListMakerFilters(listString);
            mediumViewModel.setFilters(keyword, makerFilter, centuryFilter, keywordType);

        } else if (view.getId() == clear_century_tv.getId()) {
            centuryFilter = "";
            setFilterProgressesVisibility(View.VISIBLE);
            setAppBarText();

            ArrayList<String> listString = new ArrayList<>();
            for(FilterObject filter : filterCenturyAdapter.getItems()) {
                listString.add(filter.getText());
            }
            setListCenturyFilters(listString);
            mediumViewModel.setFilters(keyword, makerFilter, centuryFilter, keywordType);

        } else if (view.getId() == clear_keywords_tv.getId()) {
            keyword = ""; keywordType = "";
            mediumViewModel.setFilters(keyword, makerFilter, centuryFilter, keywordType);
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
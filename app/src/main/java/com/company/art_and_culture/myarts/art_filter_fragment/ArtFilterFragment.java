package com.company.art_and_culture.myarts.art_filter_fragment;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
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

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.arts_show_fragment.ArtShowFragment;
import com.company.art_and_culture.myarts.pojo.FilterObject;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ArtFilterFragment extends Fragment implements View.OnClickListener {

    public static final String KEYWORD = "keyword";
    public static final String KEYWORD_TYPE = "keywordType";

    private ArtFilterViewModel artFilterViewModel;
    private RecyclerView recycler_view_art_filter, recycler_view_artists, recycler_view_keywords, recycler_view_centuries;
    private ArtFilterAdapter artFilterAdapter;
    private ProgressBar progress_bar_art_filter, progress_bar_filter, progress_bar_artist, progress_bar_century, progress_bar_keywords;
    private TextView textView, appbar_art_filter, appbar_art_count;
    private SwipeRefreshLayout art_filter_swipeRefreshLayout;
    private android.content.res.Resources res;
    private MainActivity activity;
    private String keyword = null, keywordType = null, makerFilter = "", centuryFilter = "";
    private int spanCount = 2;
    private FloatingActionButton floatingActionButton;
    private FrameLayout black_layout;
    private FilterAdapter filterMakerAdapter, filterCenturyAdapter, filterKeywordAdapter;
    private FilterAdapter.OnFilterClickListener onCenturyClickListener, onMakerClickListener, onKeywordClickListener;
    private ImageView clear_artist_tv, clear_century_tv, clear_keywords_tv;
    private ImageView search_artist_iv, search_keywords_iv;
    private boolean artistSearchPressed = false, keywordSearchPressed = false;
    private AppCompatEditText filter_artist_edit_text, filter_keyword_edit_text;
    private ArrayList<String> globalListArtists = new ArrayList<>();
    private ArrayList<FilterObject> globalListKeywords = new ArrayList<>();
    private ConstraintLayout filters_container;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_art_filter, container, false);
        bindViews(root);
        setFilterProgressesVisibility(View.GONE);

        artFilterViewModel = new ViewModelProvider(this).get(ArtFilterViewModel.class);

        res = getResources();
        int displayWidth = res.getDisplayMetrics().widthPixels;
        int displayHeight = res.getDisplayMetrics().heightPixels;

        keyword = getArguments().getString(KEYWORD);
        keywordType = getArguments().getString(KEYWORD_TYPE);

        activity = (MainActivity) getActivity();

        if(keyword == null) {
            NavHostFragment.findNavController(ArtFilterFragment.this).popBackStack();
        } else {
            artFilterViewModel.setFilters(keyword, makerFilter, centuryFilter, keywordType);
            ArtFilterDataInMemory.getInstance().setArtObserver(activity);
            subscribeObservers();

            initEditTexts();
            initRecyclerView(artFilterViewModel, displayWidth, displayHeight);
            initFiltersRecyclerView(root);
            setAppBarText();
            initSwipeRefreshLayout();
            setOnBackPressedListener(root);
        }

        return root;
    }

    @Override
    public void onDestroy() {
        ArtFilterDataInMemory.getInstance().refresh();
        super.onDestroy();
    }

    private void bindViews(View root) {
        textView = root.findViewById(R.id.text_art_filter);
        recycler_view_art_filter = root.findViewById(R.id.recycler_view_art_filter);
        progress_bar_art_filter = root.findViewById(R.id.progress_bar_art_filter);
        art_filter_swipeRefreshLayout = root.findViewById(R.id.art_filter_swipeRefreshLayout);
        appbar_art_count = root.findViewById(R.id.appbar_art_count);
        appbar_art_filter = root.findViewById(R.id.appbar_art_filter);
        floatingActionButton = root.findViewById(R.id.floating_button);
        floatingActionButton.setOnClickListener(this);
        black_layout = root.findViewById(R.id.black_layout);
        black_layout.setOnClickListener(this);
        black_layout.setVisibility(View.GONE);
        filters_container = root.findViewById(R.id.filters_container);

        clear_artist_tv = root.findViewById(R.id.clear_artist_tv);
        clear_artist_tv.setOnClickListener(this);
        clear_artist_tv.setVisibility(View.GONE);
        clear_century_tv = root.findViewById(R.id.clear_century_tv);
        clear_century_tv.setOnClickListener(this);
        clear_century_tv.setVisibility(View.GONE);
        clear_keywords_tv = root.findViewById(R.id.clear_keywords_tv);
        clear_keywords_tv.setOnClickListener(this);
        clear_keywords_tv.setVisibility(View.GONE);

        search_artist_iv = root.findViewById(R.id.search_artist_iv);
        search_artist_iv.setOnClickListener(this);
        search_keywords_iv = root.findViewById(R.id.search_keywords_iv);
        search_keywords_iv.setOnClickListener(this);

        progress_bar_filter = root.findViewById(R.id.progress_bar_filter);
        progress_bar_artist = root.findViewById(R.id.progress_bar_artist);
        progress_bar_century = root.findViewById(R.id.progress_bar_century);
        progress_bar_keywords = root.findViewById(R.id.progress_bar_keywords);

        filter_artist_edit_text = root.findViewById(R.id.filter_artist_edit_text);
        filter_keyword_edit_text = root.findViewById(R.id.filter_keyword_edit_text);
        filter_artist_edit_text.setVisibility(View.GONE);
        filter_keyword_edit_text.setVisibility(View.GONE);
        filter_artist_edit_text.setOnFocusChangeListener((v, hasFocus) -> {
            int density = (int) (getResources().getDisplayMetrics().densityDpi/160f);
            ConstraintLayout.MarginLayoutParams marginLayoutParams = (ConstraintLayout.MarginLayoutParams) filters_container.getLayoutParams();
            if (hasFocus) {
                marginLayoutParams.setMargins(30*density, 10*density, 30*density, 10*density);
                filters_container.setLayoutParams(marginLayoutParams);
                floatingActionButton.setVisibility(View.GONE);
            } else {
                marginLayoutParams.setMargins(30*density, 60*density, 30*density, 80*density);
                filters_container.setLayoutParams(marginLayoutParams);
                floatingActionButton.setVisibility(View.VISIBLE);
            }
        });
        filter_keyword_edit_text.setOnFocusChangeListener((v, hasFocus) -> {
            int density = (int) (getResources().getDisplayMetrics().densityDpi/160f);
            ConstraintLayout.MarginLayoutParams marginLayoutParams = (ConstraintLayout.MarginLayoutParams) filters_container.getLayoutParams();
            if (hasFocus) {
                marginLayoutParams.setMargins(30*density, 10*density, 30*density, 10*density);
                filters_container.setLayoutParams(marginLayoutParams);
                floatingActionButton.setVisibility(View.GONE);
            } else {
                marginLayoutParams.setMargins(30*density, 60*density, 30*density, 80*density);
                filters_container.setLayoutParams(marginLayoutParams);
                floatingActionButton.setVisibility(View.VISIBLE);
            }
        });
    }

    private void initEditTexts() {
        filter_artist_edit_text.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                filter_artist_edit_text.clearFocus();
            }
            return false;
        });
        filter_artist_edit_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            private Timer timer=new Timer();
            private final long DELAY = 700; // milliseconds
            @Override
            public void afterTextChanged(Editable editable) {
                if(filter_artist_edit_text.getText().length() > 0) {
                    final Handler handler = new Handler();
                    timer.schedule(
                            new TimerTask() {
                                @Override
                                public void run() {
                                    String filter = filter_artist_edit_text.getText().toString();
                                    new Thread(() -> {
                                        ArrayList<String> partialListArtists = new ArrayList<>();
                                        for (String artist : globalListArtists) {
                                            if (artist.toUpperCase().contains(filter.toUpperCase()))
                                                partialListArtists.add(artist);
                                        }
                                        handler.post(() -> {
                                            setListMakerFilters(partialListArtists);
                                        });
                                    }).start();
                                }
                            }, DELAY);

                } else {
                    setListMakerFilters(globalListArtists);
                }
            }
        });

        filter_keyword_edit_text.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                filter_keyword_edit_text.clearFocus();
            }
            return false;
        });
        filter_keyword_edit_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void afterTextChanged(Editable editable) {
                if(filter_keyword_edit_text.getText().length() > 0) {
                    ArrayList<FilterObject> partialListKeywords = new ArrayList<>();
                    for (FilterObject keyword: globalListKeywords){
                        if(keyword.getText().toUpperCase().contains(filter_keyword_edit_text.getText().toString().toUpperCase()))
                            partialListKeywords.add(keyword);
                    }
                    setListKeywordFilters(partialListKeywords);
                } else {
                    setListKeywordFilters(globalListKeywords);
                }
            }
        });
    }

    private void setAppBarText() {
        List<String> list = new ArrayList<>();
        list.add(makerFilter); list.add(keyword); if(centuryFilter.length() > 0) list.add(centuryFilter+res.getString(R.string.century));
        appbar_art_filter.setText(getJoinedString(list,", "));
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

        onMakerClickListener = (filterObject, position) -> {
            if(!makerFilter.equals(filterObject.getText())) {
                makerFilter = filterObject.getText();
                clear_artist_tv.setVisibility(View.VISIBLE);
                setFilterProgressesVisibility(View.VISIBLE);
                setAppBarText();
            }
            artFilterViewModel.setFilters(keyword, makerFilter, centuryFilter, keywordType);
        };

        onCenturyClickListener = (filterObject, position) -> {
            if(!centuryFilter.equals(filterObject.getText())) {
                centuryFilter = filterObject.getText();
                clear_century_tv.setVisibility(View.VISIBLE);
                setFilterProgressesVisibility(View.VISIBLE);
                setAppBarText();
            }
            artFilterViewModel.setFilters(keyword, makerFilter, centuryFilter, keywordType);
        };

        onKeywordClickListener = (filterObject, position) -> {
            if(!keyword.toUpperCase().equals(filterObject.getText().toUpperCase())) {
                keyword = filterObject.getText();
                keywordType = filterObject.getType();
                clear_keywords_tv.setVisibility(View.VISIBLE);
                setFilterProgressesVisibility(View.VISIBLE);
                setAppBarText();
            }
            artFilterViewModel.setFilters(keyword, makerFilter, centuryFilter, keywordType);
        };
    }

    private void setOnBackPressedListener(final View root) {
        //You need to add the following line for this solution to work; thanks skayred
        root.setFocusableInTouchMode(true);
        root.requestFocus();
        root.setOnKeyListener((v, keyCode, event) -> {

            if( keyCode == KeyEvent.KEYCODE_BACK ) {
                if(black_layout.isShown()) {
                    goneFilterViews();
                    return true;
                } else {
                    int scrollPosition = 0;
                    if (artFilterAdapter.getItemCount() > 0) scrollPosition = getTargetScrollPosition();
                    if (scrollPosition > 4) {
                        recycler_view_art_filter.smoothScrollToPosition(0);
                        return true;
                    }
                }
                return false;
            }
            return false;
        });
    }

    private void initSwipeRefreshLayout() {
        art_filter_swipeRefreshLayout.setOnRefreshListener(() -> {
            boolean networkState = artFilterViewModel.refresh();
            if (!networkState) {
                Toast.makeText(getContext(), R.string.network_is_unavailable, Toast.LENGTH_LONG).show();
                art_filter_swipeRefreshLayout.setRefreshing(false);
            }
        });
        art_filter_swipeRefreshLayout.setColorSchemeResources(
                R.color.colorBlue
        );

    }

    private void subscribeObservers() {

        artFilterViewModel.getArtList().observe(getViewLifecycleOwner(), arts -> {
            artFilterAdapter.submitList(arts);
            hideText();
            art_filter_swipeRefreshLayout.setRefreshing(false);
        });
        artFilterViewModel.getIsLoading().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) { showProgressBar(); } else { hideProgressBar(); }
        });
        artFilterViewModel.getIsListEmpty().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) { showText(); } else { hideText(); }
        });
        artFilterViewModel.getListMakerFilters().observe(getViewLifecycleOwner(), listStrings -> {
            if (listStrings == null){
                filterMakerAdapter.clearItems();
                globalListArtists.clear();
            } else {
                setListMakerFilters(listStrings);
                globalListArtists = listStrings;
            }
        });
        artFilterViewModel.getListCenturyFilters().observe(getViewLifecycleOwner(), listStrings -> {
            if (listStrings == null)
                filterCenturyAdapter.clearItems();
             else
                setListCenturyFilters(listStrings);
        });
        artFilterViewModel.getListKeywordFilters().observe(getViewLifecycleOwner(), listKeywords -> {
            if (listKeywords == null){
                filterKeywordAdapter.clearItems();
                globalListKeywords.clear();
            } else {
                setListKeywordFilters(listKeywords);
                globalListKeywords = listKeywords;
            }
        });

        artFilterViewModel.getArtCount().observe(getViewLifecycleOwner(), artCount -> appbar_art_count.setText(artCount.toString()));
    }

    private void setListKeywordFilters(final ArrayList<FilterObject> listKeywords) {

        final ArrayList<FilterObject> partialList;
        if(listKeywords.size()>500) partialList = new ArrayList<>(listKeywords.subList(0, 500)); else partialList = listKeywords;

        final Handler handler = new Handler();
        new Thread(() -> {
            final ArrayList<FilterObject> listKeywordFilters = new ArrayList<>();
            int position = -1;
            for (FilterObject filter : partialList) {
                if (keyword.toUpperCase().equals(filter.getText().toUpperCase())) {
                    position = partialList.indexOf(filter);
                    listKeywordFilters.add(new FilterObject(filter.getText(), true, filter.getType()));
                } else
                    listKeywordFilters.add(new FilterObject(filter.getText(), false, filter.getType()));
            }
            final int finalPosition = position;
            handler.post(() -> {
                filterKeywordAdapter = new FilterAdapter(getContext(), onKeywordClickListener);
                recycler_view_keywords.setAdapter(filterKeywordAdapter);
                filterKeywordAdapter.setItems(listKeywordFilters);
                if(finalPosition >= 0) recycler_view_keywords.scrollToPosition(finalPosition);
                setFilterProgressesVisibility(View.GONE);
            });
        }).start();
        if(keyword.length() ==0) clear_keywords_tv.setVisibility(View.GONE); else clear_keywords_tv.setVisibility(View.VISIBLE);
    }

    private void setListMakerFilters(final ArrayList<String> listStrings) {

        final ArrayList<String> partialList;
        if(listStrings.size()>500) partialList = new ArrayList<>(listStrings.subList(0, 500)); else partialList = listStrings;

        final Handler handler = new Handler();
        new Thread(() -> {
            final ArrayList<FilterObject> listMakerFilters = new ArrayList<>();
            int position = -1;
            for (String maker : partialList) {
                if (maker.equals(makerFilter)) {
                    position = partialList.indexOf(maker);
                    listMakerFilters.add(new FilterObject(maker, true, Constants.ART_MAKER));
                } else
                    listMakerFilters.add(new FilterObject(maker, false, Constants.ART_MAKER));
            }
            final int finalPosition = position;
            handler.post(() -> {
                filterMakerAdapter = new FilterAdapter(getContext(), onMakerClickListener);
                recycler_view_artists.setAdapter(filterMakerAdapter);
                filterMakerAdapter.setItems(listMakerFilters);
                if(finalPosition >= 0) recycler_view_artists.scrollToPosition(finalPosition);
                setFilterProgressesVisibility(View.GONE);
            });
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

    private void initRecyclerView(final ArtFilterViewModel artFilterViewModel, int displayWidth, int displayHeight){

        ArtFilterAdapter.OnArtClickListener onArtClickListener = (art, position) -> {
            Bundle args = new Bundle();
            args.putSerializable(ArtShowFragment.ARTS, (Serializable) ArtFilterDataInMemory.getInstance().getAllData());
            args.putInt(ArtShowFragment.POSITION, position);
            NavHostFragment.findNavController(ArtFilterFragment.this)
                    .navigate(R.id.action_artFilterFragment_to_artShowFragment, args);
        };


        artFilterAdapter = new ArtFilterAdapter(artFilterViewModel, getContext(), onArtClickListener, displayWidth, displayHeight, spanCount);
        RecyclerView.LayoutManager layoutManager = new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL);
        recycler_view_art_filter.setLayoutManager(layoutManager);
        recycler_view_art_filter.setAdapter(artFilterAdapter);
    }

    private void showProgressBar(){
        progress_bar_art_filter.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar(){
        progress_bar_art_filter.setVisibility(View.GONE);
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
                AnimatorSet set = new AnimatorSet();
                set.setDuration(500).playTogether(ObjectAnimator.ofFloat(black_layout, View.ALPHA, 0f, 1f));
                set.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) { black_layout.setVisibility(View.VISIBLE); }
                    @Override
                    public void onAnimationEnd(Animator animation) { }
                    @Override
                    public void onAnimationCancel(Animator animation) { }
                    @Override
                    public void onAnimationRepeat(Animator animation) { }
                });
                set.start();
            }

        } else if (view.getId() == black_layout.getId()) {
            goneFilterViews();

        } else if (view.getId() == clear_artist_tv.getId()) {
            makerFilter = "";
            setFilterProgressesVisibility(View.VISIBLE);
            setAppBarText();

            ArrayList<String> listString = new ArrayList<>();
            for(FilterObject filter : filterMakerAdapter.getItems()) { listString.add(filter.getText()); }
            setListMakerFilters(listString);
            artFilterViewModel.setFilters(keyword, makerFilter, centuryFilter, keywordType);

        } else if (view.getId() == clear_century_tv.getId()) {
            centuryFilter = "";
            setFilterProgressesVisibility(View.VISIBLE);
            setAppBarText();

            ArrayList<String> listString = new ArrayList<>();
            for(FilterObject filter : filterCenturyAdapter.getItems()) { listString.add(filter.getText()); }
            setListCenturyFilters(listString);
            artFilterViewModel.setFilters(keyword, makerFilter, centuryFilter, keywordType);

        } else if (view.getId() == clear_keywords_tv.getId()) {
            keyword = ""; keywordType = "";
            setFilterProgressesVisibility(View.VISIBLE);
            setAppBarText();
            setListKeywordFilters((ArrayList<FilterObject>) filterKeywordAdapter.getItems());
            artFilterViewModel.setFilters(keyword, makerFilter, centuryFilter, keywordType);

        } else if (view.getId() == search_artist_iv.getId()) {
            artistSearchPressed = !artistSearchPressed;
            if(artistSearchPressed){
                search_artist_iv.setImageDrawable(res.getDrawable(R.drawable.ic_keyboard_backspace_black_100dp));
                filter_artist_edit_text.setVisibility(View.VISIBLE);
            } else {
                search_artist_iv.setImageDrawable(res.getDrawable(R.drawable.ic_search_black_100dp));
                filter_artist_edit_text.setVisibility(View.GONE);
            }

        } else if (view.getId() == search_keywords_iv.getId()) {
            keywordSearchPressed = !keywordSearchPressed;
            if(keywordSearchPressed){
                search_keywords_iv.setImageDrawable(res.getDrawable(R.drawable.ic_keyboard_backspace_black_100dp));
                filter_keyword_edit_text.setVisibility(View.VISIBLE);
            } else {
                search_keywords_iv.setImageDrawable(res.getDrawable(R.drawable.ic_search_black_100dp));
                filter_keyword_edit_text.setVisibility(View.GONE);
            }

        }
    }

    private void goneFilterViews() {
        black_layout.setVisibility(View.GONE);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.exit_fade_out);
        black_layout.startAnimation(animation);
    }

    private int getTargetScrollPosition () {

        int scrollPosition = ((StaggeredGridLayoutManager) recycler_view_art_filter.getLayoutManager()).findFirstVisibleItemPositions(null)[0];

        return scrollPosition;
    }

}
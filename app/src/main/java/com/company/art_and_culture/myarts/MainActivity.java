package com.company.art_and_culture.myarts;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.company.art_and_culture.myarts.art_maker_fragment.MakerFragment;
import com.company.art_and_culture.myarts.art_medium_fragment.MediumFragment;
import com.company.art_and_culture.myarts.arts_show_fragment.ArtShowFragment;
import com.company.art_and_culture.myarts.art_maker_search_fragment.MakerSearchFragment;
import com.company.art_and_culture.myarts.network.NetworkQuery;
import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.pojo.ExploreObject;
import com.company.art_and_culture.myarts.pojo.ServerRequest;
import com.company.art_and_culture.myarts.pojo.ServerResponse;
import com.company.art_and_culture.myarts.pojo.Suggest;
import com.company.art_and_culture.myarts.art_search_fragment.SearchFragment;
import com.company.art_and_culture.myarts.ui.explore.ExploreFragment;
import com.company.art_and_culture.myarts.ui.favorites.FavoritesFragment;
import com.company.art_and_culture.myarts.ui.home.HomeFragment;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements
        HomeFragment.HomeEventListener, FavoritesFragment.FavoritesEventListener, SearchFragment.SearchEventListener,
        MakerFragment.MakerEventListener, View.OnClickListener, ExploreFragment.ExploreEventListener, MediumFragment.MediumEventListener {

    private int homePosition = 0;
    private int favoritesPosition = 0;
    private FavoritesFragment.Sort sort_type = FavoritesFragment.Sort.by_date;

    private Collection<Art> listArtsForArtShowFragment;
    private int clickPositionForArtShowFragment;

    private AppBarLayout app_bar_main;
    private Toolbar toolbar;
    private ArtShowFragment artShowFragment;
    private ImageView search_btn, search_back, search_clear;
    private ConstraintLayout search_layout;
    private EditText search_edit_text;
    private RecyclerView suggestions_recycler_view;
    private SuggestAdapter suggestAdapter;
    private ProgressBar suggestions_progress;
    private SearchFragment searchFragment;
    private MakerFragment makerFragment;
    private String artQueryForMediumFragment, queryTypeForMediumFragment;
    private String artMakerForMakerFragment;
    private SharedPreferences preferences;
    private MakerSearchFragment makerSearchFragment;
    private MediumFragment mediumFragment;
    private BottomNavigationView navView;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        //AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(R.id.navigation_home, R.id.navigation_explore, R.id.navigation_favorites, R.id.navigation_notifications).build();
        //NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        toolbar = findViewById(R.id.toolbar_main);
        app_bar_main = findViewById(R.id.app_bar_main);
        search_btn = findViewById(R.id.search_btn);
        search_layout = findViewById(R.id.search_layout);
        search_back = findViewById(R.id.search_back);
        search_clear = findViewById(R.id.search_clear);
        search_edit_text = findViewById(R.id.search_edit_text);
        suggestions_recycler_view = findViewById(R.id.suggestions_recycler_view);
        suggestions_progress = findViewById(R.id.suggestions_progress);

        search_layout.setVisibility(View.GONE);
        suggestions_recycler_view.setVisibility(View.GONE);
        suggestions_progress.setVisibility(View.GONE);
        search_clear.setVisibility(View.GONE);

        search_btn.setOnClickListener(this);
        search_back.setOnClickListener(this);
        search_clear.setOnClickListener(this);

        initSuggestRecyclerView();

        search_edit_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            private Timer timer=new Timer();
            private final long DELAY = 700; // milliseconds
            @Override
            public void afterTextChanged(final Editable s) {
                if (s.toString().length() == 0) {
                    getInitialSuggests(preferences.getString(Constants.USER_UNIQUE_ID,""));
                    search_clear.setVisibility(View.GONE);
                } else {
                    final Handler handler = new Handler();
                    timer.cancel();
                    timer = new Timer();
                    timer.schedule(
                            new TimerTask() {
                                @Override
                                public void run() {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (search_edit_text.getText().length() == s.length()) {
                                                getSuggests(s.toString(), preferences.getString(Constants.USER_UNIQUE_ID,""));
                                            }
                                        }
                                    });                                }
                            }, DELAY);

                    search_clear.setVisibility(View.VISIBLE);
                }
            }
        });
        search_edit_text.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    showSearchFragment();
                    hideSoftKeyboard(MainActivity.this);
                    return true;
                }
                return false;
            }
        });
        search_edit_text.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (searchFragment != null) {
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom);
                        fragmentTransaction.remove(searchFragment).commit();
                        searchFragment.finish();
                        searchFragment = null;
                    }
                }
                return false;
            }
        });

        preferences = getSharedPreferences(Constants.TAG, 0);
        getInitialSuggests(preferences.getString(Constants.USER_UNIQUE_ID,""));
    }

    private void getInitialSuggests(String userUniqueId) {

        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.GET_INITIAL_SUGGEST_OPERATION);
        request.setUserUniqueId(userUniqueId);

        Call<ServerResponse> response = NetworkQuery.getInstance().create(Constants.BASE_URL, request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                if (response.body() != null) {
                    if (response.body().getListSuggests() == null) {
                        suggestAdapter.clearItems();
                    } else {
                        setAnimationSuggestsRecyclerView ();
                        suggestAdapter.clearItems();
                        suggestAdapter.setItems(response.body().getListSuggests());
                    }
                }
            }
            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) { }
        });

    }

    public void refreshSuggests () {
        getInitialSuggests(preferences.getString(Constants.USER_UNIQUE_ID,""));
    }

    private void getSuggests(String suggestQuery, String userUniqueId) {

        suggestions_progress.setVisibility(View.VISIBLE);

        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.GET_SUGGEST_OPERATION);
        request.setSuggestQuery(suggestQuery);
        request.setUserUniqueId(userUniqueId);

        Call<ServerResponse> response = NetworkQuery.getInstance().create(Constants.BASE_URL, request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                suggestions_progress.setVisibility(View.GONE);
                if (response.body() != null) {
                    if (response.body().getListSuggests() == null) {
                        suggestAdapter.clearItems();
                    } else {
                        if (search_edit_text.getText().toString().length() > 0) {
                            setAnimationSuggestsRecyclerView ();
                            suggestAdapter.clearItems();
                            suggestAdapter.setItems(response.body().getListSuggests());
                        }
                    }
                }
            }
            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                suggestions_progress.setVisibility(View.GONE);
            }
        });

    }

    private void setAnimationSuggestsRecyclerView() {
        LayoutAnimationController layoutAnimationController = AnimationUtils.loadLayoutAnimation(this, R.anim.favorites_fall_down);
        suggestions_recycler_view.setLayoutAnimation(layoutAnimationController);
        suggestions_recycler_view.getAdapter().notifyDataSetChanged();
        suggestions_recycler_view.scheduleLayoutAnimation();
    }

    private void initSuggestRecyclerView() {
        suggestions_recycler_view.setLayoutManager(new LinearLayoutManager(this));
        SuggestAdapter.OnSuggestClickListener onSuggestClickListener = new SuggestAdapter.OnSuggestClickListener() {
            @Override
            public void onSuggestClick(Suggest suggest, int position) {
                search_edit_text.setText(suggest.getSuggestStr());
                showSearchFragment();
                hideSoftKeyboard(MainActivity.this);
            }

            @Override
            public void onSuggestLongClick(Suggest suggest, View v) {
                showPopupMenu(suggest, v);
            }
        };
        suggestAdapter = new SuggestAdapter(this, onSuggestClickListener);
        suggestions_recycler_view.setAdapter(suggestAdapter);
    }

    private void showPopupMenu(final Suggest suggest, View view) {

        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.inflate(R.menu.delete_query_pop_up_menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete_query:

                        deleteQuery(suggest.getSuggestStr(), preferences.getString(Constants.USER_UNIQUE_ID,""));

                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }

    private void deleteQuery(String suggestStr, String userUniqueId) {

        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.DELETE_SUGGEST_QUERY_OPERATION);
        request.setSearchQuery(suggestStr);
        request.setUserUniqueId(userUniqueId);

        Call<ServerResponse> response = NetworkQuery.getInstance().create(Constants.BASE_URL, request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                if (response.body() != null) {
                    if (response.body().getListSuggests() == null) {
                        suggestAdapter.clearItems();
                    } else {
                        setAnimationSuggestsRecyclerView ();
                        suggestAdapter.clearItems();
                        suggestAdapter.setItems(response.body().getListSuggests());
                    }
                }
            }
            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) { }
        });

    }

    @Override
    public void onClick(View v) {

        if (v.getId() == search_btn.getId()) {

            search_layout.setVisibility(View.VISIBLE);
            suggestions_recycler_view.setVisibility(View.VISIBLE);
            navView.setVisibility(View.GONE);
            AnimatorSet set = new AnimatorSet();
            set.setDuration(400).playTogether(
                    ObjectAnimator.ofFloat(search_layout, View.ALPHA, 0.2f, 1f),
                    ObjectAnimator.ofFloat(suggestions_recycler_view, View.ALPHA, 0.2f, 1f)
            );
            set.start();

        } else if (v.getId() == search_back.getId()) {

            hideSoftKeyboard(this);
            navView.setVisibility(View.VISIBLE);
            search_edit_text.setText("");
            AnimatorSet set = new AnimatorSet();
            set.setDuration(300).playTogether(
                    ObjectAnimator.ofFloat(search_layout, View.ALPHA, 1.0f, 0f),
                    ObjectAnimator.ofFloat(suggestions_recycler_view, View.ALPHA, 1.0f, 0f)
            );
            set.addListener(fadeOutListener(search_layout, suggestions_recycler_view));
            set.start();

            if (searchFragment != null) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom);
                fragmentTransaction.remove(searchFragment).commit();
                searchFragment.finish();
                searchFragment = null;
            }

        } else if (v.getId() == search_clear.getId()) {
            search_edit_text.setText("");
            if (searchFragment != null) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom);
                fragmentTransaction.remove(searchFragment).commit();
                searchFragment.finish();
                searchFragment = null;
            }
        }
    }

    private AnimatorListenerAdapter fadeOutListener(final ConstraintLayout search_layout, final RecyclerView suggestions_recycler_view) {
        return new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                search_layout.setVisibility(View.GONE);
                suggestions_recycler_view.setVisibility(View.GONE);
            }
        };
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }

    public boolean isSearchLayoutOpen () {
        return search_layout.isShown();
    }

    public int getToolbarHeight (){
        return toolbar.getHeight();
    }



    @Override
    public void homeScrollEvent(int position) {
        this.homePosition = position;
    }
    @Override
    public void homeArtClickEvent(Collection<Art> arts, int position) {
        this.listArtsForArtShowFragment = arts;
        this.clickPositionForArtShowFragment = position;
        showArtFragment();
    }
    @Override
    public void homeMakerClickEvent(String artMaker, String queryType) {
        this.artMakerForMakerFragment = artMaker;
        showMakerFragment();
    }
    @Override
    public void homeClassificationClickEvent(String artClassification, String queryType) {
        this.artQueryForMediumFragment = artClassification;
        this.queryTypeForMediumFragment = queryType;
        showMediumFragment();
    }



    @Override
    public void favoritesScrollEvent(int position, FavoritesFragment.Sort sort_type) {
        this.favoritesPosition = position;
        this.sort_type = sort_type;
    }
    @Override
    public void favoritesClickEvent(Collection<Art> listArts, int position) {
        this.listArtsForArtShowFragment = listArts;
        this.clickPositionForArtShowFragment = position;
        showArtFragment();
    }
    @Override
    public void favoritesOnScroll(int direction) {
        Log.i("favoritesOnScroll", direction+"");
        if (direction == 1){
            if (navView.isShown()) {
                AnimatorSet set = new AnimatorSet();
                set.setDuration(600).playTogether(
                        ObjectAnimator.ofFloat(navView, View.TRANSLATION_Y,  0f,getToolbarHeight()),
                        ObjectAnimator.ofFloat(app_bar_main, View.TRANSLATION_Y, 0f, -getToolbarHeight())
                );
                set.addListener(goneNavigationListener(navView, app_bar_main));
                set.start();
            }
        } else if (direction == 0){
            if (!navView.isShown()) {
                navView.setVisibility(View.VISIBLE);
                app_bar_main.setVisibility(View.VISIBLE);
                AnimatorSet set = new AnimatorSet();
                set.setDuration(600).playTogether(
                        ObjectAnimator.ofFloat(navView, View.TRANSLATION_Y,  getToolbarHeight(), 0f),
                        ObjectAnimator.ofFloat(app_bar_main, View.TRANSLATION_Y, -getToolbarHeight(), 0f)
                );
                set.start();
            }
        }
    }

    private Animator.AnimatorListener goneNavigationListener(final BottomNavigationView navView, final AppBarLayout app_bar_main) {
        return new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                navView.setVisibility(View.GONE);
                app_bar_main.setVisibility(View.GONE);
            }
        };
    }



    @Override
    public void searchArtClickEvent(Collection<Art> arts, int position) {
        this.listArtsForArtShowFragment = arts;
        this.clickPositionForArtShowFragment = position;
        showArtFragment();
    }
    @Override
    public void searchMakerClickEvent(String artMaker, String queryType) {
        this.artMakerForMakerFragment = artMaker;
        showMakerFragment();
    }
    @Override
    public void searchClassificationClickEvent(String artClassification, String queryType) {
        this.artQueryForMediumFragment = artClassification;
        this.queryTypeForMediumFragment = queryType;
        showMediumFragment();
    }



    @Override
    public void makerArtClickEvent(Collection<Art> arts, int position) {
        this.listArtsForArtShowFragment = arts;
        this.clickPositionForArtShowFragment = position;
        showArtFragment();
    }




    @Override
    public void mediumArtClickEvent(Collection<Art> arts, int position) {
        this.listArtsForArtShowFragment = arts;
        this.clickPositionForArtShowFragment = position;
        showArtFragment();
    }




    @Override
    public void exploreMakerSearchClick() {
        showMakerSearchFragment();
    }
    @Override
    public void exploreClickEvent(ExploreObject exploreObject, int position) {
        this.artMakerForMakerFragment = exploreObject.getText();
        showMakerFragment();
    }




    public int getHomePosition() {
        return homePosition;
    }

    public int getFavoritesPosition() {
        return favoritesPosition;
    }

    public FavoritesFragment.Sort getSort_type() {
        return sort_type;
    }

    public Collection<Art> getListArtsForArtShowFragment() {
        return listArtsForArtShowFragment;
    }

    public int getClickPositionForArtShowFragment() {
        return clickPositionForArtShowFragment;
    }

    public ArtShowFragment getArtShowFragment() {
        return artShowFragment;
    }

    public String getSearchQuery() {
        return search_edit_text.getText().toString();
    }

    public String getArtQueryForMediumFragment() {
        return artQueryForMediumFragment;
    }

    public String getQueryTypeForMediumFragment() {
        return queryTypeForMediumFragment;
    }

    public String getArtMakerForMakerFragment() {
        return artMakerForMakerFragment;
    }

    @Override
    public void onBackPressed() {

        if (artShowFragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right);
            fragmentTransaction.remove(artShowFragment).commit();
            artShowFragment = artShowFragment.finish();

        } else if (makerFragment != null) {

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom);
            fragmentTransaction.remove(makerFragment).commit();
            makerFragment.finish();
            makerFragment = null;

        } else if (mediumFragment != null) {

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom);
            fragmentTransaction.remove(mediumFragment).commit();
            mediumFragment.finish();
            mediumFragment = null;

        } else if (searchFragment != null) {

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom);
            fragmentTransaction.remove(searchFragment).commit();
            searchFragment.finish();
            searchFragment = null;

        } else if (makerSearchFragment != null) {

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right);
            fragmentTransaction.remove(makerSearchFragment).commit();
            makerSearchFragment.finish();
            makerSearchFragment = null;

        } else {
            if (isSearchLayoutOpen()) {

                search_edit_text.setText("");
                navView.setVisibility(View.VISIBLE);
                AnimatorSet set = new AnimatorSet();
                set.setDuration(300).playTogether(
                        ObjectAnimator.ofFloat(search_layout, View.ALPHA, 1.0f, 0f),
                        ObjectAnimator.ofFloat(suggestions_recycler_view, View.ALPHA, 1.0f, 0f)
                );
                set.addListener(fadeOutListener(search_layout, suggestions_recycler_view));
                set.start();

            } else {
                super.onBackPressed();
            }
        }

    }

    private void showArtFragment() {

        artShowFragment = new ArtShowFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right);
        //fragmentTransaction.addToBackStack(null);
        fragmentTransaction.add(R.id.frame_container_art_show, artShowFragment, "favoritesShowFragment").commit();
    }

    private void showSearchFragment() {

        searchFragment = new SearchFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom);
        //fragmentTransaction.addToBackStack(null);
        fragmentTransaction.add(R.id.frame_container_search, searchFragment, "searchFragment").commit();
    }

    private void showMediumFragment() {

        mediumFragment = new MediumFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom);
        //fragmentTransaction.addToBackStack(null);
        fragmentTransaction.add(R.id.frame_container_medium, mediumFragment, "mediumFragment").commit();
    }

    private void showMakerFragment() {

        makerFragment = new MakerFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom);
        //fragmentTransaction.addToBackStack(null);
        fragmentTransaction.add(R.id.frame_container_maker, makerFragment, "makerFragment").commit();
    }

    private void showMakerSearchFragment() {
        makerSearchFragment = new MakerSearchFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right);
        //fragmentTransaction.addToBackStack(null);
        fragmentTransaction.add(R.id.frame_container_maker_search, makerSearchFragment, "makerSearchFragment").commit();
    }


}

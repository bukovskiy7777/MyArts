package com.company.art_and_culture.myarts;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.company.art_and_culture.myarts.art_maker_fragment.MakerFragment;
import com.company.art_and_culture.myarts.arts_show_fragment.ArtShowFragment;
import com.company.art_and_culture.myarts.network.NetworkQuery;
import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.pojo.ServerRequest;
import com.company.art_and_culture.myarts.pojo.ServerResponse;
import com.company.art_and_culture.myarts.pojo.Suggest;
import com.company.art_and_culture.myarts.art_search_fragment.SearchFragment;
import com.company.art_and_culture.myarts.ui.favorites.FavoritesFragment;
import com.company.art_and_culture.myarts.ui.home.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Collection;

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
        MakerFragment.MakerEventListener, View.OnClickListener {

    private int homePosition = 0;
    private int favoritesPosition = 0;

    private Collection<Art> listArts;
    private int clickPosition;

    private Toolbar toolbar;
    private ArtShowFragment artShowFragment;
    private ImageView search_btn, search_back;
    private ConstraintLayout search_layout;
    private EditText search_edit_text;
    private RecyclerView suggestions_recycler_view;
    private SuggestAdapter suggestAdapter;
    private ProgressBar suggestions_progress;
    private SearchFragment searchFragment;
    private MakerFragment makerFragment;
    private String artMaker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        //AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(R.id.navigation_home, R.id.navigation_explore, R.id.navigation_favorites, R.id.navigation_notifications).build();
        //NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        toolbar = findViewById(R.id.toolbar_main);
        search_btn = findViewById(R.id.search_btn);
        search_layout = findViewById(R.id.search_layout);
        search_back = findViewById(R.id.search_back);
        search_edit_text = findViewById(R.id.search_edit_text);
        suggestions_recycler_view = findViewById(R.id.suggestions_recycler_view);
        suggestions_progress = findViewById(R.id.suggestions_progress);

        search_layout.setVisibility(View.GONE);
        suggestions_recycler_view.setVisibility(View.GONE);
        suggestions_progress.setVisibility(View.GONE);

        search_btn.setOnClickListener(this);
        search_back.setOnClickListener(this);

        initSuggestRecyclerView();

        search_edit_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                getSuggests(s.toString());
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
                        searchFragment = searchFragment.finish();
                    }
                }
                return false;
            }
        });

    }

    private void getSuggests(String searchString) {

        suggestions_progress.setVisibility(View.VISIBLE);

        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.GET_SUGGEST_OPERATION);
        request.setSearchString(searchString);

        Call<ServerResponse> response = NetworkQuery.getInstance().create(Constants.BASE_URL, request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                suggestions_progress.setVisibility(View.GONE);
                if (response.body().getListSuggests() == null) {
                    suggestAdapter.clearItems();
                    //showText();
                } else {
                    setAnimationSuggestsRecyclerView ();
                    suggestAdapter.clearItems();
                    suggestAdapter.setItems(response.body().getListSuggests());
                    //hideText();
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
        };
        suggestAdapter = new SuggestAdapter(this, onSuggestClickListener);
        suggestions_recycler_view.setAdapter(suggestAdapter);
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == search_btn.getId()) {

            search_layout.setVisibility(View.VISIBLE);
            suggestions_recycler_view.setVisibility(View.VISIBLE);
            AnimatorSet set = new AnimatorSet();
            set.setDuration(400).playTogether(
                    ObjectAnimator.ofFloat(search_layout, View.ALPHA, 0.2f, 1f),
                    ObjectAnimator.ofFloat(suggestions_recycler_view, View.ALPHA, 0.2f, 1f)
            );
            set.start();

        } else if (v.getId() == search_back.getId()) {

            hideSoftKeyboard(this);
            search_layout.setVisibility(View.GONE);
            suggestions_recycler_view.setVisibility(View.GONE);

            if (searchFragment != null) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom);
                fragmentTransaction.remove(searchFragment).commit();
                searchFragment = searchFragment.finish();
            }

        }
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
        this.listArts = arts;
        this.clickPosition = position;

        showArtFragment();
    }
    @Override
    public void homeMakerClickEvent(String artMaker) {
        this.artMaker = artMaker;
        showMakerFragment();
    }
    public int getHomePosition() {
        return homePosition;
    }



    @Override
    public void favoritesScrollEvent(int position) {
        this.favoritesPosition = position;
    }
    public int getFavoritesPosition() {
        return favoritesPosition;
    }
    @Override
    public void favoritesClickEvent(Collection<Art> listArts, int position) {
        this.listArts = listArts;
        this.clickPosition = position;

        showArtFragment();
    }



    @Override
    public void searchArtClickEvent(Collection<Art> arts, int position) {
        this.listArts = arts;
        this.clickPosition = position;

        showArtFragment();
    }
    @Override
    public void searchMakerClickEvent(String artMaker) {
        this.artMaker = artMaker;
        showMakerFragment();
    }



    @Override
    public void makerArtClickEvent(Collection<Art> arts, int position) {
        this.listArts = arts;
        this.clickPosition = position;

        showArtFragment();
    }



    public Collection<Art> getListArts() {
        return listArts;
    }

    public int getClickPosition() {
        return clickPosition;
    }

    public ArtShowFragment getArtShowFragment() {
        return artShowFragment;
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
            makerFragment = makerFragment.finish();

        } else if (searchFragment != null) {

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom);
            fragmentTransaction.remove(searchFragment).commit();
            searchFragment = searchFragment.finish();

        } else {
            if (isSearchLayoutOpen()) {

                search_layout.setVisibility(View.GONE);
                suggestions_recycler_view.setVisibility(View.GONE);

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
        fragmentTransaction.add(R.id.frame_container, artShowFragment, "favoritesShowFragment").commit();
    }

    private void showSearchFragment() {

        searchFragment = SearchFragment.getInstance();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom);
        //fragmentTransaction.addToBackStack(null);
        fragmentTransaction.add(R.id.frame_container_search, searchFragment, "searchFragment").commit();
    }

    private void showMakerFragment() {

        makerFragment = MakerFragment.getInstance();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom);
        //fragmentTransaction.addToBackStack(null);
        fragmentTransaction.add(R.id.frame_container_maker, makerFragment, "makerFragment").commit();
    }

    public String getSearchQuery() {
        return search_edit_text.getText().toString();
    }

    public String getArtMaker() {
        return artMaker;
    }

}

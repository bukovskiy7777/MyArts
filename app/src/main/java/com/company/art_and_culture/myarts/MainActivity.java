package com.company.art_and_culture.myarts;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.company.art_and_culture.myarts.network.NetworkQuery;
import com.company.art_and_culture.myarts.pojo.ServerRequest;
import com.company.art_and_culture.myarts.pojo.ServerResponse;
import com.company.art_and_culture.myarts.pojo.Suggest;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Toolbar toolbar;
    private ImageView search_btn, search_back, search_clear;
    private ConstraintLayout search_layout;
    private EditText search_edit_text;
    private RecyclerView suggestions_recycler_view;
    private SuggestAdapter suggestAdapter;
    private ProgressBar suggestions_progress;
    private SharedPreferences preferences;
    private BottomNavigationView navView;
    private NavFragments navFragments;

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

        navFragments = new NavFragments(this);

        toolbar = findViewById(R.id.toolbar_main);
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
                    navFragments.showSearchFragment();
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
                    navFragments.finishSearchFragment();
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
        LayoutAnimationController layoutAnimationController = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_fall_down);
        suggestions_recycler_view.setLayoutAnimation(layoutAnimationController);
        //suggestions_recycler_view.getAdapter().notifyDataSetChanged();
        suggestions_recycler_view.scheduleLayoutAnimation();
    }

    private void initSuggestRecyclerView() {
        suggestions_recycler_view.setLayoutManager(new LinearLayoutManager(this));
        SuggestAdapter.OnSuggestClickListener onSuggestClickListener = new SuggestAdapter.OnSuggestClickListener() {
            @Override
            public void onSuggestClick(Suggest suggest, int position) {
                search_edit_text.setText(suggest.getSuggestStr());
                navFragments.showSearchFragment();
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

            navFragments.finishSearchFragment();

        } else if (v.getId() == search_clear.getId()) {
            search_edit_text.setText("");
            navFragments.finishSearchFragment();
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
        if (activity.getCurrentFocus()!=null) inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    public boolean isSearchLayoutOpen () {
        return search_layout.isShown();
    }

    public int getToolbarHeight (){
        return toolbar.getHeight();
    }

    public boolean isToolbarOnScreen() {

        int[] location = new int[2];
        toolbar.getLocationOnScreen(location);
        int y = location[1];
        return y > 0;
    }

    public int getNavViewHeight (){
        return navView.getHeight();
    }

    public boolean isNavViewOnScreen() {

        android.content.res.Resources res = getResources();
        int displayHeight = res.getDisplayMetrics().heightPixels;
        int[] location = new int[2];
        navView.getLocationOnScreen(location);
        int y = location[1];
        return y < displayHeight;
    }

    public String getSearchQuery() {
        return search_edit_text.getText().toString();
    }

    public NavFragments getNavFragments() {
        return navFragments;
    }

    @Override
    public void onBackPressed() {

        if (navFragments.isFragmentsClosed()) {
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

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if(newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }else{
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

}

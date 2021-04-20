package com.company.art_and_culture.myarts;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.company.art_and_culture.myarts.network.NetworkQuery;
import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.pojo.ServerRequest;
import com.company.art_and_culture.myarts.pojo.ServerResponse;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.security.SecureRandom;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences preferences;
    private BottomNavigationView navView;
    private NavFragments navFragments;
    private MutableLiveData<ServerResponse> serverResponse = new MutableLiveData<>();
    private MutableLiveData<Art> art = new MutableLiveData<>();
    private MutableLiveData<Boolean> updateFolders = new MutableLiveData<>();
    private MutableLiveData<Integer> favoritesArtsCount = new MutableLiveData<>();
    private MutableLiveData<Integer> artistsCount = new MutableLiveData<>();
    private MutableLiveData<Integer> foldersCount = new MutableLiveData<>();
    private MutableLiveData<String> favoritesFilter = new MutableLiveData<>();
    private MutableLiveData<String> artistsFilter = new MutableLiveData<>();

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

        navFragments = new NavFragments(this, navController);

        preferences = getSharedPreferences(Constants.TAG, 0);
        getInitialSuggests(preferences.getString(Constants.USER_UNIQUE_ID,""));

        getUserUniqueId();
    }

    public void postNewArt(Art newArt){
        art.postValue(newArt);
    }

    public LiveData<Art> getArt() {
        return art;
    }

    public void updateFolders(boolean b) {
        updateFolders.postValue(b);
    }

    public MutableLiveData<Boolean> getUpdateFolders() {
        return updateFolders;
    }

    public void postFavoritesArtsCount (int count){ favoritesArtsCount.postValue(count);}

    public MutableLiveData<Integer> getFavoritesArtsCount() {
        return favoritesArtsCount;
    }

    public void postArtistsCount (int count){ artistsCount.postValue(count);}

    public MutableLiveData<Integer> getArtistsCount() {
        return artistsCount;
    }

    public void postFoldersCount (int count){ foldersCount.postValue(count);}

    public MutableLiveData<Integer> getFoldersCount() {
        return foldersCount;
    }

    public void postFavoritesFilter (String filter){ favoritesFilter.postValue(filter);}

    public MutableLiveData<String> getFavoritesFilter() {
        return favoritesFilter;
    }

    public void postArtistsFilter (String filter){ artistsFilter.postValue(filter);}

    public MutableLiveData<String> getArtistsFilter() {
        return artistsFilter;
    }

    public LiveData<ServerResponse> getListSuggest() {
        return serverResponse;
    }

    private void getUserUniqueId() {

        if (preferences.getString(Constants.USER_UNIQUE_ID,"").length()==0) {
            String userUniqueId = randomString(23);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(Constants.USER_UNIQUE_ID, userUniqueId);
            editor.apply();
        }
    }

    public String randomString(int len){
        String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        SecureRandom rnd = new SecureRandom();

        StringBuilder sb = new StringBuilder( len );
        for( int i = 0; i < len; i++ )
            sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
        return sb.toString();
    }

    public void getInitialSuggests(String userUniqueId) {

        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.GET_INITIAL_SUGGEST_OPERATION);
        request.setUserUniqueId(userUniqueId);

        Call<ServerResponse> response = NetworkQuery.getInstance().create(Constants.BASE_URL, request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                if (response.body() != null) {
                    if(response.body().getResult().equals(Constants.SUCCESS)) {
                        serverResponse.postValue(response.body());
                    }
                }
            }
            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) { }
        });

    }


    public void getSuggests(String suggestQuery, String userUniqueId) {

        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.GET_SUGGEST_OPERATION);
        request.setSuggestQuery(suggestQuery);
        request.setUserUniqueId(userUniqueId);

        Call<ServerResponse> response = NetworkQuery.getInstance().create(Constants.BASE_URL, request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                serverResponse.postValue(response.body());
            }
            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                serverResponse.postValue(null);
            }
        });

    }

    public void deleteSuggest(String suggestStr, String userUniqueId) {

        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.DELETE_SUGGEST_QUERY_OPERATION);
        request.setSuggestQuery(suggestStr);
        request.setUserUniqueId(userUniqueId);

        Call<ServerResponse> response = NetworkQuery.getInstance().create(Constants.BASE_URL, request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                if (response.body() != null) {
                    serverResponse.postValue(response.body());
                }
            }
            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) { }
        });

    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        if (activity.getCurrentFocus()!=null) inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
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

    public NavFragments getNavFragments() {
        return navFragments;
    }

    public void setNavViewVisible() {
        navView.setVisibility(View.VISIBLE);
    }

    public void goneNavView() {
        navView.setVisibility(View.GONE);
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

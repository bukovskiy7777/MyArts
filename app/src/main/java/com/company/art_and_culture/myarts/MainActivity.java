package com.company.art_and_culture.myarts;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.pojo.Folder;
import com.company.art_and_culture.myarts.pojo.ServerResponse;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso;

import java.security.SecureRandom;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private SharedPreferences preferences;
    private BottomNavigationView navView;
    private NavFragments navFragments;
    private ConstraintLayout save_to_folder_view;
    private FrameLayout background_save_to_folder;
    private TextView save_to, cancel;
    private ImageView add_folder, back_to_folders;
    private RecyclerView folderRecyclerView;
    private SaveToFolderAdapter foldersAdapter;
    private Art artToSaveInFolder;
    private ImageView folder_image;
    private EditText folder_title_edit_text;
    private SwitchCompat switch_isPublic;
    private MainActivityDataSource dataSource;
    private MutableLiveData<ServerResponse> serverResponse = new MutableLiveData<>();
    private MutableLiveData<Art> art = new MutableLiveData<>();
    private MutableLiveData<Boolean> updateFolders = new MutableLiveData<>();
    private MutableLiveData<Integer> favoritesArtsCount = new MutableLiveData<>();
    private MutableLiveData<Integer> artistsCount = new MutableLiveData<>();
    private MutableLiveData<Integer> foldersCount = new MutableLiveData<>();
    private MutableLiveData<String> favoritesFilter = new MutableLiveData<>();
    private MutableLiveData<String> artistsFilter = new MutableLiveData<>();
    private MutableLiveData<Boolean> isUpdateAllAppData = new MutableLiveData<>();
    private MutableLiveData<Boolean> isUpdateUserData = new MutableLiveData<>();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navView = findViewById(R.id.nav_view);

        initFoldersViews();
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        //AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(R.id.navigation_home, R.id.navigation_explore, R.id.navigation_favorites, R.id.navigation_notifications).build();
        //NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        navFragments = new NavFragments(this, navController);

        dataSource = new MainActivityDataSource (this);

        preferences = getSharedPreferences(Constants.TAG, 0);
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> dataSource.getInitialSuggests(preferences.getString(Constants.USER_UNIQUE_ID,"")), 1000);

        getUserUniqueId();

        android.content.res.Resources res = getResources();
        int displayWidth = res.getDisplayMetrics().widthPixels;
        int displayHeight = res.getDisplayMetrics().heightPixels;
        initFolderRecyclerView(displayWidth, displayHeight);
        handler.postDelayed(() -> dataSource.getFoldersList(preferences.getString(Constants.USER_UNIQUE_ID,"")), 1000);

        getUpdateFolders().observe(this, aBoolean -> {
            if(aBoolean) {
                dataSource.getFoldersList(preferences.getString(Constants.USER_UNIQUE_ID,""));

                handler.postDelayed(() -> updateFolders(false), 1000);
            }
        });
        getIsUpdateAllAppData().observe(this, aBoolean -> {
            if(aBoolean) {
                dataSource.getFoldersList(preferences.getString(Constants.USER_UNIQUE_ID,""));
                dataSource.getInitialSuggests(preferences.getString(Constants.USER_UNIQUE_ID,""));

                handler.postDelayed(() -> updateAllAppData(false), 1000);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        preferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(Constants.IS_LOGGED_IN)) {
            getUserUniqueId();
            updateAllAppData(true);
        }
        if(key.equals(Constants.USER_IMAGE_URL)) {
            updateUserData(true);
        }
    }

    private void initFoldersViews() {
        save_to_folder_view = findViewById(R.id.save_to_folder_view);
        save_to_folder_view.setVisibility(View.GONE);
        background_save_to_folder = findViewById(R.id.background_save_to_folder);
        background_save_to_folder.setVisibility(View.GONE);
        save_to = findViewById(R.id.save_to);
        cancel = findViewById(R.id.cancel);
        cancel.setOnClickListener(this);
        add_folder = findViewById(R.id.add_folder);
        add_folder.setOnClickListener(this);
        back_to_folders = findViewById(R.id.back_to_folders);
        back_to_folders.setOnClickListener(this);
        switch_isPublic = findViewById(R.id.switch_isPublic);
        switch_isPublic.setOnCheckedChangeListener((compoundButton, b) -> {
            if(b) switch_isPublic.setText(getResources().getText(R.string._public));
            else switch_isPublic.setText(getResources().getText(R.string._private));
        });
        folderRecyclerView = findViewById(R.id.recycler_view_folders);
        folder_image = findViewById(R.id.folder_image);
        folder_title_edit_text = findViewById(R.id.folder_title_edit_text);
        folder_title_edit_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if(folder_title_edit_text.getText().toString().length() > 0) {
                    cancel.setText(getResources().getString(R.string.done));
                    cancel.setTextColor(getResources().getColor(R.color.colorBlue));
                } else {
                    cancel.setText(getResources().getString(R.string.cancel));
                    cancel.setTextColor(getResources().getColor(R.color.colorBlack));
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == add_folder.getId()) {
            showCreateNewFolder();

        } else if (v.getId() == cancel.getId()) {
            if(cancel.getText() == getResources().getString(R.string.cancel)) {
                hideSaveToFolderView();
                if(foldersAdapter.getItemCount() > 0) hideCreateNewFolder();
                hideSoftKeyboard(this);
            } else if(cancel.getText() == getResources().getString(R.string.done)) {
                ArrayList<Art> artList = new ArrayList<>();
                artList.add(artToSaveInFolder);
                Folder folder = new Folder(folder_title_edit_text.getText().toString(), randomString(23),
                        preferences.getString(Constants.USER_UNIQUE_ID,""), switch_isPublic.isChecked(), artList);
                dataSource.createFolder(folder);
            }

        } else if (v.getId() == back_to_folders.getId()) {
            hideCreateNewFolder();
        }
    }

    public void showSaveToFolderView(Art art) {
        AnimatorSet set = new AnimatorSet();
        set.setDuration(600).playTogether(
                ObjectAnimator.ofFloat(save_to_folder_view, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(background_save_to_folder, View.ALPHA, 0f, 1f));
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                save_to_folder_view.setVisibility(View.VISIBLE);
                background_save_to_folder.setVisibility(View.VISIBLE);
                if(foldersAdapter.getItemCount() > 0) hideCreateNewFolder();
                else showCreateNewFolder();
            }
            @Override
            public void onAnimationEnd(Animator animation) { }
            @Override
            public void onAnimationCancel(Animator animation) { }
            @Override
            public void onAnimationRepeat(Animator animation) { }
        });
        set.start();

        artToSaveInFolder = art;

        String artImgUrl;
        if (art.getArtImgUrlSmall() != null && art.getArtImgUrlSmall().startsWith(getResources().getString(R.string.http)))
            artImgUrl = art.getArtImgUrlSmall();
        else artImgUrl= art.getArtImgUrl();
        if (art.getArtWidth() > 0) {
            int imgWidth = getResources().getDisplayMetrics().widthPixels;
            int imgHeight = (art.getArtHeight() * imgWidth) / art.getArtWidth();
            Picasso.get().load(artImgUrl).resize(imgWidth, imgHeight).onlyScaleDown().into(folder_image);
        } else Picasso.get().load(artImgUrl).into(folder_image);
    }

    void hideSaveToFolderView() {
        AnimatorSet set = new AnimatorSet();
        set.setDuration(400).playTogether(
                ObjectAnimator.ofFloat(save_to_folder_view, View.ALPHA, 1f, 0f),
                ObjectAnimator.ofFloat(background_save_to_folder, View.ALPHA, 1f, 0f));
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) { }
            @Override
            public void onAnimationEnd(Animator animation) {
                save_to_folder_view.setVisibility(View.GONE);
                background_save_to_folder.setVisibility(View.GONE);
            }
            @Override
            public void onAnimationCancel(Animator animation) {
            }
            @Override
            public void onAnimationRepeat(Animator animation) { }
        });
        set.start();
    }

    void hideCreateNewFolder() {
        folder_image.setVisibility(View.GONE);
        folder_title_edit_text.setVisibility(View.GONE);
        folder_title_edit_text.clearFocus();
        folder_title_edit_text.setText("");
        folderRecyclerView.setVisibility(View.VISIBLE);
        save_to.setText(getResources().getText(R.string.save_to));
        add_folder.setVisibility(View.VISIBLE);
        back_to_folders.setVisibility(View.GONE);
        switch_isPublic.setVisibility(View.GONE);
        switch_isPublic.setChecked(true);
        hideSoftKeyboard(this);
        cancel.setText(getResources().getString(R.string.cancel));
        cancel.setTextColor(getResources().getColor(R.color.colorBlack));
    }

    private void showCreateNewFolder() {
        folder_image.setVisibility(View.VISIBLE);
        folder_title_edit_text.setVisibility(View.VISIBLE);
        if(save_to_folder_view.isShown()) { folder_title_edit_text.requestFocus(); }
        folderRecyclerView.setVisibility(View.GONE);
        save_to.setText(getResources().getText(R.string.new_folder));
        add_folder.setVisibility(View.GONE);
        if(foldersAdapter.getItemCount() > 0) { back_to_folders.setVisibility(View.VISIBLE); }
        else { back_to_folders.setVisibility(View.GONE); }
        switch_isPublic.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        if(save_to_folder_view.isShown()) {
            hideSaveToFolderView();
            if(foldersAdapter.getItemCount() > 0) hideCreateNewFolder();
        } else
            super.onBackPressed();
    }

    public void setListFolders(ArrayList<Folder> listFolders) {
        if (listFolders == null) {
            foldersAdapter.clearItems();
            showCreateNewFolder();
        } else {
            foldersAdapter.clearItems();
            foldersAdapter.setItems(listFolders);
            hideCreateNewFolder();
        }
    }

    private void initFolderRecyclerView(int displayWidth, int displayHeight) {

        SaveToFolderAdapter.OnFolderClickListener onFolderClickListener = (folder, position) -> {
            dataSource.saveArtToFolder(artToSaveInFolder, folder, preferences.getString(Constants.USER_UNIQUE_ID,""));
            hideSaveToFolderView();
        };
        foldersAdapter = new SaveToFolderAdapter(this, onFolderClickListener, displayWidth, displayHeight);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        folderRecyclerView.setLayoutManager(layoutManager);
        folderRecyclerView.setAdapter(foldersAdapter);
    }

    public void postNewArt(Art newArt){
        art.postValue(newArt);
    }

    public LiveData<Art> getArt() {
        return art;
    }

    public void updateAllAppData(boolean b) {
        isUpdateAllAppData.postValue(b);
    }

    public MutableLiveData<Boolean> getIsUpdateAllAppData() {
        return isUpdateAllAppData;
    }

    public void updateUserData(boolean b) {
        isUpdateUserData.postValue(b);
    }

    public MutableLiveData<Boolean> getIsUpdateUserData() {
        return isUpdateUserData;
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

    public void postServerResponse (ServerResponse response){ serverResponse.postValue(response);}

    public LiveData<ServerResponse> getListSuggest() {
        return serverResponse;
    }

    public MainActivityDataSource getDataSource() {
        return dataSource;
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

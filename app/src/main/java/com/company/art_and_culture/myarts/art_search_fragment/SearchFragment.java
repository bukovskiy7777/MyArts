package com.company.art_and_culture.myarts.art_search_fragment;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.SuggestAdapter;
import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.pojo.Maker;
import com.company.art_and_culture.myarts.pojo.ServerResponse;
import com.company.art_and_culture.myarts.pojo.Suggest;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.company.art_and_culture.myarts.Constants.PERMISSION_REQUEST_CODE;
import static com.company.art_and_culture.myarts.MainActivity.hideSoftKeyboard;
import static com.company.art_and_culture.myarts.ui.home.HomeAnimations.downloadFadeIn;
import static com.company.art_and_culture.myarts.ui.home.HomeAnimations.downloadFadeOut;
import static com.company.art_and_culture.myarts.ui.home.HomeAnimations.downloadTranslation;

public class SearchFragment extends Fragment implements View.OnClickListener {

    private SearchViewModel searchViewModel;
    private RecyclerView searchRecyclerView;
    private SearchAdapter searchAdapter;
    private ProgressBar searchProgressBar, download_progress;
    private TextView textView;
    private SearchEventListener searchEventListener;
    private SharedPreferences preferences;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View download_view, done_view;
    private CircleImageView add_view;
    private ConstraintLayout download_linear;
    private MainActivity activity;
    private Target target;
    private android.content.res.Resources res;

    private ImageView search_back, search_clear;
    private EditText search_edit_text;
    private RecyclerView suggestions_recycler_view;
    private SuggestAdapter suggestAdapter;
    private ProgressBar suggestions_progress;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_art_search, container, false);
        textView = root.findViewById(R.id.text_search);
        searchRecyclerView = root.findViewById(R.id.recycler_view_search);
        searchProgressBar = root.findViewById(R.id.progress_bar_search);
        swipeRefreshLayout = root.findViewById(R.id.search_swipeRefreshLayout);

        searchViewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        res = getResources();
        int displayWidth = res.getDisplayMetrics().widthPixels;
        int displayHeight = res.getDisplayMetrics().heightPixels;

        initRecyclerView(searchViewModel, displayWidth, displayHeight);

        activity = (MainActivity) getActivity();

        if (activity != null) preferences = activity.getSharedPreferences(Constants.TAG, 0);
        if (activity != null) searchEventListener = activity.getNavFragments();

        searchViewModel.setActivity(activity);

        initSearchViews(root);
        initSuggestRecyclerView();

        initSwipeRefreshLayout();
        subscribeObservers();
        initDownloadViews(root);
        setOnBackPressedListener(root);

        if(SearchDataInMemory.getInstance().getItemCount() > 0) {
            suggestions_recycler_view.setVisibility(View.GONE);
        }

        return root;
    }

    private void initSearchViews(View root) {
        search_back = root.findViewById(R.id.search_back);
        search_clear = root.findViewById(R.id.search_clear);
        search_edit_text = root.findViewById(R.id.search_edit_text);
        suggestions_recycler_view = root.findViewById(R.id.suggestions_recycler_view);
        suggestions_progress = root.findViewById(R.id.suggestions_progress);

        suggestions_progress.setVisibility(View.GONE);
        search_clear.setVisibility(View.GONE);

        search_back.setOnClickListener(this);
        search_clear.setOnClickListener(this);

        search_edit_text.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    search_edit_text.clearFocus();
                }
                return false;
            }
        });
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
                    activity.getInitialSuggests(preferences.getString(Constants.USER_UNIQUE_ID,""));
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
                                                suggestions_progress.setVisibility(View.VISIBLE);
                                                activity.getSuggests(s.toString(), preferences.getString(Constants.USER_UNIQUE_ID,""));
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

                    searchViewModel.setSearchQuery(search_edit_text.getText().toString());
                    hideSoftKeyboard(activity);

                    AnimatorSet set = new AnimatorSet();
                    set.setDuration(300).playTogether(ObjectAnimator.ofFloat(suggestions_recycler_view, View.ALPHA, 1.0f, 0f));
                    set.addListener(fadeOutListener(suggestions_recycler_view));
                    set.start();

                    return true;
                }
                return false;
            }
        });
        search_edit_text.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if(!suggestions_recycler_view.isShown()) {
                        suggestions_recycler_view.setVisibility(View.VISIBLE);
                        AnimatorSet set = new AnimatorSet();
                        set.setDuration(400).playTogether(ObjectAnimator.ofFloat(suggestions_recycler_view, View.ALPHA, 0.2f, 1f));
                        set.start();

                        searchViewModel.setSearchQuery ("");
                    }
                }
                return false;
            }
        });
    }

    private void initSuggestRecyclerView() {
        suggestions_recycler_view.setLayoutManager(new LinearLayoutManager(getContext()));
        SuggestAdapter.OnSuggestClickListener onSuggestClickListener = new SuggestAdapter.OnSuggestClickListener() {
            @Override
            public void onSuggestClick(Suggest suggest, int position) {
                search_edit_text.setText(suggest.getSuggestStr());
                searchViewModel.setSearchQuery(suggest.getSuggestStr());

                AnimatorSet set = new AnimatorSet();
                set.setDuration(300).playTogether(ObjectAnimator.ofFloat(suggestions_recycler_view, View.ALPHA, 1.0f, 0f));
                set.addListener(fadeOutListener(suggestions_recycler_view));
                set.start();

                hideSoftKeyboard(activity);
            }

            @Override
            public void onSuggestLongClick(Suggest suggest, View v) {
                showPopupMenu(suggest, v);
            }
        };
        suggestAdapter = new SuggestAdapter(getContext(), onSuggestClickListener);
        suggestions_recycler_view.setAdapter(suggestAdapter);
    }

    private AnimatorListenerAdapter fadeOutListener(final RecyclerView suggestions_recycler_view) {
        return new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                suggestions_recycler_view.setVisibility(View.GONE);
            }
        };
    }

    private void showPopupMenu(final Suggest suggest, View view) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.inflate(R.menu.delete_query_pop_up_menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete_query:
                        activity.deleteQuery(suggest.getSuggestStr(), preferences.getString(Constants.USER_UNIQUE_ID,""));
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }

    private void initDownloadViews(View root) {
        download_linear = root.findViewById(R.id.download_linear);
        add_view = root.findViewById(R.id.add_view);
        download_view = root.findViewById(R.id.download_view);
        done_view = root.findViewById(R.id.done_view);
        download_progress = root.findViewById(R.id.download_progress);

        download_linear.setVisibility(View.INVISIBLE);
        add_view.setVisibility(View.INVISIBLE);
        download_view.setVisibility(View.INVISIBLE);
        done_view.setVisibility(View.INVISIBLE);
        download_progress.setVisibility(View.INVISIBLE);

    }

    private void setOnBackPressedListener(final View root) {
        //You need to add the following line for this solution to work; thanks skayred
        root.setFocusableInTouchMode(true);
        root.requestFocus();
        root.setOnKeyListener( new View.OnKeyListener() {
            @Override
            public boolean onKey( View v, int keyCode, KeyEvent event ) {

                if( keyCode == KeyEvent.KEYCODE_BACK ) { //&& activity.getNavFragments().getArtShowFragment() == null

                    int scrollPosition = 0;
                    if (searchAdapter.getItemCount() > 0) scrollPosition = getTargetScrollPosition();
                    if (scrollPosition > 4) {
                        searchRecyclerView.smoothScrollToPosition(0);
                        return true;
                    } else {
                        //search_edit_text.setText("");
                        searchViewModel.setSearchQuery ("");
                        return false;
                    }
                }
                return false;
            }
        } );
    }

    private void initSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                boolean networkState = searchViewModel.refresh();
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

        searchViewModel.getArtList().observe(getViewLifecycleOwner(), new Observer<PagedList<Art>>() {
            @Override
            public void onChanged(PagedList<Art> arts) {
                searchAdapter.submitList(arts);
                hideText();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        searchViewModel.getIsLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) { showProgressBar(); } else { hideProgressBar(); }
            }
        });
        searchViewModel.getIsListEmpty().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) { showText(); } else { hideText(); }
            }
        });

        activity.getListSuggest().observe(getViewLifecycleOwner(), new Observer<ServerResponse>() {
            @Override
            public void onChanged(ServerResponse response) {

                suggestions_progress.setVisibility(View.GONE);
                if (response != null) {
                    if (response.getResult().equals(Constants.SUCCESS)) {
                        if (response.getSuggestQuery().equals(search_edit_text.getText().toString())) {
                            setAnimationSuggestsRecyclerView();
                            suggestAdapter.clearItems();
                            suggestAdapter.setItems(response.getListSuggests());
                        }
                    } else {
                        if (response.getSuggestQuery() != null && response.getSuggestQuery().equals(search_edit_text.getText().toString())) {
                            suggestAdapter.clearItems();
                        }
                    }
                }

            }
        });
    }

    private void setAnimationSuggestsRecyclerView() {
        LayoutAnimationController layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_fall_down);
        suggestions_recycler_view.setLayoutAnimation(layoutAnimationController);
        //suggestions_recycler_view.getAdapter().notifyDataSetChanged();
        suggestions_recycler_view.scheduleLayoutAnimation();
    }

    private void initRecyclerView(final SearchViewModel searchViewModel, int displayWidth, int displayHeight){

        SearchAdapter.OnArtClickListener onArtClickListener = new SearchAdapter.OnArtClickListener() {

            @Override
            public void onArtImageClick(Art art, int position, int viewWidth, int viewHeight) {
                Collection<Art> listArts = new ArrayList<>();
                Art artInMemory = SearchDataInMemory.getInstance().getSingleItem(position);
                listArts.add(artInMemory);
                searchEventListener.searchArtClickEvent(listArts, 0);
            }

            @Override
            public void onArtMakerClick(Art art) {
                String artImgUrl;
                if (!art.getArtImgUrlSmall().equals(" ") && art.getArtImgUrlSmall().startsWith(getResources().getString(R.string.http))) {
                    artImgUrl = art.getArtImgUrlSmall();
                } else {
                    artImgUrl= art.getArtImgUrl();
                }
                Maker maker = new Maker(art.getArtMaker(), art.getArtistBio(), artImgUrl, art.getArtWidth(), art.getArtHeight(), art.getArtId(), art.getArtProviderId());
                searchEventListener.searchMakerClickEvent(maker);
            }

            @Override
            public void onArtClassificationClick(Art art) {
                searchEventListener.searchClassificationClickEvent(art.getArtClassification(), Constants.ART_CLASSIFICATION);
            }

            @Override
            public void onArtLikeClick(Art art, int position) {
                boolean networkState = SearchFragment.this.searchViewModel.likeArt (art, position, preferences.getString(Constants.USER_UNIQUE_ID,""));
                if (!networkState) {
                    Toast.makeText(getContext(), R.string.network_is_unavailable, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onArtShareClick(Art art) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                String text = art.getArtMaker()+" - "+art.getArtTitle()
                        + System.getProperty ("line.separator") + art.getArtImgUrl();
                //String text = art.getArtLink();
                sendIntent.putExtra(Intent.EXTRA_TEXT, text);
                sendIntent.setType("text/plain");
                Intent shareIntent = Intent.createChooser(sendIntent, null);
                startActivity(shareIntent);
            }

            @Override
            public void onArtDownloadClick(final Art art, final int x, final int y, final int viewWidth, final int viewHeight) {

                ArrayList<String> arrPerm = checkPermission();
                if(arrPerm.isEmpty()) {

                    final Handler handler = new Handler();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            final File file = getFile (art);
                            if (file.exists()) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(), R.string.file_already_downloaded, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        AnimatorSet set = startDownloadAnimation(x, y);
                                        set.addListener(new AnimatorListenerAdapter() {
                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                super.onAnimationEnd(animation);

                                                Target target = getTarget(file);
                                                int artWidth, artHeight;
                                                if (((float)viewWidth/(float)viewHeight) > 1) {
                                                    artWidth = 1600; artHeight = (int) (artWidth/((float)viewWidth/(float)viewHeight));
                                                } else {
                                                    artHeight = 1600; artWidth = (int) (artHeight/((float)viewHeight/(float)viewWidth));
                                                }
                                                Picasso.get().load(art.getArtImgUrl()).resize(artWidth,artHeight).onlyScaleDown().into(target);
                                            }
                                        });
                                        set.start();
                                    }
                                });
                            }
                        }
                    }).start();

                } else {
                    requestPermissions (arrPerm);
                }
            }

            @Override
            public void onLogoClick(Art art) {

            }
        };

        searchAdapter = new SearchAdapter(searchViewModel,getContext(), onArtClickListener, displayWidth, displayHeight);
        RecyclerView.LayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        searchRecyclerView.setLayoutManager(linearLayoutManager);
        searchRecyclerView.setAdapter(searchAdapter);
    }

    private Target getTarget(final File file) {

        target = new Target() {
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {

                final Handler handler = new Handler();
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                    try {
                        file.createNewFile();
                        FileOutputStream ostream = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, ostream);
                        ostream.flush();
                        ostream.close();

                        ContentValues values = new ContentValues();
                        values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
                        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                        getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                stopDownloadAnimation();
                                runDownLoadSuccessAnimation ();
                            }
                        });

                    } catch (IOException e) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), R.string.download_error, Toast.LENGTH_LONG).show();
                                stopDownloadAnimation();
                            }
                        });
                    }
                    }
                }).start();

            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                Toast.makeText(getContext(), R.string.download_error, Toast.LENGTH_LONG).show();
                stopDownloadAnimation();
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) { }
        };
        return target;
    }

    private void runDownLoadSuccessAnimation () {

        download_linear.setVisibility(View.VISIBLE);
        done_view.setVisibility(View.VISIBLE);

        AnimatorSet set = new AnimatorSet();
        set.playSequentially(
                downloadFadeOut(download_linear, done_view, 0, 0)
        );
        set.start();
    }

    private AnimatorSet startDownloadAnimation(int x, int y) {

        int actionBarHeight = 0;
        //int actionBarHeight = toolbar.getHeight();
        add_view.setX(x);
        add_view.setY(y - actionBarHeight);

        int[] location = new int[2];
        download_view.getLocationOnScreen(location);
        int x1 = location[0];
        int y1 = location[1];
        int targetX = x1 + download_view.getWidth()/2;
        int targetY = y1 + download_view.getHeight()/2;

        AnimatorSet set = new AnimatorSet();
        set.playSequentially(
                downloadFadeIn(download_linear, add_view, download_view),
                downloadTranslation(add_view, targetX, targetY, download_progress)
        );

        return set;
    }

    private void stopDownloadAnimation() {
        download_linear.setVisibility(View.INVISIBLE);
        add_view.setVisibility(View.INVISIBLE);
        download_view.setVisibility(View.INVISIBLE);
        done_view.setVisibility(View.INVISIBLE);
        download_progress.setVisibility(View.INVISIBLE);
    }

    private File getFile(Art art) {
        File pictureFolder = Environment.getExternalStorageDirectory();
        File mainFolder = new File(pictureFolder, res.getString(R.string.folder_my_arts_pictures));
        if (!mainFolder.exists()) {
            mainFolder.mkdirs();
        }
        return new File(mainFolder, art.getArtMaker()+" - "+art.getArtTitle()+".jpg");
    }

    private void showProgressBar(){
        //searchProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar(){
        //searchProgressBar.setVisibility(View.GONE);
    }

    private void showText(){
        //textView.setVisibility(View.VISIBLE);
    }

    private void hideText(){
        //textView.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == search_back.getId()) {

            hideSoftKeyboard(activity);
            //search_edit_text.setText("");
            searchViewModel.setSearchQuery ("");
            activity.getNavFragments().popBackStack();//finishSearchFragment();

        } else if (v.getId() == search_clear.getId()) {
            search_edit_text.setText("");
            searchViewModel.setSearchQuery ("");
            if(!suggestions_recycler_view.isShown()) {
                suggestions_recycler_view.setVisibility(View.VISIBLE);
                AnimatorSet set = new AnimatorSet();
                set.setDuration(400).playTogether(ObjectAnimator.ofFloat(suggestions_recycler_view, View.ALPHA, 0.2f, 1f));
                set.start();
            }
        }
    }

    public interface SearchEventListener {
        void searchArtClickEvent(Collection<Art> arts, int position);
        void searchMakerClickEvent(Maker maker);
        void searchClassificationClickEvent(String artClassification, String queryType);
    }

    private int getTargetScrollPosition () {

        final int firstPosition = ((LinearLayoutManager) searchRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        final int lastPosition = ((LinearLayoutManager) searchRecyclerView.getLayoutManager()).findLastVisibleItemPosition();

        Rect rvRect = new Rect();
        searchRecyclerView.getGlobalVisibleRect(rvRect);

        int targetPosition = firstPosition;
        int targetPercent = 0;
        for (int i = firstPosition; i <= lastPosition; i++) {

            Rect rowRect = new Rect();
            searchRecyclerView.getLayoutManager().findViewByPosition(i).getGlobalVisibleRect(rowRect);

            int percent;
            if (rowRect.bottom >= rvRect.bottom){
                int visibleHeightFirst =rvRect.bottom - rowRect.top;
                percent = (visibleHeightFirst * 100) / searchRecyclerView.getLayoutManager().findViewByPosition(i).getHeight();
            }else {
                int visibleHeightFirst = rowRect.bottom - rvRect.top;
                percent = (visibleHeightFirst * 100) / searchRecyclerView.getLayoutManager().findViewByPosition(i).getHeight();
            }

            if (percent>100) percent = 100;

            if (percent > targetPercent) {
                targetPercent = percent;
                targetPosition = i;
            }
        }
        return targetPosition;
    }

    private ArrayList<String> checkPermission() {

        ArrayList<String> arrPerm = new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            arrPerm.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            arrPerm.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        return arrPerm;
    }

    private void requestPermissions (ArrayList<String> arrPerm) {
        String[] permissions = new String[arrPerm.size()];
        permissions = arrPerm.toArray(permissions);
        ActivityCompat.requestPermissions(getActivity(), permissions, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0) {
                    for(int i = 0; i < grantResults.length; i++) {
                        String permission = permissions[i];
                        if(Manifest.permission.READ_EXTERNAL_STORAGE.equals(permission)) {
                            if(grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                                // you now have permission
                                Toast.makeText(getContext(), R.string.thanks_for_the_permissions_download_file_please, Toast.LENGTH_LONG).show();
                            }
                        }
                        if(Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permission)) {
                            if(grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                                // you now have permission
                                Toast.makeText(getContext(), R.string.thanks_for_the_permissions_download_file_please, Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getContext(), R.string.application_does_not_have_permission_to_download_the_file, Toast.LENGTH_LONG).show();
                }
                break;
            }
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        search_edit_text.setText("");
    }

}
package com.company.art_and_culture.myarts.bottom_menu.recommendations;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.ImageDownloader;
import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.pojo.Maker;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.company.art_and_culture.myarts.Constants.PERMISSION_REQUEST_CODE;
import static com.company.art_and_culture.myarts.bottom_menu.home.HomeAnimations.downloadFadeIn;
import static com.company.art_and_culture.myarts.bottom_menu.home.HomeAnimations.downloadFadeOut;
import static com.company.art_and_culture.myarts.bottom_menu.home.HomeAnimations.downloadTranslation;

public class RecommendationsFragment extends Fragment implements View.OnClickListener, ImageDownloader.IDownLoadResult {

    private RecommendViewModel recommendViewModel;
    private TextView textView;
    private RecyclerView recyclerView;
    private RecommendAdapter recommendAdapter;
    private ProgressBar progressBar, download_progress;
    private SwipeRefreshLayout swipeRefreshLayout;
    private android.content.res.Resources res;
    private RecommendationsEventListener recommendationsEventListener;
    private ImageView search_btn;
    private MainActivity activity;
    private SharedPreferences preferences;
    private View download_view, done_view;
    private CircleImageView add_view;
    private ConstraintLayout download_linear;
    private int bottomInitialMargin = 0, leftInitialMargin = 0;
    private int scrollPosition = 0;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        recommendViewModel = new ViewModelProvider(this).get(RecommendViewModel.class);

        View root = inflater.inflate(R.layout.fragment_recommendations, container, false);
        textView = root.findViewById(R.id.text);
        recyclerView = root.findViewById(R.id.recyclerView);
        progressBar = root.findViewById(R.id.progress_bar);
        swipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayout);
        search_btn = root.findViewById(R.id.search_btn);
        search_btn.setOnClickListener(this);

        res = getResources();
        int displayWidth = res.getDisplayMetrics().widthPixels;
        int displayHeight = res.getDisplayMetrics().heightPixels;

        initRecyclerView(displayWidth, displayHeight);

        activity = (MainActivity) getActivity();
        if (activity != null) recommendationsEventListener = activity.getNavFragments();
        if (activity != null) preferences = activity.getSharedPreferences(Constants.TAG, 0);

        if (activity != null) scrollPosition = activity.getNavFragments().getRecommendPosition();
        if (scrollPosition >= 0) recyclerView.scrollToPosition(scrollPosition);
        recommendViewModel.setActivity(activity);

        initDownloadViews(root);
        initSwipeRefreshLayout();
        subscribeObservers();
        setOnBackPressedListener(root);

        return root;
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

    private void setOnBackPressedListener(View root) {
        //You need to add the following line for this solution to work; thanks skayred
        root.setFocusableInTouchMode(true);
        root.requestFocus();
        root.setOnKeyListener( new View.OnKeyListener() {
            @Override
            public boolean onKey( View v, int keyCode, KeyEvent event ) {

                if( keyCode == KeyEvent.KEYCODE_BACK ) { //&& activity.getNavFragments().getArtShowFragment() == null // && !activity.isSearchLayoutOpen()

                    if (recommendAdapter.getItemCount() > 0) scrollPosition = getTargetScrollPosition();
                    if (scrollPosition > 4) {
                        recyclerView.smoothScrollToPosition(0);
                        return true;
                    }
                    return false;
                }
                return false;
            }
        } );
    }


    private void initRecyclerView(int displayWidth, int displayHeight) {

        RecommendAdapter.OnArtClickListener onArtClickListener = new RecommendAdapter.OnArtClickListener(){

            @Override
            public void onArtImageClick(Art art, int position) {
                recommendationsEventListener.recommendationsArtClickEvent(recommendAdapter.getItems(), position);
            }

            @Override
            public void onArtLikeClick(Art art, int position) {
                boolean networkState = recommendViewModel.likeArt (art, position, preferences.getString(Constants.USER_UNIQUE_ID,""));
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
            public void onArtDownloadClick(Art art, int x, int y, int viewWidth, int viewHeight) {
                ImageDownloader imageDownloader = ImageDownloader.getInstance(RecommendationsFragment.this);
                ArrayList<String> arrPerm = imageDownloader.checkPermission(getContext());
                if(arrPerm.isEmpty()) {

                    String folderName = res.getString(R.string.folder_my_arts_pictures);
                    boolean isExists = imageDownloader.isFileExists(art, folderName);
                    if (isExists)
                        Toast.makeText(getContext(), R.string.file_already_downloaded, Toast.LENGTH_SHORT).show();
                    else {
                        AnimatorSet set = startDownloadAnimation(x, y);
                        set.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                imageDownloader.downloadImage(art, viewWidth, viewHeight, folderName);
                            }
                        });
                        set.start();
                    }

                } else {
                    imageDownloader.requestPermissions (arrPerm, activity);
                }
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
                recommendationsEventListener.recommendationsMakerClickEvent(maker);
            }

            @Override
            public void onArtClassificationClick(Art art) {
                recommendationsEventListener.recommendationsClassificationClickEvent(art.getArtClassification(), Constants.ART_CLASSIFICATION);
            }

            @Override
            public void onSaveToFolderClick(Art art) {
                activity.showSaveToFolderView(art);
            }
        };
        recommendAdapter = new RecommendAdapter(recommendViewModel, getContext(), onArtClickListener, displayWidth, displayHeight);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(recommendAdapter);
    }

    private void subscribeObservers() {

        recommendViewModel.getArtList().observe(getViewLifecycleOwner(), objects -> {

            if (objects == null) {
                recommendAdapter.clearItems();
            } else {
                setAnimationRecyclerView (objects, recommendAdapter, recyclerView);
                recommendAdapter.clearItems();
                recommendAdapter.setItems(objects);
            }
            swipeRefreshLayout.setRefreshing(false);
        });
        recommendViewModel.getIsLoading().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) { showProgressBar(); } else { hideProgressBar(); }
        });
        recommendViewModel.getIsListEmpty().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) { showText(); } else { hideText(); }
        });

    }

    private void initSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            boolean networkState = refresh();
            if (!networkState) {
                Toast.makeText(getContext(), R.string.network_is_unavailable, Toast.LENGTH_LONG).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorBlue
        );
    }

    private void showProgressBar(){
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar(){
        progressBar.setVisibility(View.GONE);
    }

    private void showText(){
        textView.setVisibility(View.VISIBLE);
    }

    private void hideText(){
        textView.setVisibility(View.GONE);
    }

    public boolean refresh () {
        return recommendViewModel.refresh();
    }

    private void setAnimationRecyclerView(ArrayList<Art> objects, RecommendAdapter adapter, RecyclerView recyclerView) {

        LayoutAnimationController layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_fade_in);
        recyclerView.setLayoutAnimation(layoutAnimationController);
        //recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (recommendAdapter.getItemCount() > 0) scrollPosition = getTargetScrollPosition();
        recommendationsEventListener.recommendScrollEvent(scrollPosition);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == search_btn.getId()) {
            recommendationsEventListener.recommendationsSearchClickEvent();
        }
    }

    @Override
    public void onDownloadSuccess(File file) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        stopDownloadAnimation();
        runDownLoadSuccessAnimation ();
    }

    @Override
    public void onDownloadFailure() {
        Toast.makeText(getContext(), R.string.download_error, Toast.LENGTH_LONG).show();
        stopDownloadAnimation();
    }

    public interface RecommendationsEventListener {
        void recommendationsArtClickEvent(Collection<Art> arts, int position);
        void recommendationsMakerClickEvent(Maker maker);
        void recommendationsSearchClickEvent();
        void recommendationsClassificationClickEvent(String artClassification, String queryType);
        void recommendScrollEvent(int scrollPosition);
    }

    private void runDownLoadSuccessAnimation () {

        download_linear.setVisibility(View.VISIBLE);
        done_view.setVisibility(View.VISIBLE);

        AnimatorSet set = new AnimatorSet();
        set.playSequentially(
                downloadFadeOut(download_linear, done_view, leftInitialMargin, bottomInitialMargin)
        );
        set.start();
    }

    private AnimatorSet startDownloadAnimation(int x, int y) {

        add_view.setX(x);
        add_view.setY(y);

        ConstraintLayout.MarginLayoutParams marginLayoutParams = (ConstraintLayout.MarginLayoutParams) download_linear.getLayoutParams();
        bottomInitialMargin = marginLayoutParams.bottomMargin;
        leftInitialMargin = marginLayoutParams.leftMargin;
        int bottomMargin = bottomInitialMargin;

        if (activity.isNavViewOnScreen()) {
            int navViewHeight = activity.getNavViewHeight();
            bottomMargin = bottomMargin + navViewHeight;
        }
        marginLayoutParams.setMargins(leftInitialMargin, 0, 0, bottomMargin);
        download_linear.setLayoutParams(marginLayoutParams);

        int[] location = new int[2];
        download_view.getLocationOnScreen(location);
        int x1 = location[0];
        int y1 = location[1];
        int targetX = x1 + download_view.getWidth()/2;
        int targetY = y1 + download_view.getHeight()/2;

        if (activity.isNavViewOnScreen()) {
            int navViewHeight = activity.getNavViewHeight();
            targetY = targetY - navViewHeight;
        }

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

    private int getTargetScrollPosition () {

        final int firstPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        final int lastPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition();

        Rect rvRect = new Rect();
        recyclerView.getGlobalVisibleRect(rvRect);

        int targetPosition = firstPosition;
        int targetPercent = 0;
        for (int i = firstPosition; i <= lastPosition; i++) {

            Rect rowRect = new Rect();
            recyclerView.getLayoutManager().findViewByPosition(i).getGlobalVisibleRect(rowRect);

            int percent;
            if (rowRect.bottom >= rvRect.bottom){
                int visibleHeightFirst =rvRect.bottom - rowRect.top;
                percent = (visibleHeightFirst * 100) / recyclerView.getLayoutManager().findViewByPosition(i).getHeight();
            }else {
                int visibleHeightFirst = rowRect.bottom - rvRect.top;
                percent = (visibleHeightFirst * 100) / recyclerView.getLayoutManager().findViewByPosition(i).getHeight();
            }

            if (percent>100) percent = 100;

            if (percent > targetPercent) {
                targetPercent = percent;
                targetPosition = i;
            }
        }
        return targetPosition;
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

}
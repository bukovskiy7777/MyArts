package com.company.art_and_culture.myarts.ui.home;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
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
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.pojo.Art;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;

import static com.company.art_and_culture.myarts.Constants.PERMISSION_REQUEST_CODE;
import static com.company.art_and_culture.myarts.ui.home.DownloadAnimations.fadeIn;
import static com.company.art_and_culture.myarts.ui.home.DownloadAnimations.fadeOut;
import static com.company.art_and_culture.myarts.ui.home.DownloadAnimations.translation;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private RecyclerView homeRecyclerView;
    private HomeAdapter homeAdapter;
    private ProgressBar homeProgressBar, download_progress;
    private TextView textView;
    private android.content.res.Resources res;
    private int scrollPosition = 0;
    private OnScrollEventListener scrollEventListener;
    private SharedPreferences preferences;
    private String userUniqueId;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View download_view, done_view;
    private CircleImageView add_view;
    private ConstraintLayout download_linear;
    private MainActivity activity;
    private Target target;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);
        textView = root.findViewById(R.id.text_home);
        homeRecyclerView = root.findViewById(R.id.recycler_view_home);
        homeProgressBar = root.findViewById(R.id.progress_bar_home);
        swipeRefreshLayout = root.findViewById(R.id.home_swipeRefreshLayout);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        res = getResources();
        int displayWidth = res.getDisplayMetrics().widthPixels;
        int displayHeight = res.getDisplayMetrics().heightPixels;

        initRecyclerView(homeViewModel, displayWidth, displayHeight);

        activity = (MainActivity) getActivity();
        if (activity != null) scrollPosition = activity.getPosition();
        if (scrollPosition >= 0) homeRecyclerView.scrollToPosition(scrollPosition);

        if (activity != null) preferences = activity.getSharedPreferences(Constants.TAG, 0);
        userUniqueId = getUserUniqueId();

        initSwipeRefreshLayout();
        subscribeObservers();
        initDownloadViews(root);
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

                if( keyCode == KeyEvent.KEYCODE_BACK ) {
                    if (homeAdapter.getItemCount() > 0) scrollPosition = getTargetScrollPosition();
                    if (scrollPosition > 4) {
                        homeRecyclerView.smoothScrollToPosition(0);
                        return true;
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
                boolean networkState = homeViewModel.refresh();
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

        homeViewModel.getArtList().observe(getViewLifecycleOwner(), new Observer<PagedList<Art>>() {
            @Override
            public void onChanged(PagedList<Art> arts) {
                homeAdapter.submitList(arts);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        homeViewModel.getIsLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) { showProgressBar(); } else { hideProgressBar(); }
            }
        });
        homeViewModel.getIsListEmpty().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                Log.i("updateIsListEmptyState", aBoolean+" "+ "homeFragment");
                if (aBoolean) { showText(); } else { hideText(); }
            }
        });
    }

    private void initRecyclerView(HomeViewModel homeViewModel, int displayWidth, int displayHeight){

        HomeAdapter.OnArtClickListener onArtClickListener = new HomeAdapter.OnArtClickListener() {

            @Override
            public void onArtImageClick(Art art, int viewWidth, int viewHeight) {

            }

            @Override
            public void onArtMakerClick(Art art) {

            }

            @Override
            public void onArtClassificationClick(Art art) {

            }

            @Override
            public void onArtLikeClick(Art art, int position) {
                HomeFragment.this.homeViewModel.likeArt (art, position, userUniqueId);
            }

            @Override
            public void onArtShareClick(Art art) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                String text = art.getArtMaker()+" - "+art.getArtTitle()
                        + System.getProperty ("line.separator") + art.getArtImgUrlSmall();
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
        };

        homeAdapter = new HomeAdapter(homeViewModel,getContext(), onArtClickListener, displayWidth, displayHeight);
        RecyclerView.LayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        homeRecyclerView.setLayoutManager(linearLayoutManager);
        homeRecyclerView.setAdapter(homeAdapter);
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
                fadeOut(download_linear, done_view)
        );
        set.start();
    }

    private AnimatorSet startDownloadAnimation(int x, int y) {

        int actionBarHeight = 0;
        if (activity != null) actionBarHeight = activity.getSupportActionBar().getHeight();

        add_view.setX(x);
        add_view.setY(y - actionBarHeight);

        AnimatorSet set = new AnimatorSet();
        set.playSequentially(
                fadeIn(download_linear, add_view, download_view),
                translation(add_view, download_view, download_progress)
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
        File mainFolder = new File(pictureFolder, "My Arts Pictures");
        if (!mainFolder.exists()) {
            mainFolder.mkdirs();
        }
        return new File(mainFolder, art.getArtMaker()+" - "+art.getArtTitle()+".jpg");
    }

    private void showProgressBar(){
        homeProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar(){
        homeProgressBar.setVisibility(View.GONE);
    }

    private void showText(){
        textView.setVisibility(View.VISIBLE);
    }

    private void hideText(){
        textView.setVisibility(View.GONE);
    }

    public interface OnScrollEventListener {
        void scrollEvent(int position);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            scrollEventListener = (OnScrollEventListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement onSomeEventListener");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (homeAdapter.getItemCount() > 0) scrollPosition = getTargetScrollPosition();
        scrollEventListener.scrollEvent(scrollPosition);
    }

    private int getTargetScrollPosition () {

        final int firstPosition = ((LinearLayoutManager) homeRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        final int lastPosition = ((LinearLayoutManager) homeRecyclerView.getLayoutManager()).findLastVisibleItemPosition();

        Rect rvRect = new Rect();
        homeRecyclerView.getGlobalVisibleRect(rvRect);

        int targetPosition = firstPosition;
        int targetPercent = 0;
        for (int i = firstPosition; i <= lastPosition; i++) {

            Rect rowRect = new Rect();
            homeRecyclerView.getLayoutManager().findViewByPosition(i).getGlobalVisibleRect(rowRect);

            int percent;
            if (rowRect.bottom >= rvRect.bottom){
                int visibleHeightFirst =rvRect.bottom - rowRect.top;
                percent = (visibleHeightFirst * 100) / homeRecyclerView.getLayoutManager().findViewByPosition(i).getHeight();
            }else {
                int visibleHeightFirst = rowRect.bottom - rvRect.top;
                percent = (visibleHeightFirst * 100) / homeRecyclerView.getLayoutManager().findViewByPosition(i).getHeight();
            }

            if (percent>100) percent = 100;

            if (percent > targetPercent) {
                targetPercent = percent;
                targetPosition = i;
            }
        }
        return targetPosition;
    }

    private String getUserUniqueId() {

        if (preferences.getString(Constants.USER_UNIQUE_ID,"").length()==0) {
            userUniqueId = randomString(23);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(Constants.USER_UNIQUE_ID, userUniqueId);
            editor.apply();
        }
        return preferences.getString(Constants.USER_UNIQUE_ID,"");
    }

    private String randomString(int len){
        String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        SecureRandom rnd = new SecureRandom();

        StringBuilder sb = new StringBuilder( len );
        for( int i = 0; i < len; i++ )
            sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
        return sb.toString();
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
                    Toast.makeText(getContext(), R.string.sorry__the_application_does_not_have_permission_to_download_the_file, Toast.LENGTH_LONG).show();
                }
                break;
            }
        }

    }




}
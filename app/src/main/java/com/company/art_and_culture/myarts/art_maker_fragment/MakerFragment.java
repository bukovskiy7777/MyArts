package com.company.art_and_culture.myarts.art_maker_fragment;

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

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.pojo.Maker;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

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
import static com.company.art_and_culture.myarts.ui.home.HomeAnimations.downloadFadeIn;
import static com.company.art_and_culture.myarts.ui.home.HomeAnimations.downloadFadeOut;
import static com.company.art_and_culture.myarts.ui.home.HomeAnimations.downloadTranslation;

public class MakerFragment extends Fragment {

    private MakerViewModel makerViewModel;
    private RecyclerView makerRecyclerView;
    private MakerAdapter makerAdapter;
    private MakerEventListener makerEventListener;
    private SwipeRefreshLayout swipeRefreshLayout;
    private android.content.res.Resources res;
    private MainActivity activity;
    private Maker maker;
    private TextView textView;
    private ProgressBar makerProgressBar, download_progress;
    private View download_view, done_view;
    private CircleImageView add_view;
    private ConstraintLayout download_linear;
    private Target target;
    private SharedPreferences preferences;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_art_maker, container, false);
        makerRecyclerView = root.findViewById(R.id.recycler_view_maker);
        swipeRefreshLayout = root.findViewById(R.id.maker_swipeRefreshLayout);
        textView = root.findViewById(R.id.text_maker);
        makerProgressBar = root.findViewById(R.id.progress_bar_maker);

        makerViewModel = new ViewModelProvider(this).get(MakerViewModel.class);

        res = getResources();
        int displayWidth = res.getDisplayMetrics().widthPixels;
        int displayHeight = res.getDisplayMetrics().heightPixels;

        activity = (MainActivity) getActivity();
        if (activity != null) maker = activity.getNavFragments().getMakerForMakerFragment();
        if (activity != null) preferences = activity.getSharedPreferences(Constants.TAG, 0);
        if (activity != null) makerEventListener = activity.getNavFragments();

        makerViewModel.setArtMaker(maker);

        initRecyclerView(makerViewModel, displayWidth, displayHeight, maker);
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

    private void setOnBackPressedListener(final View root) {
        //You need to add the following line for this solution to work; thanks skayred
        root.setFocusableInTouchMode(true);
        root.requestFocus();
        root.setOnKeyListener( new View.OnKeyListener() {
            @Override
            public boolean onKey( View v, int keyCode, KeyEvent event ) {

                if( keyCode == KeyEvent.KEYCODE_BACK && activity.getNavFragments().getArtShowFragment() == null ) {
                    int scrollPosition = 0;
                    if (makerAdapter.getItemCount() > 0) scrollPosition = getTargetScrollPosition();
                    if (scrollPosition > 4) {
                        makerRecyclerView.smoothScrollToPosition(0);
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
                boolean networkState = makerViewModel.refresh();
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

        makerViewModel.getArtList().observe(getViewLifecycleOwner(), new Observer<PagedList<Art>>() {
            @Override
            public void onChanged(PagedList<Art> arts) {
                makerAdapter.submitList(arts);
                hideText();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        makerViewModel.getIsLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) { showProgressBar(); } else { hideProgressBar(); }
            }
        });
        makerViewModel.getIsListEmpty().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) { showText(); } else { hideText(); }
            }
        });
    }

    private void initRecyclerView(final MakerViewModel makerViewModel, int displayWidth, int displayHeight, Maker maker){

        MakerAdapter.OnArtClickListener onArtClickListener = new MakerAdapter.OnArtClickListener() {

            @Override
            public void onArtImageClick(Art art, int position) {
                ArrayList<Art> artInMemory = MakerDataInMemory.getInstance().getAllData();
                makerEventListener.makerArtClickEvent(artInMemory, position);
            }

            @Override
            public void onArtLikeClick(Art art, int position) {
                boolean networkState = MakerFragment.this.makerViewModel.likeArt (art, position, preferences.getString(Constants.USER_UNIQUE_ID,""));
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
            public void onMakerLikeClick(Maker maker) {
                //Maker maker = new Maker (artMaker, artistBio, artistImageUrl, artHeaderImageUrl, artWidth, artHeight);
                boolean networkState = MakerFragment.this.makerViewModel.likeMaker (maker, preferences.getString(Constants.USER_UNIQUE_ID,""));
                if (!networkState) {
                    Toast.makeText(getContext(), R.string.network_is_unavailable, Toast.LENGTH_SHORT).show();
                }
            }

        };

        makerAdapter = new MakerAdapter(makerViewModel,getContext(), onArtClickListener, displayWidth, displayHeight, maker);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        makerRecyclerView.setLayoutManager(layoutManager);
        makerRecyclerView.setAdapter(makerAdapter);
    }

    private void showProgressBar(){
        makerProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar(){
        makerProgressBar.setVisibility(View.GONE);
    }

    private void showText(){
        textView.setVisibility(View.VISIBLE);
    }

    private void hideText(){
        textView.setVisibility(View.GONE);
    }

    public interface MakerEventListener {
        void makerArtClickEvent(Collection<Art> arts, int position);
    }

    private int getTargetScrollPosition () {

        int targetPosition = 0;
        if(makerAdapter.getItemCount() > 0) {
            final int firstPosition = ((LinearLayoutManager) makerRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
            final int lastPosition = ((LinearLayoutManager) makerRecyclerView.getLayoutManager()).findLastVisibleItemPosition();

            Rect rvRect = new Rect();
            makerRecyclerView.getGlobalVisibleRect(rvRect);

            targetPosition = firstPosition;
            int targetPercent = 0;
            for (int i = firstPosition; i <= lastPosition; i++) {

                Rect rowRect = new Rect();
                makerRecyclerView.getLayoutManager().findViewByPosition(i).getGlobalVisibleRect(rowRect);

                int percent;
                if (rowRect.bottom >= rvRect.bottom){
                    int visibleHeightFirst =rvRect.bottom - rowRect.top;
                    percent = (visibleHeightFirst * 100) / makerRecyclerView.getLayoutManager().findViewByPosition(i).getHeight();
                }else {
                    int visibleHeightFirst = rowRect.bottom - rvRect.top;
                    percent = (visibleHeightFirst * 100) / makerRecyclerView.getLayoutManager().findViewByPosition(i).getHeight();
                }

                if (percent>100) percent = 100;

                if (percent > targetPercent) {
                    targetPercent = percent;
                    targetPosition = i;
                }
            }
        }
        return targetPosition;
    }

    private File getFile(Art art) {
        File pictureFolder = Environment.getExternalStorageDirectory();
        File mainFolder = new File(pictureFolder, res.getString(R.string.folder_my_arts_pictures));
        if (!mainFolder.exists()) {
            mainFolder.mkdirs();
        }
        return new File(mainFolder, art.getArtMaker()+" - "+art.getArtTitle()+".jpg");
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

    private AnimatorSet startDownloadAnimation(int x, int y) {

        int actionBarHeight = 0;
        //if (activity != null) actionBarHeight = activity.getToolbarHeight();
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

    private void runDownLoadSuccessAnimation () {

        download_linear.setVisibility(View.VISIBLE);
        done_view.setVisibility(View.VISIBLE);

        AnimatorSet set = new AnimatorSet();
        set.playSequentially(
                downloadFadeOut(download_linear, done_view, 0, 0)
        );
        set.start();
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



}
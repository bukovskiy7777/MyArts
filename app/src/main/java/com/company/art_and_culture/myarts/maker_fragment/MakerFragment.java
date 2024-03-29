package com.company.art_and_culture.myarts.maker_fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.ImageDownloader;
import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.arts_show_fragment.ArtShowFragment;
import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.pojo.FilterObject;
import com.company.art_and_culture.myarts.pojo.Maker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.company.art_and_culture.myarts.CommonAnimations.downloadFadeIn;
import static com.company.art_and_culture.myarts.CommonAnimations.downloadFadeOut;
import static com.company.art_and_culture.myarts.CommonAnimations.downloadTranslation;
import static com.company.art_and_culture.myarts.CommonAnimations.likeFadeIn;
import static com.company.art_and_culture.myarts.CommonAnimations.likeScaleDown;
import static com.company.art_and_culture.myarts.CommonAnimations.shareScaleDown;
import static com.company.art_and_culture.myarts.CommonAnimations.shareScaleUp;
import static com.company.art_and_culture.myarts.Constants.PERMISSION_REQUEST_CODE;
import static com.company.art_and_culture.myarts.bottom_menu.favorites.Favorites.FavoritesAnimations.scaleDown;
import static com.company.art_and_culture.myarts.bottom_menu.favorites.Favorites.FavoritesAnimations.scaleUp;

public class MakerFragment extends Fragment implements ImageDownloader.IDownLoadResult, View.OnClickListener, View.OnTouchListener {

    public static final String MAKER = "maker";

    private MakerViewModel makerViewModel;
    private RecyclerView makerRecyclerView, recycler_view_keywords;
    private KeywordAdapter.OnFilterClickListener onKeywordClickListener;
    private KeywordAdapter keywordAdapter;
    private MakerAdapter makerAdapter;
    private CoordinatorLayout coordinator;
    private android.content.res.Resources res;
    private MainActivity activity;
    private Maker maker;
    private TextView textView;
    private ProgressBar makerProgressBar, download_progress;
    private View download_view, done_view;
    private CircleImageView add_view;
    private ConstraintLayout download_linear;
    private SharedPreferences preferences;
    private ImageView art_image_header, maker_image;
    private TextView maker_name, maker_bio, maker_description, read_more, wikipedia, art_count, art_tags, tags_clear;
    private ImageButton maker_like, maker_share, arts_in_list, arts_in_columns;
    private String makerWikiImageUrl, makerWikiPageUrl;
    private FloatingActionButton floating_button;
    private int displayWidth, displayHeight;
    private int spanCount = 1;
    private String keyword;
    private ArrayList<FilterObject> listKeywordFilters = new ArrayList<>();
    private ImageDownloader imageDownloader;
    private Art downloadArt;
    private int downloadX, downloadY;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_art_maker, container, false);
        initViews(root);

        makerViewModel = new ViewModelProvider(this).get(MakerViewModel.class);

        res = getResources();
        displayWidth = res.getDisplayMetrics().widthPixels;
        displayHeight = res.getDisplayMetrics().heightPixels;

        activity = (MainActivity) getActivity();
        if (activity != null) preferences = activity.getSharedPreferences(Constants.TAG, 0);

        maker = (Maker) getArguments().getSerializable(MAKER);
        if(maker == null) {
            NavHostFragment.findNavController(MakerFragment.this).popBackStack();
        } else {
            makerViewModel.setArtMaker(maker);
            makerViewModel.setActivity(activity);
            FilterObject filterObject = makerViewModel.getFilterObject();
            if(filterObject != null) keyword = filterObject.getText();

            initRecyclerView(makerViewModel, displayWidth, displayHeight, spanCount);
            initTagsRecyclerView();
            subscribeObserverMakerData();
            subscribeObserverArtsData();
            initDownloadViews(root);
            setOnBackPressedListener(root);
            setMakerInfo(maker);
        }

        if (spanCount == 3) {
            arts_in_columns.setImageResource(R.drawable.ic_apps_blue_100dp);
        } else if (spanCount == 1) {
            arts_in_list.setImageResource(R.drawable.ic_baseline_format_list_bulleted_24_blue);
        }
        coordinator.setVisibility(View.GONE);

        return root;
    }

    private void initTagsRecyclerView() {
        onKeywordClickListener = (filterObject, position) -> {

            if(keyword == null || !keyword.equals(filterObject.getText())) {

                boolean networkState = makerViewModel.makerTagClick(filterObject);
                if (!networkState) Toast.makeText(getContext(), R.string.network_is_unavailable, Toast.LENGTH_SHORT).show();
                keyword = filterObject.getText();
                tags_clear.setVisibility(View.VISIBLE);
            }
        };
        keywordAdapter = new KeywordAdapter(getContext(), onKeywordClickListener);
        recycler_view_keywords.setAdapter(keywordAdapter);
    }

    private void initViews(View root) {
        makerRecyclerView = root.findViewById(R.id.recycler_view_maker);
        recycler_view_keywords = root.findViewById(R.id.recycler_view_keywords);
        coordinator = root.findViewById(R.id.coordinator);
        textView = root.findViewById(R.id.text_maker);
        tags_clear = root.findViewById(R.id.tags_clear);
        tags_clear.setVisibility(View.GONE);
        art_tags = root.findViewById(R.id.art_tags);
        art_tags.setVisibility(View.GONE);
        makerProgressBar = root.findViewById(R.id.progress_bar_maker);
        art_image_header = root.findViewById(R.id.art_image_header);
        maker_image = root.findViewById(R.id.maker_image);
        maker_name = root.findViewById(R.id.maker_name);
        maker_bio = root.findViewById(R.id.maker_bio);
        maker_description = root.findViewById(R.id.maker_description);
        maker_like = root.findViewById(R.id.maker_like);
        maker_share = root.findViewById(R.id.maker_share);
        read_more = root.findViewById(R.id.read_more);
        wikipedia = root.findViewById(R.id.wikipedia);
        art_count = root.findViewById(R.id.art_count);
        floating_button = root.findViewById(R.id.floating_button);
        arts_in_list = root.findViewById(R.id.arts_in_list);
        arts_in_columns = root.findViewById(R.id.arts_in_columns);
        arts_in_list.setOnClickListener(this);
        arts_in_columns.setOnClickListener(this);
        tags_clear.setOnClickListener(this);
        floating_button.setOnClickListener(this);
        maker_like.setOnClickListener(this);
        maker_share.setOnClickListener(this);
        read_more.setOnClickListener(this);
        read_more.setVisibility(View.GONE);
        wikipedia.setOnClickListener(this);
        wikipedia.setOnTouchListener(this);
        wikipedia.setVisibility(View.GONE);
    }

    private void setMakerInfo(Maker maker) {
        maker_name.setText(maker.getArtMaker());
        maker_bio.setText(maker.getArtistBio());

        if (maker.getArtWidth() > 0) {
            int imgWidth = displayWidth;
            int imgHeight = (maker.getArtHeight() * imgWidth) / maker.getArtWidth();
            art_image_header.getLayoutParams().height = Math.min(imgHeight, art_image_header.getMaxHeight());
            Picasso.get().load(maker.getArtHeaderImageUrl()).placeholder(R.color.colorSilver).resize(imgWidth, imgHeight).onlyScaleDown().into(art_image_header);
        } else {
            Picasso.get().load(maker.getArtHeaderImageUrl()).placeholder(R.color.colorSilver).into(art_image_header);
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == read_more.getId()) {
            if (maker_description.getMaxLines() == 4) {
                Paint paint = new Paint();
                paint.setTextSize(getContext().getResources().getDimension(R.dimen.text_size_maker_description));
                float widthText = paint.measureText(maker_description.getText().toString());
                float numLines = (float) ((widthText/displayWidth) * 1.2);
                read_more.setText(getContext().getResources().getString(R.string.show_less));

                ObjectAnimator animation = ObjectAnimator.ofInt(maker_description, "maxLines", (int) (numLines + 10));
                animation.setDuration(600).start();
            } else {
                ObjectAnimator animation = ObjectAnimator.ofInt(maker_description, "maxLines", 4);
                animation.setDuration(600).start();
                read_more.setText(getContext().getResources().getString(R.string.read_more));
            }

        } else if (v.getId() == maker_like.getId()) {
            Maker newMaker = new Maker (maker.getArtMaker(), maker.getArtistBio(), maker.getArtHeaderImageUrl(),
                            maker.getArtWidth(), maker.getArtHeight(), makerWikiImageUrl, maker.getArtHeaderId(), maker.getArtHeaderProviderId());
            boolean networkState = makerViewModel.likeMaker (newMaker, preferences.getString(Constants.USER_UNIQUE_ID,""));
            if (!networkState) {
                Toast.makeText(getContext(), R.string.network_is_unavailable, Toast.LENGTH_SHORT).show();
            }

        } else if(v.getId() == maker_share.getId()) {
            AnimatorSet set = new AnimatorSet();
            set.playSequentially(shareScaleUp(maker_share), shareScaleDown(maker_share));
            set.start();

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            String text;
            if (makerWikiPageUrl != null) {
                text = maker.getArtMaker()+" - "+maker.getArtistBio() + System.getProperty ("line.separator") + makerWikiPageUrl;
            } else {
                text = maker.getArtMaker()+" - "+maker.getArtistBio() + System.getProperty ("line.separator") + maker.getArtHeaderImageUrl();
            }
            //String text = art.getArtLink();
            sendIntent.putExtra(Intent.EXTRA_TEXT, text);
            sendIntent.setType("text/plain");
            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);

        } else if(v.getId() == wikipedia.getId()) {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            builder.setStartAnimations(getContext(), R.anim.enter_from_right, R.anim.exit_to_left);
            builder.setExitAnimations(getContext(), R.anim.enter_from_left, R.anim.exit_to_right);
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(getContext(), Uri.parse(makerWikiPageUrl));

        } else if(v.getId() == floating_button.getId()) {
            makerViewModel.refresh();

        } else if(v.getId() == arts_in_list.getId()) {

            spanCount = 1;
            initRecyclerView(makerViewModel, displayWidth, displayHeight, spanCount);
            subscribeObserverArtsData();

            arts_in_columns.setImageResource(R.drawable.ic_apps_black_100dp);
            arts_in_list.setImageResource(R.drawable.ic_baseline_format_list_bulleted_24_blue);
            AnimatorSet set = new AnimatorSet();
            set.playSequentially(scaleUp(arts_in_list), scaleDown(arts_in_list));
            set.start();

        } else if(v.getId() == arts_in_columns.getId()) {

            spanCount = 3;
            initRecyclerView(makerViewModel, displayWidth, displayHeight, spanCount);
            subscribeObserverArtsData();

            arts_in_columns.setImageResource(R.drawable.ic_apps_blue_100dp);
            arts_in_list.setImageResource(R.drawable.ic_baseline_format_list_bulleted_24);
            AnimatorSet set = new AnimatorSet();
            set.playSequentially(scaleUp(arts_in_columns), scaleDown(arts_in_columns));
            set.start();

        } else if(v.getId() == tags_clear.getId()) {
            if (keyword != null) {
                if(keyword.length() > 0) {

                    makerViewModel.makerTagClick(new FilterObject("", false, ""));
                    keyword = "";
                    tags_clear.setVisibility(View.GONE);
                    for (int i = 0; i < listKeywordFilters.size(); i++) {
                        if (listKeywordFilters.get(i).isChosen()) listKeywordFilters.get(i).setChosen(false);
                    }
                    keywordAdapter.clearItems();
                    keywordAdapter.setItems(listKeywordFilters);

                }
            }
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (view.getId() == wikipedia.getId()) {
            switch(motionEvent.getAction()){
                case MotionEvent.ACTION_DOWN:
                    ((TextView)view).setTextColor(getContext().getResources().getColor(R.color.colorBlack));
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    ((TextView)view).setTextColor(getContext().getResources().getColor(R.color.colorText));
                    break;
            }
        }
        return false;
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

                if( keyCode == KeyEvent.KEYCODE_BACK ) { // && activity.getNavFragments().getArtShowFragment() == null
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

    private void subscribeObserverMakerData() {

        makerViewModel.getIsLoading().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) { showProgressBar(); } else { hideProgressBar(); }
        });
        makerViewModel.getIsListEmpty().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) { showText(); } else { hideText(); }
        });
        makerViewModel.getMaker().observe(getViewLifecycleOwner(), this::setMakerDataInViews);
        makerViewModel.getArtCountMaker().observe(getViewLifecycleOwner(), artCountMaker -> {
            String artCount = getContext().getResources().getString(R.string.artworks_count) +" "+ artCountMaker;
            art_count.setText(artCount);
        });
        makerViewModel.getIsMakerLiked().observe(getViewLifecycleOwner(), isLiked -> {

            if(isLiked) maker_like.setImageResource(R.drawable.ic_favorite_red_100dp);
            else maker_like.setImageResource(R.drawable.ic_favorite_border_black_100dp);
            maker_like.setScaleType(ImageView.ScaleType.FIT_CENTER);
            AnimatorSet set = new AnimatorSet();
            set.playSequentially(likeFadeIn(maker_like), likeScaleDown(maker_like));
            set.start();
        });
        makerViewModel.getMakerKeywords().observe(getViewLifecycleOwner(), this::setListKeywordsInViews);

    }

    private void setListKeywordsInViews(ArrayList<FilterObject> listKeywords) {
        if (listKeywords == null) {
            keywordAdapter.clearItems();
            art_tags.setVisibility(View.GONE);
        } else {
            int spanCount = 3;
            if(listKeywords.size() < 5) spanCount = 1;
            else if(listKeywords.size() < 15) spanCount = 2;
            RecyclerView.LayoutManager layoutManager = new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.HORIZONTAL);
            recycler_view_keywords.setLayoutManager(layoutManager);

            int position = -1;
            listKeywordFilters.clear();
            for (FilterObject filter : listKeywords) {
                if (keyword != null && keyword.toUpperCase().equals(filter.getText().toUpperCase())) {
                    position = listKeywords.indexOf(filter);
                    listKeywordFilters.add(new FilterObject(filter.getText(), true, filter.getType()));
                } else
                    listKeywordFilters.add(new FilterObject(filter.getText(), false, filter.getType()));
            }
            keywordAdapter.clearItems();
            keywordAdapter.setItems(listKeywordFilters);

            if(position >= 0) {
                recycler_view_keywords.scrollToPosition(position);
                tags_clear.setVisibility(View.VISIBLE);
            }
            if(listKeywords.size() == 0) {
                art_tags.setVisibility(View.GONE);
            }else {
                art_tags.setVisibility(View.VISIBLE);
            }
        }
    }

    private void subscribeObserverArtsData(){
        makerViewModel.getArtList().observe(getViewLifecycleOwner(), arts -> {
            makerAdapter.submitList(arts);
            hideText();
        });
    }

    private void setMakerDataInViews(Maker maker) {
        if (maker.getMakerWikiDescription() != null) {
            maker_description.setVisibility(View.VISIBLE);
            wikipedia.setVisibility(View.VISIBLE);

            maker_description.setText(maker.getMakerWikiDescription());
            Paint paint = new Paint();
            paint.setTextSize(getContext().getResources().getDimension(R.dimen.text_size_maker_description));
            float widthText = paint.measureText(maker_description.getText().toString());
            float numLines = (float) ((widthText/displayWidth) * 1.2);
            if (numLines > 3) { read_more.setVisibility(View.VISIBLE); } else { read_more.setVisibility(View.GONE); }
        } else {
            maker_description.setVisibility(View.GONE);
            wikipedia.setVisibility(View.GONE);
            read_more.setVisibility(View.GONE);
        }

        makerWikiImageUrl = "";
        if(maker.getMakerWikiImageUrl() != null && maker.getMakerWikiImageUrl().length() > 0) {
            makerWikiImageUrl = maker.getMakerWikiImageUrl();
            Picasso.get().load(maker.getMakerWikiImageUrl()).placeholder(R.color.colorSilver).into(maker_image);
        }

        if(maker.isLiked()) maker_like.setImageResource(R.drawable.ic_favorite_red_100dp);
        else maker_like.setImageResource(R.drawable.ic_favorite_border_black_100dp);
        maker_like.setScaleType(ImageView.ScaleType.FIT_CENTER);

        if(keyword == null || keyword.length() == 0) {
            String artCount = getContext().getResources().getString(R.string.artworks_count) +" "+ maker.getArtCount();
            art_count.setText(artCount);
        }

        makerWikiPageUrl = maker.getMakerWikiPageUrl();
    }

    private void initRecyclerView(final MakerViewModel makerViewModel, int displayWidth, int displayHeight, int spanCount){

        MakerAdapter.OnArtClickListener onArtClickListener = new MakerAdapter.OnArtClickListener() {

            @Override
            public void onArtImageClick(Art art, int position) {
                ArrayList<Art> artInMemory = MakerDataInMemory.getInstance().getAllData();

                Bundle args = new Bundle();
                args.putSerializable(ArtShowFragment.ARTS, (Serializable) artInMemory);
                args.putInt(ArtShowFragment.POSITION, position);
                NavHostFragment.findNavController(MakerFragment.this)
                        .navigate(R.id.action_makerFragment_to_artShowFragment, args);
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
            public void onArtDownloadClick(final Art art, final int x, final int y) {

                downloadArt = art; downloadX = x; downloadY = y;

                imageDownloader = ImageDownloader.getInstance(MakerFragment.this);
                ArrayList<String> arrPerm = imageDownloader.checkPermission(getContext());
                if(arrPerm.isEmpty()) { startDownloading(); }
                else { requestPermissions(arrPerm.toArray(new String[0]), PERMISSION_REQUEST_CODE); }
            }

            @Override
            public void onSaveToFolderClick(Art art) {
                activity.showSaveToFolderView(art);
            }

        };

        makerAdapter = new MakerAdapter(makerViewModel, getContext(), onArtClickListener, displayWidth, displayHeight, spanCount);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), spanCount);
        makerRecyclerView.setLayoutManager(layoutManager);
        makerRecyclerView.setAdapter(makerAdapter);
    }

    private void startDownloading() {
        String folderName = res.getString(R.string.folder_my_arts_pictures);
        boolean isExists = imageDownloader.isFileExists(downloadArt, folderName);
        if (isExists)
            Toast.makeText(getContext(), R.string.file_already_downloaded, Toast.LENGTH_SHORT).show();
        else {
            AnimatorSet set = startDownloadAnimation(downloadX, downloadY);
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    imageDownloader.downloadImage(downloadArt, folderName);
                }
            });
            set.start();
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

    private void showProgressBar(){
        makerProgressBar.setVisibility(View.VISIBLE);
        //coordinator.setVisibility(View.GONE);
    }

    private void hideProgressBar(){
        makerProgressBar.setVisibility(View.GONE);
        coordinator.setVisibility(View.VISIBLE);

    }

    private void showText(){
        textView.setVisibility(View.VISIBLE);
    }

    private void hideText(){
        textView.setVisibility(View.GONE);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // you now have permission
                startDownloading();
            } else {
                // permission denied, boo! Disable the functionality that depends on this permission.
                Toast.makeText(getContext(), R.string.application_does_not_have_permission_to_download_the_file, Toast.LENGTH_LONG).show();
            }
        }
    }

}
package com.company.art_and_culture.myarts.museum_fragment;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.pojo.ArtProvider;
import com.company.art_and_culture.myarts.pojo.Maker;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;

import static com.company.art_and_culture.myarts.CommonAnimations.likeFadeIn;
import static com.company.art_and_culture.myarts.CommonAnimations.likeScaleDown;
import static com.company.art_and_culture.myarts.CommonAnimations.shareScaleDown;
import static com.company.art_and_culture.myarts.CommonAnimations.shareScaleUp;

public class MuseumFragment extends Fragment implements View.OnClickListener, View.OnTouchListener {

    private MuseumViewModel museumViewModel;
    private RecyclerView recycler_view_art, recycler_view_artists;
    private ArtMuseumAdapter artMuseumAdapter;
    private ProgressBar progress_bar_museum;
    private MuseumEventListener museumEventListener;
    private android.content.res.Resources res;
    private MainActivity activity;
    private int spanCount = 2;
    private String artProviderId;
    private TextView museum_name, museum_city, museum_address, museum_phone_number, museum_web_address,
            museum_description, read_more, wikipedia, button_show_tickets, label;
    private ImageButton museum_close_btn, museum_like, museum_share;
    private ImageView background;
    private CoordinatorLayout coordinator;
    private AppBarLayout museum_app_bar;
    private FloatingActionButton floating_button;
    private int displayWidth, displayHeight;
    private ArtistsAdapter artistsAdapter;
    private ArtProvider museum;
    private boolean isLabelVisible = false;
    public enum State{collapsed, expanded}
    private State appBarState;
    private SharedPreferences preferences;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_museum, container, false);
        bindViews(root);

        museumViewModel = new ViewModelProvider(this).get(MuseumViewModel.class);

        res = getResources();
        displayWidth = res.getDisplayMetrics().widthPixels;
        displayHeight = res.getDisplayMetrics().heightPixels;

        activity = (MainActivity) getActivity();
        if (activity != null && artProviderId == null) artProviderId = activity.getNavFragments().getArtProviderIdForMuseumFragment();
        if (activity != null) museumEventListener = activity.getNavFragments();
        if (activity != null) preferences = activity.getSharedPreferences(Constants.TAG, 0);

        if(artProviderId == null) {
            activity.getNavFragments().popBackStack();
        } else {
            museumViewModel.setArtProviderId(artProviderId);
            museumViewModel.setActivity(activity);
            subscribeObservers();

            initRecyclerView(museumViewModel, displayWidth, displayHeight);
            initArtistsRecyclerView(displayWidth, displayHeight);
            setOnBackPressedListener(root);
            initAppBar();
        }
        coordinator.setVisibility(View.GONE);

        return root;
    }

    private void initAppBar() {
        museum_app_bar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

                if (Math.abs(verticalOffset)-appBarLayout.getTotalScrollRange() >= 0) {
                    //  Collapsed
                    if(!isLabelVisible) {
                        label.animate().alpha(1.0f).translationY(0);
                        museum_close_btn.animate().alpha(1.0f).translationY(0);
                        museum_close_btn.setEnabled(true);
                        isLabelVisible = !isLabelVisible;
                        appBarState = State.collapsed;
                    }
                } else {
                    //Expanded
                    if(isLabelVisible) {
                        label.animate().alpha(0.0f).translationY(-label.getHeight());
                        museum_close_btn.animate().alpha(0.0f).translationY(-label.getHeight());
                        museum_close_btn.setEnabled(false);
                        isLabelVisible = !isLabelVisible;
                        appBarState = State.expanded;
                    }
                }
            }
        });
    }

    private void bindViews(View root) {
        coordinator = root.findViewById(R.id.coordinator);
        museum_app_bar = root.findViewById(R.id.museum_app_bar);
        recycler_view_art = root.findViewById(R.id.recycler_view_art);
        recycler_view_artists = root.findViewById(R.id.recycler_view_artists);
        progress_bar_museum = root.findViewById(R.id.progress_bar_museum);
        //museum_swipeRefreshLayout = root.findViewById(R.id.museum_swipeRefreshLayout);
        background = root.findViewById(R.id.background);
        museum_name = root.findViewById(R.id.museum_name);
        museum_city = root.findViewById(R.id.museum_city);
        label = root.findViewById(R.id.label);
        if(appBarState == null || appBarState == State.expanded) label.setAlpha(0.0f); else label.setAlpha(1.0f);
        museum_close_btn = root.findViewById(R.id.museum_close_btn);
        museum_close_btn.setOnClickListener(this);
        if(appBarState == null || appBarState == State.expanded) museum_close_btn.setAlpha(0.0f); else museum_close_btn.setAlpha(1.0f);
        museum_address = root.findViewById(R.id.museum_address);
        museum_address.setOnClickListener(this);
        museum_phone_number = root.findViewById(R.id.museum_phone_number);
        museum_phone_number.setOnClickListener(this);
        museum_web_address = root.findViewById(R.id.museum_web_address);
        museum_web_address.setOnClickListener(this);
        museum_description = root.findViewById(R.id.museum_description);
        floating_button = root.findViewById(R.id.floating_button);
        floating_button.setOnClickListener(this);

        read_more = root.findViewById(R.id.read_more);
        read_more.setOnClickListener(this);
        wikipedia = root.findViewById(R.id.wikipedia);
        wikipedia.setOnClickListener(this);
        wikipedia.setOnTouchListener(this);
        button_show_tickets = root.findViewById(R.id.button_show_tickets);
        button_show_tickets.setOnClickListener(this);

        museum_like = root.findViewById(R.id.museum_like);
        museum_like.setOnClickListener(this);
        museum_share = root.findViewById(R.id.museum_share);
        museum_share.setOnClickListener(this);
    }

    @Override
    public void onDestroy() {
        artProviderId = "";
        museumViewModel.setArtProviderId(artProviderId);
        super.onDestroy();
    }

    private void setOnBackPressedListener(final View root) {
        //You need to add the following line for this solution to work; thanks skayred
        root.setFocusableInTouchMode(true);
        root.requestFocus();
        root.setOnKeyListener( new View.OnKeyListener() {
            @Override
            public boolean onKey( View v, int keyCode, KeyEvent event ) {

                if( keyCode == KeyEvent.KEYCODE_BACK ) {
                    int scrollPosition = 0;
                    if (artMuseumAdapter.getItemCount() > 0) scrollPosition = getTargetScrollPosition();
                    if (scrollPosition > 4) {
                        recycler_view_art.smoothScrollToPosition(0);
                        return true;
                    }
                    return false;
                }
                return false;
            }
        } );
    }

    private void subscribeObservers() {

        museumViewModel.getArtList().observe(getViewLifecycleOwner(), arts -> {
            setAnimationRecyclerViewArts();
            artMuseumAdapter.submitList(arts);
            hideText();
        });
        museumViewModel.getIsLoading().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) { showProgressBar(); } else { hideProgressBar(); }
        });
        museumViewModel.getIsListEmpty().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) { showText(); } else { hideText(); }
        });
        museumViewModel.getArtProvider().observe(getViewLifecycleOwner(), artProvider -> {
            setMuseumDataInViews(artProvider);
            museum = artProvider;
        });
        museumViewModel.getArtProviderLike().observe(getViewLifecycleOwner(), aBoolean -> {
            if(aBoolean) museum_like.setImageResource(R.drawable.ic_favorite_red_100dp);
            else museum_like.setImageResource(R.drawable.ic_favorite_border_black_100dp);
            museum_like.setScaleType(ImageView.ScaleType.FIT_CENTER);
            AnimatorSet set = new AnimatorSet();
            set.playSequentially(likeFadeIn(museum_like), likeScaleDown(museum_like));
            set.start();
        });
        museumViewModel.getListMakers().observe(getViewLifecycleOwner(), makers -> {
            if (makers == null) {
                artistsAdapter.clearItems();
            } else {
                setAnimationRecyclerViewArtists();
                artistsAdapter.clearItems();
                artistsAdapter.setItems(makers);
            }
        });
    }

    private void setAnimationRecyclerViewArtists() {
        LayoutAnimationController layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_fade_in);
        recycler_view_artists.setLayoutAnimation(layoutAnimationController);
        recycler_view_artists.scheduleLayoutAnimation();
    }

    private void setAnimationRecyclerViewArts() {
        LayoutAnimationController layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_fade_in);
        recycler_view_art.setLayoutAnimation(layoutAnimationController);
        recycler_view_art.scheduleLayoutAnimation();
    }

    private void setMuseumDataInViews(ArtProvider artProvider) {
        Picasso.get().load(artProvider.getProviderHeaderUrl()).into(background);
        museum_name.setText(artProvider.getArtProvider());
        label.setText(artProvider.getArtProvider());
        museum_city.setText(artProvider.getArtProviderCity() + ", " + artProvider.getArtProviderCountry());
        museum_address.setText(artProvider.getProviderAddress());
        museum_phone_number.setText(artProvider.getProviderPhone());
        museum_web_address.setText(artProvider.getProviderSiteUrl());
        museum_description.setText(artProvider.getProviderDescription());

        if(artProvider.isLiked()) museum_like.setImageResource(R.drawable.ic_favorite_red_100dp);
        else museum_like.setImageResource(R.drawable.ic_favorite_border_black_100dp);
        museum_like.setScaleType(ImageView.ScaleType.FIT_CENTER);
    }

    private void initRecyclerView(final MuseumViewModel museumViewModel, int displayWidth, int displayHeight){

        ArtMuseumAdapter.OnArtClickListener onArtClickListener = (art, position) -> {
            ArrayList<Art> artInMemory = ArtDataInMemory.getInstance().getAllData();
            museumEventListener.onArtClickEvent(artInMemory, position);
        };

        artMuseumAdapter = new ArtMuseumAdapter(museumViewModel, getContext(), onArtClickListener, displayWidth, displayHeight, spanCount);
        RecyclerView.LayoutManager layoutManager = new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL);
        recycler_view_art.setLayoutManager(layoutManager);
        recycler_view_art.setAdapter(artMuseumAdapter);
    }

    private void initArtistsRecyclerView(int displayWidth, int displayHeight) {

        ArtistsAdapter.OnMakerClickListener onMakerClickListener = (maker, position) -> museumEventListener.onArtistsClickEvent(maker);
        artistsAdapter = new ArtistsAdapter(getContext(), onMakerClickListener, displayWidth, displayHeight);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false);
        recycler_view_artists.setLayoutManager(layoutManager);
        recycler_view_artists.setAdapter(artistsAdapter);
    }

    private void showProgressBar(){
        progress_bar_museum.setVisibility(View.VISIBLE);
        //coordinator.setVisibility(View.GONE);
        floating_button.setImageDrawable(res.getDrawable(R.drawable.ic_outline_change_circle_gray));
    }

    private void hideProgressBar(){
        progress_bar_museum.setVisibility(View.GONE);
        coordinator.setVisibility(View.VISIBLE);
        floating_button.setImageDrawable(res.getDrawable(R.drawable.ic_outline_change_circle_blue));
    }

    private void showText(){
        //textView.setVisibility(View.VISIBLE);
    }

    private void hideText(){
        //textView.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == museum_web_address.getId()) {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            builder.setStartAnimations(getContext(), R.anim.enter_from_right, R.anim.exit_to_left);
            builder.setExitAnimations(getContext(), R.anim.enter_from_left, R.anim.exit_to_right);
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(getContext(), Uri.parse(museum.getProviderSiteUrl()));

        } else if(view.getId() == museum_address.getId()) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?q="+museum_address.getText().toString()));
            startActivity(intent);

        } else if(view.getId() == museum_phone_number.getId()) {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + museum_phone_number.getText().toString()));
            startActivity(intent);

        } else if(view.getId() == read_more.getId()) {
            if (museum_description.getMaxLines() == 4) {
                Paint paint = new Paint();
                paint.setTextSize(getContext().getResources().getDimension(R.dimen.text_size_maker_description));
                float widthText = paint.measureText(museum_description.getText().toString());
                float numLines = (float) ((widthText/displayWidth) * 1.2);
                read_more.setText(getContext().getResources().getString(R.string.show_less));

                ObjectAnimator animation = ObjectAnimator.ofInt(museum_description, "maxLines", (int) (numLines + 10));
                animation.setDuration(600).start();
            } else {
                ObjectAnimator animation = ObjectAnimator.ofInt(museum_description, "maxLines", 4);
                animation.setDuration(600).start();
                read_more.setText(getContext().getResources().getString(R.string.read_more));
            }

        } else if(view.getId() == wikipedia.getId()) {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            builder.setStartAnimations(getContext(), R.anim.enter_from_right, R.anim.exit_to_left);
            builder.setExitAnimations(getContext(), R.anim.enter_from_left, R.anim.exit_to_right);
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(getContext(), Uri.parse(museum.getProviderWikiUrl()));

        } else if(view.getId() == button_show_tickets.getId()) {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            builder.setStartAnimations(getContext(), R.anim.enter_from_right, R.anim.exit_to_left);
            builder.setExitAnimations(getContext(), R.anim.enter_from_left, R.anim.exit_to_right);
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(getContext(), Uri.parse(museum.getProviderTicketsUrl()));

        } else if(view.getId() == floating_button.getId()) {
            museumViewModel.refresh();

        } else if(view.getId() == museum_close_btn.getId()) {
            activity.getNavFragments().popBackStack();

        } else if(view.getId() == museum_share.getId()) {
            AnimatorSet set = new AnimatorSet();
            set.playSequentially(shareScaleUp(museum_share), shareScaleDown(museum_share));
            set.start();
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            String text = museum.getArtProvider() + System.getProperty ("line.separator") + museum.getProviderSiteUrl();
            sendIntent.putExtra(Intent.EXTRA_TEXT, text);
            sendIntent.setType("text/plain");
            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);

        } else if(view.getId() == museum_like.getId()) {
            boolean networkState = museumViewModel.likeMuseum (museum, preferences.getString(Constants.USER_UNIQUE_ID,""));
            if (!networkState) {
                Toast.makeText(getContext(), R.string.network_is_unavailable, Toast.LENGTH_SHORT).show();
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

    public interface MuseumEventListener {
        void onArtClickEvent(Collection<Art> arts, int position);
        void onArtistsClickEvent(Maker maker);
    }

    private int getTargetScrollPosition () {

        int scrollPosition = ((StaggeredGridLayoutManager) recycler_view_art.getLayoutManager()).findFirstVisibleItemPositions(null)[0];

        return scrollPosition;
    }

}

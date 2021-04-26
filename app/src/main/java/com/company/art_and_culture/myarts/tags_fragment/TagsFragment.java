package com.company.art_and_culture.myarts.tags_fragment;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.pojo.Attribute;
import com.company.art_and_culture.myarts.pojo.FilterObject;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class TagsFragment extends Fragment implements View.OnClickListener {

    private RecyclerView recycler_view_tags, recycler_view_filter;
    private TagsViewModel tagsViewModel;
    private FrameLayout title_layout, black_layout;
    private TextView title_tv;
    private ProgressBar download_progress;
    private TagsAdapter tagsAdapter;
    private int spanCount = 3;
    private android.content.res.Resources res;
    private TagsEventListener tagsEventListener;

    private int filterSpanCount = 5;
    private int filterPosition = 0;
    private FilterAdapter filterAdapter;
    private ArrayList<FilterObject> filterList = new ArrayList<>();
    private View background_view;
    private FloatingActionButton floatingActionButton;
    private MainActivity activity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_tags, container, false);

        title_layout = root.findViewById(R.id.title_layout);
        title_tv = root.findViewById(R.id.title);
        black_layout = root.findViewById(R.id.black_layout);
        recycler_view_tags = root.findViewById(R.id.recycler_view_tags);
        download_progress = root.findViewById(R.id.download_progress);

        recycler_view_filter = root.findViewById(R.id.recycler_view_filter);
        floatingActionButton = root.findViewById(R.id.floating_button);
        background_view = root.findViewById(R.id.background_view);

        res = getResources();
        int displayWidth = res.getDisplayMetrics().widthPixels;
        int displayHeight = res.getDisplayMetrics().heightPixels;

        tagsViewModel = new ViewModelProvider(this).get(TagsViewModel.class);

        activity = (MainActivity) getActivity();
        tagsEventListener = activity.getNavFragments();

        initFilterRecyclerView(displayWidth, displayHeight);
        filterPosition = activity.getNavFragments().getFilterPositionForTagsFragment();
        filterList = getFilterList(filterPosition);
        filterAdapter.setItems(filterList);
        setFilter (filterPosition);

        recycler_view_filter.setVisibility(View.GONE);
        background_view.setVisibility(View.GONE);
        black_layout.setVisibility(View.GONE);

        floatingActionButton.setOnClickListener(this);
        black_layout.setOnClickListener(this);

        initTagRecyclerView(displayWidth, displayHeight, spanCount);

        subscribeObservers();

        setOnBackPressedListener(root);

        return root;
    }

    private void setOnBackPressedListener(View root) {
        //You need to add the following line for this solution to work; thanks skayred
        root.setFocusableInTouchMode(true);
        root.requestFocus();
        root.setOnKeyListener( new View.OnKeyListener() {
            @Override
            public boolean onKey( View v, int keyCode, KeyEvent event ) {

                if( keyCode == KeyEvent.KEYCODE_BACK ) {
                    if(recycler_view_filter.isShown()) {
                        goneFilterViews();
                        return true;
                    } else {
                        int scrollPosition = 0;
                        if (tagsAdapter.getItemCount() > 0) scrollPosition = getTargetScrollPosition();
                        if (scrollPosition > 4 * spanCount) {
                            recycler_view_tags.smoothScrollToPosition(0);
                            return true;
                        }
                    }
                    return false;
                }
                return false;
            }
        } );
    }

    private int getTargetScrollPosition () {

        return ((GridLayoutManager) recycler_view_tags.getLayoutManager()).findFirstVisibleItemPosition();
    }

    private ArrayList<FilterObject> getFilterList(int filterPosition) {

        ArrayList<FilterObject> filterList = new ArrayList<>();
        FilterObject filterObject;
        if(filterPosition == 0) {
            filterObject = new FilterObject(res.getString(R.string.all), true);
        } else {
            filterObject = new FilterObject(res.getString(R.string.all), false);
        }
        filterList.add(filterObject);
        String filter = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for(int i = 0; i < filter.length(); i++) {
            if(filterPosition-1 == i) {
                filterObject = new FilterObject(String.valueOf(filter.charAt(i)), true);
            } else {
                filterObject = new FilterObject(String.valueOf(filter.charAt(i)), false);
            }
            filterList.add(filterObject);
        }
        return filterList;
    }

    private void subscribeObservers() {

        tagsViewModel.getTagsList().observe(getViewLifecycleOwner(), new Observer<PagedList<Attribute>>() {
            @Override
            public void onChanged(PagedList<Attribute> attributes) {
                tagsAdapter.submitList(attributes);
            }
        });
        tagsViewModel.getIsLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) { download_progress.setVisibility(View.VISIBLE); } else { download_progress.setVisibility(View.GONE); }
            }
        });
    }

    private void initTagRecyclerView(int displayWidth, int displayHeight, int spanCount) {

        TagsAdapter.OnTagsClickListener onTagsClickListener = new TagsAdapter.OnTagsClickListener() {
            @Override
            public void onTagsClick(Attribute attribute, int position) {
                tagsEventListener.tagClickEvent(attribute);
            }
        };
        tagsAdapter = new TagsAdapter(getContext(), onTagsClickListener, displayWidth, displayHeight, spanCount);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), spanCount);
        recycler_view_tags.setLayoutManager(layoutManager);
        recycler_view_tags.setAdapter(tagsAdapter);
    }

    private void initFilterRecyclerView(int displayWidth, int displayHeight) {

        FilterAdapter.OnFilterClickListener onFilterClickListener = new FilterAdapter.OnFilterClickListener() {
            @Override
            public void onFilterClick(String item, int position) {
                filterList = getFilterList(position);
                filterAdapter.clearItems();
                filterAdapter.setItems(filterList);
                filterPosition = position;
                setFilter (position);
            }
        };
        filterAdapter = new FilterAdapter(getContext(), onFilterClickListener, displayWidth, displayHeight, filterSpanCount);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), filterSpanCount);
        recycler_view_filter.setLayoutManager(layoutManager);
        recycler_view_filter.setAdapter(filterAdapter);
    }

    private void setFilter(int position) {
        tagsViewModel.setFilter(filterList.get(position).getText());
    }

    @Override
    public void onPause() {
        super.onPause();
        tagsEventListener.tagFilterPositionEvent(filterPosition);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == floatingActionButton.getId()) {
            if(recycler_view_filter.isShown()) {
                goneFilterViews();
            } else {
                AnimatorSet set = new AnimatorSet();
                set.setDuration(500).playTogether(
                        ObjectAnimator.ofFloat(recycler_view_filter, View.ALPHA, 0f, 1f),
                        ObjectAnimator.ofFloat(background_view, View.ALPHA, 0f, 1f),
                        ObjectAnimator.ofFloat(black_layout, View.ALPHA, 0f, 1f));
                set.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        recycler_view_filter.setVisibility(View.VISIBLE);
                        background_view.setVisibility(View.VISIBLE);
                        black_layout.setVisibility(View.VISIBLE);
                    }
                    @Override
                    public void onAnimationEnd(Animator animation) { }
                    @Override
                    public void onAnimationCancel(Animator animation) { }
                    @Override
                    public void onAnimationRepeat(Animator animation) { }
                });
                set.start();
            }
        } else if (view.getId() == black_layout.getId()) {
            goneFilterViews();
        }
    }

    private void goneFilterViews() {
        recycler_view_filter.setVisibility(View.GONE);
        background_view.setVisibility(View.GONE);
        black_layout.setVisibility(View.GONE);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.filter_layout_fade_out);
        recycler_view_filter.startAnimation(animation);
        background_view.startAnimation(animation);
    }

    public interface TagsEventListener {
        void tagClickEvent(Attribute attribute);
        void tagFilterPositionEvent(int position);
    }

}

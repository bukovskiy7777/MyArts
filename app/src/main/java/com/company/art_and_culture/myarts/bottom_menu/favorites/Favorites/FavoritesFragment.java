package com.company.art_and_culture.myarts.bottom_menu.favorites.Favorites;

import android.animation.AnimatorSet;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.pojo.Art;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import static com.company.art_and_culture.myarts.bottom_menu.favorites.Favorites.FavoritesAnimations.scaleDown;
import static com.company.art_and_culture.myarts.bottom_menu.favorites.Favorites.FavoritesAnimations.scaleUp;

public class FavoritesFragment extends Fragment implements View.OnClickListener {

    private FavoritesViewModel favoritesViewModel;
    private ConstraintLayout blank_favorites;
    private FrameLayout sort_layout;
    private RecyclerView favoritesRecyclerView;
    private ProgressBar favoritesProgressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private android.content.res.Resources res;
    private FavoritesAdapter favoritesAdapter;
    private FavoritesEventListener favoritesEventListener;
    private int spanCount = 3;
    private int displayWidth, displayHeight;
    private ArrayList<Art> globalListArts = new ArrayList<>();
    private ImageButton sort_by_century, sort_by_maker, sort_by_date;
    private TextView favorites_sort_by;
    private MainActivity activity;
    public enum Sort {by_date, by_maker, by_century}
    private int sortDirection = 0;
    private String filter = "";

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_favorites_items, container, false);
        blank_favorites = root.findViewById(R.id.blank_favorites);
        favoritesRecyclerView = root.findViewById(R.id.recycler_view_favorites);
        favoritesProgressBar = root.findViewById(R.id.progress_bar_favorites);
        swipeRefreshLayout = root.findViewById(R.id.favorites_swipeRefreshLayout);
        sort_layout = root.findViewById(R.id.sort_layout);
        favorites_sort_by = root.findViewById(R.id.favorites_sort_by);
        sort_by_century = root.findViewById(R.id.sort_by_century);
        sort_by_maker = root.findViewById(R.id.sort_by_maker);
        sort_by_date = root.findViewById(R.id.sort_by_date);

        sort_layout.setVisibility(View.GONE);

        sort_by_century.setOnClickListener(this);
        sort_by_maker.setOnClickListener(this);
        sort_by_date.setOnClickListener(this);

        favoritesViewModel =new ViewModelProvider(this).get(FavoritesViewModel.class);

        res = getResources();
        displayWidth = res.getDisplayMetrics().widthPixels;
        displayHeight = res.getDisplayMetrics().heightPixels;

        activity = (MainActivity) getActivity();
        favoritesViewModel.setActivity(activity);

        Sort sort_type = Sort.by_date;
        if (activity != null) sort_type = activity.getNavFragments().getSort_type();
        if (!sort_type.equals(Sort.by_date)) spanCount = 1;
        initRecyclerView(displayWidth, displayHeight, sort_type, spanCount);

        int scrollPosition = 0;
        if (activity != null) scrollPosition = activity.getNavFragments().getFavoritesPosition();
        if (scrollPosition >= 0) favoritesRecyclerView.scrollToPosition(scrollPosition);

        if (activity != null) favoritesEventListener = activity.getNavFragments();

        initSwipeRefreshLayout();
        subscribeObservers();
        hideText();

        if (sort_type.equals(Sort.by_date)) {
            sort_by_date.setImageResource(R.drawable.ic_apps_blue_100dp);
        } else if (sort_type.equals(Sort.by_century)) {
            sort_by_century.setImageResource(R.drawable.ic_access_time_blue_100dp);
        } else if (sort_type.equals(Sort.by_maker)) {
            sort_by_maker.setImageResource(R.drawable.ic_baseline_sort_by_alpha_blue);
        }

        return root;
    }

    public boolean refresh () {
        return favoritesViewModel.refresh();
    }

    private void initRecyclerView(int displayWidth, int displayHeight, Sort sort_type, int spanCount) {

        FavoritesAdapter.OnArtClickListener onArtClickListener = new FavoritesAdapter.OnArtClickListener() {
            @Override
            public void onArtImageClick(Art art, int position) {
                favoritesEventListener.favoritesClickEvent(favoritesAdapter.getItems(), position);
            }
        };
        favoritesAdapter = new FavoritesAdapter(getContext(), onArtClickListener, displayWidth, displayHeight, spanCount, sort_type);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), spanCount);
        favoritesRecyclerView.setLayoutManager(layoutManager);
        favoritesRecyclerView.setAdapter(favoritesAdapter);
    }

    private void subscribeObservers() {

        favoritesViewModel.getArtList().observe(getViewLifecycleOwner(), new Observer<ArrayList<Art>>() {
            @Override
            public void onChanged(ArrayList<Art> arts) {

                if(arts != null) globalListArts = arts; else globalListArts.clear();
                activity.postFavoritesArtsCount(globalListArts.size());
                setListArts(globalListArts);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        favoritesViewModel.getIsLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) { showProgressBar(); } else { hideProgressBar(); }
            }
        });
        favoritesViewModel.getIsListEmpty().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) { showText(); } else { hideText(); }
            }
        });
        if (activity != null) activity.getFavoritesFilter().observe(getViewLifecycleOwner(), s -> {
            filter = s;
            setListArts(globalListArts);
        });

    }

    private void setListArts(ArrayList<Art> arts) {

        ArrayList<Art> partialListArts = new ArrayList<>();
        if(filter.length() > 0) {
            for (Art art : arts) {
                if (art.getArtTitle().toUpperCase().contains(filter.toUpperCase())
                        || art.getArtMaker().toUpperCase().contains(filter.toUpperCase()))
                    partialListArts.add(art);
            }
        } else {
            partialListArts = arts;
        }

        if (partialListArts.size() == 0) {
            favoritesAdapter.clearItems();
            sort_layout.setVisibility(View.GONE);
        } else {
            if (favoritesAdapter.getSort_type().equals(Sort.by_century))
                Collections.sort(partialListArts, (one, other) -> one.getCentury().compareTo(other.getCentury()));
            else if (favoritesAdapter.getSort_type().equals(Sort.by_maker))
                Collections.sort(partialListArts, (one, other) -> one.getArtMaker().compareTo(other.getArtMaker()));

            setAnimationRecyclerView (partialListArts);
            favoritesAdapter.clearItems();
            favoritesAdapter.setItems(partialListArts);
            sort_layout.setVisibility(View.VISIBLE);
        }
    }

    private void setAnimationRecyclerView(ArrayList<Art> arts) {

        boolean animate = false;
        if (favoritesAdapter.getItemCount() > 0) {

            if (arts.size() != favoritesAdapter.getItemCount()) {
                animate = true;
            } else {

                for (int i = 0; i < arts.size(); i++) {
                    if (!arts.get(i).getArtId().equals(favoritesAdapter.getItems().get(i).getArtId())) {
                        animate = true;
                    }
                }
            }
            if (animate) {
                LayoutAnimationController layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_fall_down);
                favoritesRecyclerView.setLayoutAnimation(layoutAnimationController);
                //favoritesRecyclerView.getAdapter().notifyDataSetChanged();
                favoritesRecyclerView.scheduleLayoutAnimation();
            }
        } else {
            LayoutAnimationController layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_fade_in);
            favoritesRecyclerView.setLayoutAnimation(layoutAnimationController);
            //favoritesRecyclerView.getAdapter().notifyDataSetChanged();
            favoritesRecyclerView.scheduleLayoutAnimation();
        }
    }

    private void initSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                boolean networkState = refresh();
                if (!networkState) {
                    Toast.makeText(getContext(), R.string.network_is_unavailable, Toast.LENGTH_LONG).show();
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorBlue
        );

    }

    private void showProgressBar(){
        favoritesProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar(){
        favoritesProgressBar.setVisibility(View.GONE);
    }

    private void showText(){
        blank_favorites.setVisibility(View.VISIBLE);
    }

    private void hideText(){
        blank_favorites.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {

        ArrayList<Art> newList = new ArrayList<>();
        if(filter.length() > 0) {
            for (Art art : globalListArts) {
                if (art.getArtTitle().toUpperCase().contains(filter.toUpperCase())
                        || art.getArtMaker().toUpperCase().contains(filter.toUpperCase()))
                    newList.add(art);
            }
        } else {
            newList = new ArrayList<>(globalListArts);
        }

        if(v.getId() == sort_by_century.getId()) {

            if(sortDirection == 0) {
                sortDirection = 1;
                Collections.sort(newList, new Comparator<Art>() {
                    @Override
                    public int compare(Art one, Art other) {
                        return one.getCentury().compareToIgnoreCase(other.getCentury());
                    }
                });
            } else {
                sortDirection = 0;
                Collections.sort(newList, new Comparator<Art>() {
                    @Override
                    public int compare(Art one, Art other) {
                        return other.getCentury().compareToIgnoreCase(one.getCentury());
                    }
                });
            }
            spanCount = 1;
            initRecyclerView(displayWidth, displayHeight, Sort.by_century, spanCount);

            sort_by_date.setImageResource(R.drawable.ic_apps_black_100dp);
            sort_by_century.setImageResource(R.drawable.ic_access_time_blue_100dp);
            sort_by_maker.setImageResource(R.drawable.ic_baseline_sort_by_alpha_black);
            setAnimationRecyclerView (newList);
            favoritesAdapter.clearItems();
            favoritesAdapter.setItems(newList);

            AnimatorSet set = new AnimatorSet();
            set.playSequentially(scaleUp(sort_by_century), scaleDown(sort_by_century));
            set.start();

        } else if(v.getId() == sort_by_maker.getId()) {

            if(sortDirection == 0) {
                sortDirection = 1;
                Collections.sort(newList, new Comparator<Art>() {
                    @Override
                    public int compare(Art one, Art other) {
                        return one.getArtMaker().compareToIgnoreCase(other.getArtMaker());
                    }
                });
            } else {
                sortDirection = 0;
                Collections.sort(newList, new Comparator<Art>() {
                    @Override
                    public int compare(Art one, Art other) {
                        return other.getArtMaker().compareToIgnoreCase(one.getArtMaker());
                    }
                });
            }
            spanCount = 1;
            initRecyclerView(displayWidth, displayHeight, Sort.by_maker, spanCount);

            sort_by_date.setImageResource(R.drawable.ic_apps_black_100dp);
            sort_by_century.setImageResource(R.drawable.ic_access_time_black_100dp);
            sort_by_maker.setImageResource(R.drawable.ic_baseline_sort_by_alpha_blue);
            setAnimationRecyclerView (newList);
            favoritesAdapter.clearItems();
            favoritesAdapter.setItems(newList);

            AnimatorSet set = new AnimatorSet();
            set.playSequentially(scaleUp(sort_by_maker), scaleDown(sort_by_maker));
            set.start();

        } else if(v.getId() == sort_by_date.getId()) {

            if(sortDirection == 0) {
                sortDirection = 1;
                Collections.reverse(newList);
            } else {
                sortDirection = 0;
            }
            spanCount = 3;
            initRecyclerView(displayWidth, displayHeight, Sort.by_date, spanCount);

            sort_by_date.setImageResource(R.drawable.ic_apps_blue_100dp);
            sort_by_century.setImageResource(R.drawable.ic_access_time_black_100dp);
            sort_by_maker.setImageResource(R.drawable.ic_baseline_sort_by_alpha_black);
            setAnimationRecyclerView (newList);
            favoritesAdapter.clearItems();
            favoritesAdapter.setItems(newList);

            AnimatorSet set = new AnimatorSet();
            set.playSequentially(scaleUp(sort_by_date), scaleDown(sort_by_date));
            set.start();
        }
    }

    public interface FavoritesEventListener {
        void favoritesScrollEvent(int position, Sort sort_type);
        void favoritesClickEvent(Collection<Art> art, int position);
    }

    public int getTargetScrollPosition () {

        if (favoritesRecyclerView.isShown() && favoritesRecyclerView.getAdapter().getItemCount() > 0) {

            final int firstPosition = ((LinearLayoutManager) favoritesRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
            final int lastPosition = ((LinearLayoutManager) favoritesRecyclerView.getLayoutManager()).findLastVisibleItemPosition();
            Rect rvRect = new Rect();
            favoritesRecyclerView.getGlobalVisibleRect(rvRect);
            int scrollPosition = firstPosition;
            int targetPercent = 0;

            for (int i = firstPosition; i <= lastPosition; i++) {

                Rect rowRect = new Rect();
                favoritesRecyclerView.getLayoutManager().findViewByPosition(i).getGlobalVisibleRect(rowRect);

                int percent;
                if (rowRect.bottom >= rvRect.bottom){
                    int visibleHeightFirst =rvRect.bottom - rowRect.top;
                    percent = (visibleHeightFirst * 100) / favoritesRecyclerView.getLayoutManager().findViewByPosition(i).getHeight();
                }else {
                    int visibleHeightFirst = rowRect.bottom - rvRect.top;
                    percent = (visibleHeightFirst * 100) / favoritesRecyclerView.getLayoutManager().findViewByPosition(i).getHeight();
                }

                if (percent>100) percent = 100;

                if (percent > targetPercent) {
                    targetPercent = percent;
                    scrollPosition = i;
                }
            }

            return scrollPosition;

        } else {
            return 0;
        }
    }

    public int getSpanCount() {
        return spanCount;
    }

    public void scrollRecyclerViewToStart () {
        favoritesRecyclerView.smoothScrollToPosition(0);
    }

    public void postScrollDataToMainActivity () {
        int scrollPosition = 0;
        if (favoritesAdapter.getItemCount() > 0) scrollPosition = getTargetScrollPosition();
        favoritesEventListener.favoritesScrollEvent(scrollPosition, favoritesAdapter.getSort_type());
    }

}
package com.company.art_and_culture.myarts.filter_maker_fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.pojo.Maker;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import de.hdodenhof.circleimageview.CircleImageView;

public class FilterMakerFragment extends Fragment {

    private RecyclerView recycler_view_filter, recycler_view_date, recycler_view_maker;
    private FilterMakerViewModel filterMakerViewModel;
    private CircleImageView circle_filter_view;
    private ImageView time_view;
    private View background_view;
    private FrameLayout title_layout;
    private FilterAdapter filterAdapter;
    private DateAdapter dateAdapter;
    private FilterMakerAdapter filterMakerAdapter;
    private int spanCount = 3;
    private int filterSpanCount = 5;
    private int filterPosition = 0, datePosition = 0;
    private android.content.res.Resources res;
    private ArrayList<String> filterList = new ArrayList<>();
    private ArrayList<String> dateList = new ArrayList<>();
    private FilterMakerEventListener filterMakerEventListener;
    private ProgressBar download_progress;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_filter_maker, container, false);

        recycler_view_filter = root.findViewById(R.id.recycler_view_filter);
        recycler_view_date = root.findViewById(R.id.recycler_view_date);
        circle_filter_view = root.findViewById(R.id.circle_filter_view);
        time_view = root.findViewById(R.id.time_view);
        background_view = root.findViewById(R.id.background_view);
        title_layout = root.findViewById(R.id.title_layout);
        recycler_view_maker = root.findViewById(R.id.recycler_view_maker);
        download_progress = root.findViewById(R.id.download_progress);

        res = getResources();
        int displayWidth = res.getDisplayMetrics().widthPixels;
        int displayHeight = res.getDisplayMetrics().heightPixels;

        filterMakerViewModel = new ViewModelProvider(this).get(FilterMakerViewModel.class);

        initFilterRecyclerView(displayWidth, displayHeight);
        initDateRecyclerView(displayWidth, displayHeight);
        initMakerRecyclerView(displayWidth, displayHeight);

        circle_filter_view.getLayoutParams().height = (int) (displayWidth / filterSpanCount * filterAdapter.getK() * 0.8);
        circle_filter_view.getLayoutParams().width = (int) (displayWidth / filterSpanCount * filterAdapter.getK() * 0.8);

        filterList = getFilterList();
        filterAdapter.setItems(filterList);

        dateList = getDateList();
        dateAdapter.setItems(dateList);

        MainActivity activity = (MainActivity) getActivity();
        filterPosition = activity.getNavFragments().getFilterMakerPosition();
        datePosition = activity.getNavFragments().getDateMakerPosition();
        filterMakerViewModel.setFilter(filterList.get(filterPosition), dateList.get(datePosition));
        recycler_view_filter.scrollToPosition(filterPosition);
        recycler_view_date.scrollToPosition(datePosition);

        filterMakerEventListener = activity.getNavFragments();

        subscribeObservers();

        return root;
    }

    private void subscribeObservers() {

        filterMakerViewModel.getMakerList().observe(getViewLifecycleOwner(), new Observer<PagedList<Maker>>() {
            @Override
            public void onChanged(PagedList<Maker> makers) {
                filterMakerAdapter.submitList(makers);
            }
        });
        filterMakerViewModel.getIsLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) { download_progress.setVisibility(View.VISIBLE); } else { download_progress.setVisibility(View.GONE); }
            }
        });
    }

    private void initMakerRecyclerView(int displayWidth, int displayHeight) {

        FilterMakerAdapter.OnMakerClickListener onMakerClickListener = new FilterMakerAdapter.OnMakerClickListener() {
            @Override
            public void onMakerClick(Maker maker, int position) {
                filterMakerEventListener.filterMakerClickEvent(maker);
            }
        };
        filterMakerAdapter = new FilterMakerAdapter(getContext(), onMakerClickListener, displayWidth, displayHeight, spanCount);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), spanCount);
        recycler_view_maker.setLayoutManager(layoutManager);
        recycler_view_maker.setAdapter(filterMakerAdapter);

        //ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) recycler_view_maker.getLayoutParams();
        //marginLayoutParams.setMargins(0,0,0, (int) (displayWidth / filterSpanCount * filterAdapter.getK() + displayWidth / filterSpanCount * dateAdapter.getK()));
        //recycler_view_maker.setLayoutParams(marginLayoutParams);

        int paddingBottom = (int) (displayWidth / filterSpanCount * filterAdapter.getK() + displayWidth / filterSpanCount * dateAdapter.getK());
        int paddingTop = recycler_view_maker.getPaddingTop();
        recycler_view_maker.setPadding(0,paddingTop, 0,paddingBottom);

        recycler_view_maker.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(dy > 10) {

                    AnimatorSet set = new AnimatorSet();
                    set.setDuration(500).playTogether(
                            ObjectAnimator.ofFloat(recycler_view_filter, View.ALPHA, 1.0f, 0f),
                            ObjectAnimator.ofFloat(recycler_view_date, View.ALPHA, 1.0f, 0f),
                            ObjectAnimator.ofFloat(circle_filter_view, View.ALPHA, 1.0f, 0f),
                            ObjectAnimator.ofFloat(time_view, View.ALPHA, 1.0f, 0f),
                            ObjectAnimator.ofFloat(background_view, View.ALPHA, 1.0f, 0f),
                            ObjectAnimator.ofFloat(title_layout, View.ALPHA, 1.0f, 0f)
                    );
                    set.addListener(fadeOutListener());
                    set.start();

                } else if (dy < -5) {

                    if (recycler_view_filter.getAlpha() == 0) {

                        recycler_view_filter.setVisibility(View.VISIBLE);
                        recycler_view_date.setVisibility(View.VISIBLE);
                        circle_filter_view.setVisibility(View.VISIBLE);
                        time_view.setVisibility(View.VISIBLE);
                        background_view.setVisibility(View.VISIBLE);
                        title_layout.setVisibility(View.VISIBLE);
                        AnimatorSet set = new AnimatorSet();
                        set.setDuration(300).playTogether(
                                ObjectAnimator.ofFloat(recycler_view_filter, View.ALPHA, 0.2f, 1f),
                                ObjectAnimator.ofFloat(recycler_view_date, View.ALPHA, 0.2f, 1f),
                                ObjectAnimator.ofFloat(circle_filter_view, View.ALPHA, 0.2f, 1f),
                                ObjectAnimator.ofFloat(time_view, View.ALPHA, 0.2f, 1f),
                                ObjectAnimator.ofFloat(background_view, View.ALPHA, 0.2f, 1f),
                                ObjectAnimator.ofFloat(title_layout, View.ALPHA, 0.2f, 1f)
                        );
                        set.start();

                    }
                }
            }
        });
    }

    private AnimatorListenerAdapter fadeOutListener() {
        return new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                recycler_view_filter.setVisibility(View.GONE);
                recycler_view_date.setVisibility(View.GONE);
                circle_filter_view.setVisibility(View.GONE);
                time_view.setVisibility(View.GONE);
                background_view.setVisibility(View.GONE);
                title_layout.setVisibility(View.GONE);
            }
        };
    }

    public interface FilterMakerEventListener {
        void filterMakerClickEvent(Maker maker);
        void filterMakerOnPauseEvent(int filterPosition, int datePosition);
    }

    private ArrayList<String> getDateList() {

        ArrayList<String> filterList = new ArrayList<>();
        filterList.add(res.getString(R.string.all));
        for(int i = 0; i < 15; i++){ // i < 21
            filterList.add(String.valueOf(21-i));
        }
        return filterList;
    }

    private ArrayList<String> getFilterList() {
        ArrayList<String> filterList = new ArrayList<>();
        filterList.add(res.getString(R.string.all));
        String filter = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for(int i = 0; i < filter.length(); i++){
            filterList.add(String.valueOf(filter.charAt(i)));
        }
        return filterList;
    }

    private void initFilterRecyclerView(int displayWidth, int displayHeight) {

        FilterAdapter.OnFilterClickListener onFilterClickListener = new FilterAdapter.OnFilterClickListener() {
            @Override
            public void onFilterClick(String item, int position) {
                recycler_view_filter.smoothScrollToPosition(position);
            }
        };
        filterAdapter = new FilterAdapter(getContext(), onFilterClickListener, displayWidth, displayHeight, filterSpanCount);
        RecyclerView.LayoutManager layoutManager = new SpeedyFilterLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
        recycler_view_filter.setLayoutManager(layoutManager);
        recycler_view_filter.setAdapter(filterAdapter);
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recycler_view_filter);

        int padding = (displayWidth / filterSpanCount) * (filterSpanCount/2);
        recycler_view_filter.setPadding(padding,0, padding,0);

        recycler_view_filter.addOnScrollListener(new RecyclerView.OnScrollListener() {

            int previousState = 0;
            int currentState = 0;

            int previousPosition = 0;
            int currentPosition = 0;

            private Timer timer=new Timer();
            private final long DELAY = 700; // milliseconds

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                previousState = currentState;
                currentState = newState;

                if (previousState == 2 && currentState == 0) {

                    currentPosition = getTargetFilterPosition();

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
                                            int newPosition = getTargetFilterPosition();
                                            if (newPosition == currentPosition && newPosition != previousPosition) {
                                                previousPosition = currentPosition;
                                                getMakers(newPosition, datePosition);
                                                filterPosition = newPosition;
                                                filterMakerAdapter.setLastPosition(-1);
                                                //AnimatorSet set = new AnimatorSet();
                                                //set.setDuration(600).playSequentially(ObjectAnimator.ofFloat(recycler_view_explore, View.ALPHA, 1f, 0.0f));
                                                //set.start();

                                            }
                                        }
                                    });
                                }
                            }, DELAY);
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    private void initDateRecyclerView(int displayWidth, int displayHeight) {

        DateAdapter.OnDateClickListener onDateClickListener = new DateAdapter.OnDateClickListener() {
            @Override
            public void onDateClick(String item, int position) {
                recycler_view_date.smoothScrollToPosition(position);
            }
        };
        dateAdapter = new DateAdapter(getContext(), onDateClickListener, displayWidth, displayHeight, filterSpanCount);
        RecyclerView.LayoutManager layoutManager = new SpeedyFilterLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
        recycler_view_date.setLayoutManager(layoutManager);
        recycler_view_date.setAdapter(dateAdapter);
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recycler_view_date);

        int padding = (displayWidth / filterSpanCount) * (filterSpanCount/2);
        recycler_view_date.setPadding(padding,0, padding,0);

        recycler_view_date.addOnScrollListener(new RecyclerView.OnScrollListener() {

            int previousState = 0;
            int currentState = 0;

            int previousPosition = 0;
            int currentPosition = 0;

            private Timer timer=new Timer();
            private final long DELAY = 700; // milliseconds

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                previousState = currentState;
                currentState = newState;

                if (previousState == 2 && currentState == 0) {

                    currentPosition = getTargetDatePosition();

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
                                            int newPosition = getTargetDatePosition();
                                            if (newPosition == currentPosition && newPosition != previousPosition) {
                                                previousPosition = currentPosition;
                                                getMakers(filterPosition, newPosition);
                                                datePosition = newPosition;
                                                filterMakerAdapter.setLastPosition(-1);
                                                //AnimatorSet set = new AnimatorSet();
                                                //set.setDuration(600).playSequentially(ObjectAnimator.ofFloat(recycler_view_explore, View.ALPHA, 1f, 0.0f));
                                                //set.start();

                                            }
                                        }
                                    });
                                }
                            }, DELAY);
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    private void getMakers(int filterPosition, int datePosition) {
        filterMakerViewModel.setFilter(filterList.get(filterPosition), dateList.get(datePosition));
    }

    private int getTargetFilterPosition() {
        if (filterList.size() > 0) {
            int position = ((LinearLayoutManager) recycler_view_filter.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
            if (position < 0) position = 0;
            return position;
        } else {
            return 0;
        }
    }

    private int getTargetDatePosition() {
        if (dateList.size() > 0) {
            int position = ((LinearLayoutManager) recycler_view_date.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
            if (position < 0) position = 0;
            return position;
        } else {
            return 0;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        filterMakerEventListener.filterMakerOnPauseEvent(getTargetFilterPosition(), getTargetDatePosition());
    }
}

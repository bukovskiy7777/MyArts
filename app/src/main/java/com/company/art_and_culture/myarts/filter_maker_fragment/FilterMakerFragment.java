package com.company.art_and_culture.myarts.filter_maker_fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

    private RecyclerView recycler_view_filter, recycler_view_maker;
    private FilterMakerViewModel filterMakerViewModel;
    private CircleImageView circle_filter_view;
    private FilterAdapter filterAdapter;
    private FilterMakerAdapter filterMakerAdapter;
    private int spanCount = 3;
    private int filterSpanCount = 5;
    private android.content.res.Resources res;
    private ArrayList<String> filterList = new ArrayList<>();
    private FilterMakerEventListener filterMakerEventListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_filter_maker, container, false);

        recycler_view_filter = root.findViewById(R.id.recycler_view_filter);
        circle_filter_view = root.findViewById(R.id.circle_filter_view);
        recycler_view_maker = root.findViewById(R.id.recycler_view_maker);

        res = getResources();
        int displayWidth = res.getDisplayMetrics().widthPixels;
        int displayHeight = res.getDisplayMetrics().heightPixels;

        filterMakerViewModel = new ViewModelProvider(this).get(FilterMakerViewModel.class);

        initFilterRecyclerView(displayWidth, displayHeight);
        initExploreRecyclerView(displayWidth, displayHeight);

        circle_filter_view.getLayoutParams().height = (int) (displayWidth / filterSpanCount * filterAdapter.getK() * 0.8);
        circle_filter_view.getLayoutParams().width = (int) (displayWidth / filterSpanCount * filterAdapter.getK() * 0.8);

        filterList = getFilterList();
        filterAdapter.setItems(filterList);

        MainActivity activity = (MainActivity) getActivity();
        int filterPosition = activity.getFilterExploreFilterPosition();
        filterMakerViewModel.setFilter(filterList.get(filterPosition));
        recycler_view_filter.scrollToPosition(filterPosition);

        subscribeObservers();

        return root;
    }

    private void subscribeObservers() {

        filterMakerViewModel.getMakerList().observe(this, new Observer<PagedList<Maker>>() {
            @Override
            public void onChanged(PagedList<Maker> exploreObjects) {
                //Log.i("filterExploreViewModel", exploreObjects.size()+"  fd");
                filterMakerAdapter.submitList(exploreObjects);
            }
        });
        filterMakerViewModel.getIsInitialLoaded().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                //Log.i("filterExploreViewModel", aBoolean+"  fd");
                //recycler_view_explore.setAlpha(1f);
            }
        });
    }

    private void initExploreRecyclerView(int displayWidth, int displayHeight) {

        FilterMakerAdapter.OnMakerClickListener onExploreClickListener = new FilterMakerAdapter.OnMakerClickListener() {
            @Override
            public void onMakerClick(Maker maker, int position) {
                filterMakerEventListener.filterMakerClickEvent(maker);
            }
        };
        filterMakerAdapter = new FilterMakerAdapter(getContext(), onExploreClickListener, displayWidth, displayHeight, spanCount);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), spanCount);
        recycler_view_maker.setLayoutManager(layoutManager);
        recycler_view_maker.setAdapter(filterMakerAdapter);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) recycler_view_maker.getLayoutParams();
        marginLayoutParams.setMargins(0,0,0, (int) (displayWidth / filterSpanCount * filterAdapter.getK()));
        recycler_view_maker.setLayoutParams(marginLayoutParams);
    }

    public interface FilterMakerEventListener {
        void filterMakerClickEvent(Maker maker);
        void filterMakerOnPauseEvent(int position);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            filterMakerEventListener = (FilterMakerEventListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement onSomeEventListener");
        }
    }

    private ArrayList<String> getFilterList() {
        /*
        for (char c = 'A'; c <= 'Z'; c++) {
            filterList.add(String.valueOf(c));
        }
        */
        ArrayList<String> filterList = new ArrayList<>();
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
                                                getMakers(newPosition);
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

    private void getMakers(int position) {
        filterMakerViewModel.setFilter(filterList.get(position));
    }

    private int getTargetFilterPosition() {
        if (filterList.size() > 0) {
            return ((LinearLayoutManager) recycler_view_filter.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        } else {
            return 0;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        filterMakerEventListener.filterMakerOnPauseEvent(getTargetFilterPosition());
    }
}

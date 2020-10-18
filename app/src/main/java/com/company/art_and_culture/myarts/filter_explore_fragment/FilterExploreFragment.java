package com.company.art_and_culture.myarts.filter_explore_fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.pojo.ExploreObject;
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

public class FilterExploreFragment extends Fragment {

    private RecyclerView recycler_view_filter, recycler_view_explore;
    private FilterExploreViewModel filterExploreViewModel;
    private CircleImageView circle_filter_view;
    private FilterAdapter filterAdapter;
    private FilterExploreAdapter filterExploreAdapter;
    private int spanCount = 3;
    private android.content.res.Resources res;
    private ArrayList<String> filterList = new ArrayList<>();
    private FilterExploreEventListener filterExploreEventListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_filter_explore, container, false);

        recycler_view_filter = root.findViewById(R.id.recycler_view_filter);
        circle_filter_view = root.findViewById(R.id.circle_filter_view);
        recycler_view_explore = root.findViewById(R.id.recycler_view_explore);

        res = getResources();
        int displayWidth = res.getDisplayMetrics().widthPixels;
        int displayHeight = res.getDisplayMetrics().heightPixels;

        initFilterRecyclerView(displayWidth, displayHeight);
        initExploreRecyclerView(displayWidth, displayHeight);

        circle_filter_view.getLayoutParams().height = (int) (displayWidth * filterAdapter.getK() * 0.8);
        circle_filter_view.getLayoutParams().width = (int) (displayWidth * filterAdapter.getK() * 0.8);

        filterExploreViewModel = new ViewModelProvider(this).get(FilterExploreViewModel.class);

        filterList = getFilterList();
        filterAdapter.setItems(filterList);
        filterExploreViewModel.setFilter(filterList.get(0));

        subscribeObservers();

        return root;
    }

    private void subscribeObservers() {

        filterExploreViewModel.getExploreList().observe(this, new Observer<PagedList<ExploreObject>>() {
            @Override
            public void onChanged(PagedList<ExploreObject> exploreObjects) {
                filterExploreAdapter.submitList(exploreObjects);
            }
        });
    }

    private void initExploreRecyclerView(int displayWidth, int displayHeight) {

        FilterExploreAdapter.OnExploreClickListener onExploreClickListener = new FilterExploreAdapter.OnExploreClickListener() {
            @Override
            public void onExploreClick(ExploreObject exploreObject, int position) {
                String imageUrl;
                if (!exploreObject.getImageUrlSmall().equals(" ") && exploreObject.getImageUrlSmall().startsWith(getResources().getString(R.string.http))) {
                    imageUrl = exploreObject.getImageUrlSmall();
                } else {
                    imageUrl= exploreObject.getImageUrl();
                }
                Maker maker = new Maker(exploreObject.getText(), exploreObject.getArtistBio(), imageUrl, exploreObject.getWidth(), exploreObject.getHeight());
                filterExploreEventListener.filterExploreMakerClickEvent(maker);
            }
        };
        filterExploreAdapter = new FilterExploreAdapter(getContext(), onExploreClickListener, displayWidth, displayHeight, spanCount);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), spanCount);
        recycler_view_explore.setLayoutManager(layoutManager);
        recycler_view_explore.setAdapter(filterExploreAdapter);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) recycler_view_explore.getLayoutParams();
        marginLayoutParams.setMargins(0,0,0, (int) (displayWidth * filterAdapter.getK()));
        recycler_view_explore.setLayoutParams(marginLayoutParams);
    }

    public interface FilterExploreEventListener {
        void filterExploreMakerClickEvent(Maker maker);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            filterExploreEventListener = (FilterExploreEventListener) context;
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
        filterAdapter = new FilterAdapter(getContext(), onFilterClickListener, displayWidth, displayHeight);
        RecyclerView.LayoutManager layoutManager = new SpeedyFilterLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
        recycler_view_filter.setLayoutManager(layoutManager);
        recycler_view_filter.setAdapter(filterAdapter);
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recycler_view_filter);

        int padding = (int) (displayWidth * filterAdapter.getK() * 2);
        recycler_view_filter.setPadding(padding,0, padding,0);

        recycler_view_filter.addOnScrollListener(new RecyclerView.OnScrollListener() {

            int previousState = 0;
            int currentState = 0;

            int previousPosition = 0;
            int currentPosition = 0;

            private Timer timer=new Timer();
            private final long DELAY = 900; // milliseconds

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
        //Log.i("recycler_view_filter", position+" "+ filterList.get(position));
        filterExploreViewModel.setFilter(filterList.get(position));
    }

    private int getTargetFilterPosition() {
        if (filterList.size() > 0) {
            return ((LinearLayoutManager) recycler_view_filter.getLayoutManager()).findFirstVisibleItemPosition();
        } else {
            return 0;
        }
    }

    public void finish() {
        filterExploreViewModel.finish ();
    }
}

package com.company.art_and_culture.myarts.attribute_fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.filter_maker_fragment.FilterAdapter;
import com.company.art_and_culture.myarts.filter_maker_fragment.SpeedyFilterLayoutManager;
import com.company.art_and_culture.myarts.pojo.Attribute;

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

public class AttributeFragment extends Fragment {

    private RecyclerView recycler_view_attribute, recycler_view_filter;
    private AttributeViewModel attributeViewModel;
    private FrameLayout title_layout;
    private TextView title_tv;
    private ProgressBar download_progress;
    private AttributeAdapter attributeAdapter;
    private int spanCount = 3;
    private android.content.res.Resources res;
    private AttributeEventListener attributeEventListener;
    private String attributeType;

    private int filterSpanCount = 5;
    private int filterPosition = 0;
    private FilterAdapter filterAdapter;
    private ArrayList<String> filterList = new ArrayList<>();
    private CircleImageView circle_filter_view;
    private View background_view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_attribute, container, false);

        title_layout = root.findViewById(R.id.title_layout);
        title_tv = root.findViewById(R.id.title);
        recycler_view_attribute = root.findViewById(R.id.recycler_view_attribute);
        download_progress = root.findViewById(R.id.download_progress);

        recycler_view_filter = root.findViewById(R.id.recycler_view_filter);
        circle_filter_view = root.findViewById(R.id.circle_filter_view);
        background_view = root.findViewById(R.id.background_view);

        res = getResources();
        int displayWidth = res.getDisplayMetrics().widthPixels;
        int displayHeight = res.getDisplayMetrics().heightPixels;

        attributeViewModel = new ViewModelProvider(this).get(AttributeViewModel.class);

        MainActivity activity = (MainActivity) getActivity();
        attributeType = activity.getNavFragments().getTypeForAttributeFragment();
        attributeViewModel.setAttributeType(attributeType);

        attributeEventListener = activity.getNavFragments();

        if(attributeType.equals(Constants.ART_CULTURE)) {
            title_tv.setText(res.getString(R.string.artist_culture));
        } else if (attributeType.equals(Constants.ART_MEDIUM)) {
            title_tv.setText(res.getString(R.string.art_mediums));
        } else if (attributeType.equals(Constants.ART_CLASSIFICATION)) {
            title_tv.setText(res.getString(R.string.art_classifications));
        } else if (attributeType.equals(Constants.ART_TAG)) {
            title_tv.setText(res.getString(R.string.art_tags));
        }

        if(attributeType.equals(Constants.ART_CLASSIFICATION)) {
            spanCount = 1;
        } else {
            spanCount = 3;
        }

        if(attributeType.equals(Constants.ART_TAG)) {

            initFilterRecyclerView(displayWidth, displayHeight);

            filterList = getFilterList();
            filterAdapter.setItems(filterList);

            filterPosition = 0; //activity.getFilterTagPosition();
            attributeViewModel.setFilter(filterList.get(filterPosition));
            recycler_view_filter.scrollToPosition(filterPosition);

            recycler_view_filter.setVisibility(View.VISIBLE);
            circle_filter_view.setVisibility(View.VISIBLE);
            background_view.setVisibility(View.VISIBLE);
        } else {
            recycler_view_filter.setVisibility(View.GONE);
            circle_filter_view.setVisibility(View.GONE);
            background_view.setVisibility(View.GONE);
        }

        initAttributeRecyclerView(displayWidth, displayHeight, spanCount, attributeType);

        subscribeObservers();

        return root;
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

    private void subscribeObservers() {

        attributeViewModel.getAttributeList().observe(this, new Observer<PagedList<Attribute>>() {
            @Override
            public void onChanged(PagedList<Attribute> attributes) {
                attributeAdapter.submitList(attributes);
            }
        });
        attributeViewModel.getIsLoading().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) { download_progress.setVisibility(View.VISIBLE); } else { download_progress.setVisibility(View.GONE); }
            }
        });
    }

    private void initAttributeRecyclerView(int displayWidth, int displayHeight, int spanCount, String attributeType) {

        AttributeAdapter.OnAttributeClickListener onAttributeClickListener = new AttributeAdapter.OnAttributeClickListener() {
            @Override
            public void onAttributeClick(Attribute attribute, int position) {
                attributeEventListener.attributeClickEvent(attribute);
            }
        };
        attributeAdapter = new AttributeAdapter(getContext(), onAttributeClickListener, displayWidth, displayHeight, spanCount);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), spanCount);
        recycler_view_attribute.setLayoutManager(layoutManager);
        recycler_view_attribute.setAdapter(attributeAdapter);

        int paddingBottom = 0;
        if(attributeType.equals(Constants.ART_CLASSIFICATION)) {
            paddingBottom = 0;
        } else {
            paddingBottom = (int) (displayWidth / spanCount);
            //paddingBottom = (int) (displayWidth / filterSpanCount * filterAdapter.getK());
        }

        int paddingTop = recycler_view_attribute.getPaddingTop();
        recycler_view_attribute.setPadding(0,paddingTop, 0,paddingBottom);

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
                                                setFilter(newPosition);
                                                filterPosition = newPosition;
                                                attributeAdapter.setLastPosition(-1);
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

    private void setFilter(int position) {
        attributeViewModel.setFilter(filterList.get(position));
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

    public interface AttributeEventListener {
        void attributeClickEvent(Attribute attribute);
    }

}

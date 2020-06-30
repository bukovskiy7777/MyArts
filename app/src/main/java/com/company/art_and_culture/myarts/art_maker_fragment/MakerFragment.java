package com.company.art_and_culture.myarts.art_maker_fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.pojo.Art;

import java.util.ArrayList;
import java.util.Collection;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MakerFragment extends Fragment {

    private MakerViewModel makerViewModel;
    private RecyclerView makerRecyclerView;
    private MakerAdapter makerAdapter;
    private ProgressBar makerProgressBar;
    private TextView textView, appbar_maker;
    private MakerEventListener makerEventListener;
    private SwipeRefreshLayout swipeRefreshLayout;
    private android.content.res.Resources res;
    private MainActivity activity;
    private static MakerFragment instance;
    private String artMaker;
    private int scrollPosition = 0;
    private int scrollPixels = 0;
    private int spanCount = 2;

    public static MakerFragment getInstance() {
        if(instance == null){
            instance = new MakerFragment();
        }
        return instance;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_art_maker, container, false);
        textView = root.findViewById(R.id.text_maker);
        makerRecyclerView = root.findViewById(R.id.recycler_view_maker);
        makerProgressBar = root.findViewById(R.id.progress_bar_maker);
        swipeRefreshLayout = root.findViewById(R.id.maker_swipeRefreshLayout);
        appbar_maker = root.findViewById(R.id.appbar_maker);

        makerViewModel = new ViewModelProvider(this).get(MakerViewModel.class);

        res = getResources();
        int displayWidth = res.getDisplayMetrics().widthPixels;
        int displayHeight = res.getDisplayMetrics().heightPixels;

        initRecyclerView(makerViewModel, displayWidth, displayHeight);

        activity = (MainActivity) getActivity();

        if (activity != null) artMaker = activity.getArtMaker();
        appbar_maker.setText(artMaker);

        initSwipeRefreshLayout();
        subscribeObservers();
        setOnBackPressedListener(root);

        return root;
    }


    private void setOnBackPressedListener(final View root) {
        //You need to add the following line for this solution to work; thanks skayred
        root.setFocusableInTouchMode(true);
        root.requestFocus();
        root.setOnKeyListener( new View.OnKeyListener() {
            @Override
            public boolean onKey( View v, int keyCode, KeyEvent event ) {

                if( keyCode == KeyEvent.KEYCODE_BACK && activity.getArtShowFragment() == null ) {
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

    private void initRecyclerView(final MakerViewModel makerViewModel, int displayWidth, int displayHeight){

        MakerAdapter.OnArtClickListener onArtClickListener = new MakerAdapter.OnArtClickListener() {

            @Override
            public void onArtImageClick(Art art, int position) {
                Collection<Art> listArts = new ArrayList<>();
                Art artInMemory = MakerDataInMemory.getInstance().getSingleItem(position);
                listArts.add(artInMemory);
                makerEventListener.makerArtClickEvent(listArts, 0);
            }

        };

        makerAdapter = new MakerAdapter(makerViewModel,getContext(), onArtClickListener, displayWidth, displayHeight, spanCount);
        RecyclerView.LayoutManager layoutManager = new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL);
        makerRecyclerView.setLayoutManager(layoutManager);
        makerRecyclerView.setAdapter(makerAdapter);
        makerRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                scrollPixels = scrollPixels + dy;
            }
        });
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

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            makerEventListener = (MakerEventListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement onSomeEventListener");
        }
    }

    private int getTargetScrollPosition () {

        int displayWidth = res.getDisplayMetrics().widthPixels;
        int itemHeight = displayWidth/spanCount;

        int scrollPosition = (scrollPixels/itemHeight)* spanCount;

        return scrollPosition;
    }


    public String getArtMaker() {
        return artMaker;
    }

    public MakerFragment finish() {
        makerViewModel.finish ();
        instance = null;
        return instance;
    }


}
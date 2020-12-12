package com.company.art_and_culture.myarts.bottom_menu.favorites.Folders;

import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.pojo.Folder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class FoldersFragment extends Fragment implements View.OnClickListener {

    private FoldersViewModel foldersViewModel;
    private TextView create_folder_button;
    private FrameLayout create_folder_layout;
    private FloatingActionButton floatingActionButton;
    private int spanCount = 1;
    private ProgressBar foldersProgressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView folderRecyclerView;
    private FoldersAdapter foldersAdapter;
    private FoldersEventListener folderEventListener;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_favorites_folder, container, false);

        foldersViewModel =new ViewModelProvider(this).get(FoldersViewModel.class);

        create_folder_layout = root.findViewById(R.id.create_folder_layout);
        create_folder_button = root.findViewById(R.id.create_folder_button);
        folderRecyclerView = root.findViewById(R.id.recycler_view_folders);
        foldersProgressBar = root.findViewById(R.id.progress_bar_folders);
        swipeRefreshLayout = root.findViewById(R.id.folders_swipeRefreshLayout);
        floatingActionButton = root.findViewById(R.id.floating_button);

        create_folder_layout.setVisibility(View.GONE);
        floatingActionButton.setVisibility(View.GONE);

        floatingActionButton.setOnClickListener(this);
        create_folder_button.setOnClickListener(this);

        android.content.res.Resources res = getResources();
        int displayWidth = res.getDisplayMetrics().widthPixels;
        int displayHeight = res.getDisplayMetrics().heightPixels;

        initRecyclerView(displayWidth, displayHeight);

        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) folderEventListener = activity.getNavFragments();

        initSwipeRefreshLayout();
        subscribeObservers(activity);

        return root;
    }

    private void initRecyclerView(int displayWidth, int displayHeight) {

        FoldersAdapter.OnFolderClickListener onFolderClickListener = new FoldersAdapter.OnFolderClickListener() {
            @Override
            public void onFolderImageClick(Folder folder, int position) {
                folderEventListener.folderClick(folder.getFolderUniqueId(), folder.getUserUniqueId());
            }
        };
        foldersAdapter = new FoldersAdapter(getContext(), onFolderClickListener, displayWidth, displayHeight, spanCount);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), spanCount);
        folderRecyclerView.setLayoutManager(layoutManager);
        folderRecyclerView.setAdapter(foldersAdapter);
    }

    private void subscribeObservers(MainActivity activity) {

        foldersViewModel.getFoldersList().observe(getViewLifecycleOwner(), new Observer<ArrayList<Folder>>() {
            @Override
            public void onChanged(ArrayList<Folder> objects) {

                if (objects == null) {
                    foldersAdapter.clearItems();
                } else {
                    setAnimationRecyclerView (objects, foldersAdapter, folderRecyclerView);
                    foldersAdapter.clearItems();
                    foldersAdapter.setItems(objects);
                }
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        foldersViewModel.getIsLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) { showProgressBar(); } else { hideProgressBar(); }
            }
        });
        foldersViewModel.getIsListEmpty().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) { showText(); } else { hideText(); }
            }
        });

        activity.getUpdateFolders().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                refresh();
            }
        });

    }

    private void setAnimationRecyclerView(ArrayList<Folder> objects, FoldersAdapter foldersAdapter, RecyclerView recyclerView) {

        LayoutAnimationController layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_fade_in);
        recyclerView.setLayoutAnimation(layoutAnimationController);
        //recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
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
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light,
                android.R.color.holo_blue_bright
        );
    }

    private void showProgressBar(){
        foldersProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar(){
        foldersProgressBar.setVisibility(View.GONE);
    }

    private void showText(){
        create_folder_layout.setVisibility(View.VISIBLE);
        floatingActionButton.setVisibility(View.GONE);
    }

    private void hideText(){
        create_folder_layout.setVisibility(View.GONE);
        floatingActionButton.setVisibility(View.VISIBLE);
    }

    public boolean refresh () {
        return foldersViewModel.refresh();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == floatingActionButton.getId() || view.getId() == create_folder_button.getId()) {
            Log.i("create_folder_button", "clicked");
            folderEventListener.createFolderClick();
        }
    }

    public interface FoldersEventListener {
        void folderClick(String folderUniqueId, String userUniqueId);
        void createFolderClick();
    }


}

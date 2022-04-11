package com.company.art_and_culture.myarts.bottom_menu.favorites.Folders;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.pojo.Folder;
import com.company.art_and_culture.myarts.show_folder_fragment.ShowFolderFragment;

import java.io.Serializable;
import java.util.ArrayList;

public class FoldersFragment extends Fragment implements View.OnClickListener {

    private FoldersViewModel foldersViewModel;
    private TextView create_folder_button;
    private FrameLayout create_folder_layout;
    private int spanCount = 1;
    private ProgressBar foldersProgressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView folderRecyclerView;
    private FoldersAdapter foldersAdapter;
    private MainActivity activity;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_favorites_folder, container, false);

        foldersViewModel =new ViewModelProvider(this).get(FoldersViewModel.class);

        create_folder_layout = root.findViewById(R.id.create_folder_layout);
        create_folder_button = root.findViewById(R.id.create_folder_button);
        folderRecyclerView = root.findViewById(R.id.recycler_view_folders);
        foldersProgressBar = root.findViewById(R.id.progress_bar_folders);
        swipeRefreshLayout = root.findViewById(R.id.folders_swipeRefreshLayout);
        create_folder_layout.setVisibility(View.GONE);
        create_folder_button.setOnClickListener(this);

        android.content.res.Resources res = getResources();
        int displayWidth = res.getDisplayMetrics().widthPixels;
        int displayHeight = res.getDisplayMetrics().heightPixels;

        initRecyclerView(displayWidth, displayHeight);

        activity = (MainActivity) getActivity();

        initSwipeRefreshLayout();
        subscribeObservers();

        return root;
    }

    private void initRecyclerView(int displayWidth, int displayHeight) {

        FoldersAdapter.OnFolderClickListener onFolderClickListener = new FoldersAdapter.OnFolderClickListener() {
            @Override
            public void onFolderImageClick(Folder folder, int position) {
                Bundle args = new Bundle();
                args.putSerializable(ShowFolderFragment.FOLDER, (Serializable) folder);
                NavHostFragment.findNavController(FoldersFragment.this)
                        .navigate(R.id.action_navigation_favorites_to_showFolderFragment, args);
            }
        };
        foldersAdapter = new FoldersAdapter(getContext(), onFolderClickListener, displayWidth, displayHeight, spanCount);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), spanCount);
        folderRecyclerView.setLayoutManager(layoutManager);
        folderRecyclerView.setAdapter(foldersAdapter);
    }

    private void subscribeObservers() {

        foldersViewModel.getFoldersList().observe(getViewLifecycleOwner(), objects -> {
            if (objects == null) {
                foldersAdapter.clearItems();
                activity.postFoldersCount(0);
            } else {
                setAnimationRecyclerView (objects, foldersAdapter, folderRecyclerView);
                foldersAdapter.clearItems();
                foldersAdapter.setItems(objects);
                activity.postFoldersCount(objects.size());
            }
            swipeRefreshLayout.setRefreshing(false);
        });
        foldersViewModel.getIsLoading().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) { showProgressBar(); } else { hideProgressBar(); }
        });
        foldersViewModel.getIsListEmpty().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) { showText(); } else { hideText(); }
        });
        activity.getUpdateFolders().observe(getViewLifecycleOwner(), aBoolean -> {
            if(aBoolean) {
                foldersViewModel.refresh();
                //activity.updateFolders(false);
            }
        });
        activity.getIsUpdateAllAppData().observe(getViewLifecycleOwner(), aBoolean -> {
            if(aBoolean) {
                foldersViewModel.refresh();
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
                boolean networkState = foldersViewModel.refresh();
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
        foldersProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar(){
        foldersProgressBar.setVisibility(View.GONE);
    }

    private void showText(){
        create_folder_layout.setVisibility(View.VISIBLE);
    }

    private void hideText(){
        create_folder_layout.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == create_folder_button.getId()) {
            NavHostFragment.findNavController(FoldersFragment.this)
                    .navigate(R.id.action_navigation_favorites_to_createFolderFragment);
        }
    }

}

package com.company.art_and_culture.myarts.show_folder_fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.art_search_fragment.SearchFragment;
import com.company.art_and_culture.myarts.arts_show_fragment.ArtShowFragment;
import com.company.art_and_culture.myarts.create_folder_fragment.CreateFolderFragment;
import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.pojo.Folder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.Serializable;
import java.util.ArrayList;

public class ShowFolderFragment extends Fragment implements View.OnClickListener {

    public static final String FOLDER = "folder";

    private RecyclerView recycler_view_items;
    private ShowFolderViewModel showFolderViewModel;
    private TextView folder_title;
    private ImageView back_button, delete_btn;
    private ProgressBar download_progress;
    private ArtsAdapter artsAdapter;
    private int spanCount = 2;
    private android.content.res.Resources res;
    private FloatingActionButton floatingActionButton;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MainActivity activity;
    private Folder currentFolder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_show_folder, container, false);

        folder_title = root.findViewById(R.id.folder_title);
        back_button = root.findViewById(R.id.back_button);
        delete_btn = root.findViewById(R.id.delete_btn);
        recycler_view_items = root.findViewById(R.id.recycler_view_items);
        download_progress = root.findViewById(R.id.download_progress);
        swipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayout);
        floatingActionButton = root.findViewById(R.id.floating_button);
        floatingActionButton.setOnClickListener(this);
        back_button.setOnClickListener(this);
        delete_btn.setOnClickListener(this);

        res = getResources();
        int displayWidth = res.getDisplayMetrics().widthPixels;
        int displayHeight = res.getDisplayMetrics().heightPixels;

        showFolderViewModel = new ViewModelProvider(this).get(ShowFolderViewModel.class);

        currentFolder = (Folder) getArguments().getSerializable(FOLDER);
        showFolderViewModel.setFolder(currentFolder);

        activity = (MainActivity) getActivity();
        showFolderViewModel.setActivity(activity);

        if(currentFolder.getUserUniqueId().equals(activity.getSharedPreferences(Constants.TAG, 0).getString(Constants.USER_UNIQUE_ID,""))){
            delete_btn.setVisibility(View.VISIBLE);
            floatingActionButton.setVisibility(View.VISIBLE);
        } else {
            delete_btn.setVisibility(View.GONE);
            floatingActionButton.setVisibility(View.GONE);
        }

        folder_title.setText(currentFolder.getTitle());

        initItemsRecyclerView(displayWidth, displayHeight, spanCount);

        subscribeObservers();

        setOnBackPressedListener(root);

        initSwipeRefreshLayout();

        return root;
    }

    @Override
    public void onDetach() {
        showFolderViewModel.detach();
        super.onDetach();
    }

    private void initSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            boolean networkState = showFolderViewModel.refresh();
            if (!networkState) {
                Toast.makeText(getContext(), R.string.network_is_unavailable, Toast.LENGTH_LONG).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorBlack
        );
    }

    private void setOnBackPressedListener(View root) {
        //You need to add the following line for this solution to work; thanks skayred
        root.setFocusableInTouchMode(true);
        root.requestFocus();
        root.setOnKeyListener( new View.OnKeyListener() {
            @Override
            public boolean onKey( View v, int keyCode, KeyEvent event ) {

                if( keyCode == KeyEvent.KEYCODE_BACK ) {
                    int scrollPosition = 0;
                    if (artsAdapter.getItemCount() > 0) scrollPosition = getTargetScrollPosition();
                    if (scrollPosition > 4) {
                        recycler_view_items.smoothScrollToPosition(0);
                        return true;
                    }
                    return false;
                }
                return false;
            }
        } );
    }

    private int getTargetScrollPosition () {

        return ((GridLayoutManager) recycler_view_items.getLayoutManager()).findFirstVisibleItemPosition();
    }

    private void subscribeObservers() {

        showFolderViewModel.getArtsList().observe(getViewLifecycleOwner(), new Observer<ArrayList<Art>>() {
            @Override
            public void onChanged(ArrayList<Art> arts) {
                if (arts == null) {
                    artsAdapter.clearItems();
                } else {
                    setAnimationRecyclerView (recycler_view_items);
                    artsAdapter.clearItems();
                    artsAdapter.setItems(arts);
                }
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        showFolderViewModel.getIsLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) { download_progress.setVisibility(View.VISIBLE); } else { download_progress.setVisibility(View.GONE); }
            }
        });
    }

    private void initItemsRecyclerView(int displayWidth, int displayHeight, int spanCount) {

        ArtsAdapter.OnArtClickListener onArtClickListener = new ArtsAdapter.OnArtClickListener() {
            @Override
            public void onArtImageClick(Art art, int position) {
                Bundle args = new Bundle();
                args.putSerializable(ArtShowFragment.ARTS, (Serializable) artsAdapter.getItems());
                args.putInt(ArtShowFragment.POSITION, position);
                NavHostFragment.findNavController(ShowFolderFragment.this)
                        .navigate(R.id.action_showFolderFragment_to_artShowFragment, args);
            }
        };
        artsAdapter = new ArtsAdapter(getContext(), onArtClickListener, displayWidth, displayHeight, spanCount);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), spanCount);
        recycler_view_items.setLayoutManager(layoutManager);
        recycler_view_items.setAdapter(artsAdapter);
    }

    private void setAnimationRecyclerView(RecyclerView recyclerView) {

        LayoutAnimationController layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_fade_in);
        recyclerView.setLayoutAnimation(layoutAnimationController);
        //recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == floatingActionButton.getId()) {
            Bundle args = new Bundle();
            args.putSerializable(CreateFolderFragment.FOLDER, (Serializable) currentFolder);
            NavHostFragment.findNavController(ShowFolderFragment.this)
                    .navigate(R.id.action_showFolderFragment_to_createFolderFragment, args);
        } else if (view.getId() == back_button.getId()) {
            NavHostFragment.findNavController(ShowFolderFragment.this).popBackStack();
        } else if (view.getId() == delete_btn.getId()) {
            showDialog();
        }
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = this.getLayoutInflater();
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.dialog_back_create_folder, null);
        builder.setView(view);
        builder.setTitle(res.getString(R.string.want_to_delete));
        builder.setPositiveButton(res.getString(R.string.yes), (dialogInterface, which) -> showFolderViewModel.deleteFolder(currentFolder));
        builder.setNegativeButton(res.getString(R.string.no), (dialogInterface, which) -> dialogInterface.cancel());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}

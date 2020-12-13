package com.company.art_and_culture.myarts.show_folder_fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.pojo.Folder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ShowFolderFragment extends Fragment implements View.OnClickListener {

    private RecyclerView recycler_view_items;
    private ShowFolderViewModel showFolderViewModel;
    private TextView folder_title;
    private ImageView back_button, delete_btn;
    private ProgressBar download_progress;
    private ArtsAdapter artsAdapter;
    private int spanCount = 2;
    private android.content.res.Resources res;
    private ShowFolderEventListener showFolderEventListener;
    private FloatingActionButton floatingActionButton;
    private MainActivity activity;
    private AlertDialog dialog;
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
        floatingActionButton = root.findViewById(R.id.floating_button);
        floatingActionButton.setOnClickListener(this);
        back_button.setOnClickListener(this);
        delete_btn.setOnClickListener(this);

        res = getResources();
        int displayWidth = res.getDisplayMetrics().widthPixels;
        int displayHeight = res.getDisplayMetrics().heightPixels;

        showFolderViewModel = new ViewModelProvider(this).get(ShowFolderViewModel.class);

        activity = (MainActivity) getActivity();
        showFolderEventListener = activity.getNavFragments();
        currentFolder = activity.getNavFragments().getFolderForShowFolderFragment();
        //showFolderViewModel.setActivity(activity);
        showFolderViewModel.setFolder(currentFolder);

        if(currentFolder.getUserUniqueId().equals(activity.getSharedPreferences(Constants.TAG, 0).getString(Constants.USER_UNIQUE_ID,""))){
            delete_btn.setVisibility(View.VISIBLE);
        } else {
            delete_btn.setVisibility(View.GONE);
        }

        folder_title.setText(currentFolder.getTitle());

        initItemsRecyclerView(displayWidth, displayHeight, spanCount);

        subscribeObservers();

        setOnBackPressedListener(root);

        return root;
    }

    @Override
    public void onDetach() {
        showFolderViewModel.refresh();
        super.onDetach();
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
                    artsAdapter.clearItems();
                    artsAdapter.setItems(arts);
                }
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
                showFolderEventListener.artFolderClickEvent(artsAdapter.getItems(), position);
            }
        };
        artsAdapter = new ArtsAdapter(getContext(), onArtClickListener, displayWidth, displayHeight, spanCount);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), spanCount);
        recycler_view_items.setLayoutManager(layoutManager);
        recycler_view_items.setAdapter(artsAdapter);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == floatingActionButton.getId()) {
            showFolderEventListener.folderEditClickListener(currentFolder);
        } else if (view.getId() == back_button.getId()) {
            activity.getNavFragments().popBackStack();
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
        builder.setPositiveButton(res.getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {

                dialog = null;
                // delete folder

            }
        });
        builder.setNegativeButton(res.getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialog = null;
                dialogInterface.dismiss();
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                dialog = null;
            }
        });
        if(dialog == null) {
            dialog = builder.create();
            dialog.show();
        }
    }

    public interface ShowFolderEventListener {
        void artFolderClickEvent(Collection<Art> arts, int position);
        void folderEditClickListener(Folder folder);
    }

}

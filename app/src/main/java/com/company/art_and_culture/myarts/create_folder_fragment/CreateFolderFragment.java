package com.company.art_and_culture.myarts.create_folder_fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.art_search_fragment.SearchFragment;
import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.pojo.Folder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class CreateFolderFragment extends Fragment implements View.OnClickListener {

    public static final String FOLDER = "folder";

    private RecyclerView recycler_view_items;
    private ConstraintLayout blank_favorites;
    private CreateFolderViewModel createFolderViewModel;
    private FrameLayout title_layout;
    private TextView title, items_count, switch_description;
    private ImageButton back_button;
    private EditText title_edit_text;
    private SwitchCompat switch_isPublic;
    private ProgressBar download_progress;
    private ArtsAdapter artsAdapter;
    private int spanCount = 2;
    private android.content.res.Resources res;
    private FloatingActionButton floatingActionButton;
    private int countChosen = 0;
    private MainActivity activity;
    private AlertDialog dialog;
    private String folderUniqueId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_create_folder, container, false);

        title_layout = root.findViewById(R.id.title_layout);
        blank_favorites = root.findViewById(R.id.blank_favorites);
        title = root.findViewById(R.id.title);
        items_count = root.findViewById(R.id.items_count);
        switch_description = root.findViewById(R.id.switch_description);
        back_button = root.findViewById(R.id.back_button);
        title_edit_text = root.findViewById(R.id.title_edit_text);
        switch_isPublic = root.findViewById(R.id.switch_isPublic);
        recycler_view_items = root.findViewById(R.id.recycler_view_items);
        download_progress = root.findViewById(R.id.download_progress);
        floatingActionButton = root.findViewById(R.id.floating_button);
        floatingActionButton.setOnClickListener(this);
        back_button.setOnClickListener(this);

        res = getResources();
        int displayWidth = res.getDisplayMetrics().widthPixels;
        int displayHeight = res.getDisplayMetrics().heightPixels;

        switch_isPublic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) switch_description.setText(res.getText(R.string.public_visible_to_all_users));
                else switch_description.setText(res.getText(R.string.private_visible_only_to_me));
            }
        });
        title_edit_text.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    title_edit_text.clearFocus();
                }
                return false;
            }
        });
        title_edit_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void afterTextChanged(Editable editable) {
                if(countChosen > 0 && title_edit_text.getText().length() > 0) {
                    floatingActionButton.setImageDrawable(res.getDrawable(R.drawable.ic_baseline_playlist_add_check_100));
                } else {
                    floatingActionButton.setImageDrawable(res.getDrawable(R.drawable.ic_baseline_playlist_add_check_100_black));
                }
            }
        });

        createFolderViewModel = new ViewModelProvider(this).get(CreateFolderViewModel.class);

        activity = (MainActivity) getActivity();
        Folder folderForEdit = null;
        if (getArguments() != null) { folderForEdit = (Folder) getArguments().getSerializable(FOLDER); }
        if(folderForEdit == null) {
            createFolderViewModel.getFolderForEdit(null);
            title.setText(res.getText(R.string.create_folder));
            folderUniqueId = activity.randomString(23);
        } else {
            createFolderViewModel.getFolderForEdit(folderForEdit);
            title.setText(res.getText(R.string.edit_folder));
            title_edit_text.setText(folderForEdit.getTitle());
            items_count.setText(Integer.toString(folderForEdit.getItemsCount()));
            switch_isPublic.setChecked(folderForEdit.isPublic());
            folderUniqueId = folderForEdit.getFolderUniqueId();
        }
        createFolderViewModel.setActivity(activity);

        initItemsRecyclerView(displayWidth, displayHeight, spanCount);
        subscribeObservers();
        setOnBackPressedListener(root);
        hideText();

        return root;
    }

    private void showText(){
        blank_favorites.setVisibility(View.VISIBLE);
    }

    private void hideText(){
        blank_favorites.setVisibility(View.GONE);
    }

    @Override
    public void onDetach() {
        //createFolderEventListener.onCreateFolderFragmentClose();
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
                    } else if(countChosen > 0) {
                        showDialog();
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

        createFolderViewModel.getArtsList().observe(getViewLifecycleOwner(), new Observer<ArrayList<Art>>() {
            @Override
            public void onChanged(ArrayList<Art> arts) {
                if (arts == null) {
                    artsAdapter.clearItems();
                    showText();
                } else {
                    artsAdapter.clearItems();
                    artsAdapter.setItems(arts);
                    hideText();

                    countChosen = 0;
                    for(int i = 0; i < artsAdapter.getItems().size(); i++) {
                        if(artsAdapter.getItems().get(i).isChosenForAddToFolder()) countChosen = countChosen + 1;;
                    }
                    items_count.setText(String.valueOf(countChosen));

                    if(countChosen > 0 && title_edit_text.getText().length() > 0) {
                        floatingActionButton.setImageDrawable(res.getDrawable(R.drawable.ic_baseline_playlist_add_check_100));
                    } else {
                        floatingActionButton.setImageDrawable(res.getDrawable(R.drawable.ic_baseline_playlist_add_check_100_black));
                    }
                }
            }
        });
        createFolderViewModel.getIsLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
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
                if(art.isChosenForAddToFolder()) art.setChosenForAddToFolder(false);
                else art.setChosenForAddToFolder(true);

                ArrayList<Art> artListNew = new ArrayList<>();
                artListNew.addAll(artsAdapter.getItems());
                artListNew.remove(position);
                artListNew.add(position, art);

                ArtsAdapter.ItemDiffUtilCallback itemDiffUtilCallback = new ArtsAdapter.ItemDiffUtilCallback(artsAdapter.getItems(), artListNew);
                DiffUtil.DiffResult itemDiffResult = DiffUtil.calculateDiff(itemDiffUtilCallback);
                artsAdapter.clearItems();
                artsAdapter.setItems(artListNew);
                itemDiffResult.dispatchUpdatesTo(artsAdapter);

                countChosen = 0;
                for(int i = 0; i < artListNew.size(); i++) {
                    if(artListNew.get(i).isChosenForAddToFolder()) countChosen = countChosen + 1;
                }
                items_count.setText(String.valueOf(countChosen));

                if(countChosen > 0 && title_edit_text.getText().length() > 0) {
                    floatingActionButton.setImageDrawable(res.getDrawable(R.drawable.ic_baseline_playlist_add_check_100));
                } else {
                    floatingActionButton.setImageDrawable(res.getDrawable(R.drawable.ic_baseline_playlist_add_check_100_black));
                }
            }
        };
        artsAdapter = new ArtsAdapter(getContext(), onArtClickListener, displayWidth, displayHeight, spanCount);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), spanCount);
        recycler_view_items.setLayoutManager(layoutManager);
        recycler_view_items.setAdapter(artsAdapter);

        recycler_view_items.addOnScrollListener(new RecyclerView.OnScrollListener() {

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
                            ObjectAnimator.ofFloat(title_layout, View.ALPHA, 1.0f, 0f)
                    );
                    set.addListener(fadeOutListener());
                    set.start();

                } else if (dy < -5) {

                    if (title_layout.getAlpha() == 0) {
                        title_layout.setVisibility(View.VISIBLE);
                        AnimatorSet set = new AnimatorSet();
                        set.setDuration(300).playTogether(
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
                title_layout.setVisibility(View.GONE);
            }
        };
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == floatingActionButton.getId()) {
            if(countChosen > 0 && title_edit_text.getText().length() > 0) {

                ArrayList<Art> artList = new ArrayList<>();
                for(int i = 0; i < artsAdapter.getItems().size(); i++) {
                    if(artsAdapter.getItems().get(i).isChosenForAddToFolder()) artList.add(artsAdapter.getItems().get(i));;
                }

                Folder folder = new Folder(title_edit_text.getText().toString(), folderUniqueId,
                        getContext().getSharedPreferences(Constants.TAG,0).getString(Constants.USER_UNIQUE_ID,""),
                        switch_isPublic.isChecked(), artList);

                createFolderViewModel.createFolder(folder, activity);

            } else if (title_edit_text.getText().length() == 0) {
                title_layout.setVisibility(View.VISIBLE);
                title_layout.setAlpha(1f);
                title_edit_text.requestFocus();
                InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                Toast.makeText(getContext(), res.getString(R.string.specify_folder_title_please), Toast.LENGTH_LONG).show();
            } else if (countChosen == 0) {
                Toast.makeText(getContext(), res.getString(R.string.choose_at_least_one_image_please), Toast.LENGTH_LONG).show();
            }
        } else if (view.getId() == back_button.getId()) {
            if(countChosen > 0) {
                showDialog();
            } else {
                NavHostFragment.findNavController(CreateFolderFragment.this).popBackStack();
            }
        }
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = this.getLayoutInflater();
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.dialog_back_create_folder, null);
        builder.setView(view);
        builder.setTitle(res.getString(R.string.want_to_exit));
        builder.setPositiveButton(res.getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                NavHostFragment.findNavController(CreateFolderFragment.this).popBackStack();
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

}

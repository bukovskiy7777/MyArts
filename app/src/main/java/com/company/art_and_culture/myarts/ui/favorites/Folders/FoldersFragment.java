package com.company.art_and_culture.myarts.ui.favorites.Folders;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.company.art_and_culture.myarts.R;

public class FoldersFragment extends Fragment {

    private FoldersViewModel foldersViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_favorites_folder, container, false);

        foldersViewModel =new ViewModelProvider(this).get(FoldersViewModel.class);

        return root;
    }


}

package com.company.art_and_culture.myarts.attribute_fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.filter_maker_fragment.DateAdapter;
import com.company.art_and_culture.myarts.filter_maker_fragment.FilterAdapter;
import com.company.art_and_culture.myarts.filter_maker_fragment.FilterMakerAdapter;
import com.company.art_and_culture.myarts.filter_maker_fragment.FilterMakerViewModel;
import com.company.art_and_culture.myarts.filter_maker_fragment.SpeedyFilterLayoutManager;
import com.company.art_and_culture.myarts.pojo.Attribute;
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

public class AttributeFragment extends Fragment {

    private RecyclerView recycler_view_attribute;
    private AttributeViewModel attributeViewModel;
    private FrameLayout title_layout;
    private TextView title_tv;
    private AttributeAdapter attributeAdapter;
    private int spanCount = 3;
    private android.content.res.Resources res;
    private AttributeEventListener attributeEventListener;
    private String attributeType;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_attribute, container, false);

        title_layout = root.findViewById(R.id.title_layout);
        title_tv = root.findViewById(R.id.title);
        recycler_view_attribute = root.findViewById(R.id.recycler_view_attribute);

        res = getResources();
        int displayWidth = res.getDisplayMetrics().widthPixels;
        int displayHeight = res.getDisplayMetrics().heightPixels;

        attributeViewModel = new ViewModelProvider(this).get(AttributeViewModel.class);

        initAttributeRecyclerView(displayWidth, displayHeight);

        MainActivity activity = (MainActivity) getActivity();
        attributeType = activity.getTypeForAttributeFragment();
        attributeViewModel.setAttributeType(attributeType);

        if(attributeType.equals(Constants.ART_CULTURE)) {
            title_tv.setText(res.getString(R.string.artist_culture));
        } else if (attributeType.equals(Constants.ART_MEDIUM)) {
            title_tv.setText(res.getString(R.string.art_mediums));
        } else if (attributeType.equals(Constants.ART_CLASSIFICATION)) {
            title_tv.setText(res.getString(R.string.art_classifications));
        }

        subscribeObservers();

        return root;
    }

    private void subscribeObservers() {

        attributeViewModel.getAttributeList().observe(this, new Observer<PagedList<Attribute>>() {
            @Override
            public void onChanged(PagedList<Attribute> attributes) {
                attributeAdapter.submitList(attributes);
            }
        });
    }

    private void initAttributeRecyclerView(int displayWidth, int displayHeight) {

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

    }


    public interface AttributeEventListener {
        void attributeClickEvent(Attribute attribute);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            attributeEventListener = (AttributeEventListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement onSomeEventListener");
        }
    }


}

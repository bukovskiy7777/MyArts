package com.company.art_and_culture.myarts.attribute_fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.pojo.Attribute;

public class AttributeFragment extends Fragment {

    private RecyclerView recycler_view_attribute;
    private AttributeViewModel attributeViewModel;
    private FrameLayout title_layout;
    private TextView title_tv;
    private ProgressBar download_progress;
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
        download_progress = root.findViewById(R.id.download_progress);

        res = getResources();
        int displayWidth = res.getDisplayMetrics().widthPixels;
        int displayHeight = res.getDisplayMetrics().heightPixels;

        attributeViewModel = new ViewModelProvider(this).get(AttributeViewModel.class);

        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            attributeType = activity.getNavFragments().getTypeForAttributeFragment();
            attributeViewModel.setAttributeType(attributeType);
        } else {
            activity.getNavFragments().popBackStack();
        }

        attributeEventListener = activity.getNavFragments();

        if(attributeType.equals(Constants.ART_CULTURE)) {
            title_tv.setText(res.getString(R.string.artist_culture));
        } else if (attributeType.equals(Constants.ART_MEDIUM)) {
            title_tv.setText(res.getString(R.string.art_mediums));
        } else if (attributeType.equals(Constants.ART_CLASSIFICATION)) {
            title_tv.setText(res.getString(R.string.art_classifications));
        }

        if(attributeType.equals(Constants.ART_CLASSIFICATION)) {
            spanCount = 1;
        } else {
            spanCount = 3;
        }

        initAttributeRecyclerView(displayWidth, displayHeight, spanCount);

        subscribeObservers();

        return root;
    }

    private void subscribeObservers() {

        attributeViewModel.getAttributeList().observe(getViewLifecycleOwner(), new Observer<PagedList<Attribute>>() {
            @Override
            public void onChanged(PagedList<Attribute> attributes) {
                attributeAdapter.submitList(attributes);
            }
        });
        attributeViewModel.getIsLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) { download_progress.setVisibility(View.VISIBLE); } else { download_progress.setVisibility(View.GONE); }
            }
        });
    }

    private void initAttributeRecyclerView(int displayWidth, int displayHeight, int spanCount) {

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

}

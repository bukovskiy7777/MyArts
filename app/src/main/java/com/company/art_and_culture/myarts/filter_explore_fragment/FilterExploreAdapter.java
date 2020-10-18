package com.company.art_and_culture.myarts.filter_explore_fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.art_medium_fragment.MediumViewModel;
import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.pojo.ExploreObject;
import com.company.art_and_culture.myarts.ui.home.LifecycleViewHolder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

public class FilterExploreAdapter extends PagedListAdapter<ExploreObject, FilterExploreAdapter.FilterExploreViewHolder> {

    private Context context;
    private OnExploreClickListener onExploreClickListener;
    private int displayWidth, displayHeight, spanCount;


    public FilterExploreAdapter(Context context, OnExploreClickListener onExploreClickListener, int displayWidth, int displayHeight, int spanCount) {
        super(itemDiffUtilCallback);
        this.context = context;
        this.onExploreClickListener = onExploreClickListener;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
        this.spanCount = spanCount;
    }

    public interface OnExploreClickListener {
        void onExploreClick(ExploreObject exploreObject, int position);
    }

    @NonNull
    @Override
    public FilterExploreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_explore, parent, false);
        return new FilterExploreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FilterExploreViewHolder holder, int position) {
        ExploreObject exploreObject = getItem(position);
        if (exploreObject != null) {
            holder.bind(exploreObject, position);
        } else {
            // Null defines a placeholder item - PagedListAdapter will automatically invalidate
            // this row when the actual object is loaded from the database
            //holder.clear();
        }
    }

    class FilterExploreViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ExploreObject exploreObject;
        private int position;
        private ImageView explore_image;
        private TextView explore_text, explore_count;
        private String imageUrl = " ";
        private final Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                Picasso.get().load(imageUrl).resize(bitmap.getWidth()/spanCount,bitmap.getHeight()/spanCount).placeholder(R.color.colorSilver).into(explore_image);
            }
            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) { }
            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) { }
        };

        FilterExploreViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.getLayoutParams().width = displayWidth/spanCount;
            itemView.getLayoutParams().height = displayWidth/spanCount;
            explore_image = itemView.findViewById(R.id.explore_image);
            explore_text = itemView.findViewById(R.id.explore_text);
            explore_count = itemView.findViewById(R.id.explore_count);
            explore_image.setOnClickListener(this);
        }

        void bind(final ExploreObject exploreObject, final int position) {
            this.exploreObject = exploreObject;
            this.position = position;

            if (!exploreObject.getImageUrlSmall().equals(" ") && exploreObject.getImageUrlSmall().startsWith(context.getResources().getString(R.string.http))) {
                imageUrl = exploreObject.getImageUrlSmall();
            } else {
                imageUrl= exploreObject.getImageUrl();
            }

            explore_image.setImageDrawable(context.getResources().getDrawable(R.drawable.art_placeholder));
            if (exploreObject.getWidth() > 0) {
                int imgWidth = displayWidth/spanCount;
                int imgHeight = (exploreObject.getHeight() * imgWidth) / exploreObject.getWidth();
                Picasso.get().load(imageUrl).placeholder(R.color.colorSilver).resize(imgWidth, imgHeight).onlyScaleDown().into(explore_image);
            } else {
                Picasso.get().load(imageUrl).placeholder(R.color.colorSilver).into(target);
            }

            explore_text.setText(exploreObject.getText());
            String text = exploreObject.getArtCount()+" "+context.getResources().getString(R.string.items);
            explore_count.setText(text);

        }

        @Override
        public void onClick(View v) {

            if (v.getId() == explore_image.getId()) {
                onExploreClickListener.onExploreClick(exploreObject, position);
            }
        }

    }

    private static DiffUtil.ItemCallback<ExploreObject> itemDiffUtilCallback = new DiffUtil.ItemCallback<ExploreObject>() {

        @Override
        public boolean areItemsTheSame(@NonNull ExploreObject oldItem, @NonNull ExploreObject newItem) {
            return oldItem.getText().equals(newItem.getText()) && oldItem.getType().equals(newItem.getType());
        }

        @Override
        public boolean areContentsTheSame(@NonNull ExploreObject oldItem, @NonNull ExploreObject newItem) {
            return oldItem.getText().equals(newItem.getText()) && oldItem.getType().equals(newItem.getType())
                    && oldItem.getImageUrl().equals(newItem.getImageUrl());
        }
    };

}

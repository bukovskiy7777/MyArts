package com.company.art_and_culture.myarts.tags_fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.pojo.Attribute;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

public class TagsAdapter extends PagedListAdapter<Attribute, TagsAdapter.TagsViewHolder> {

    private Context context;
    private OnTagsClickListener onTagsClickListener;
    private int displayWidth, displayHeight, spanCount;
    private int lastPosition = -1;


    public TagsAdapter(Context context, OnTagsClickListener onTagsClickListener, int displayWidth, int displayHeight, int spanCount) {
        super(itemDiffUtilCallback);
        this.context = context;
        this.onTagsClickListener = onTagsClickListener;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
        this.spanCount = spanCount;
    }

    public interface OnTagsClickListener {
        void onTagsClick(Attribute attribute, int position);
    }

    @NonNull
    @Override
    public TagsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attribute, parent, false);
        return new TagsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TagsViewHolder holder, int position) {
        Attribute attribute = getItem(position);
        if (attribute != null) {
            holder.bind(attribute, position);
            setAnimation(holder.itemView, position);
        } else {
            // Null defines a placeholder item - PagedListAdapter will automatically invalidate
            // this row when the actual object is loaded from the database
            //holder.clear();
        }
    }

    public void setLastPosition(int lastPosition) {
        this.lastPosition = lastPosition;
    }

    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.item_fade_in);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    class TagsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Attribute attribute;
        private int position;
        private ImageView attribute_image;
        private TextView attribute_text, art_count;
        private String imageUrl = " ";
        private final Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                Picasso.get().load(imageUrl).resize(bitmap.getWidth()/spanCount,bitmap.getHeight()/spanCount).placeholder(R.color.colorSilver).into(attribute_image);
            }
            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) { }
            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) { }
        };

        TagsViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.getLayoutParams().width = displayWidth/spanCount;
            itemView.getLayoutParams().height = displayWidth/spanCount;
            attribute_image = itemView.findViewById(R.id.attribute_image);
            attribute_text = itemView.findViewById(R.id.attribute_text);
            art_count = itemView.findViewById(R.id.art_count);
            attribute_image.setOnClickListener(this);
        }

        void bind(final Attribute attribute, final int position) {
            this.attribute = attribute;
            this.position = position;

            attribute_image.setImageDrawable(context.getResources().getDrawable(R.drawable.art_placeholder));
            imageUrl= attribute.getArtImageUrl();
            if (attribute.getArtWidth() > 0) {
                int imgWidth = displayWidth/spanCount;
                int imgHeight = (attribute.getArtHeight() * imgWidth) / attribute.getArtWidth();
                Picasso.get().load(imageUrl).placeholder(R.color.colorSilver).resize(imgWidth, imgHeight).onlyScaleDown().into(attribute_image);
            } else {
                Picasso.get().load(imageUrl).placeholder(R.color.colorSilver).into(target);
            }

            attribute_text.setText(attribute.getText());
            if (attribute.getItemsCount() > 0) {
                art_count.setVisibility(View.VISIBLE);
                String text = attribute.getItemsCount()+" "+context.getResources().getString(R.string.items);
                art_count.setText(text);
            } else {
                art_count.setVisibility(View.INVISIBLE);
            }


        }

        @Override
        public void onClick(View v) {

            if (v.getId() == attribute_image.getId()) {
                onTagsClickListener.onTagsClick(attribute, position);
            }
        }

    }

    private static DiffUtil.ItemCallback<Attribute> itemDiffUtilCallback = new DiffUtil.ItemCallback<Attribute>() {

        @Override
        public boolean areItemsTheSame(@NonNull Attribute oldItem, @NonNull Attribute newItem) {
            return oldItem.getText().equals(newItem.getText());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Attribute oldItem, @NonNull Attribute newItem) {
            return oldItem.getText().equals(newItem.getText()) && oldItem.getArtImageUrl().equals(newItem.getArtImageUrl());
        }
    };

}

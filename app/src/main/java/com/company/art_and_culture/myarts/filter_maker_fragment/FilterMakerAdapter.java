package com.company.art_and_culture.myarts.filter_maker_fragment;

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
import com.company.art_and_culture.myarts.pojo.Maker;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

public class FilterMakerAdapter extends PagedListAdapter<Maker, FilterMakerAdapter.FilterMakerViewHolder> {

    private Context context;
    private OnMakerClickListener onMakerClickListener;
    private int displayWidth, displayHeight, spanCount;
    private int lastPosition = -1;


    public FilterMakerAdapter(Context context, OnMakerClickListener onMakerClickListener, int displayWidth, int displayHeight, int spanCount) {
        super(itemDiffUtilCallback);
        this.context = context;
        this.onMakerClickListener = onMakerClickListener;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
        this.spanCount = spanCount;
    }

    public interface OnMakerClickListener {
        void onMakerClick(Maker maker, int position);
    }

    @NonNull
    @Override
    public FilterMakerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_filter_maker, parent, false);
        return new FilterMakerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FilterMakerViewHolder holder, int position) {
        Maker maker = getItem(position);
        if (maker != null) {
            holder.bind(maker, position);
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

    class FilterMakerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Maker maker;
        private int position;
        private ImageView maker_image;
        private TextView maker_name, art_count;
        private String imageUrl = " ";
        private final Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                Picasso.get().load(imageUrl).resize(bitmap.getWidth()/spanCount,bitmap.getHeight()/spanCount).placeholder(R.color.colorSilver).into(maker_image);
            }
            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) { }
            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) { }
        };

        FilterMakerViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.getLayoutParams().width = displayWidth/spanCount;
            itemView.getLayoutParams().height = displayWidth/spanCount;
            maker_image = itemView.findViewById(R.id.maker_image);
            maker_name = itemView.findViewById(R.id.maker_name);
            art_count = itemView.findViewById(R.id.art_count);
            maker_image.setOnClickListener(this);
        }

        void bind(final Maker maker, final int position) {
            this.maker = maker;
            this.position = position;

            maker_image.setImageDrawable(context.getResources().getDrawable(R.drawable.art_placeholder));
            if (maker.getMakerWikiImageUrl() != null && maker.getMakerWikiImageUrl().startsWith(context.getResources().getString(R.string.http))) {
                imageUrl = maker.getMakerWikiImageUrl();
                Picasso.get().load(imageUrl).placeholder(R.color.colorSilver).into(maker_image);
            } else {
                imageUrl= maker.getArtHeaderImageUrl();

                if (maker.getArtWidth() > 0) {
                    int imgWidth = displayWidth/spanCount;
                    int imgHeight = (maker.getArtHeight() * imgWidth) / maker.getArtWidth();
                    Picasso.get().load(imageUrl).placeholder(R.color.colorSilver).resize(imgWidth, imgHeight).onlyScaleDown().into(maker_image);
                } else {
                    Picasso.get().load(imageUrl).placeholder(R.color.colorSilver).into(target);
                }
            }

            maker_name.setText(maker.getArtMaker());
            String text = maker.getArtCount()+" "+context.getResources().getString(R.string.items);
            art_count.setText(text);

        }

        @Override
        public void onClick(View v) {

            if (v.getId() == maker_image.getId()) {
                onMakerClickListener.onMakerClick(maker, position);
            }
        }

    }

    private static DiffUtil.ItemCallback<Maker> itemDiffUtilCallback = new DiffUtil.ItemCallback<Maker>() {

        @Override
        public boolean areItemsTheSame(@NonNull Maker oldItem, @NonNull Maker newItem) {
            return oldItem.getArtMaker().equals(newItem.getArtMaker());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Maker oldItem, @NonNull Maker newItem) {
            return oldItem.getArtMaker().equals(newItem.getArtMaker()) && oldItem.getArtHeaderImageUrl().equals(newItem.getArtHeaderImageUrl());
        }
    };

}

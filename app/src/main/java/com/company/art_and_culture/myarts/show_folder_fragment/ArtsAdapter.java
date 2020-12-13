package com.company.art_and_culture.myarts.show_folder_fragment;

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
import com.company.art_and_culture.myarts.pojo.Art;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

public class ArtsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private OnArtClickListener onArtClickListener;
    private int displayWidth, displayHeight, spanCount;
    private double k = 0.4;
    private List<Art> artList=new ArrayList<>();
    private int lastPosition = -1;

    public ArtsAdapter(Context context, OnArtClickListener onArtClickListener, int displayWidth, int displayHeight,
                       int spanCount) {
        this.context = context;
        this.onArtClickListener = onArtClickListener;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
        this.spanCount = spanCount;
    }

    public interface OnArtClickListener {
        void onArtImageClick(Art art, int position);
    }

    public void setItems(Collection<Art> arts){
        artList.addAll(arts);
        notifyDataSetChanged();
    }

    public List<Art> getItems() {
        return artList;
    }

    public void clearItems(){
        artList.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_art_add_to_folder, parent, false);
        return new ArtViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Art art = artList.get(position);
        if (art != null) {
            ArtViewHolder viewHolder = (ArtViewHolder) holder;
            viewHolder.bind(art, position);
            //setAnimation(holder.itemView, position);
        } else {
            // Null defines a placeholder item - PagedListAdapter will automatically invalidate
            // this row when the actual object is loaded from the database
            // holder.clear();
        }
    }

    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.item_fade_in);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        holder.itemView.clearAnimation();
    }

    @Override
    public int getItemCount() {
        return artList.size();
    }

    class ArtViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Art art;
        private int position;
        private final ImageView art_image;
        private final TextView art_maker;
        private final View choose_view, circle_choose_view;
        private final View color_filter_view;
        private String artImgUrl = "";
        private final Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                Picasso.get().load(artImgUrl).placeholder(R.color.colorSilver).into(art_image);
            }
            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) { }
            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) { }
        };

        ArtViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.getLayoutParams().width = displayWidth/spanCount;
            itemView.getLayoutParams().height = displayWidth/spanCount;
            art_image = itemView.findViewById(R.id.art_image);
            art_maker = itemView.findViewById(R.id.art_maker);
            choose_view = itemView.findViewById(R.id.choose_view);
            circle_choose_view = itemView.findViewById(R.id.circle_choose_view);
            color_filter_view = itemView.findViewById(R.id.color_filter_view);
            art_image.setOnClickListener(this);
        }

        void bind(Art art, int position) {
            this.art = art;
            this.position = position;

            if (art.getArtImgUrlSmall() != null && art.getArtImgUrlSmall().startsWith(context.getResources().getString(R.string.http))) {
                artImgUrl = art.getArtImgUrlSmall();
            } else {
                artImgUrl= art.getArtImgUrl();
            }

            art_image.setImageDrawable(context.getResources().getDrawable(R.drawable.art_placeholder));
            if (art.getArtWidth() > 0) {
                int imgWidth = displayWidth/spanCount;
                int imgHeight = (art.getArtHeight() * imgWidth) / art.getArtWidth();
                Picasso.get().load(artImgUrl).placeholder(R.color.colorSilver).resize(imgWidth, imgHeight).onlyScaleDown().into(art_image);
            } else {
                Picasso.get().load(artImgUrl).placeholder(R.color.colorSilver).into(target);
            }

            art_maker.setText(art.getArtMaker());

            choose_view.setVisibility(View.GONE);
            color_filter_view.setVisibility(View.GONE);
            circle_choose_view.setVisibility(View.GONE);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == art_image.getId()) {
                onArtClickListener.onArtImageClick(art, position);
            }
        }
    }

    public static class ItemDiffUtilCallback extends DiffUtil.Callback {
        private final List<Art> oldList;
        private final List<Art> newList;

        public ItemDiffUtilCallback(List<Art> oldList, List<Art> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            Art oldArt = oldList.get(oldItemPosition);
            Art newArt = newList.get(newItemPosition);
            return oldArt.getArtId().equals(newArt.getArtId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Art oldArt = oldList.get(oldItemPosition);
            Art newArt = newList.get(newItemPosition);

            return oldArt.getArtId().equals(newArt.getArtId()) && oldArt.isChosenForAddToFolder() == newArt.isChosenForAddToFolder();
        }
    }

}

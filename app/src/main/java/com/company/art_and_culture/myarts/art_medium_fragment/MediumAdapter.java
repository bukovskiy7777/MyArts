package com.company.art_and_culture.myarts.art_medium_fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.ui.home.LifecycleViewHolder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;

public class MediumAdapter extends PagedListAdapter<Art, MediumAdapter.MediumViewHolder> {

    private Context context;
    private OnArtClickListener onArtClickListener;
    private int displayWidth, displayHeight, spanCount;
    private MediumViewModel mediumViewModel;


    public MediumAdapter(MediumViewModel mediumViewModel, Context context, OnArtClickListener onArtClickListener, int displayWidth, int displayHeight, int spanCount) {
        super(itemDiffUtilCallback);
        this.context = context;
        this.mediumViewModel = mediumViewModel;
        this.onArtClickListener = onArtClickListener;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
        this.spanCount = spanCount;
    }

    @Override
    public void onViewAttachedToWindow(@NonNull MediumViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        holder.onAttached();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull MediumViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.onDetached();
    }

    public interface OnArtClickListener {
        void onArtImageClick(Art art, int position);
    }

    @NonNull
    @Override
    public MediumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medium, parent, false);
        return new MediumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediumViewHolder holder, int position) {
        Art art = getItem(position);
        if (art != null) {
            holder.bind(art, position);
        } else {
            // Null defines a placeholder item - PagedListAdapter will automatically invalidate
            // this row when the actual object is loaded from the database
            //holder.clear();
        }
    }

    class MediumViewHolder extends LifecycleViewHolder implements View.OnClickListener {

        private Art art;
        private int position;
        private ImageView art_image;
        private String artImgUrl;
        private final Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                int imgWidth = displayWidth/spanCount;
                int imgHeight = (bitmap.getHeight() * imgWidth) / bitmap.getWidth();
                if (imgHeight <= art_image.getMaxHeight()) {
                    art_image.getLayoutParams().height = imgHeight;
                } else {
                    art_image.getLayoutParams().height = art_image.getMaxHeight();
                }
                art.setArtWidth(bitmap.getWidth());
                art.setArtHeight(bitmap.getHeight());
                mediumViewModel.writeDimentionsOnServer(art);
                Picasso.get().load(artImgUrl).placeholder(R.color.colorSilver).into(art_image);
            }
            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) { }
            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) { }
        };

        MediumViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.getLayoutParams().width = displayWidth/spanCount;
            //itemView.getLayoutParams().height = displayWidth/spanCount;
            art_image = itemView.findViewById(R.id.art_image);
            art_image.setOnClickListener(this);
        }

        void bind(final Art art, final int position) {
            this.art = art;
            this.position = position;

            if (!art.getArtImgUrlSmall().equals(" ") && art.getArtImgUrlSmall().startsWith(context.getResources().getString(R.string.http))) {
                artImgUrl = art.getArtImgUrlSmall();
            } else {
                artImgUrl= art.getArtImgUrl();
            }

            art_image.setImageDrawable(context.getResources().getDrawable(R.drawable.art_placeholder));
            if (art.getArtWidth() > 0) {
                int imgWidth = displayWidth/spanCount;
                int imgHeight = (art.getArtHeight() * imgWidth) / art.getArtWidth();
                if (imgHeight <= art_image.getMaxHeight()) {
                    art_image.getLayoutParams().height = imgHeight;
                } else {
                    art_image.getLayoutParams().height = art_image.getMaxHeight();
                }
                Picasso.get().load(artImgUrl).placeholder(R.color.colorSilver).resize(imgWidth, imgHeight).onlyScaleDown().into(art_image);
            } else {
                art_image.getLayoutParams().height = displayWidth/spanCount;
                Picasso.get().load(artImgUrl).placeholder(R.color.colorSilver).into(target);
            }

        }

        @Override
        public void onClick(View v) {

            if (v.getId() == art_image.getId()) {
                onArtClickListener.onArtImageClick(art, position);

            }
        }

    }

    private static DiffUtil.ItemCallback<Art> itemDiffUtilCallback = new DiffUtil.ItemCallback<Art>() {

        @Override
        public boolean areItemsTheSame(@NonNull Art oldItem, @NonNull Art newItem) {
            return oldItem.getArtId().equals(newItem.getArtId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Art oldItem, @NonNull Art newItem) {
            return oldItem.getArtId().equals(newItem.getArtId()) && oldItem.getIsLiked() == newItem.getIsLiked();
        }
    };

}

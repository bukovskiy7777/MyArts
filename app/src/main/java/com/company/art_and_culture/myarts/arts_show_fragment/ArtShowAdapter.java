package com.company.art_and_culture.myarts.arts_show_fragment;

import android.animation.AnimatorSet;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.ui.home.LifecycleViewHolder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import static com.company.art_and_culture.myarts.ui.home.HomeAnimations.likeFadeIn;
import static com.company.art_and_culture.myarts.ui.home.HomeAnimations.likeScaleDown;
import static com.company.art_and_culture.myarts.ui.home.HomeAnimations.shareScaleDown;
import static com.company.art_and_culture.myarts.ui.home.HomeAnimations.shareScaleUp;

public class ArtShowAdapter extends RecyclerView.Adapter<ArtShowAdapter.ArtShowViewHolder> {

    private Context context;
    private OnArtClickListener onArtClickListener;
    private int displayWidth, displayHeight;
    private ArtShowViewModel artShowViewModel;
    private List<Art> artList=new ArrayList<>();


    public ArtShowAdapter(ArtShowViewModel artShowViewModel, Context context, OnArtClickListener onArtClickListener, int displayWidth, int displayHeight) {
        this.context = context;
        this.artShowViewModel = artShowViewModel;
        this.onArtClickListener = onArtClickListener;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ArtShowViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        holder.onAttached();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ArtShowViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.onDetached();
    }

    public interface OnArtClickListener {
        void onArtLikeClick(Art art, int position);
        void onArtShareClick(Art art);
        void onArtDownloadClick(Art art, int x, int y, int artWidth, int artHeight);
        void onLogoClick(Art art);
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
    public ArtShowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_art_show, parent, false);
        return new ArtShowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtShowViewHolder holder, int position) {
        Art art = artList.get(position);
        if (art != null) {
            holder.bind(art, position);
        } else {
            // Null defines a placeholder item - PagedListAdapter will automatically invalidate
            // this row when the actual object is loaded from the database
            // holder.clear();
        }
    }

    @Override
    public int getItemCount() {
        return artList.size();
    }

    class ArtShowViewHolder extends LifecycleViewHolder implements View.OnClickListener {

        private Art art;
        private int position;
        private ImageView art_image, logo_image;
        private TextView art_maker, art_title, art_description;
        private ImageButton art_share, art_download, art_like, button_go_to_museum;
        private String artImgUrl;
        private ConstraintLayout art_header, art_footer;
        private int artWidth, artHeight;
        int imgWidth, imgHeight;
        private final Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                artWidth = bitmap.getWidth();
                artHeight = bitmap.getHeight();

                Picasso.get().load(artImgUrl).placeholder(R.color.colorSilver).into(art_image);
            }
            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) { }
            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) { }
        };

        ArtShowViewHolder(@NonNull View itemView) {
            super(itemView);
            art_image = itemView.findViewById(R.id.art_image);

            art_header = itemView.findViewById(R.id.art_header);
            logo_image = itemView.findViewById(R.id.logo_image);
            button_go_to_museum = itemView.findViewById(R.id.button_go_to_museum);
            art_title = itemView.findViewById(R.id.art_title);

            art_footer = itemView.findViewById(R.id.art_footer);
            art_maker = itemView.findViewById(R.id.art_maker);
            art_description = itemView.findViewById(R.id.art_description);
            art_share = itemView.findViewById(R.id.art_share);
            art_download = itemView.findViewById(R.id.art_download);
            art_like = itemView.findViewById(R.id.art_like);

            art_image.setOnClickListener(this);
            art_share.setOnClickListener(this);
            art_download.setOnClickListener(this);
            art_like.setOnClickListener(this);
            logo_image.setOnClickListener(this);
            button_go_to_museum.setOnClickListener(this);
        }

        void bind(final Art art, final int position) {
            this.art = art;
            this.position = position;

            art_header.setVisibility(View.VISIBLE);
            art_footer.setVisibility(View.VISIBLE);

            art_title.setText(art.getArtTitle());
            art_maker.setText(art.getArtMaker());
            art_description.setText(art.getArtLongTitle());

            Picasso.get().load(art.getArtLogoUrl()).into(logo_image);

            if(ArtShowDataInMemory.getInstance().getSingleItem(position).getIsLiked()){
                art_like.setImageResource(R.drawable.ic_favorite_red_100dp);
                art_like.setScaleType(ImageView.ScaleType.FIT_CENTER);
            } else {
                art_like.setImageResource(R.drawable.ic_favorite_border_black_100dp);
                art_like.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }

            art_image.setImageDrawable(context.getResources().getDrawable(R.drawable.art_placeholder));

            if (art.getArtWidth() > 0) {
                artImgUrl= art.getArtImgUrl();

                artWidth = art.getArtWidth();
                artHeight = art.getArtHeight();
                if (art.getArtWidth() > art.getArtHeight()) {
                    imgWidth = 1600;
                    imgHeight = (art.getArtHeight() * imgWidth) / art.getArtWidth();
                } else {
                    imgHeight = 1600;
                    imgWidth = (art.getArtWidth() * imgHeight) / art.getArtHeight();
                }
                Picasso.get().load(artImgUrl).placeholder(R.color.colorSilver).resize(imgWidth, imgHeight).onlyScaleDown().into(art_image);
            } else {
                artImgUrl = art.getArtImgUrlSmall();
                if (!artImgUrl.equals(" ")) {
                    Picasso.get().load(artImgUrl).placeholder(R.color.colorSilver).into(target);
                }
            }

            artShowViewModel.getArt().observe(this, new Observer<Art>() {
                @Override
                public void onChanged(Art newArt) {
                    if (newArt.getArtId().equals(art.getArtId()) && newArt.getArtProvider().equals(art.getArtProvider())) {
                        if(newArt.getIsLiked()){
                            art_like.setImageResource(R.drawable.ic_favorite_red_100dp);
                            art_like.setScaleType(ImageView.ScaleType.FIT_CENTER);
                            AnimatorSet set = new AnimatorSet();
                            set.playSequentially(likeFadeIn(art_like), likeScaleDown(art_like));
                            set.start();
                        } else {
                            art_like.setImageResource(R.drawable.ic_favorite_border_black_100dp);
                            art_like.setScaleType(ImageView.ScaleType.FIT_CENTER);
                            AnimatorSet set = new AnimatorSet();
                            set.playSequentially(likeFadeIn(art_like), likeScaleDown(art_like));
                            set.start();
                        }
                    }
                }
            });
        }

        @Override
        public void onClick(View v) {

            if (v.getId() == art_share.getId()) {
                onArtClickListener.onArtShareClick(art);
                AnimatorSet set = new AnimatorSet();
                set.playSequentially(shareScaleUp(art_share), shareScaleDown(art_share));
                set.start();

            } else if (v.getId() == art_download.getId()) {
                int[] location = new int[2];
                art_download.getLocationOnScreen(location);
                int x = location[0];
                int y = location[1];
                onArtClickListener.onArtDownloadClick(art, x, y, artWidth, artHeight);

            } else if (v.getId() == art_like.getId()) {
                onArtClickListener.onArtLikeClick(art, position);

            } else if (v.getId() == logo_image.getId() || v.getId() == button_go_to_museum.getId()) {
                onArtClickListener.onLogoClick(art);

            } else if (v.getId() == art_image.getId()) {
                if (art_header.isShown()) {
                    art_header.setVisibility(View.GONE);
                    art_footer.setVisibility(View.GONE);
                } else {
                    art_header.setVisibility(View.VISIBLE);
                    art_footer.setVisibility(View.VISIBLE);
                }
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

            return oldArt.getArtId().equals(newArt.getArtId()) && oldArt.getIsLiked() == newArt.getIsLiked();
        }
    }

}

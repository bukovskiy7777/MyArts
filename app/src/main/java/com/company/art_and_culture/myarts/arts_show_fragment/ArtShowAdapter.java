package com.company.art_and_culture.myarts.arts_show_fragment;

import android.animation.AnimatorSet;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.company.art_and_culture.myarts.LifecycleViewHolder;
import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.pojo.Art;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.company.art_and_culture.myarts.CommonAnimations.shareScaleDown;
import static com.company.art_and_culture.myarts.CommonAnimations.shareScaleUp;
import static com.company.art_and_culture.myarts.CommonAnimations.startLikeAnimations;

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
        void onArtDownloadClick(Art art, int x, int y);
        void onLogoClick(Art art);
        void onMakerClick(Art art, View view);
        void onSaveToFolderClick(Art art);
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
        private TextView art_maker, art_title, art_description, art_classification;
        private ImageButton art_share, art_download, art_like, button_go_to_museum;
        private ConstraintLayout liked_layout;
        private boolean isLikeClicked = false;
        private ImageView liked_image;
        private TextView save_to_folder;
        private String artImgUrl;
        private ConstraintLayout art_header, art_footer;
        private int artWidth, artHeight;
        int imgWidth, imgHeight;

        ArtShowViewHolder(@NonNull View itemView) {
            super(itemView);
            art_image = itemView.findViewById(R.id.art_image);

            art_header = itemView.findViewById(R.id.art_header);
            logo_image = itemView.findViewById(R.id.logo_image);
            button_go_to_museum = itemView.findViewById(R.id.button_go_to_museum);
            art_title = itemView.findViewById(R.id.art_title);

            art_footer = itemView.findViewById(R.id.art_footer);
            art_classification = itemView.findViewById(R.id.art_classification);
            art_maker = itemView.findViewById(R.id.art_maker);
            art_description = itemView.findViewById(R.id.art_description);
            art_share = itemView.findViewById(R.id.art_share);
            art_download = itemView.findViewById(R.id.art_download);
            art_like = itemView.findViewById(R.id.art_like);
            liked_image = itemView.findViewById(R.id.liked_image);
            save_to_folder = itemView.findViewById(R.id.save_to_folder);
            liked_layout = itemView.findViewById(R.id.liked_layout);
            liked_layout.setVisibility(View.GONE);

            art_maker.setOnClickListener(this);
            art_image.setOnClickListener(this);
            art_share.setOnClickListener(this);
            art_download.setOnClickListener(this);
            art_like.setOnClickListener(this);
            logo_image.setOnClickListener(this);
            button_go_to_museum.setOnClickListener(this);
            save_to_folder.setOnClickListener(this);
        }

        void bind(final Art art, final int position) {
            this.art = art;
            this.position = position;

            art_header.setVisibility(View.VISIBLE);
            art_footer.setVisibility(View.VISIBLE);

            art_title.setText(art.getArtTitle());
            art_classification.setText(art.getArtClassification());
            art_maker.setText(art.getArtMaker());
            art_description.setText(art.getArtLongTitle());

            Picasso.get().load(art.getArtLogoUrl()).into(logo_image);

            if(ArtShowDataInMemory.getInstance().getSingleItem(position) != null && ArtShowDataInMemory.getInstance().getSingleItem(position).getIsLiked()){
                art_like.setImageResource(R.drawable.ic_favorite_red_100dp);
                art_like.setScaleType(ImageView.ScaleType.FIT_CENTER);
            } else {
                art_like.setImageResource(R.drawable.ic_favorite_border_black_100dp);
                art_like.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }

            artImgUrl= art.getArtImgUrl();

            if (art.getArtWidth() > 0) {

                artWidth = art.getArtWidth();
                artHeight = art.getArtHeight();
                if (art.getArtWidth() > art.getArtHeight()) {
                    imgWidth = 1600;
                    imgHeight = (art.getArtHeight() * imgWidth) / art.getArtWidth();
                } else {
                    imgHeight = 1600;
                    imgWidth = (art.getArtWidth() * imgHeight) / art.getArtHeight();
                }
                Picasso.get().load(artImgUrl).resize(imgWidth, imgHeight).onlyScaleDown().into(art_image);
            } else {
                Picasso.get().load(artImgUrl).into(art_image);
            }

            MainActivity activity = (MainActivity) context;
            activity.getArt().observe(this, newArt -> {
                if (newArt.getArtId().equals(art.getArtId()) && newArt.getArtProvider().equals(art.getArtProvider())) {
                    startLikeAnimations(newArt, art_like, liked_layout, isLikeClicked);
                }
            });

            liked_image.setImageDrawable(context.getResources().getDrawable(R.drawable.art_placeholder));
            if (art.getArtWidth() > 0) {
                int imgWidth = displayWidth/4;
                int imgHeight = (art.getArtHeight() * imgWidth) / art.getArtWidth();
                Picasso.get().load(artImgUrl).resize(imgWidth, imgHeight).onlyScaleDown().into(liked_image);
            } else {
                Picasso.get().load(artImgUrl).into(liked_image);
            }
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
                onArtClickListener.onArtDownloadClick(art, x, y);

            } else if (v.getId() == art_like.getId()) {
                onArtClickListener.onArtLikeClick(art, position);
                isLikeClicked = true;

            } else if (v.getId() == logo_image.getId() || v.getId() == button_go_to_museum.getId()) {
                onArtClickListener.onLogoClick(art);

            } else if (v.getId() == save_to_folder.getId()) {
                onArtClickListener.onSaveToFolderClick(art);

            } else if (v.getId() == art_image.getId()) {
                if (art_header.isShown()) {
                    art_header.setVisibility(View.GONE);
                    art_footer.setVisibility(View.GONE);
                } else {
                    art_header.setVisibility(View.VISIBLE);
                    art_footer.setVisibility(View.VISIBLE);
                }

            } else if (v.getId() == art_maker.getId()) {
                onArtClickListener.onMakerClick(art, v);
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

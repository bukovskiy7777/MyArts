package com.company.art_and_culture.myarts.bottom_menu.recommendations;

import android.animation.AnimatorSet;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.company.art_and_culture.myarts.LifecycleViewHolder;
import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.pojo.Art;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.company.art_and_culture.myarts.CommonAnimations.shareScaleDown;
import static com.company.art_and_culture.myarts.CommonAnimations.shareScaleUp;
import static com.company.art_and_culture.myarts.CommonAnimations.startLikeAnimations;

public class RecommendAdapter extends RecyclerView.Adapter<RecommendAdapter.RecommendationsViewHolder> {

    private Context context;
    private OnArtClickListener onArtClickListener;
    private int displayWidth, displayHeight;
    private RecommendViewModel recommendViewModel;
    private int lastPosition = -1;
    private List<Art> artList=new ArrayList<>();


    public RecommendAdapter(RecommendViewModel recommendViewModel, Context context,
                            OnArtClickListener onArtClickListener, int displayWidth, int displayHeight) {
        this.context = context;
        this.recommendViewModel = recommendViewModel;
        this.onArtClickListener = onArtClickListener;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RecommendationsViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        holder.onAttached();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecommendationsViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.onDetached();
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

    public interface OnArtClickListener {
        void onArtImageClick(Art art, int position);
        void onArtLikeClick(Art art, int position);
        void onArtShareClick(Art art);
        void onArtDownloadClick(Art art, int x, int y, int viewWidth, int viewHeight);
        void onArtMakerClick(Art art);
        void onArtClassificationClick(Art art);
        void onSaveToFolderClick(Art art);
    }

    @NonNull
    @Override
    public RecommendationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recommendations, parent, false);
        return new RecommendationsViewHolder (view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecommendationsViewHolder holder, int position) {
        Art art = artList.get(position);
        if (art != null) {
            holder.bind(art, position);
            setAnimation(holder.itemView, position);
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

    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.item_fade_in);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    class RecommendationsViewHolder extends LifecycleViewHolder implements View.OnClickListener {

        private Art art;
        private int position;
        private ImageView art_image, maker_image;
        private TextView art_title, art_maker, art_classification;
        private ImageButton art_share, art_download, art_like;
        private ConstraintLayout liked_layout;
        private boolean isLikeClicked = false;
        private ImageView liked_image;
        private TextView save_to_folder;
        private String artImgUrl, makerImgUrl;
        private final Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                int imgWidth = displayWidth;
                int imgHeight = (bitmap.getHeight() * imgWidth) / bitmap.getWidth();
                art_image.getLayoutParams().height = Math.min(imgHeight, art_image.getMaxHeight());

                art.setArtWidth(bitmap.getWidth());
                art.setArtHeight(bitmap.getHeight());
                recommendViewModel.writeDimentionsOnServer(art);
                Picasso.get().load(artImgUrl).placeholder(R.color.colorSilver).into(art_image);
            }
            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) { }
            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) { }
        };

        RecommendationsViewHolder(@NonNull View itemView) {
            super(itemView);

            art_title = itemView.findViewById(R.id.art_title);
            art_maker = itemView.findViewById(R.id.art_maker);
            art_classification = itemView.findViewById(R.id.art_classification);
            art_share = itemView.findViewById(R.id.art_share);
            art_download = itemView.findViewById(R.id.art_download);
            art_like = itemView.findViewById(R.id.art_like);
            art_image = itemView.findViewById(R.id.art_image);
            maker_image = itemView.findViewById(R.id.maker_image);
            liked_image = itemView.findViewById(R.id.liked_image);
            save_to_folder = itemView.findViewById(R.id.save_to_folder);
            liked_layout = itemView.findViewById(R.id.liked_layout);
            liked_layout.setVisibility(View.GONE);
            art_maker.setOnClickListener(this);
            maker_image.setOnClickListener(this);
            art_image.setOnClickListener(this);
            art_share.setOnClickListener(this);
            art_download.setOnClickListener(this);
            art_like.setOnClickListener(this);
            art_classification.setOnClickListener(this);
            save_to_folder.setOnClickListener(this);
        }

        void bind(final Art art, final int position) {
            this.art = art;
            this.position = position;

            if(art.getIsLiked()) art_like.setImageResource(R.drawable.ic_favorite_red_100dp);
            else art_like.setImageResource(R.drawable.ic_favorite_border_black_100dp);
            art_like.setScaleType(ImageView.ScaleType.FIT_CENTER);

            if (art.getArtImgUrlSmall() != null && art.getArtImgUrlSmall().startsWith(context.getResources().getString(R.string.http))) {
                artImgUrl = art.getArtImgUrlSmall();
            } else {
                artImgUrl= art.getArtImgUrl();
            }

            art_image.setImageDrawable(context.getResources().getDrawable(R.drawable.art_placeholder));
            if (art.getArtWidth() > 0) {
                int imgWidth = displayWidth;
                int imgHeight = (art.getArtHeight() * imgWidth) / art.getArtWidth();
                art_image.getLayoutParams().height = Math.min(imgHeight, art_image.getMaxHeight());
                Picasso.get().load(artImgUrl).placeholder(R.color.colorSilver).resize(imgWidth, imgHeight).onlyScaleDown().into(art_image);
            } else {
                art_image.getLayoutParams().height = displayWidth;
                Picasso.get().load(artImgUrl).placeholder(R.color.colorSilver).into(target);
            }

            if(art.getMakerImgUrl() != null && art.getMakerImgUrl().startsWith(context.getResources().getString(R.string.http))) {
                makerImgUrl = art.getMakerImgUrl();
                Picasso.get().load(makerImgUrl).placeholder(R.color.colorSilver).into(maker_image);
            } else {
                maker_image.setImageDrawable(context.getResources().getDrawable(R.drawable.maker_placeholder));
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

            art_title.setText(art.getArtLongTitle());
            art_maker.setText(art.getArtMaker());
            art_classification.setText(art.getArtClassification());
        }

        @Override
        public void onClick(View v) {

            if (v.getId() == art_image.getId()) {
                onArtClickListener.onArtImageClick(art, position);

            } else if(v.getId() == art_maker.getId() || v.getId() == maker_image.getId()) {
                onArtClickListener.onArtMakerClick(art);

            } else if (v.getId() == art_share.getId()) {
                onArtClickListener.onArtShareClick(art);
                AnimatorSet set = new AnimatorSet();
                set.playSequentially(shareScaleUp(art_share), shareScaleDown(art_share));
                set.start();

            } else if (v.getId() == art_download.getId()) {
                int[] location = new int[2];
                art_download.getLocationOnScreen(location);
                int x = location[0];
                int y = location[1];
                onArtClickListener.onArtDownloadClick(art, x, y, art.getArtWidth(), art.getArtHeight());

            } else if (v.getId() == art_like.getId()) {
                onArtClickListener.onArtLikeClick(art, position);
                isLikeClicked = true;

            } else if (v.getId() == save_to_folder.getId()) {
                onArtClickListener.onSaveToFolderClick(art);

            } else if (v.getId() == art_classification.getId()) {
                onArtClickListener.onArtClassificationClick(art);
            }
        }

    }

}

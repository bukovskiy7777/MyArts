package com.company.art_and_culture.myarts.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;

import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.pojo.Art;
import com.squareup.picasso.Picasso;

public class HomeAdapter extends PagedListAdapter<Art, HomeAdapter.HomeViewHolder> {

    private Context context;
    private OnArtClickListener onArtClickListener;
    private int displayWidth, displayHeight;
    private HomeViewModel homeViewModel;


    public HomeAdapter(HomeViewModel homeViewModel, Context context, OnArtClickListener onArtClickListener, int displayWidth, int displayHeight) {
        super(itemDiffUtilCallback);
        this.context = context;
        this.homeViewModel = homeViewModel;
        this.onArtClickListener = onArtClickListener;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
    }

    @Override
    public void onViewAttachedToWindow(@NonNull HomeViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        holder.onAttached();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull HomeViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.onDetached();
    }

    public interface OnArtClickListener {
        void onArtImageClick(Art art, int viewWidth, int viewHeight);
        void onArtMakerClick(Art art);
        void onArtClassificationClick(Art art);
        void onArtLikeClick(Art art, int position);
        void onArtShareClick(Art art);
        void onArtDownloadClick(Art art, int x, int y, int viewWidth, int viewHeight);
    }

    @NonNull
    @Override
    public HomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_item, parent, false);
        return new HomeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeViewHolder holder, int position) {
        Art art = getItem(position);
        if (art != null) {
            holder.bind(art, position);
        } else {
            // Null defines a placeholder item - PagedListAdapter will automatically invalidate
            // this row when the actual object is loaded from the database
            // holder.clear();
        }
    }

    class HomeViewHolder extends LifecycleViewHolder implements View.OnClickListener {

        private Art art;
        private int position;
        private ImageView art_image;
        private TextView art_maker, art_title, art_classification;
        private ImageButton art_share, art_download, art_like;

        HomeViewHolder(@NonNull View itemView) {
            super(itemView);
            art_image = itemView.findViewById(R.id.art_image);
            art_maker = itemView.findViewById(R.id.art_maker);
            art_title = itemView.findViewById(R.id.art_title);
            art_classification = itemView.findViewById(R.id.art_classification);
            art_share = itemView.findViewById(R.id.art_share);
            art_download = itemView.findViewById(R.id.art_download);
            art_like = itemView.findViewById(R.id.art_like);

            art_image.setOnClickListener(this);
            art_maker.setOnClickListener(this);
            art_classification.setOnClickListener(this);
            art_share.setOnClickListener(this);
            art_download.setOnClickListener(this);
            art_like.setOnClickListener(this);

        }

        void bind(final Art art, int position) {
            this.art = art;
            this.position = position;

            art_title.setText(art.getArtLongTitle());
            art_maker.setText(art.getArtMaker());
            art_classification.setText(art.getArtClassification());

            if(art.getIsLiked()){
                art_like.setImageResource(R.drawable.ic_favorite_black_100dp);
                art_like.setScaleType(ImageView.ScaleType.FIT_CENTER);
            } else {
                art_like.setImageResource(R.drawable.ic_favorite_border_black_100dp);
                art_like.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }

            if (art.getArtWidth() > 0) {
                String artImgUrl= art.getArtImgUrl();
                int imgWidth = displayWidth;
                int imgHeight = (art.getArtHeight() * imgWidth) / art.getArtWidth();
                Picasso.get().load(artImgUrl).placeholder(R.color.colorSilver).resize(imgWidth, imgHeight).onlyScaleDown().into(art_image);
            } else {
                String artImgUrl = art.getArtImgUrlSmall();
                if (!artImgUrl.equals(" ")) {
                    Picasso.get().load(artImgUrl).placeholder(R.color.colorSilver).into(art_image);
                } else {
                    art_image.setImageDrawable(context.getResources().getDrawable(R.drawable.art_placeholder));
                }
            }

            homeViewModel.getArt().observe(this, new Observer<Art>() {
                @Override
                public void onChanged(Art newArt) {
                    if (newArt.getArtId().equals(art.getArtId()) && newArt.getArtProvider().equals(art.getArtProvider())) {
                        if(newArt.getIsLiked()){
                            art_like.setImageResource(R.drawable.ic_favorite_black_100dp);
                            art_like.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        } else {
                            art_like.setImageResource(R.drawable.ic_favorite_border_black_100dp);
                            art_like.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        }
                    }
                }
            });
        }

        @Override
        public void onClick(View v) {

            if (v.getId() == art_image.getId()) {
                onArtClickListener.onArtImageClick(art, v.getWidth(), v.getHeight());
            } else if (v.getId() == art_maker.getId()) {
                onArtClickListener.onArtMakerClick(art);
            } else if (v.getId() == art_classification.getId()) {
                onArtClickListener.onArtClassificationClick(art);
            } else if (v.getId() == art_share.getId()) {
                onArtClickListener.onArtShareClick(art);
            } else if (v.getId() == art_download.getId()) {
                int[] location = new int[2];
                art_download.getLocationOnScreen(location);
                int x = location[0];
                int y = location[1];
                onArtClickListener.onArtDownloadClick(art, x, y, art_image.getWidth(), art_image.getHeight());
            } else if (v.getId() == art_like.getId()) {
                onArtClickListener.onArtLikeClick(art, position);
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

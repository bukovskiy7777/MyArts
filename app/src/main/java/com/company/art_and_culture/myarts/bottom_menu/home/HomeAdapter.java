package com.company.art_and_culture.myarts.bottom_menu.home;

import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;

import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.pojo.Art;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import static com.company.art_and_culture.myarts.bottom_menu.home.HomeAnimations.likeFadeIn;
import static com.company.art_and_culture.myarts.bottom_menu.home.HomeAnimations.likeScaleDown;
import static com.company.art_and_culture.myarts.bottom_menu.home.HomeAnimations.shareScaleDown;
import static com.company.art_and_culture.myarts.bottom_menu.home.HomeAnimations.shareScaleUp;

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
        void onArtImageClick(Art art, int position, int viewWidth, int viewHeight);
        void onArtMakerClick(Art art);
        void onArtClassificationClick(Art art);
        void onArtLikeClick(Art art, int position);
        void onArtShareClick(Art art);
        void onArtDownloadClick(Art art, int x, int y, int viewWidth, int viewHeight);
        void onLogoClick(Art art);
    }

    @NonNull
    @Override
    public HomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home, parent, false);
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

    class HomeViewHolder extends LifecycleViewHolder implements View.OnClickListener, View.OnTouchListener {

        private Art art = new Art();
        private int position;
        private ImageView art_image, logo_image;
        private TextView art_maker, art_title, art_classification, logo_text;
        private ImageButton art_share, art_download, art_like;
        private String artImgUrl;
        private final Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                int imgWidth = displayWidth;
                int imgHeight = (bitmap.getHeight() * imgWidth) / bitmap.getWidth();
                if (imgHeight <= art_image.getMaxHeight()) {
                    art_image.getLayoutParams().height = imgHeight;
                } else {
                    art_image.getLayoutParams().height = art_image.getMaxHeight();
                }
                art.setArtWidth(bitmap.getWidth());
                art.setArtHeight(bitmap.getHeight());
                homeViewModel.writeDimentionsOnServer(art);
                Picasso.get().load(artImgUrl).placeholder(R.color.colorSilver).into(art_image);
            }
            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) { }
            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) { }
        };

        @SuppressLint("ClickableViewAccessibility")
        HomeViewHolder(@NonNull View itemView) {
            super(itemView);
            art_image = itemView.findViewById(R.id.art_image);
            art_maker = itemView.findViewById(R.id.art_maker);
            art_title = itemView.findViewById(R.id.art_title);
            art_classification = itemView.findViewById(R.id.art_classification);
            art_share = itemView.findViewById(R.id.art_share);
            art_download = itemView.findViewById(R.id.art_download);
            art_like = itemView.findViewById(R.id.art_like);
            logo_image = itemView.findViewById(R.id.logo_image);
            logo_text = itemView.findViewById(R.id.logo_text);

            art_image.setOnClickListener(this);
            art_maker.setOnClickListener(this);
            art_classification.setOnClickListener(this);
            art_share.setOnClickListener(this);
            art_download.setOnClickListener(this);
            art_like.setOnClickListener(this);
            logo_image.setOnClickListener(this);
            logo_text.setOnClickListener(this);

            art_maker.setOnTouchListener(this);
            art_classification.setOnTouchListener(this);

        }

        void bind(final Art art, int position) {
            this.art = art;
            this.position = position;

            art_title.setText(art.getArtLongTitle());
            art_maker.setText(art.getArtMaker());
            art_classification.setText(art.getArtClassification());
            logo_text.setText(art.getArtProvider());

            Picasso.get().load(art.getArtLogoUrl()).into(logo_image);

            if(HomeDataInMemory.getInstance().getSingleItem(position).getIsLiked()){
                art_like.setImageResource(R.drawable.ic_favorite_red_100dp);
                art_like.setScaleType(ImageView.ScaleType.FIT_CENTER);
            } else {
                art_like.setImageResource(R.drawable.ic_favorite_border_black_100dp);
                art_like.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }

            if (art.getArtImgUrlSmall() != null && art.getArtImgUrlSmall().startsWith(context.getResources().getString(R.string.http))) {
                artImgUrl = art.getArtImgUrlSmall();
            } else {
                artImgUrl= art.getArtImgUrl();
            }

            art_image.setImageDrawable(context.getResources().getDrawable(R.drawable.art_placeholder));
            if (art.getArtWidth() > 0) {
                int imgWidth = displayWidth;
                int imgHeight = (art.getArtHeight() * imgWidth) / art.getArtWidth();
                if (imgHeight <= art_image.getMaxHeight()) {
                    art_image.getLayoutParams().height = imgHeight;
                } else {
                    art_image.getLayoutParams().height = art_image.getMaxHeight();
                }
                Picasso.get().load(artImgUrl).placeholder(R.color.colorSilver).resize(imgWidth, imgHeight).onlyScaleDown().into(art_image);
            } else {
                art_image.getLayoutParams().height = displayWidth;
                Picasso.get().load(artImgUrl).placeholder(R.color.colorSilver).into(target);
            }

            MainActivity activity = (MainActivity) context;
            activity.getArt().observe(this, new Observer<Art>() {
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

            if (v.getId() == art_image.getId()) {
                onArtClickListener.onArtImageClick(art, position, v.getWidth(), v.getHeight());

            } else if (v.getId() == art_maker.getId()) {
                onArtClickListener.onArtMakerClick(art);

            } else if (v.getId() == art_classification.getId()) {
                onArtClickListener.onArtClassificationClick(art);

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
                onArtClickListener.onArtDownloadClick(art, x, y, art_image.getWidth(), art_image.getHeight());

            } else if (v.getId() == art_like.getId()) {
                onArtClickListener.onArtLikeClick(art, position);

            } else if (v.getId() == logo_image.getId() || v.getId() == logo_text.getId()) {
                onArtClickListener.onLogoClick(art);
            }
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (v.getId() == art_maker.getId()) {
                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        ((TextView)v).setTextColor(context.getResources().getColor(R.color.colorBlack));
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        ((TextView)v).setTextColor(context.getResources().getColor(R.color.colorBlueLight));
                        break;
                }
            } else if (v.getId() == art_classification.getId()) {
                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        ((TextView)v).setTextColor(context.getResources().getColor(R.color.colorBlack));
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        ((TextView)v).setTextColor(context.getResources().getColor(R.color.colorBlueLight));
                        break;
                }
            }

            return false;
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

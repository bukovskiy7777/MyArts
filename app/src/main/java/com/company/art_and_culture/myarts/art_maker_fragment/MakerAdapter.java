package com.company.art_and_culture.myarts.art_maker_fragment;

import android.animation.AnimatorSet;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.pojo.Maker;
import com.company.art_and_culture.myarts.ui.home.LifecycleViewHolder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;

import static com.company.art_and_culture.myarts.ui.home.HomeAnimations.likeFadeIn;
import static com.company.art_and_culture.myarts.ui.home.HomeAnimations.likeScaleDown;
import static com.company.art_and_culture.myarts.ui.home.HomeAnimations.shareScaleDown;
import static com.company.art_and_culture.myarts.ui.home.HomeAnimations.shareScaleUp;

public class MakerAdapter extends PagedListAdapter<Art, MakerAdapter.MakerViewHolder> {

    private Maker globalMaker;
    private Context context;
    private OnArtClickListener onArtClickListener;
    private int displayWidth, displayHeight;
    private MakerViewModel makerViewModel;
    private double k = 0.4;


    public MakerAdapter(MakerViewModel makerViewModel, Context context, OnArtClickListener onArtClickListener,
                        int displayWidth, int displayHeight, Maker maker) {
        super(itemDiffUtilCallback);
        this.context = context;
        this.makerViewModel = makerViewModel;
        this.onArtClickListener = onArtClickListener;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;

        this.globalMaker = maker;
    }

    @Override
    public void onViewAttachedToWindow(@NonNull MakerViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        holder.onAttached();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull MakerViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.onDetached();
    }

    public interface OnArtClickListener {
        void onArtImageClick(Art art, int position);
        void onArtLikeClick(Art art, int position);
        void onArtShareClick(Art art);
        void onArtDownloadClick(Art art, int x, int y, int viewWidth, int viewHeight);
        void onMakerLikeClick(Maker maker);
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0) {
            return 0;
        } else {
            return 1;
        }
    }

    @NonNull
    @Override
    public MakerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType == 0) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_maker_with_header, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_maker, parent, false);
        }
        return new MakerViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull MakerViewHolder holder, int position) {
        Art art = getItem(position);
        if (art != null) {
            holder.bind(art, position);
        } else {
            // Null defines a placeholder item - PagedListAdapter will automatically invalidate
            // this row when the actual object is loaded from the database
            //holder.clear();
        }
    }

    class MakerViewHolder extends LifecycleViewHolder implements View.OnClickListener {

        private Art art;
        private int position;
        private ImageView art_image, art_image_header, maker_image;
        private TextView art_title, art_maker, maker_name, maker_bio, maker_description, read_more, wikipedia, art_count;
        private ImageButton art_share, art_download, art_like, maker_like, maker_share;
        private String artImgUrl, makerWikiImageUrl;
        private final Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                art.setArtWidth(bitmap.getWidth());
                art.setArtHeight(bitmap.getHeight());
                makerViewModel.writeDimentionsOnServer(art);
                Picasso.get().load(artImgUrl).placeholder(R.color.colorSilver).into(art_image);
            }
            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) { }
            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) { }
        };

        MakerViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);

            if(viewType == 0) {
                art_image_header = itemView.findViewById(R.id.art_image_header);
                maker_image = itemView.findViewById(R.id.maker_image);
                maker_name = itemView.findViewById(R.id.maker_name);
                maker_bio = itemView.findViewById(R.id.maker_bio);
                maker_description = itemView.findViewById(R.id.maker_description);
                maker_like = itemView.findViewById(R.id.maker_like);
                maker_share = itemView.findViewById(R.id.maker_share);
                read_more = itemView.findViewById(R.id.read_more);
                wikipedia = itemView.findViewById(R.id.wikipedia);
                art_count = itemView.findViewById(R.id.art_count);
                maker_like.setOnClickListener(this);
                maker_share.setOnClickListener(this);
                read_more.setOnClickListener(this);
                read_more.setVisibility(View.GONE);
                wikipedia.setOnClickListener(this);
                wikipedia.setVisibility(View.GONE);
            }
            art_title = itemView.findViewById(R.id.art_title);
            art_maker = itemView.findViewById(R.id.art_maker);
            art_share = itemView.findViewById(R.id.art_share);
            art_download = itemView.findViewById(R.id.art_download);
            art_like = itemView.findViewById(R.id.art_like);
            art_image = itemView.findViewById(R.id.art_image);
            art_maker.setOnClickListener(this);
            art_image.setOnClickListener(this);
            art_share.setOnClickListener(this);
            art_download.setOnClickListener(this);
            art_like.setOnClickListener(this);

            art_image.getLayoutParams().height = (int) (displayWidth * k);
            art_image.getLayoutParams().width = (int) (displayWidth * k);
        }

        void bind(final Art art, final int position) {
            this.art = art;
            this.position = position;

            if (position == 0) {

                maker_name.setText(globalMaker.getArtMaker());
                maker_bio.setText(globalMaker.getArtistBio());

                if (globalMaker.getArtWidth() > 0) {
                    int imgWidth = displayWidth;
                    int imgHeight = (globalMaker.getArtHeight() * imgWidth) / globalMaker.getArtWidth();
                    if (imgHeight <= art_image_header.getMaxHeight()) {
                        art_image_header.getLayoutParams().height = imgHeight;
                    } else {
                        art_image_header.getLayoutParams().height = art_image_header.getMaxHeight();
                    }
                    Picasso.get().load(globalMaker.getArtHeaderImageUrl()).placeholder(R.color.colorSilver).resize(imgWidth, imgHeight).onlyScaleDown().into(art_image_header);
                } else {
                    Picasso.get().load(globalMaker.getArtHeaderImageUrl()).placeholder(R.color.colorSilver).into(art_image_header);
                }

                makerViewModel.getMakerFirstTime().observe(this, new Observer<Maker>() {
                    @Override
                    public void onChanged(Maker maker) {

                        if (maker.getMakerWikiDescription() != null) {
                            maker_description.setVisibility(View.VISIBLE);
                            wikipedia.setVisibility(View.VISIBLE);

                            maker_description.setText(maker.getMakerWikiDescription());
                            Paint paint = new Paint();
                            paint.setTextSize(context.getResources().getDimension(R.dimen.text_size_maker_description));
                            float widthText = paint.measureText(maker_description.getText().toString());
                            float numLines = (float) ((widthText/displayWidth) * 1.2);
                            if (numLines > 3) { read_more.setVisibility(View.VISIBLE); } else { read_more.setVisibility(View.GONE); }
                        } else {
                            maker_description.setVisibility(View.GONE);
                            wikipedia.setVisibility(View.GONE);
                            read_more.setVisibility(View.GONE);
                        }

                        makerWikiImageUrl = "";
                        if(maker.getMakerWikiImageUrl() != null && maker.getMakerWikiImageUrl().length() > 0) {
                            makerWikiImageUrl = maker.getMakerWikiImageUrl();
                            Picasso.get().load(maker.getMakerWikiImageUrl()).placeholder(R.color.colorSilver).into(maker_image);
                        }

                        if(maker.isLiked()){
                            maker_like.setImageResource(R.drawable.ic_favorite_red_100dp);
                            maker_like.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        } else {
                            maker_like.setImageResource(R.drawable.ic_favorite_border_black_100dp);
                            maker_like.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        }

                        String artCount = context.getResources().getString(R.string.artworks_count) +" "+ maker.getArtCount();
                        art_count.setText(artCount);
                    }
                });

                makerViewModel.getMaker().observe(this, new Observer<Maker>() {
                    @Override
                    public void onChanged(Maker maker) {

                        if(maker.isLiked()){
                            maker_like.setImageResource(R.drawable.ic_favorite_red_100dp);
                            maker_like.setScaleType(ImageView.ScaleType.FIT_CENTER);
                            AnimatorSet set = new AnimatorSet();
                            set.playSequentially(likeFadeIn(maker_like), likeScaleDown(maker_like));
                            set.start();
                        } else {
                            maker_like.setImageResource(R.drawable.ic_favorite_border_black_100dp);
                            maker_like.setScaleType(ImageView.ScaleType.FIT_CENTER);
                            AnimatorSet set = new AnimatorSet();
                            set.playSequentially(likeFadeIn(maker_like), likeScaleDown(maker_like));
                            set.start();
                        }
                    }
                });

            }

            if(MakerDataInMemory.getInstance().getSingleItem(position).getIsLiked()){
                art_like.setImageResource(R.drawable.ic_favorite_red_100dp);
                art_like.setScaleType(ImageView.ScaleType.FIT_CENTER);
            } else {
                art_like.setImageResource(R.drawable.ic_favorite_border_black_100dp);
                art_like.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }

            if (!art.getArtImgUrlSmall().equals(" ") && art.getArtImgUrlSmall().startsWith(context.getResources().getString(R.string.http))) {
                artImgUrl = art.getArtImgUrlSmall();
            } else {
                artImgUrl= art.getArtImgUrl();
            }

            art_image.setImageDrawable(context.getResources().getDrawable(R.drawable.art_placeholder));
            if (art.getArtWidth() > 0) {
                int imgWidth = displayWidth;
                int imgHeight = (art.getArtHeight() * imgWidth) / art.getArtWidth();
                Picasso.get().load(artImgUrl).placeholder(R.color.colorSilver).resize(imgWidth, imgHeight).onlyScaleDown().into(art_image);
            } else {
                Picasso.get().load(artImgUrl).placeholder(R.color.colorSilver).into(target);
            }

            makerViewModel.getArt().observe(this, new Observer<Art>() {
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

            art_title.setText(art.getArtLongTitle());
            art_maker.setText(art.getArtMaker());

        }

        @Override
        public void onClick(View v) {

            if (v.getId() == art_image.getId() || v.getId() == art_maker.getId()) {
                onArtClickListener.onArtImageClick(art, position);

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

            } else if(v.getId() == read_more.getId()) {
                if (maker_description.getMaxLines() == 4) {
                    Paint paint = new Paint();
                    paint.setTextSize(context.getResources().getDimension(R.dimen.text_size_maker_description));
                    float widthText = paint.measureText(maker_description.getText().toString());
                    float numLines = (float) ((widthText/displayWidth) * 1.2);
                    maker_description.setMaxLines((int) (numLines + 10));
                    read_more.setText(context.getResources().getString(R.string.show_less));
                } else {
                    maker_description.setMaxLines(4);
                    read_more.setText(context.getResources().getString(R.string.read_more));
                }

            } else if (v.getId() == maker_like.getId()) {
                Maker maker = new Maker (globalMaker.getArtMaker(), globalMaker.getArtistBio(), globalMaker.getArtHeaderImageUrl(),
                        globalMaker.getArtWidth(), globalMaker.getArtHeight(), makerWikiImageUrl, globalMaker.getArtHeaderId(), globalMaker.getArtHeaderProviderId());
                onArtClickListener.onMakerLikeClick(maker);

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

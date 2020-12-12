package com.company.art_and_culture.myarts.bottom_menu.favorites.Favorites;

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

public class FavoritesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private FavoritesFragment.Sort sort_type;
    private Context context;
    private OnArtClickListener onArtClickListener;
    private int displayWidth, displayHeight, spanCount;
    private double k = 0.4;
    private List<Art> artList=new ArrayList<>();
    private int lastPosition = -1;

    public FavoritesAdapter(Context context, OnArtClickListener onArtClickListener, int displayWidth, int displayHeight,
                            int spanCount, FavoritesFragment.Sort sort_type) {
        this.context = context;
        this.onArtClickListener = onArtClickListener;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
        this.spanCount = spanCount;
        this.sort_type = sort_type;
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

    public FavoritesFragment.Sort getSort_type() {
        return sort_type;
    }

    public void clearItems(){
        artList.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {

        if (sort_type.equals(FavoritesFragment.Sort.by_date)) {
            return 1;

        } else if (sort_type.equals(FavoritesFragment.Sort.by_maker)) {
            if (position > 0) {
                if (!artList.get(position-1).getArtMaker().equals(artList.get(position).getArtMaker())) {
                    return 3;
                } else {
                    return 2;
                }
            } else {
                return 3;
            }

        } else if (sort_type.equals(FavoritesFragment.Sort.by_century)) {
            if (position > 0){
                if (!artList.get(position-1).getCentury().equals(artList.get(position).getCentury())) {
                    return 3;
                } else {
                    return 2;
                }
            } else {
                return 3;
            }

        } else {
            return 1;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 1) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorites_1, parent, false);
            return new Favorites_1_ViewHolder(view);
        } else if (viewType == 2) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorites_2, parent, false);
            return new Favorites_2_ViewHolder(view);
        } else if (viewType == 3) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorites_3, parent, false);
            return new Favorites_3_ViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorites_1, parent, false);
            return new Favorites_1_ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Art art = artList.get(position);
        if (art != null) {
            if (holder.getItemViewType() == 1) {
                Favorites_1_ViewHolder viewHolder = (Favorites_1_ViewHolder) holder;
                viewHolder.bind(art, position);
            } else if (holder.getItemViewType() == 2) {
                Favorites_2_ViewHolder viewHolder = (Favorites_2_ViewHolder) holder;
                viewHolder.bind(art, position);
            } else if (holder.getItemViewType() == 3) {
                Favorites_3_ViewHolder viewHolder = (Favorites_3_ViewHolder) holder;
                viewHolder.bind(art, position);
            }
            setAnimation(holder.itemView, position);
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

    class Favorites_1_ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Art art;
        private int position;
        private ImageView art_image;
        private TextView art_maker;
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

        Favorites_1_ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.getLayoutParams().width = displayWidth/spanCount;
            itemView.getLayoutParams().height = displayWidth/spanCount;
            art_image = itemView.findViewById(R.id.art_image);
            art_maker = itemView.findViewById(R.id.art_maker);
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
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == art_image.getId()) {
                onArtClickListener.onArtImageClick(art, position);
            }
        }
    }

    class Favorites_2_ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Art art;
        private int position;
        private ImageView art_image;
        private TextView art_maker, art_title;
        private String artImgUrl = " ";
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

        Favorites_2_ViewHolder(@NonNull View itemView) {
            super(itemView);

            art_image = itemView.findViewById(R.id.art_image);
            art_maker = itemView.findViewById(R.id.art_maker);
            art_title = itemView.findViewById(R.id.art_title);

            itemView.getLayoutParams().width = displayWidth/spanCount;
            itemView.getLayoutParams().height = (int) (displayWidth * k);
            art_image.getLayoutParams().height = (int) (displayWidth * k);
            art_image.getLayoutParams().width = (int) (displayWidth * k);

            art_image.setOnClickListener(this);
            art_maker.setOnClickListener(this);
            art_title.setOnClickListener(this);
        }

        void bind(Art art, int position) {
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
                Picasso.get().load(artImgUrl).placeholder(R.color.colorSilver).resize(imgWidth, imgHeight).onlyScaleDown().into(art_image);
            } else {
                Picasso.get().load(artImgUrl).placeholder(R.color.colorSilver).into(target);
            }

            art_maker.setText(art.getArtMaker());
            art_title.setText(art.getArtLongTitle());
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == art_image.getId() || v.getId() == art_maker.getId() || v.getId() == art_title.getId()) {
                onArtClickListener.onArtImageClick(art, position);
            }
        }
    }

    class Favorites_3_ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Art art;
        private int position;
        private ImageView art_image;
        private TextView art_maker, art_title, header;
        private String artImgUrl = " ";
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

        Favorites_3_ViewHolder(@NonNull View itemView) {
            super(itemView);

            art_image = itemView.findViewById(R.id.art_image);
            art_maker = itemView.findViewById(R.id.art_maker);
            art_title = itemView.findViewById(R.id.art_title);
            header = itemView.findViewById(R.id.header);

            itemView.getLayoutParams().width = displayWidth/spanCount;
            //itemView.getLayoutParams().height = (int) (displayWidth * k);
            art_image.getLayoutParams().height = (int) (displayWidth * k);
            art_image.getLayoutParams().width = (int) (displayWidth * k);

            art_image.setOnClickListener(this);
            art_maker.setOnClickListener(this);
            art_title.setOnClickListener(this);
        }

        void bind(Art art, int position) {
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
                Picasso.get().load(artImgUrl).placeholder(R.color.colorSilver).resize(imgWidth, imgHeight).onlyScaleDown().into(art_image);
            } else {
                Picasso.get().load(artImgUrl).placeholder(R.color.colorSilver).into(target);
            }

            art_maker.setText(art.getArtMaker());
            art_title.setText(art.getArtLongTitle());

            if (sort_type.equals(FavoritesFragment.Sort.by_maker)) {
                header.setText(art.getArtMaker());
            } else if (sort_type.equals(FavoritesFragment.Sort.by_century)) {
                String text = art.getCentury() + context.getResources().getString(R.string.th_century);
                header.setText(text);
            }
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == art_image.getId() || v.getId() == art_maker.getId() || v.getId() == art_title.getId()) {
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

            return oldArt.getArtId().equals(newArt.getArtId()) && oldArt.getIsLiked() == newArt.getIsLiked();
        }
    }

}

package com.company.art_and_culture.myarts.ui.favorites;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.pojo.Maker;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

public class ArtistsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private OnMakerClickListener onMakerClickListener;
    private int displayWidth, displayHeight;
    private double k = 0.33;
    private List<Maker> makerList=new ArrayList<>();

    public ArtistsAdapter(Context context, OnMakerClickListener onMakerClickListener, int displayWidth, int displayHeight) {
        this.context = context;
        this.onMakerClickListener = onMakerClickListener;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
    }

    public interface OnMakerClickListener {
        void onMakerImageClick(Maker maker, int position);
    }

    public void setItems(Collection<Maker> makers){
        makerList.addAll(makers);
        notifyDataSetChanged();
    }

    public List<Maker> getItems() {
        return makerList;
    }

    public void clearItems(){
        makerList.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_artists, parent, false);
        return new ArtistsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Maker maker = makerList.get(position);
        if (maker != null) {
            ArtistsViewHolder viewHolder = (ArtistsViewHolder) holder;
            viewHolder.bind(maker, position);
        } else {
            // Null defines a placeholder item - PagedListAdapter will automatically invalidate
            // this row when the actual object is loaded from the database
            // holder.clear();
        }
    }

    @Override
    public int getItemCount() {
        return makerList.size();
    }


    class ArtistsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Maker maker;
        private int position;
        private ImageView maker_image;
        private TextView maker_name, maker_bio, maker_art_count;

        ArtistsViewHolder(@NonNull View itemView) {
            super(itemView);

            maker_image = itemView.findViewById(R.id.maker_image);
            maker_name = itemView.findViewById(R.id.maker_name);
            maker_bio = itemView.findViewById(R.id.maker_bio);
            maker_art_count = itemView.findViewById(R.id.maker_art_count);

            maker_image.getLayoutParams().height = (int) (displayWidth * k);
            maker_image.getLayoutParams().width = (int) (displayWidth * k);

            itemView.setOnClickListener(this);
            //maker_image.setOnClickListener(this);
            //maker_name.setOnClickListener(this);
        }

        void bind(Maker maker, int position) {
            this.maker = maker;
            this.position = position;

            if(maker.getMakerWikiImageUrl() != null && maker.getMakerWikiImageUrl().length() > 0) {
                Picasso.get().load(maker.getMakerWikiImageUrl()).placeholder(R.color.colorSilver).into(maker_image);
            }

            maker_name.setText(maker.getArtMaker());
            maker_bio.setText(maker.getArtistBio());
            String count = maker.getArtCount() +" "+ context.getResources().getString(R.string.artworks);
            maker_art_count.setText(count);
        }

        @Override
        public void onClick(View v) {
            //if (v.getId() == maker_image.getId() || v.getId() == maker_name.getId()) {
                //onMakerClickListener.onMakerImageClick(maker, position);
            //}
            onMakerClickListener.onMakerImageClick(maker, position);
        }
    }

}

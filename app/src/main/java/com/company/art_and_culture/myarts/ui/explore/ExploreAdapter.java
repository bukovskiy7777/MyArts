package com.company.art_and_culture.myarts.ui.explore;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.pojo.ExploreObject;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

class ExploreAdapter extends RecyclerView.Adapter<ExploreAdapter.ExploreViewHolder> {

    private Context context;
    private OnExploreClickListener onExploreClickListener;
    private int displayWidth, displayHeight, spanCount;
    private List<ExploreObject> exploreList=new ArrayList<>();

    public ExploreAdapter(Context context, OnExploreClickListener onExploreClickListener, int displayWidth, int displayHeight, int spanCount) {
        this.context = context;
        this.onExploreClickListener = onExploreClickListener;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
        this.spanCount = spanCount;
    }

    public interface OnExploreClickListener {
        void onExploreImageClick(ExploreObject exploreObject, int position);
    }

    public void setItems(Collection<ExploreObject> arts){
        exploreList.addAll(arts);
        notifyDataSetChanged();
    }

    public List<ExploreObject> getItems() {
        return exploreList;
    }

    public void clearItems(){
        exploreList.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ExploreAdapter.ExploreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_explore, parent, false);
        return new ExploreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExploreAdapter.ExploreViewHolder holder, int position) {
        ExploreObject exploreObject = exploreList.get(position);
        if (exploreObject != null) {
            holder.bind(exploreObject, position);
        } else {
            // Null defines a placeholder item - PagedListAdapter will automatically invalidate
            // this row when the actual object is loaded from the database
            // holder.clear();
        }
    }

    @Override
    public int getItemCount() {
        return exploreList.size();
    }

    class ExploreViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ExploreObject exploreObject;
        private int position;
        private ImageView explore_image;
        private TextView explore_text, explore_count;
        private String imageUrl = " ";
        private final Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                Picasso.get().load(imageUrl).placeholder(R.color.colorSilver).into(explore_image);
            }
            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) { }
            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) { }
        };

        ExploreViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.getLayoutParams().width = displayWidth/spanCount;
            itemView.getLayoutParams().height = displayWidth/spanCount;
            explore_image = itemView.findViewById(R.id.explore_image);
            explore_text = itemView.findViewById(R.id.explore_text);
            explore_count = itemView.findViewById(R.id.explore_count);
            explore_image.setOnClickListener(this);
        }

        void bind(ExploreObject exploreObject, int position) {
            this.exploreObject = exploreObject;
            this.position = position;

            if (!exploreObject.getImageUrlSmall().equals(" ") && exploreObject.getImageUrlSmall().startsWith(context.getResources().getString(R.string.http))) {
                imageUrl = exploreObject.getImageUrlSmall();
            } else {
                imageUrl= exploreObject.getImageUrl();
            }

            //explore_image.getLayoutParams().width = displayWidth/spanCount;
            //explore_image.getLayoutParams().height = displayWidth/spanCount;
            explore_image.setImageDrawable(context.getResources().getDrawable(R.drawable.art_placeholder));
            if (exploreObject.getWidth() > 0) {
                int imgWidth = displayWidth/spanCount;
                int imgHeight = (exploreObject.getHeight() * imgWidth) / exploreObject.getWidth();
                Picasso.get().load(imageUrl).placeholder(R.color.colorSilver).resize(imgWidth, imgHeight).onlyScaleDown().into(explore_image);
            } else {
                Picasso.get().load(imageUrl).placeholder(R.color.colorSilver).into(target);
            }

            explore_text.setText(exploreObject.getText());
            String text = exploreObject.getArtCount()+" "+context.getResources().getString(R.string.items);
            explore_count.setText(text);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == explore_image.getId()) {
                onExploreClickListener.onExploreImageClick(exploreObject, position);
            }
        }
    }
}
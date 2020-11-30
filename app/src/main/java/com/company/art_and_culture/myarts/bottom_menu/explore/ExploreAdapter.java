package com.company.art_and_culture.myarts.bottom_menu.explore;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.pojo.ExploreObject;
import com.squareup.picasso.Picasso;

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
    private int lastPosition = -1;

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
    public int getItemCount() {
        return exploreList.size();
    }

    class ExploreViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ExploreObject exploreObject;
        private int position;
        private ImageView explore_image;
        private TextView explore_text, explore_count;

        ExploreViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.getLayoutParams().width = displayWidth/spanCount;
            itemView.getLayoutParams().height = (int) (displayWidth/spanCount * 0.7);
            explore_image = itemView.findViewById(R.id.explore_image);
            explore_text = itemView.findViewById(R.id.explore_text);
            explore_count = itemView.findViewById(R.id.explore_count);
            explore_image.setOnClickListener(this);
        }

        void bind(ExploreObject exploreObject, int position) {
            this.exploreObject = exploreObject;
            this.position = position;

            if (exploreObject.getImageUrl() != null && exploreObject.getImageUrl().startsWith(context.getResources().getString(R.string.http))) {
                Picasso.get().load(exploreObject.getImageUrl()).placeholder(R.color.colorSilver).into(explore_image);
            }

            explore_text.setText(exploreObject.getText());
            String text = exploreObject.getItemsCount()+" "+context.getResources().getString(R.string.items);
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

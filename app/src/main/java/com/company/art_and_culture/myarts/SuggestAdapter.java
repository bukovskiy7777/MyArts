package com.company.art_and_culture.myarts;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.pojo.Suggest;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

public class SuggestAdapter extends RecyclerView.Adapter<SuggestAdapter.SuggestViewHolder> {

    private Context context;
    private OnSuggestClickListener onSuggestsClickListener;
    private List<Suggest> suggestList = new ArrayList<>();

    public SuggestAdapter(Context context, OnSuggestClickListener onSuggestsClickListener) {
        this.context = context;
        this.onSuggestsClickListener = onSuggestsClickListener;
    }

    public interface OnSuggestClickListener {
        void onSuggestClick(Suggest suggest, int position);
    }

    public void setItems(Collection<Suggest> suggests){
        suggestList.addAll(suggests);
        notifyDataSetChanged();
    }

    public List<Suggest> getItems() {
        return suggestList;
    }

    public void clearItems(){
        suggestList.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SuggestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_suggest, parent, false);
        return new SuggestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestViewHolder holder, int position) {
        Suggest suggest = suggestList.get(position);
        if (suggest != null) {
            holder.bind(suggest, position);
        } else {
            // Null defines a placeholder item - PagedListAdapter will automatically invalidate
            // this row when the actual object is loaded from the database
            // holder.clear();
        }
    }

    @Override
    public int getItemCount() {
        return suggestList.size();
    }

    class SuggestViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Suggest suggest;
        private int position;
        private TextView tv_suggest, art_count;
        private ImageView imageView;

        SuggestViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_suggest = itemView.findViewById(R.id.tv_suggest);
            art_count = itemView.findViewById(R.id.art_count);
            imageView = itemView.findViewById(R.id.image);
            tv_suggest.setOnClickListener(this);
            art_count.setOnClickListener(this);
            imageView.setOnClickListener(this);
        }

        void bind(Suggest suggest, int position) {
            this.suggest = suggest;
            this.position = position;

            tv_suggest.setText(suggest.getSuggestStr());
            String text = context.getResources().getString(R.string.more_then)+" "+suggest.getCountArts()+" "+context.getResources().getString(R.string.items);
            art_count.setText(text);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == tv_suggest.getId() || v.getId() == art_count.getId() || v.getId() == imageView.getId()) {
                onSuggestsClickListener.onSuggestClick(suggest, position);
            }
        }
    }

    public static class ItemDiffUtilCallback extends DiffUtil.Callback {
        private final List<Suggest> oldList;
        private final List<Suggest> newList;

        public ItemDiffUtilCallback(List<Suggest> oldList, List<Suggest> newList) {
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
            Suggest oldSuggest = oldList.get(oldItemPosition);
            Suggest newSuggest = newList.get(newItemPosition);
            return oldSuggest.getSuggestStr().equals(newSuggest.getSuggestStr());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Suggest oldSuggest = oldList.get(oldItemPosition);
            Suggest newSuggest = newList.get(newItemPosition);

            return oldSuggest.getSuggestStr().equals(newSuggest.getSuggestStr()) && oldSuggest.getCountArts() == newSuggest.getCountArts();
        }
    }

}

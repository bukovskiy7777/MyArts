package com.company.art_and_culture.myarts.art_search_fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.pojo.Suggest;

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
        void onSuggestLongClick(Suggest suggest, View v);
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

    class SuggestViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnTouchListener, View.OnLongClickListener {

        private Suggest suggest;
        private int position;
        private TextView tv_suggest, art_count;
        private ImageView imageView, image_is_used_earlie;
        private View parentView;

        @SuppressLint("ClickableViewAccessibility")
        SuggestViewHolder(@NonNull View itemView) {
            super(itemView);
            parentView = itemView;

            tv_suggest = itemView.findViewById(R.id.tv_suggest);
            art_count = itemView.findViewById(R.id.art_count);
            imageView = itemView.findViewById(R.id.image);
            image_is_used_earlie = itemView.findViewById(R.id.image_is_used_earlie);

            tv_suggest.setOnClickListener(this);
            art_count.setOnClickListener(this);
            imageView.setOnClickListener(this);
            image_is_used_earlie.setOnClickListener(this);

            tv_suggest.setOnTouchListener(this);
            art_count.setOnTouchListener(this);
            imageView.setOnTouchListener(this);
            image_is_used_earlie.setOnTouchListener(this);

            tv_suggest.setOnLongClickListener(this);
            art_count.setOnLongClickListener(this);
            imageView.setOnLongClickListener(this);
            image_is_used_earlie.setOnLongClickListener(this);
        }

        void bind(Suggest suggest, int position) {
            this.suggest = suggest;
            this.position = position;

            tv_suggest.setText(suggest.getSuggestStr());

            String text = context.getResources().getString(R.string.more_then)+" "+suggest.getCountArts()+" "+context.getResources().getString(R.string.items);
            art_count.setText(text);
            if(suggest.getCountArts() > 0) {
                art_count.setVisibility(View.VISIBLE);
            } else {
                art_count.setVisibility(View.GONE);
            }

            if (suggest.isUsedEarlie()) {
                image_is_used_earlie.setVisibility(View.VISIBLE);
            } else {
                image_is_used_earlie.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == tv_suggest.getId() || v.getId() == art_count.getId() || v.getId() == imageView.getId()|| v.getId() == image_is_used_earlie.getId()) {
                onSuggestsClickListener.onSuggestClick(suggest, position);
            }
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (v.getId() == tv_suggest.getId() || v.getId() == art_count.getId() || v.getId() == imageView.getId()|| v.getId() == image_is_used_earlie.getId()) {
                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        parentView.setBackgroundColor(context.getResources().getColor(R.color.colorSilver));
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        parentView.setBackgroundColor(0);
                        break;
                }
            }
            return false;
        }

        @Override
        public boolean onLongClick(View v) {
            if (v.getId() == tv_suggest.getId() || v.getId() == art_count.getId() || v.getId() == imageView.getId()|| v.getId() == image_is_used_earlie.getId()) {
                onSuggestsClickListener.onSuggestLongClick(suggest, v);
            }
            return true;
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

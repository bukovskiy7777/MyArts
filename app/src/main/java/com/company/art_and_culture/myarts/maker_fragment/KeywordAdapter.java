package com.company.art_and_culture.myarts.maker_fragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.pojo.FilterObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class KeywordAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private OnFilterClickListener onFilterClickListener;
    private Context context;
    private ArrayList<FilterObject> itemsList = new ArrayList<>();

    public KeywordAdapter(Context context, OnFilterClickListener onFilterClickListener) {
        this.context = context;
        this.onFilterClickListener = onFilterClickListener;
    }

    public interface OnFilterClickListener {
        void onFilterClick(FilterObject item, int position);
    }

    public void setItems(Collection<FilterObject> items){
        itemsList.addAll(items);
        notifyDataSetChanged();
    }

    public List<FilterObject> getItems() {
        return itemsList;
    }

    public void clearItems(){
        itemsList.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_filter_on_maker_fragment, parent, false);
        return new FilterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        FilterObject filterObject = itemsList.get(position);
        if (filterObject != null) {
            FilterViewHolder viewHolder = (FilterViewHolder) holder;
            viewHolder.bind(filterObject, position);
        } else {
            // Null defines a placeholder item - PagedListAdapter will automatically invalidate
            // this row when the actual object is loaded from the database
            // holder.clear();
        }
    }

    @Override
    public int getItemCount() {
        return itemsList.size();
    }

    class FilterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnTouchListener {

        private FilterObject filterObject;
        private int position;
        private TextView filter_text;

        FilterViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnTouchListener(this);
            filter_text = itemView.findViewById(R.id.filter_text);
        }

        void bind(FilterObject filterObject, int position) {
            this.filterObject = filterObject;
            this.position = position;

            filter_text.setText(filterObject.getText());

            if(filterObject.isChosen())
                filter_text.setBackground(context.getDrawable(R.drawable.filter_item_chosen_on_maker_fragm));
            else
                filter_text.setBackground(context.getDrawable(R.drawable.filter_item_on_maker_fragment));
        }

        @Override
        public void onClick(View v) {
            onFilterClickListener.onFilterClick(filterObject, position);

            for (FilterObject filter : itemsList) {
                if(filter.isChosen()) filter.setChosen(false);
            }
            itemsList.get(position).setChosen(true);
            notifyDataSetChanged();
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch(motionEvent.getAction()){
                case MotionEvent.ACTION_DOWN:
                    filter_text.setBackground(context.getDrawable(R.drawable.filter_item_chosen_on_maker_fragm));
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    if(filterObject.isChosen())
                        filter_text.setBackground(context.getDrawable(R.drawable.filter_item_chosen_on_maker_fragm));
                    else
                        filter_text.setBackground(context.getDrawable(R.drawable.filter_item_on_maker_fragment));
                    break;
            }
            return false;
        }
    }

}

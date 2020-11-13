package com.company.art_and_culture.myarts.attrib_tags_fragment;

import android.content.Context;
import android.view.LayoutInflater;
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
import de.hdodenhof.circleimageview.CircleImageView;

public class FilterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private OnFilterClickListener onFilterClickListener;
    private int displayWidth, displayHeight;
    private int filterSpanCount;
    private ArrayList<FilterObject> itemsList = new ArrayList<>();

    public FilterAdapter(Context context, OnFilterClickListener onFilterClickListener, int displayWidth, int displayHeight, int filterSpanCount) {
        this.context = context;
        this.onFilterClickListener = onFilterClickListener;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
        this.filterSpanCount = filterSpanCount;
    }

    public interface OnFilterClickListener {
        void onFilterClick(String item, int position);
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

    @Override
    public int getItemViewType(int position) {
        //return super.getItemViewType(position);
        if(position == 0) {
            return 0;
        } else {
            return 1;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 0) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_filter_tag_all, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_filter_tag, parent, false);
        }
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

    class FilterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private String filter;
        private int position;
        private TextView filter_text;
        private CircleImageView circle_filter_view;

        FilterViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            filter_text = itemView.findViewById(R.id.filter_text);
            circle_filter_view = itemView.findViewById(R.id.circle_filter_view);
        }

        void bind(FilterObject filterObject, int position) {
            this.filter = filterObject.getText();
            this.position = position;

            filter_text.setText(filter);
            if(filterObject.isChosen()) {
                circle_filter_view.setVisibility(View.VISIBLE);
            } else {
                circle_filter_view.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View v) {
            onFilterClickListener.onFilterClick(filter, position);
        }
    }

}

package com.company.art_and_culture.myarts.filter_explore_fragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.company.art_and_culture.myarts.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FilterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private OnFilterClickListener onFilterClickListener;
    private int displayWidth, displayHeight;
    private int filterSpanCount;
    private ArrayList<String> itemsList = new ArrayList<>();

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

    public void setItems(Collection<String> items){
        itemsList.addAll(items);
        notifyDataSetChanged();
    }

    public List<String> getItems() {
        return itemsList;
    }

    public void clearItems(){
        itemsList.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_filter, parent, false);
        return new ArtistsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        String filter = itemsList.get(position);
        if (filter != null) {
            ArtistsViewHolder viewHolder = (ArtistsViewHolder) holder;
            viewHolder.bind(filter, position);
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


    class ArtistsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private String filter;
        private int position;
        private TextView filter_text;

        ArtistsViewHolder(@NonNull View itemView) {
            super(itemView);

            itemView.getLayoutParams().height = displayWidth / filterSpanCount;
            itemView.getLayoutParams().width = displayWidth / filterSpanCount;
            itemView.setOnClickListener(this);

            filter_text = itemView.findViewById(R.id.filter_text);
        }

        void bind(String filter, int position) {
            this.filter = filter;
            this.position = position;

            filter_text.setText(filter);
        }

        @Override
        public void onClick(View v) {
            onFilterClickListener.onFilterClick(filter, position);
        }
    }

}

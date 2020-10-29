package com.company.art_and_culture.myarts.filter_maker_fragment;

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

public class DateAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private OnDateClickListener onDateClickListener;
    private int displayWidth, displayHeight;
    private int filterSpanCount;
    private double k = 0.5;
    private ArrayList<String> itemsList = new ArrayList<>();

    public DateAdapter(Context context, OnDateClickListener onDateClickListener, int displayWidth, int displayHeight, int filterSpanCount) {
        this.context = context;
        this.onDateClickListener = onDateClickListener;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
        this.filterSpanCount = filterSpanCount;
    }

    public interface OnDateClickListener {
        void onDateClick(String item, int position);
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
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date_all, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date, parent, false);
        }
        return new ArtistsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        String date = itemsList.get(position);
        if (date != null) {
            ArtistsViewHolder viewHolder = (ArtistsViewHolder) holder;
            viewHolder.bind(date, position);
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

    public double getK() {
        return k;
    }

    class ArtistsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private String date;
        private int position;
        private TextView date_text;

        ArtistsViewHolder(@NonNull View itemView) {
            super(itemView);

            itemView.getLayoutParams().height = (int) ((displayWidth / filterSpanCount) * k);
            itemView.getLayoutParams().width = displayWidth / filterSpanCount;
            itemView.setOnClickListener(this);

            date_text = itemView.findViewById(R.id.date_text);
        }

        void bind(String date, int position) {
            this.date = date;
            this.position = position;

            String text;
            if(tryParseInt(date)) {
                if (Integer.parseInt(date)>20) {
                    text = context.getResources().getString(R.string.now);
                } else {
                    text = date + context.getString(R.string.zero_zero_date_filter);
                }
            } else {
                text = date;
            }
            date_text.setText(text);
        }

        boolean tryParseInt(String value) {
            try {
                Integer.parseInt(value);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        @Override
        public void onClick(View v) {
            onDateClickListener.onDateClick(date, position);
        }
    }

}

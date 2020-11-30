package com.company.art_and_culture.myarts.bottom_menu.favorites.Folders;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.pojo.Folder;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

class FoldersAdapter extends RecyclerView.Adapter<FoldersAdapter.FoldersViewHolder> {

    private Context context;
    private OnFolderClickListener onFolderClickListener;
    private int displayWidth, displayHeight, spanCount;
    private List<Folder> folderList=new ArrayList<>();
    private int lastPosition = -1;

    public FoldersAdapter(Context context, OnFolderClickListener onFolderClickListener, int displayWidth, int displayHeight, int spanCount) {
        this.context = context;
        this.onFolderClickListener = onFolderClickListener;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
        this.spanCount = spanCount;
    }

    public interface OnFolderClickListener {
        void onFolderImageClick(Folder folder, int position);
    }

    public void setItems(Collection<Folder> folders){
        folderList.addAll(folders);
        notifyDataSetChanged();
    }

    public List<Folder> getItems() {
        return folderList;
    }

    public void clearItems(){
        folderList.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FoldersAdapter.FoldersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_folder, parent, false);
        return new FoldersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoldersAdapter.FoldersViewHolder holder, int position) {
        Folder folder = folderList.get(position);
        if (folder != null) {
            holder.bind(folder, position);
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
        return folderList.size();
    }

    class FoldersViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Folder folder;
        private int position;
        private ImageView folder_image;
        private TextView folder_title, folder_count;

        FoldersViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.getLayoutParams().width = displayWidth/spanCount;
            itemView.getLayoutParams().height = (int) (displayWidth/spanCount * 0.7);
            folder_image = itemView.findViewById(R.id.folder_image);
            folder_title = itemView.findViewById(R.id.folder_title);
            folder_count = itemView.findViewById(R.id.folder_count);
            folder_image.setOnClickListener(this);
        }

        void bind(Folder folder, int position) {
            this.folder = folder;
            this.position = position;

            if (folder.getArtImageUrl() != null && folder.getArtImageUrl().startsWith(context.getResources().getString(R.string.http))) {
                Picasso.get().load(folder.getArtImageUrl()).placeholder(R.color.colorSilver).into(folder_image);
            }

            folder_title.setText(folder.getTitle());
            String text = folder.getItemsCount()+" "+context.getResources().getString(R.string.items);
            folder_count.setText(text);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == folder_image.getId()) {
                onFolderClickListener.onFolderImageClick(folder, position);
            }
        }
    }
}

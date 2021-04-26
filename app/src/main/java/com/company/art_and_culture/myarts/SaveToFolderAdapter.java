package com.company.art_and_culture.myarts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.company.art_and_culture.myarts.pojo.Folder;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SaveToFolderAdapter extends RecyclerView.Adapter<SaveToFolderAdapter.FoldersViewHolder> {

    private Context context;
    private OnFolderClickListener onFolderClickListener;
    private int displayWidth, displayHeight;
    private List<Folder> folderList=new ArrayList<>();

    public SaveToFolderAdapter(Context context, OnFolderClickListener onFolderClickListener, int displayWidth, int displayHeight) {
        this.context = context;
        this.onFolderClickListener = onFolderClickListener;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
    }

    public interface OnFolderClickListener {
        void onFolderClick(Folder folder, int position);
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
    public FoldersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_save_to_folder, parent, false);
        return new FoldersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoldersViewHolder holder, int position) {
        Folder folder = folderList.get(position);
        if (folder != null) {
            holder.bind(folder, position);
        } else {
            // Null defines a placeholder item - PagedListAdapter will automatically invalidate
            // this row when the actual object is loaded from the database
            // holder.clear();
        }
    }

    @Override
    public int getItemCount() {
        return folderList.size();
    }

    public class FoldersViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Folder folder;
        private int position;
        private ImageView folder_image;
        private TextView folder_title, folder_count;

        FoldersViewHolder(@NonNull View itemView) {
            super(itemView);
            folder_image = itemView.findViewById(R.id.folder_image);
            folder_title = itemView.findViewById(R.id.folder_title);
            folder_count = itemView.findViewById(R.id.folder_count);
            itemView.setOnClickListener(this);
        }

        void bind(Folder folder, int position) {
            this.folder = folder;
            this.position = position;

            if (folder.getArtImageUrl() != null && folder.getArtImageUrl().startsWith(context.getResources().getString(R.string.http))) {
                if (folder.getArtWidth() > 0) {
                    int imgWidth = displayWidth / 3;
                    int imgHeight = (folder.getArtHeight() * imgWidth) / folder.getArtWidth();
                    Picasso.get().load(folder.getArtImageUrl()).resize(imgWidth, imgHeight).onlyScaleDown().into(folder_image);
                } else {
                    Picasso.get().load(folder.getArtImageUrl()).into(folder_image);
                }
            }

            folder_title.setText(folder.getTitle());
            String text = folder.getItemsCount()+" "+context.getResources().getString(R.string.items);
            folder_count.setText(text);
        }

        @Override
        public void onClick(View v) {
            onFolderClickListener.onFolderClick(folder, position);
        }
    }
}

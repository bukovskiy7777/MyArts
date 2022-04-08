package com.company.art_and_culture.myarts.museum_fragment

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.company.art_and_culture.myarts.LifecycleViewHolder
import com.company.art_and_culture.myarts.R
import com.company.art_and_culture.myarts.pojo.Art
import com.squareup.picasso.Picasso
import com.squareup.picasso.Picasso.LoadedFrom
import com.squareup.picasso.Target
import kotlinx.coroutines.launch

class ArtMuseumAdapter(
    private val displayWidth: Int,
    private val displayHeight: Int,
    private val spanCount: Int,
    private val onArtClickListener: OnArtClickListener,
    private val museumViewModel: MuseumViewModel,
    private val context: Context): PagingDataAdapter<Art, ArtMuseumAdapter.ArtMuseumViewHolder>(ART_COMPARATOR) {


    override fun onViewAttachedToWindow(holder: ArtMuseumViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.onAttached()
    }

    override fun onViewDetachedFromWindow(holder: ArtMuseumViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.onDetached()
    }

    interface OnArtClickListener {
        fun onArtImageClick(art: Art?, itemPosition: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtMuseumViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_art_museum, parent, false)
        return ArtMuseumViewHolder(view, displayWidth, displayHeight, spanCount, museumViewModel, context, onArtClickListener)
    }

    override fun onBindViewHolder(holder: ArtMuseumViewHolder, itemPosition: Int) {
        val art = getItem(itemPosition)
        if (art != null) {
            holder.bind(art, itemPosition)
        } else {
            // Null defines a placeholder item - PagedListAdapter will automatically invalidate
            // this row when the actual object is loaded from the database
            //holder.clear();
        }
    }

    companion object {
        private val ART_COMPARATOR = object : DiffUtil.ItemCallback<Art>() {
            override fun areItemsTheSame(oldItem: Art, newItem: Art): Boolean =
                oldItem.artId == newItem.artId

            override fun areContentsTheSame(oldItem: Art, newItem: Art): Boolean =
                oldItem.artId == newItem.artId && oldItem.isLiked == newItem.isLiked
        }
    }


    class ArtMuseumViewHolder(
        val itemView: View,
        val displayWidth: Int,
        val displayHeight: Int,
        val spanCount: Int,
        val museumViewModel: MuseumViewModel,
        val context: Context,
        val onArtClickListener: OnArtClickListener
    ) : LifecycleViewHolder(itemView), View.OnClickListener {
        private var art: Art? = null
        private var itemPosition = 0
        private lateinit var art_image: ImageView
        private var artImgUrl: String? = null
        private val target: Target?

        init {
            itemView.layoutParams.width = displayWidth / spanCount
            //itemView.getLayoutParams().height = displayWidth/spanCount;
            art_image = itemView.findViewById(R.id.art_image)
            art_image.setOnClickListener(this)

            target = try {
                object : Target {
                    override fun onBitmapLoaded(bitmap: Bitmap, from: LoadedFrom) {
                        val imgWidth: Int = displayWidth / spanCount
                        val imgHeight = bitmap.height * imgWidth / bitmap.width
                        if (imgHeight <= art_image.maxHeight) { art_image.layoutParams.height  = imgHeight
                        } else { art_image.layoutParams.height = art_image.maxHeight }
                        art?.artWidth = bitmap.width
                        art?.artHeight = bitmap.height

                        lifecycleScope.launch {
                            museumViewModel.writeDimensionsOnServer(art)
                        }

                        Picasso.get().load(artImgUrl).placeholder(R.color.colorSilver).into(art_image)
                    }

                    override fun onBitmapFailed(e: Exception, errorDrawable: Drawable) {}
                    override fun onPrepareLoad(placeHolderDrawable: Drawable) {}
                }
            } catch (e: Exception) {
                null
            }
        }

        fun bind(art: Art, itemPosition: Int) {
            this.art = art
            this.itemPosition = itemPosition
            artImgUrl = if (art.artImgUrlSmall != null && art.artImgUrlSmall.startsWith(context.resources.getString(R.string.http))) {
                art.artImgUrlSmall
            } else {
                art.artImgUrl
            }
            art_image.setImageDrawable(context.resources.getDrawable(R.drawable.art_placeholder))
            if (art.artWidth > 0) {
                val imgWidth: Int = displayWidth / spanCount
                val imgHeight = art.artHeight * imgWidth / art.artWidth
                    if (imgHeight <= art_image.maxHeight) {
                        art_image.layoutParams.height = imgHeight
                    } else {
                        art_image.layoutParams.height = art_image.maxHeight
                    }

                Picasso.get().load(artImgUrl).placeholder(R.color.colorSilver).resize(imgWidth, imgHeight).onlyScaleDown().into(art_image)
            } else {
                art_image.layoutParams.height = displayWidth / spanCount
                if (target != null) Picasso.get().load(artImgUrl).placeholder(R.color.colorSilver).into(target)
            }
        }

        override fun onClick(v: View) {
            if (v.id == art_image.id) {
                onArtClickListener.onArtImageClick(art, itemPosition)
            }
        }
    }


}
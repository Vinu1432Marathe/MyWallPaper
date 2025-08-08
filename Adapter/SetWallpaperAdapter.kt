package com.mahadev.shivahd.live.wallpaper.Adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mahadev.shivahd.live.wallpaper.Model.ModelWallpaper
import com.mahadev.shivahd.live.wallpaper.Other.NewSplashActivity
import com.mahadev.shivahd.live.wallpaper.Other.PrefsManager
import com.mahadev.shivahd.live.wallpaper.Other.SharedPrefsManager
import com.mahadev.shivahd.live.wallpaper.R


class SetWallpaperAdapter(
    private val context: Context,
    private var list: MutableList<ModelWallpaper>, private val selectedUrlList: MutableList<String>,
    private val onSelectionChanged: () -> Unit
) : RecyclerView.Adapter<SetWallpaperAdapter.VH>() {


    private var favoriteList: MutableList<ModelWallpaper> = PrefsManager.getFavoriteList(context)

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val imgWallpaper: ImageView = view.findViewById(R.id.imageView)
        val ivHeart: ImageView = view.findViewById(R.id.favIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.adpter_setwallpaper, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]

        NewSplashActivity.ImgUri = SharedPrefsManager.getImgUri(context)
        val imageUrl = NewSplashActivity.ImgUri + "hd/" + item.albumImage + ".webp"
        val imageUri = Uri.parse(imageUrl)

        Glide.with(context)
            .load(imageUrl)
            .placeholder(R.drawable.ic_placeholder)
            .into(holder.imgWallpaper)

        holder.ivHeart.visibility =
            if (selectedUrlList.contains(imageUrl)) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener {
            if (selectedUrlList.contains(imageUrl)) {
                selectedUrlList.remove(imageUrl)
            } else {
                selectedUrlList.add(imageUrl)
            }
            notifyItemChanged(position)
        }

//        val imageUrl = urlFirst + item.albumImage + ".webp"
//
//        Glide.with(context).load(urlFirst + item.albumImage + ".webp")
//            .placeholder(R.drawable.ic_placeholder).error(R.drawable.ic_placeholder)
//            .into(holder.imgWallpaper)
//
//        val isSelected = selectedUrlList.contains(item)
//        holder.ivHeart.visibility = if (isSelected) View.VISIBLE else View.GONE
//        // Set heart icon based on selection
//
//        holder.itemView.setOnClickListener {
//
//            if (selectedUrlList.contains(imageUrl)) {
//                selectedUrlList.remove(imageUrl)
//                holder.ivHeart.visibility = View.GONE
//            } else {
//                selectedUrlList.add(imageUrl)
//                holder.ivHeart.visibility = View.VISIBLE
//            }
//
//            onSelectionChanged()
//        }
    }

    override fun getItemCount() = list.size


    fun updateList(newList: List<ModelWallpaper>) {
        // keep a quick‑lookup set of IDs that are already in the adapter
        val existingIds = list.map { it.imageID }.toHashSet()

        val itemsToAdd = newList.filter { it.imageID !in existingIds }

        if (itemsToAdd.isNotEmpty()) {
            val start = list.size
            list.addAll(itemsToAdd)
            notifyItemRangeInserted(start, itemsToAdd.size)
        }
    }
}



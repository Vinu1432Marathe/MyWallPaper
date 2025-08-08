package com.mahadev.shivahd.live.wallpaper.Adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.addsdemo.mysdk.utils.UtilsClass
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.mahadev.shivahd.live.wallpaper.Activity.WallpaperShowActivity
import com.mahadev.shivahd.live.wallpaper.Model.ModelWallpaper
import com.mahadev.shivahd.live.wallpaper.Other.NewSplashActivity
import com.mahadev.shivahd.live.wallpaper.Other.PrefsManager
import com.mahadev.shivahd.live.wallpaper.Other.SharedPrefsManager
import com.mahadev.shivahd.live.wallpaper.R


class HoriWallpaperAdapter(
    private val context: Context,
    private var list: List<ModelWallpaper>
) : RecyclerView.Adapter<HoriWallpaperAdapter.VH>() {

    private var favoriteList: MutableList<ModelWallpaper> = PrefsManager.getFavoriteList(context)

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val imgWallpaper: ImageView = view.findViewById(R.id.imageView)
        val ivHeart: ImageView = view.findViewById(R.id.favIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.adpter_horiwallpaper, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]

//        Glide.with(context).load(urlFirst+item.albumImage+".webp").into(holder.imgWallpaper)
        NewSplashActivity.ImgUri = SharedPrefsManager.getImgUri(context)
        Glide.with(context).load(NewSplashActivity.ImgUri + "hd/" + item.albumImage + ".webp")
            .placeholder(R.drawable.ic_placeholder).error(R.drawable.ic_placeholder)
            .into(holder.imgWallpaper)


        val isFavorite = favoriteList.contains(item)
        holder.ivHeart.setImageResource(
            if (isFavorite) R.drawable.ic_fill else R.drawable.ic_unfill
        )

        val wallpaper = ModelWallpaper(
            imageID = item.imageID,
            categoryID = item.categoryID,
            albumImage = item.albumImage,
            totalLikes = item.totalLikes,
            totalDownloads = item.totalDownloads,
            creatorInfo = item.creatorInfo ?: "",
            creatorLink = item.creatorLink ?: "",
            licence = item.licence ?: "" // ✅ default value for null
        )

        holder.itemView.setOnClickListener {

            val intent = Intent(context, WallpaperShowActivity::class.java)
                .putExtra("imageID", Gson().toJson(wallpaper))
            UtilsClass.startSpecialActivity(context as Activity?, intent, false)

        }

        holder.ivHeart.setOnClickListener {
            if (favoriteList.contains(item)) {
                favoriteList.remove(item)
                holder.ivHeart.setImageResource(R.drawable.ic_unfill)
                Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show()
            } else {
                favoriteList.add(item)
                holder.ivHeart.setImageResource(R.drawable.ic_fill)
                Toast.makeText(context, "Added to favorites", Toast.LENGTH_SHORT).show()
            }
            PrefsManager.saveFavoriteList(context, favoriteList)
            notifyItemChanged(position)
        }
    }

    override fun getItemCount() = list.size

//    fun updateList(newList: List<ModelWallpaper>) {
//        list = newList
//        notifyDataSetChanged()
//    }

    fun updateList(newList: List<ModelWallpaper>) {
        list = if (newList.size > 10) {
            newList.subList(0, 10)
        } else {
            newList
        }
        notifyDataSetChanged()
    }

}


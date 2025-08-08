package com.mahadev.shivahd.live.wallpaper.Adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.addsdemo.mysdk.ADPrefrences.Ads_Listing_Adapter
import com.addsdemo.mysdk.utils.UtilsClass
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.mahadev.shivahd.live.wallpaper.Activity.WallpaperShowActivity
import com.mahadev.shivahd.live.wallpaper.Model.ModelWallpaper
import com.mahadev.shivahd.live.wallpaper.Other.NewSplashActivity
import com.mahadev.shivahd.live.wallpaper.Other.SharedPrefsManager
import com.mahadev.shivahd.live.wallpaper.R

class FavWallpaperAdapter(
    private val context: Context,
    private var list: List<ModelWallpaper>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private val mergedList: ArrayList<Any> = ArrayList()
    internal val TYPE_NORMAL = 0
    private val TYPE_AD = 1
    private val SHOW_AD = "SHOW_AD"
    private var nextAdPosition = 2 // First ad at position 2

    init {
        Ads_Listing_Adapter.admob_nativehashmap?.clear()
        mergeDataToList(list)
    }

    override fun getItemViewType(position: Int): Int {
        return if (mergedList[position] is String) TYPE_AD else TYPE_NORMAL
    }

    private fun mergeDataToList(newItems: List<ModelWallpaper>) {
        for (item in newItems) {
            mergedList.add(item)
            if (mergedList.size == nextAdPosition) {
                mergedList.add(SHOW_AD)
                nextAdPosition += 7
            }
        }
    }

    inner class AdViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val llline_full: LinearLayout = itemView.findViewById(R.id.llline_full)
        val llnative_full: LinearLayout = itemView.findViewById(R.id.llnative_full)
    }


    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val imgWallpaper: ImageView = view.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_AD) {
            val adView = LayoutInflater.from(context).inflate(R.layout.ad_layout, parent, false)
            AdViewHolder(adView)
        } else {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.adapter_favwallpaper, parent, false)
            return VH(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == TYPE_AD) {
            if (UtilsClass.remoteConfigModel != null && context is Activity) {
                (holder as AdViewHolder).llline_full.layoutParams.height =
                    300
                Ads_Listing_Adapter.NativeFull_Show(
                    context,
                    holder.llnative_full,
                    holder.llline_full,
                    "small",
                    position
                )
            }
        } else {
//            val item = list[position]

            val item = mergedList[position] as ModelWallpaper
            val viewHolder: VH = holder as VH
            NewSplashActivity.ImgUri = SharedPrefsManager.getImgUri(context)
            Glide.with(context).load(NewSplashActivity.ImgUri + "hd/" + item.albumImage + ".webp")
                .placeholder(R.drawable.ic_placeholder).error(R.drawable.ic_placeholder)
                .into(viewHolder.imgWallpaper)

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
        }

    }

    override fun getItemCount() = mergedList.size

    fun updateList(newList: List<ModelWallpaper>) {
        list = newList
        notifyDataSetChanged()
    }
}

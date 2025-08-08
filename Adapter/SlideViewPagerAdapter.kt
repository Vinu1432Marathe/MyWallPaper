package com.mahadev.shivahd.live.wallpaper.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mahadev.shivahd.live.wallpaper.Model.Model_slide
import com.mahadev.shivahd.live.wallpaper.R

class SlideViewPagerAdapter(private val items: List<Model_slide>) :
    RecyclerView.Adapter<SlideViewPagerAdapter.PageViewHolder>() {

    inner class PageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val txtTitle: TextView = itemView.findViewById(R.id.txtTitle)
//        val txtDis: TextView = itemView.findViewById(R.id.txtDis)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.page_item, parent, false)
        return PageViewHolder(view)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
//        holder.textView.text = items[position]
        val modelSlide = items.get(position)

        holder.imageView.setImageResource(modelSlide.Image)
        holder.txtTitle.text = modelSlide.Title
//        holder.txtDis.text = modelSlide.Dis


    }

    override fun getItemCount(): Int = items.size
}

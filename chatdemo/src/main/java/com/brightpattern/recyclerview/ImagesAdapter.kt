package com.brightpattern.recyclerview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.brightpattern.chatdemo.R
import com.google.android.material.imageview.ShapeableImageView

@SuppressLint("NotifyDataSetChanged")
class ImagesAdapter : RecyclerView.Adapter<ImagesAdapter.MyViewHolder>() {

    var items: MutableList<Uri> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = items.size

    class MyViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val imageView: ShapeableImageView = v.findViewById(R.id.itemImage)
    }

    fun removedAt(position: Int) {
        notifyItemRemoved(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val context: Context = parent.context
        val layoutInflater = LayoutInflater.from(context)
        return MyViewHolder(layoutInflater.inflate(R.layout.item_image, parent, false))
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val uri = items[position]
        val context = holder.imageView.context
        val bitmap = context.contentResolver.openInputStream(uri).use { data ->
            BitmapFactory.decodeStream(data)
        }
        holder.imageView.setImageBitmap(bitmap)

    }
}
package com.example.GamrUI

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView


class MediaAdapter(private var mediaUris: List<Uri>) : RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    inner class MediaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val mediaView: ImageView = view.findViewById(R.id.imageViewMedia)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_media, parent, false)
        return MediaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val uri = mediaUris[position]
        val context = holder.mediaView.context
        val contentResolver = context.contentResolver
        val type = contentResolver.getType(uri)
        holder.mediaView.setImageURI(uri)  // You could use Glide/Picasso for videos

        // Force square items
        holder.mediaView.post {
            val width = holder.mediaView.width
            holder.mediaView.layoutParams.height = width
            holder.mediaView.requestLayout()
        }

        val playIcon = holder.itemView.findViewById<ImageView>(R.id.playIcon)
        if (type != null && type.startsWith("video")) {
            playIcon.visibility = View.VISIBLE
        } else {
            playIcon.visibility = View.GONE
        }

    }

    override fun getItemCount(): Int = mediaUris.size

    fun updateMedia(newUris: List<Uri>) {
        mediaUris = newUris
        notifyDataSetChanged()
    }
}

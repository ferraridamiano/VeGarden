package com.example.vegarden

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class PostsAdapter(private val postsList: List<PostsViewModel>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_TEXT = 1
        const val VIEW_TYPE_PHOTO = 2
    }

    private inner class TextViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var textview: TextView = itemView.findViewById(R.id.textView)
        fun bind(position: Int) {
            val recyclerViewModel = postsList[position]
            textview.text = recyclerViewModel.textOrUrl
        }
    }

    private inner class PhotoViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView = itemView.findViewById(R.id.ivPhoto)
        fun bind(position: Int) {
            val recyclerViewModel = postsList[position]
            Picasso.get().load(recyclerViewModel.textOrUrl).into(imageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_TEXT) {
            return TextViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.post_text, parent, false)
            )
        }
        // else photo
        return PhotoViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.post_photo, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (postsList[position].viewType == VIEW_TYPE_TEXT) {
            (holder as TextViewHolder).bind(position)
        } else {
            (holder as PhotoViewHolder).bind(position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return postsList[position].viewType
    }

    override fun getItemCount(): Int = postsList.size

}
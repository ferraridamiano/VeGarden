package com.example.vegarden

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PostsAdapter(private val postsList: List<PostsViewModel>) :
    RecyclerView.Adapter<PostsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.post, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val itemsViewModel = postsList[position]

        holder.textView.text = itemsViewModel.text

    }

    override fun getItemCount(): Int = postsList.size

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val textView: TextView = itemView.findViewById(R.id.textView)
    }
}
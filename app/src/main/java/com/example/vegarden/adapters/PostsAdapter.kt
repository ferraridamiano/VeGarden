package com.example.vegarden.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.vegarden.PostsViewModel
import com.example.vegarden.R
import com.example.vegarden.fragments.GardenFragment
import com.squareup.picasso.Picasso
import java.util.Calendar
import java.util.Date

class PostsAdapter(private val postsList: List<PostsViewModel>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TEXT = 1
        const val PHOTO = 2
    }

    private inner class TextViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvPostText: TextView = itemView.findViewById(R.id.tvPostText)
        var tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        var tvUser: TextView = itemView.findViewById(R.id.tvUser)

        fun bind(position: Int) {
            val recyclerViewModel = postsList[position]
            tvPostText.text = recyclerViewModel.textOrUrl
            tvTimestamp.text = getDaysSincePost(recyclerViewModel.timestamp)
            if (recyclerViewModel.userNameSurname == null)
                tvUser.visibility = View.GONE
            else {
                tvUser.text = recyclerViewModel.userNameSurname
                tvUser.setOnClickListener {
                    val bundle = Bundle()
                    bundle.putString("gardenUserUid", recyclerViewModel.postUserUid)
                    bundle.putBoolean("isMyGarden", false)
                    val gardenFragment = GardenFragment()
                    gardenFragment.arguments = bundle
                    val activity = itemView.context as AppCompatActivity
                    activity.supportFragmentManager.beginTransaction()
                        .replace(R.id.flFragment, gardenFragment)
                        .addToBackStack(null).commit()
                }
            }
        }
    }

    private inner class PhotoViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView = itemView.findViewById(R.id.ivPhoto)
        var tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        var tvUser: TextView = itemView.findViewById(R.id.tvUser)

        fun bind(position: Int) {
            val recyclerViewModel = postsList[position]
            Picasso.get().load(recyclerViewModel.textOrUrl).into(imageView)
            tvTimestamp.text = getDaysSincePost(recyclerViewModel.timestamp)
            if (recyclerViewModel.userNameSurname == null)
                tvUser.visibility = View.GONE
            else {
                tvUser.text = recyclerViewModel.userNameSurname
                tvUser.setOnClickListener {
                    val bundle = Bundle()
                    bundle.putString("gardenUserUid", recyclerViewModel.postUserUid)
                    bundle.putBoolean("isMyGarden", false)
                    val gardenFragment = GardenFragment()
                    gardenFragment.arguments = bundle
                    val activity = itemView.context as AppCompatActivity
                    activity.supportFragmentManager.beginTransaction()
                        .replace(R.id.flFragment, gardenFragment)
                        .addToBackStack(null).commit()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TEXT) {
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

        if (postsList[position].viewType == TEXT) {
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

private fun getDaysSincePost(postDate: Date): String {
    val daysSincePost =
        (Calendar.getInstance().time.time - postDate.time).floorDiv(86_400_000)

    if (daysSincePost < 1)
        return "Today"
    else if (daysSincePost < 2)
        return "Yesterday"
    return "$daysSincePost days ago"
}


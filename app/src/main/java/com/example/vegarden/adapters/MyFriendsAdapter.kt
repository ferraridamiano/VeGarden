package com.example.vegarden.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vegarden.R
import com.example.vegarden.models.MyFriendsViewModel
import com.squareup.picasso.Picasso

class MyFriendsAdapter(private val friendsList: List<MyFriendsViewModel>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private inner class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        var ivProfilePhoto: ImageView = itemView.findViewById(R.id.ivProfilePhoto)
        var tvNameSurname: TextView = itemView.findViewById(R.id.tvNameSurname)

        fun bind(position: Int){
            val recyclerViewModel = friendsList[position]
            if(recyclerViewModel.profilePhoto == null){
                ivProfilePhoto.setImageResource(R.drawable.ic_account_circle)
            } else {
                Picasso.get().load(recyclerViewModel.profilePhoto).into(ivProfilePhoto)
            }
            tvNameSurname.text = recyclerViewModel.nameSurname
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.my_friend_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return friendsList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bind(position)
    }
}

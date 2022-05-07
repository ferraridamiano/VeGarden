package com.example.vegarden.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vegarden.R
import com.example.vegarden.adapters.MyFriendsAdapter
import com.example.vegarden.adapters.PostsAdapter
import com.example.vegarden.databinding.ActivityMyFriendsBinding
import com.example.vegarden.fragments.GardenFragment
import com.example.vegarden.models.MyFriendsViewModel
import com.example.vegarden.models.PostsViewModel
import com.example.vegarden.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class MyFriendsActivity : AppCompatActivity() {

    lateinit var binding: ActivityMyFriendsBinding
    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMyFriendsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore
    }

    override fun onResume() {
        super.onResume()
        refreshFriends()
    }

    private fun refreshFriends() {
        binding.rvMyFriends.layoutManager = LinearLayoutManager(this)
        val arrayFriends = ArrayList<MyFriendsViewModel>()
        val adapter = MyFriendsAdapter(arrayFriends)
        binding.rvMyFriends.adapter = adapter

        adapter.onItemClick = { friend ->
            Log.d("Damiano", friend.uid)
            /*val bundle = Bundle()
            bundle.putString("gardenUserUid", friend.uid)
            bundle.putBoolean("isMyGarden", false)
            val gardenFragment = GardenFragment()
            gardenFragment.arguments = bundle
            getActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.flFragment, gardenFragment)
                .addToBackStack(null).commit()*/
        }

        db.collection("users").document(auth.currentUser!!.uid).get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)!!
                db.collection("users").whereIn("uid", user.myFriends).get()
                    .addOnSuccessListener { documents ->
                        documents.forEach { document ->
                            val friend = document.toObject(User::class.java)
                            arrayFriends.add(
                                MyFriendsViewModel(
                                    friend.uid!!,
                                    "${friend.name} ${friend.surname}",
                                    friend.profilePhoto
                                )
                            )
                        }
                        adapter.notifyItemRangeChanged(0, documents.size())
                    }
            }


    }
}
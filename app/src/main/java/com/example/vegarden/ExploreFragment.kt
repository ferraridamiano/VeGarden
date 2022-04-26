package com.example.vegarden

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vegarden.databinding.FragmentExploreBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class ExploreFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var _binding: FragmentExploreBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        db = Firebase.firestore
        storage = Firebase.storage
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refreshPosts()
    }

    override fun onResume() {
        super.onResume()
        refreshPosts()
    }

    private fun refreshPosts() {
        binding.rvPosts.layoutManager = LinearLayoutManager(requireContext())
        val arrayPosts = ArrayList<PostsViewModel>()
        val adapter = PostsAdapter(arrayPosts)
        binding.rvPosts.adapter = adapter
        db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING).limit(10)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(requireContext(), "No posts found", Toast.LENGTH_SHORT).show()
                } else {
                    val userUids = mutableSetOf<String>()
                    // Reduce all the UIDs with get rid of non repeating elements
                    documents.forEach { userUids.add(it.data["user"] as String) }
                    val mapUidNameSurname = mutableMapOf<String, String>()
                    db.collection("users").whereIn("uid", userUids.toList()).get()
                        .addOnSuccessListener { usersData ->
                            usersData.forEach { userData ->
                                val name = userData.data["name"] as String
                                val surname = userData.data["surname"] as String
                                mapUidNameSurname[userData.data["uid"] as String] = "$name $surname"
                            }
                            documents.forEach { postData ->
                                arrayPosts.add(
                                    PostsViewModel(
                                        if (postData["type"].toString() == "post")
                                            PostsAdapter.TEXT
                                        else PostsAdapter.PHOTO,
                                        postData["content"] as String,
                                        (postData["timestamp"] as Timestamp).toDate(),
                                        mapUidNameSurname[postData["user"] as String]
                                    )
                                )
                            }
                            adapter.notifyItemRangeChanged(0, arrayPosts.size)
                        }
                }
            }
    }
}
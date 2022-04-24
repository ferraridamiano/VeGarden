package com.example.vegarden

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vegarden.databinding.FragmentExploreBinding
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
        db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING).limit(10)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(requireContext(), "No posts found", Toast.LENGTH_SHORT).show()
                } else {
                    binding.rvPosts.layoutManager = LinearLayoutManager(requireContext())
                    val data = ArrayList<PostsViewModel>()
                    documents.forEach { document ->
                        if(document.data["type"].toString() == "post"){
                            data.add(PostsViewModel(PostsAdapter.VIEW_TYPE_TEXT, document.data["content"].toString()))
                        } else { // is a photo
                            data.add(PostsViewModel(PostsAdapter.VIEW_TYPE_PHOTO, document.data["content"].toString()))
                        }
                    }
                    binding.rvPosts.adapter = PostsAdapter(data)
                }
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
                Toast.makeText(requireContext(), "Error connecting to internet", Toast.LENGTH_SHORT)
                    .show()
            }
    }
}
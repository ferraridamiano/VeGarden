package com.example.vegarden

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vegarden.databinding.FragmentFeedBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class FeedFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var _binding: FragmentFeedBinding? = null

    // This value is true if is the explore fragment (3rd item in the menu) or false if it is the
    // friends fragment (2nd item in the menu)
    private var isExploreFragment = true

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        isExploreFragment = arguments?.getBoolean("isExploreFragment")!!
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
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
        if (isExploreFragment) {
            db.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING).limit(10)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        Snackbar.make(
                            requireActivity().findViewById(R.id.flFragment),
                            "There are no posts",
                            Snackbar.LENGTH_SHORT
                        ).setAnchorView(requireActivity().findViewById(R.id.bottomNavigationBar))
                            .setAction("") {}.show()
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
                                    mapUidNameSurname[userData.data["uid"] as String] =
                                        "$name $surname"
                                }
                                documents.forEach { postData ->
                                    val postUserUid = postData["user"] as String
                                    // Doesn't show my posts
                                    if (postUserUid != auth.currentUser!!.uid) {
                                        arrayPosts.add(
                                            PostsViewModel(
                                                if (postData["type"].toString() == "post")
                                                    PostsAdapter.TEXT
                                                else PostsAdapter.PHOTO,
                                                postData["content"] as String,
                                                (postData["timestamp"] as Timestamp).toDate(),
                                                mapUidNameSurname[postUserUid],
                                                postUserUid
                                            )
                                        )
                                    }
                                }
                                adapter.notifyItemRangeChanged(0, arrayPosts.size)
                            }
                    }
                }
        } else { // Friends fragment
            db.collection("users").document(auth.currentUser!!.uid).get()
                .addOnSuccessListener { document ->
                    val user = document.toObject(User::class.java)
                    if (user!!.myFriends.isEmpty()) {
                        Snackbar.make(
                            requireActivity().findViewById(R.id.flFragment),
                            "There are no posts of your friends",
                            Snackbar.LENGTH_SHORT
                        ).setAnchorView(requireActivity().findViewById(R.id.bottomNavigationBar))
                            .setAction("") {}.show()
                    } else {
                        db.collection("posts").whereIn("user", user.myFriends)
                            .orderBy("timestamp", Query.Direction.DESCENDING).limit(10)
                            .get()
                            .addOnSuccessListener { documents ->
                                if (documents.isEmpty) {
                                    Toast.makeText(
                                        requireContext(),
                                        "No posts found",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
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
                                                mapUidNameSurname[userData.data["uid"] as String] =
                                                    "$name $surname"
                                            }
                                            documents.forEach { postData ->
                                                val postUserUid = postData["user"] as String
                                                // Doesn't show my posts
                                                if (postUserUid != auth.currentUser!!.uid) {
                                                    arrayPosts.add(
                                                        PostsViewModel(
                                                            if (postData["type"].toString() == "post")
                                                                PostsAdapter.TEXT
                                                            else PostsAdapter.PHOTO,
                                                            postData["content"] as String,
                                                            (postData["timestamp"] as Timestamp).toDate(),
                                                            mapUidNameSurname[postUserUid],
                                                            postUserUid
                                                        )
                                                    )
                                                }
                                            }
                                            adapter.notifyItemRangeChanged(0, arrayPosts.size)
                                        }
                                }
                            }
                    }
                }

        }
    }
}
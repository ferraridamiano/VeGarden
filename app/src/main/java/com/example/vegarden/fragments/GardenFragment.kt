package com.example.vegarden.fragments

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vegarden.R
import com.example.vegarden.activities.AddPostActivity
import com.example.vegarden.activities.ChangePlotActivity
import com.example.vegarden.adapters.PostsAdapter
import com.example.vegarden.databinding.FragmentGardenBinding
import com.example.vegarden.getPlotDrawable
import com.example.vegarden.models.GardenPlot
import com.example.vegarden.models.PostsViewModel
import com.example.vegarden.models.User
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
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView
import java.io.Serializable
import java.util.Calendar
import kotlin.collections.ArrayList

class GardenFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth
    private var _binding: FragmentGardenBinding? = null
    private var isMyGarden = true
    private lateinit var gardenUserUid: String
    private var postsLoaded = false
    private var gardenLoaded = false

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        gardenUserUid = arguments?.getString("gardenUserUid")!!
        isMyGarden = arguments?.getBoolean("isMyGarden")!!
        _binding = FragmentGardenBinding.inflate(inflater, container, false)
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
        // Appbar
        activity?.title =
            resources.getString(if (isMyGarden) R.string.my_vegarden else R.string.app_name)
        //Change title of posts
        if (isMyGarden) {
            binding.tvPostsPhotos.text = resources.getString(R.string.my_posts_and_photos)
            // Speed dial
            binding.speedDial.addActionItem(
                SpeedDialActionItem.Builder(R.id.addPhoto, R.drawable.ic_add_a_photo)
                    .setLabel(getString(R.string.post_a_photo))
                    .setFabBackgroundColor(
                        ResourcesCompat.getColor(
                            resources,
                            R.color.secondary,
                            null
                        )
                    )
                    .setFabImageTintColor(ResourcesCompat.getColor(resources, R.color.black, null))
                    .create()
            )
            binding.speedDial.addActionItem(
                SpeedDialActionItem.Builder(R.id.addPost, R.drawable.ic_post_add)
                    .setLabel(getString(R.string.write_a_post))
                    .setFabBackgroundColor(
                        ResourcesCompat.getColor(
                            resources,
                            R.color.secondary,
                            null
                        )
                    )
                    .setFabImageTintColor(ResourcesCompat.getColor(resources, R.color.black, null))
                    .create()
            )

            binding.speedDial.setOnActionSelectedListener(SpeedDialView.OnActionSelectedListener { actionItem ->
                when (actionItem.id) {
                    R.id.addPhoto -> {
                        //fileChooser.launch("image/*")
                        pickImageFromGallery()
                        binding.speedDial.close()
                        return@OnActionSelectedListener true // close with animation
                    }
                    R.id.addPost -> {
                        startActivity(Intent(requireContext(), AddPostActivity::class.java))
                        binding.speedDial.close()
                        return@OnActionSelectedListener true // close with animation
                    }
                }
                false
            })
            // main FAB icon color
            binding.speedDial.mainFab.supportImageTintList = ColorStateList.valueOf(resources.getColor(R.color.black, null))
        } else {
            db.collection("users").document(gardenUserUid).get()
                .addOnSuccessListener { document ->
                    val name = document["name"] as String
                    binding.tvPostsPhotos.text = getString(R.string.user_posts_and_photos, name)
                    activity?.title = getString(R.string.user_vegarden, name)
                }

            binding.speedDial.visibility = View.GONE

            val firestoreRef = db.collection("users").document(auth.currentUser!!.uid)
            firestoreRef.get().addOnSuccessListener { document ->
                binding.fabAddRemoveFriend.visibility = View.VISIBLE
                val currentUser = document.toObject(User::class.java)!!
                // Already friends
                if (currentUser.myFriends.contains(gardenUserUid)) setFabToRemoveFriend(currentUser)
                // Not yet friends
                else setFabToAddFriend(currentUser)
            }
        }
    }

    private fun pickImageFromGallery() {
        //Intent to pick image
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    companion object {
        //image pick code
        private val IMAGE_PICK_CODE = 1000
        //Permission code
        private val PERMISSION_CODE = 1001
    }

    //handle requested permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                    pickImageFromGallery()
                }
            }
        }
    }

    //handle result of picked image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE){
            val imageUri = data?.data!!
            val ref =
                storage.reference.child("images/$gardenUserUid-${System.currentTimeMillis()}")
            ref.putFile(imageUri).continueWithTask { task ->
                if (!task.isSuccessful) {
                    Snackbar.make(
                        requireActivity().findViewById(R.id.flFragment),
                        getString(R.string.error_picking_image),
                        Snackbar.LENGTH_SHORT
                    ).setAnchorView(requireActivity().findViewById(R.id.speedDial)).show()
                }
                ref.downloadUrl
            }.addOnSuccessListener { downloadUri ->
                val newPost = hashMapOf(
                    "user" to gardenUserUid,
                    "type" to "photo",
                    "content" to downloadUri,
                    "timestamp" to Calendar.getInstance().time
                )
                db.collection("posts").add(newPost)
                    .addOnSuccessListener {
                        refreshPosts()
                    }.addOnFailureListener {
                        Snackbar.make(
                            requireActivity().findViewById(R.id.flFragment),
                            getString(R.string.connection_error),
                            Snackbar.LENGTH_SHORT
                        ).setAnchorView(requireActivity().findViewById(R.id.speedDial)).show()
                    }
            }.addOnFailureListener {
                Snackbar.make(
                    requireActivity().findViewById(R.id.flFragment),
                    getString(R.string.connection_error),
                    Snackbar.LENGTH_SHORT
                ).setAnchorView(requireActivity().findViewById(R.id.speedDial)).show()
            }
        }
    }

    private fun setFabToAddFriend(currentUser: User) {
        //Change icon
        binding.fabAddRemoveFriend.setImageDrawable(
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.ic_person_add,
                null
            )
        )
        // Add friend and change fab icon and function
        binding.fabAddRemoveFriend.setOnClickListener {
            currentUser.myFriends = currentUser.myFriends.plus(gardenUserUid)
            db.collection("users").document(auth.currentUser!!.uid).set(currentUser)
                .addOnSuccessListener {
                    Snackbar.make(
                        requireActivity().findViewById(R.id.flFragment),
                        getString(R.string.friend_added), Snackbar.LENGTH_SHORT
                    )
                        .setAnchorView(requireActivity().findViewById(R.id.fabAddRemoveFriend))
                        .show()
                    setFabToRemoveFriend(currentUser)
                }
        }
    }

    private fun setFabToRemoveFriend(currentUser: User) {
        //Change icon
        binding.fabAddRemoveFriend.setImageDrawable(
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.ic_person_remove,
                null
            )
        )
        // Remove friend and change fab icon and function
        binding.fabAddRemoveFriend.setOnClickListener {
            currentUser.myFriends = currentUser.myFriends.minus(gardenUserUid)
            db.collection("users").document(auth.currentUser!!.uid).set(currentUser)
                .addOnSuccessListener {
                    Snackbar.make(
                        requireActivity().findViewById(R.id.flFragment),
                        getString(R.string.friend_removed), Snackbar.LENGTH_SHORT
                    )
                        .setAnchorView(requireActivity().findViewById(R.id.fabAddRemoveFriend))
                        .show()
                    setFabToAddFriend(currentUser)
                }
        }
    }

    override fun onResume() {
        super.onResume()
        postsLoaded = false
        gardenLoaded = false
        refreshGarden()
        refreshPosts()
    }

    private fun refreshGarden() {
        var garden: List<View>?
        db.collection("gardens").document(gardenUserUid).get()
            .addOnSuccessListener { document ->
                val data = document.data!!.toMap()
                val rows = (data["rows"] as Long).toInt()
                val cols = (data["columns"] as Long).toInt()
                val width = (data["width"] as Long).toInt()
                val height = (data["height"] as Long).toInt()
                val plotSize = (data["plotSize"] as Long).toInt()
                //Garden description
                binding.tvGardenDescription.text =
                    getString(R.string.garden_description, width, height, width * height, plotSize)

                // Retrieve and convert to object the vegetable garden
                db.collection("gardens").document(gardenUserUid).collection("plots").get()
                    .addOnSuccessListener { plots ->
                        val gardenList = arrayListOf<GardenPlot>()
                        val positions = arrayListOf<Pair<Int, Int>>()
                        plots.forEach { plot ->
                            gardenList.add(
                                GardenPlot(
                                    (plot.data["cropID"] as Long).toInt(),
                                    (plot.data["sowingDate"] as Timestamp?)?.toDate(),
                                    (plot.data["numberOfPlants"] as Long?)?.toInt(),
                                    plot.data["userNotes"] as String?
                                )
                            )
                            positions.add(
                                Pair(
                                    (plot.data["rowNumber"] as Long).toInt(),
                                    (plot.data["columnNumber"] as Long).toInt()
                                )
                            )
                        }
                        // Here we convert a list in a grid (matrix) of plots
                        val gardenMatrix = arrayListOf<ArrayList<GardenPlot>>()
                        for (i in 0 until rows) {
                            val gardenRow = arrayListOf<GardenPlot>()
                            for (j in 0 until cols) {
                                for (k in 0 until gardenList.size) {
                                    val position = positions[k]
                                    if (position.first == i && position.second == j) {
                                        gardenRow.add(gardenList[k])
                                        gardenList.removeAt(k)
                                        positions.removeAt(k)
                                        break
                                    }
                                }
                            }
                            gardenMatrix.add(gardenRow)
                        }

                        garden = createVegetableGarden(
                            gardenMatrix,
                            ResourcesCompat.getDrawable(resources, R.color.plotsSeparator, null)!!,
                            resources.getDimension(R.dimen.plotsSeparatorSize).toInt()
                        )
                        binding.garden.removeAllViews()
                        garden!!.forEach { binding.garden.addView(it) }
                        gardenLoaded = true
                        checkLoadingStatusAndShow()
                    }
            }
            .addOnFailureListener {
                Snackbar.make(
                    requireActivity().findViewById(R.id.flFragment),
                    getString(R.string.connection_error),
                    Snackbar.LENGTH_SHORT
                ).setAnchorView(requireActivity().findViewById(R.id.speedDial)).show()
            }
    }

    private fun checkLoadingStatusAndShow() {
        if (gardenLoaded && postsLoaded) {
            binding.progressBar.visibility = View.GONE
            binding.nestedScrollView.visibility = View.VISIBLE
        }
    }

    private fun refreshPosts() {
        binding.rvPosts.layoutManager = LinearLayoutManager(requireContext())
        val arrayPosts = ArrayList<PostsViewModel>()
        val adapter = PostsAdapter(arrayPosts)
        binding.rvPosts.adapter = adapter

        db.collection("posts")
            .whereEqualTo("user", gardenUserUid)
            .orderBy("timestamp", Query.Direction.DESCENDING).limit(10)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    documents.forEach { document ->
                        arrayPosts.add(
                            PostsViewModel(
                                if (document.data["type"].toString() == "post")
                                    PostsAdapter.TEXT
                                else PostsAdapter.PHOTO,
                                document.data["content"] as String,
                                (document.data["timestamp"] as Timestamp).toDate(),
                                null,
                                gardenUserUid
                            )
                        )
                    }
                    adapter.notifyItemRangeChanged(0, arrayPosts.size)
                }
                postsLoaded = true
                checkLoadingStatusAndShow()
            }
            .addOnFailureListener {
                Snackbar.make(
                    requireActivity().findViewById(R.id.flFragment),
                    getString(R.string.connection_error),
                    Snackbar.LENGTH_SHORT
                ).setAnchorView(requireActivity().findViewById(R.id.speedDial)).show()
            }
    }

    // Utility functions to draw the garden

    private fun createVegetableGarden(
        garden: ArrayList<ArrayList<GardenPlot>>,
        separatorColor: Drawable,
        separatorSize: Int
    ): List<View> {
        val listOfRows = mutableListOf<View>()
        // create a list with [rows] rows and [columns] columns
        garden.forEachIndexed { index, gardenRow ->
            listOfRows.add(createRow(gardenRow, index, separatorColor, separatorSize))
            val separator = View(requireContext())
            separator.layoutParams =
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, separatorSize)
            separator.background = separatorColor
            listOfRows.add(separator)
        }
        listOfRows.removeAt(listOfRows.size - 1) // Removes latest separator
        return listOfRows.toList()
    }

    private fun createRow(
        gardenRow: ArrayList<GardenPlot>,
        rowNumber: Int,
        separatorColor: Drawable,
        separatorSize: Int
    ): LinearLayout {
        // Create a row with an horizontal linear layout
        val row = LinearLayout(requireContext())
        row.orientation = LinearLayout.HORIZONTAL
        row.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        // Then add [count] plot to the row
        gardenRow.forEachIndexed { index, plot ->
            row.addView(createPlot(plot, rowNumber, index))
            val separator = View(requireContext())
            separator.layoutParams =
                LinearLayout.LayoutParams(separatorSize, LinearLayout.LayoutParams.MATCH_PARENT)
            separator.background = separatorColor
            row.addView(separator)
        }
        row.removeViewAt(row.childCount - 1)  // Removes latest separator
        return row
    }

    private fun createPlot(plot: GardenPlot, rowNumber: Int, columnNumber: Int): ImageView {
        val imageView = ImageView(requireContext())
        imageView.setImageDrawable(getPlotDrawable(requireContext(), plot.cropID))

        imageView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1.0f
        )
        imageView.adjustViewBounds = true
        imageView.isClickable = true
        imageView.setOnClickListener {
            val intent = Intent(requireContext(), ChangePlotActivity::class.java)
            intent.putExtra("gardenPlot", plot as Serializable)
            intent.putExtra("rowNumber", rowNumber)
            intent.putExtra("columnNumber", columnNumber)
            intent.putExtra("isMyGarden", isMyGarden)
            intent.putExtra("gardenUserUid", gardenUserUid)
            startActivity(intent)
        }
        return imageView
    }
}
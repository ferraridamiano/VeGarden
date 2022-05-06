package com.example.vegarden

import android.content.ContentValues
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vegarden.databinding.FragmentGardenBinding
import com.google.firebase.Timestamp
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
import java.util.Date
import kotlin.collections.ArrayList

class GardenFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var _binding: FragmentGardenBinding? = null
    private var isMyGarden = true
    private lateinit var gardenUserUid: String

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
        db = Firebase.firestore
        storage = Firebase.storage
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Get garden and refresh thew fragment
        refreshGarden()
        // Retrieve and display posts
        refreshPosts()
        //Change title of posts
        if (isMyGarden) {
            binding.tvPostsPhotos.text = resources.getString(R.string.my_posts_and_photos)
        } else {
            db.collection("users").document(gardenUserUid).get()
                .addOnSuccessListener { document ->
                    val name = document["name"] as String
                    binding.tvPostsPhotos.text = getString(R.string.user_posts_and_photos, name)
                }
        }

        // Speed dial
        binding.speedDial.addActionItem(
            SpeedDialActionItem.Builder(R.id.addPhoto, R.drawable.ic_baseline_add_a_photo_24)
                .setLabel(getString(R.string.add_a_photo))
                .setFabBackgroundColor(ResourcesCompat.getColor(resources, R.color.secondary, null))
                .setFabImageTintColor(ResourcesCompat.getColor(resources, R.color.white, null))
                .create()
        )
        binding.speedDial.addActionItem(
            SpeedDialActionItem.Builder(R.id.addPost, R.drawable.ic_baseline_post_add_24)
                .setLabel(getString(R.string.add_a_post))
                .setFabBackgroundColor(ResourcesCompat.getColor(resources, R.color.secondary, null))
                .setFabImageTintColor(ResourcesCompat.getColor(resources, R.color.white, null))
                .create()
        )

        binding.speedDial.setOnActionSelectedListener(SpeedDialView.OnActionSelectedListener { actionItem ->
            when (actionItem.id) {
                R.id.addPhoto -> {
                    fileChooserContract.launch("image/*")

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
    }

    private val fileChooserContract =
        registerForActivityResult(ActivityResultContracts.GetContent()) { imageUri ->
            if (imageUri != null) {
                val ref =
                    storage.reference.child("images/$gardenUserUid-${System.currentTimeMillis()}")
                ref.putFile(imageUri).continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    ref.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        val newPost = hashMapOf(
                            "user" to gardenUserUid,
                            "type" to "photo",
                            "content" to downloadUri,
                            "timestamp" to Calendar.getInstance().time
                        )
                        db.collection("posts").add(newPost)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Post added", Toast.LENGTH_SHORT)
                                    .show()
                                refreshPosts()
                            }.addOnFailureListener {
                                Toast.makeText(
                                    requireContext(),
                                    "Error connecting to internet",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        // TODO Handle failures
                    }
                }
            }
        }


    override fun onResume() {
        super.onResume()
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
                        binding.progressBar.visibility = View.GONE

                    }

            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
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
                if (documents.isEmpty) {
                    Toast.makeText(requireContext(), "No posts found", Toast.LENGTH_SHORT).show()
                } else {
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
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
                Toast.makeText(requireContext(), "Error connecting to internet", Toast.LENGTH_SHORT)
                    .show()
            }
    }

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
            val intent = Intent(requireContext(), ChangeCropActivity::class.java)
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
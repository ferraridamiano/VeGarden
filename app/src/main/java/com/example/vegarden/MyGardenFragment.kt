package com.example.vegarden

import android.content.ContentValues
import android.content.Context
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
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vegarden.databinding.FragmentMyGardenBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView

class MyGardenFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var _binding: FragmentMyGardenBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyGardenBinding.inflate(inflater, container, false)
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
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Plot garden
        var garden: List<View>?
        db.collection("gardens").document(auth.currentUser!!.uid).get()
            .addOnSuccessListener { document ->
                val data = document.data!!.toMap()
                val rows = data["rows"].toString().toInt()
                val cols = data["columns"].toString().toInt()
                garden = createVegetableGarden(
                    requireContext(),
                    rows,
                    cols,
                    ResourcesCompat.getDrawable(resources, R.color.plotsSeparator, null)!!,
                    resources.getDimension(R.dimen.plotsSeparatorSize).toInt()
                )
                binding.garden.removeAllViews()
                garden!!.forEach { binding.garden.addView(it) }
                binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
            }

        // Retrieve and display posts
        refreshPosts()

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
                    Toast.makeText(requireContext(), "add photo", Toast.LENGTH_SHORT).show()
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

    override fun onResume() {
        super.onResume()
        refreshPosts()
    }

    private fun refreshPosts() {
        db.collection("posts")
            .whereEqualTo("user", auth.currentUser!!.uid)
            .orderBy("timestamp", Query.Direction.DESCENDING).limit(10)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(requireContext(), "No posts found", Toast.LENGTH_SHORT).show()
                } else {
                    binding.rvPosts.layoutManager = LinearLayoutManager(requireContext())
                    val data = ArrayList<PostsViewModel>()
                    documents.forEach { document ->
                        data.add(PostsViewModel(document.data["content"].toString()))
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

private fun createVegetableGarden(
    context: Context,
    rows: Int,
    columns: Int,
    separatorColor: Drawable,
    separatorSize: Int
): List<View> {
    val listOfRows = mutableListOf<View>()
    // create a list with [rows] rows and [columns] columns
    for (i in 1..rows) {
        listOfRows.add(createRow(context, columns, separatorColor, separatorSize))
        val separator = View(context)
        separator.layoutParams =
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, separatorSize)
        separator.background = separatorColor
        listOfRows.add(separator)
    }
    listOfRows.removeAt(listOfRows.size - 1) // Removes latest separator
    return listOfRows.toList()
}

private fun createRow(
    context: Context,
    count: Int,
    separatorColor: Drawable,
    separatorSize: Int
): LinearLayout {
    // Create a row with an horizontal linear layout
    val row = LinearLayout(context)
    row.orientation = LinearLayout.HORIZONTAL
    row.layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    )
    // Then add [count] plot to the row
    for (i in 1..count) {
        row.addView(createPlot(context))
        val separator = View(context)
        separator.layoutParams =
            LinearLayout.LayoutParams(separatorSize, LinearLayout.LayoutParams.MATCH_PARENT)
        separator.background = separatorColor
        row.addView(separator)
    }
    row.removeViewAt(row.childCount - 1)  // Removes latest separator
    return row
}

private fun createPlot(context: Context): ImageView {
    val imageView = ImageView(context)
    imageView.setImageResource(R.drawable.plot)
    imageView.layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT,
        LinearLayout.LayoutParams.WRAP_CONTENT,
        1.0f
    )
    imageView.adjustViewBounds = true
    return imageView
}
package com.example.vegarden

import android.content.ContentValues
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.example.vegarden.databinding.FragmentMyGardenBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

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

        var rows = 0
        var cols = 0
        var garden: List<View>?

        db.collection("gardens").document(auth.currentUser!!.uid).get()
            .addOnSuccessListener { document ->
                val data = document.data!!.toMap()
                rows = data["rows"].toString().toInt()
                cols = data["columns"].toString().toInt()
                garden = createVegetableGarden(
                    requireContext(),
                    rows,
                    cols,
                    ResourcesCompat.getDrawable(resources, R.color.plotsSeparator, null)!!,
                    resources.getDimension(R.dimen.plotsSeparatorSize).toInt()
                )
                binding.column.removeAllViews()
                garden!!.forEach { binding.column.addView(it) }
                binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
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
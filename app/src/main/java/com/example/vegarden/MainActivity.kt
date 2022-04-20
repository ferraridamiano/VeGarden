package com.example.vegarden

import android.content.ContentValues.TAG
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.example.vegarden.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        val db = Firebase.firestore

        var rows = 1
        var cols = 1

        var garden = createVegetableGarden(this, rows, cols)
        binding.column.removeAllViews()
        garden.forEach { binding.column.addView(it) }

        db.collection("gardens").document(auth.currentUser!!.uid).get()
            .addOnSuccessListener { document ->
                val data = document.data!!.toMap()
                rows = data["rows"].toString().toInt()
                cols = data["columns"].toString().toInt()
                garden = createVegetableGarden(this, rows, cols)
                binding.column.removeAllViews()
                garden.forEach { binding.column.addView(it) }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }
}

fun createVegetableGarden(context:Context, rows: Int, columns: Int) : List<LinearLayout> {
    val listOfRows = mutableListOf<LinearLayout>()
    // create a list with [rows] rows and [columns] columns
    for(i in 1..rows){
        listOfRows.add(createRow(context, columns))
    }
    return listOfRows.toList()
}

fun createRow(context:Context, count: Int): LinearLayout {
    // Create a row with an horizontal linear layout
    val row = LinearLayout(context)
    row.orientation = LinearLayout.HORIZONTAL
    row.layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    )
    // Then add [count] plot to the row
    for(i in 1..count){
        row.addView(createPlot(context))
    }
    return row
}

fun createPlot(context: Context): ImageView {
    val imageView = ImageView(context)
    imageView.setImageResource(R.drawable.plot)
    imageView.layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT,
        LinearLayout.LayoutParams.MATCH_PARENT,
        1.0f
    )
    imageView.adjustViewBounds = true
    return imageView
}
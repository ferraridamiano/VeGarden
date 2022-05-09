package com.example.vegarden.activities

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.vegarden.R
import com.example.vegarden.models.GardenPlot
import com.example.vegarden.databinding.ActivityGardenSetupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlin.math.sqrt

class GardenSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGardenSetupBinding
    private lateinit var auth: FirebaseAuth
    private var plotSize: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGardenSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        val db = Firebase.firestore

        binding.tvPlotSize.text = getString(R.string.the_size_of_a_plot_is, plotSize.toString())

        // Depending on text it will show/hide elements and compute the area

        binding.etWidth.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                onTextChange(s.toString(), binding.etHeight.text.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.etHeight.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                onTextChange(binding.etWidth.text.toString(), s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // If the user unfocus a field with an invalid value it will show an error
        binding.etWidth.setOnFocusChangeListener { _, focus ->
            run {
                val etText = binding.etWidth.text.toString()
                if (!focus && (etText.isBlank() || etText.toInt() <= 0)) {
                    binding.tilWidth.error = "Error"
                    // The following line prevent the error icon to be displayed on top of the "m"
                    binding.tvWidthUnit.visibility = View.INVISIBLE
                } else {
                    binding.tilWidth.error = null
                    binding.tvWidthUnit.visibility = View.VISIBLE
                }
            }
        }

        binding.etHeight.setOnFocusChangeListener { _, focus ->
            run {
                val etText = binding.etHeight.text.toString()
                if (!focus && (etText.isBlank() || etText.toInt() <= 0)) {
                    binding.tilHeight.error = "Error"
                    // The following line prevent the error icon to be displayed on top of the "m"
                    binding.tvHeightUnit.visibility = View.INVISIBLE
                } else {
                    binding.tilHeight.error = null
                    binding.tvHeightUnit.visibility = View.VISIBLE
                }
            }
        }

        // FAB click
        binding.fabNext.setOnClickListener {
            // Create a new empty vegetable garden
            // Here we want that the smaller number is the columns and the greater value is the rows
            var rows: Int
            var columns: Int
            val a = binding.etWidth.text.toString().toInt()
            val b = binding.etHeight.text.toString().toInt()
            if (a > b) {
                rows = a
                columns = b
            } else {
                rows = b
                columns = a
            }

            // A bigger plot size leads to smaller grid
            rows = (rows / sqrt(plotSize.toDouble())).toInt()
            columns = (columns / sqrt(plotSize.toDouble())).toInt()

            val newGarden = hashMapOf(
                "rows" to rows,
                "columns" to columns,
                "plotSize" to plotSize,
            )

            // Generate an empty vegetable garden
            val plotsRef =
                db.collection("gardens").document(auth.currentUser!!.uid).collection("plots")
            db.runBatch { batch ->
                for (row_number in 0 until rows) {
                    for (column_number in 0 until columns) {
                        val mapPlot = GardenPlot(0, null, null, null).toMap()
                        mapPlot["rowNumber"] = row_number
                        mapPlot["columnNumber"] = column_number
                        batch.set(plotsRef.document(), mapPlot)
                    }
                }
            }.addOnCompleteListener {
                Log.d("GardenFragment", "done")
            }

            db.collection("gardens").document(auth.currentUser!!.uid).set(newGarden)
                .addOnSuccessListener {
                    Log.d(
                        ContentValues.TAG,
                        "DocumentSnapshot added with ID: ${auth.currentUser!!.uid}"
                    )
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .addOnFailureListener { e ->
                    Log.w(ContentValues.TAG, "Error adding document", e)
                    Toast.makeText(this, "Connection error. Try again later...", Toast.LENGTH_SHORT)
                        .show()
                }
        }
    }

    private fun onTextChange(width: String, height: String) {
        if (isSizeCorrect(width, height)) {
            val intWidth = width.toInt()
            val intHeight = height.toInt()
            val area = intWidth * intHeight
            binding.tvArea.text = getString(
                R.string.total_area,
                area.toString()
            )
            plotSize = when (area) {
                in 1..100 -> 1
                in 100..500 -> 4
                else -> 9
            }
            binding.tvPlotSize.text = getString(R.string.the_size_of_a_plot_is, plotSize.toString())
            binding.tvArea.visibility = View.VISIBLE
            binding.fabNext.visibility = View.VISIBLE
        } else {
            binding.tvArea.visibility = View.INVISIBLE
            binding.fabNext.visibility = View.INVISIBLE
        }
    }
}

private fun isSizeCorrect(str1: String, str2: String): Boolean =
    str1.isNotBlank() && str2.isNotBlank() && str1.toInt() > 0 && str2.toInt() > 0
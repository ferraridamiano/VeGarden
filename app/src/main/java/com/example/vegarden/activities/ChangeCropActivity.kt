package com.example.vegarden.activities

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.vegarden.models.GardenPlot
import com.example.vegarden.R
import com.example.vegarden.databinding.ActivityChangeCropBinding
import com.example.vegarden.getPlotDrawable
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ChangeCropActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangeCropBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var selectedCrop: Int = 0
    private var selectedNumberOfPlants: Int? = null
    private var selectedSowingDate: Date? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChangeCropBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Appbar
        title = getString(R.string.plot_details)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        auth = Firebase.auth
        db = Firebase.firestore

        val gardenPlot = intent.getSerializableExtra("gardenPlot") as GardenPlot
        val rowNumber = intent.getIntExtra("rowNumber", 0)
        val columnNumber = intent.getIntExtra("columnNumber", 0)
        val isMyGarden = intent.getBooleanExtra("isMyGarden", false)
        val gardenUserUid = intent.getStringExtra("gardenUserUid")!!

        val cropsList = resources.getStringArray(R.array.crops_list)

        selectedCrop = gardenPlot.cropID
        selectedSowingDate = gardenPlot.sowingDate
        selectedNumberOfPlants = gardenPlot.numberOfPlants

        //Set cultivated crop
        binding.ivPreview.setImageDrawable(getPlotDrawable(this, selectedCrop))
        binding.tvCrop.text = cropsList[selectedCrop]
        //Set date
        binding.tvDate.text = getStringDateSinceSowing(selectedSowingDate)
        //Set number of plants
        binding.tvPlants.text =
            if (gardenPlot.numberOfPlants == null) resources.getString(R.string.unknown)
            else gardenPlot.numberOfPlants.toString()

        // If it is my garden, then the user can edit everything and save
        if (isMyGarden) {
            //Set user's notes
            binding.etNotes.hint = resources.getString(R.string.my_notes)
            binding.etNotes.setText(gardenPlot.userNotes)

            binding.llNotes.visibility = View.GONE

            binding.llCrops.isClickable = true
            binding.llCrops.setOnClickListener {

                MaterialAlertDialogBuilder(this)
                    .setTitle(resources.getString(R.string.crop))
                    .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                        binding.tvCrop.text = cropsList[selectedCrop]
                        binding.ivPreview.setImageDrawable(getPlotDrawable(this, selectedCrop))
                    }
                    .setSingleChoiceItems(cropsList, selectedCrop) { _, which ->
                        selectedCrop = which
                    }.show()
            }
            binding.llSowingDate.isClickable = true
            binding.llSowingDate.setOnClickListener {
                sowingDatePicker(selectedSowingDate)
            }

            binding.llNumberPlants.isClickable = true
            binding.llNumberPlants.setOnClickListener {
                plantsNumberPicker(selectedNumberOfPlants)
            }

            binding.fabConfirm.setOnClickListener {

                val confirmedPlot = GardenPlot(
                    selectedCrop,
                    selectedSowingDate,
                    selectedNumberOfPlants,
                    binding.etNotes.text.toString()
                )
                db.collection("gardens")
                    .document(auth.currentUser!!.uid)
                    .collection("plots")
                    .whereEqualTo("columnNumber", columnNumber)
                    .whereEqualTo("rowNumber", rowNumber)
                    .get().addOnSuccessListener {
                        val reference = it.first().reference
                        reference.update(confirmedPlot.toMap()).addOnSuccessListener {
                            Toast.makeText(this, "Plot saved", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
            }
        }
        // Otherwise it will able just to see the data
        else {
            // If there are not notes it will not show anything
            if (gardenPlot.userNotes.isNullOrBlank()) {
                binding.llNotes.visibility = View.GONE
            } else {
                //Set user's notes
                db.collection("users").document(gardenUserUid).get()
                    .addOnSuccessListener { document ->
                        val name = document["name"] as String
                        binding.tvLabelNotes.text = getString(R.string.user_notes, name)
                    }
                binding.tvNotes.text = gardenPlot.userNotes
            }

            binding.tilNotes.visibility = View.GONE

            binding.fabConfirm.visibility = View.GONE

        }

    }

    private fun sowingDatePicker(initialDate: Date?) {
        val myCalendar = Calendar.getInstance()

        val initialYear: Int?
        val initialMonth: Int?
        val initialDay: Int?

        // if the initial date is null, pick today
        if (initialDate == null) {
            initialYear = myCalendar.get(Calendar.YEAR)
            initialMonth = myCalendar.get(Calendar.MONTH)
            initialDay = myCalendar.get(Calendar.DAY_OF_MONTH)
        } else {
            initialYear = SimpleDateFormat("yyyy", Locale.ITALY).format(initialDate).toInt()
            initialMonth = SimpleDateFormat("MM", Locale.ITALY).format(initialDate).toInt() - 1
            initialDay = SimpleDateFormat("dd", Locale.ITALY).format(initialDate).toInt()
        }

        DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                val selectedDate = "$selectedDayOfMonth/${selectedMonth + 1}/$selectedYear"
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ITALY)
                selectedSowingDate = sdf.parse(selectedDate)
                binding.tvDate.text = getStringDateSinceSowing(selectedSowingDate)
            }, initialYear, initialMonth, initialDay
        ).show()
    }

    private fun getStringDateSinceSowing(sowingDate: Date?): String {
        if (sowingDate == null) return resources.getString(R.string.unknown)
        val daysSinceSowing =
            (Calendar.getInstance().time.time - sowingDate.time).floorDiv(86_400_000).toInt()
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ITALY)

        if (daysSinceSowing == 0) {
            return "${sdf.format(sowingDate)}   (${resources.getString(R.string.today)})"
        } else if (daysSinceSowing == 1) {
            return "${sdf.format(sowingDate)}   (${resources.getString(R.string.yesterday)})"
        }
        return "${sdf.format(sowingDate)}   ($daysSinceSowing ${resources.getString(R.string.days_ago)})"
    }

    private fun plantsNumberPicker(initialValue: Int?, maxValue: Int = 50): Dialog? {
        val minValue = 0
        val numberPicker = NumberPicker(this)
        numberPicker.maxValue = maxValue
        numberPicker.minValue = minValue
        numberPicker.value = initialValue ?: 0
        val stringLabels = Array(maxValue - minValue + 1) { i ->
            if (i > 0) "$i" else resources.getString(R.string.unknown)
        }
        numberPicker.displayedValues = stringLabels
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.number_of_plants))
        builder.setMessage(resources.getString(R.string.choose_a_value))
        builder.setPositiveButton("OK") { _, _ ->
            selectedNumberOfPlants = numberPicker.value
            binding.tvPlants.text = if (selectedNumberOfPlants == 0)
                resources.getString(R.string.unknown) else selectedNumberOfPlants.toString()
        }
        builder.setNegativeButton(resources.getString(R.string.cancel)) { _, _ -> }
        builder.setView(numberPicker)
        builder.create()
        return builder.show()
    }
}
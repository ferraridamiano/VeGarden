package com.example.vegarden

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.vegarden.databinding.ActivityChangeCropBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Calendar
import java.util.Locale

class ChangeCropActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangeCropBinding

    private var selectedCrop: Int = 0
    private var selectedNumberOfPlants: Int? = null
    private var selectedSowingDate: Date? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChangeCropBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gardenPlot = intent.getSerializableExtra("gardenPlot") as GardenPlot
        setEditableView(gardenPlot)

    }

    private fun setEditableView(gardenPlot: GardenPlot) {
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
        //Set user's notes
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
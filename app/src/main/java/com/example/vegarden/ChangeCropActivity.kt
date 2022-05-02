package com.example.vegarden

import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.View
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.vegarden.databinding.ActivityChangeCropBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*


class ChangeCropActivity : AppCompatActivity() {

    lateinit var binding: ActivityChangeCropBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChangeCropBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setEditableView()


    }

    private fun setEditableView() {
        var selectedItem = 0

        binding.llNotes.visibility = View.GONE
        binding.llCrops.isClickable = true
        binding.llCrops.setOnClickListener {
            val cropsList = resources.getStringArray(R.array.crops_list)

            MaterialAlertDialogBuilder(this)
                .setTitle(resources.getString(R.string.crop))
                .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                    binding.tvCrop.text = cropsList[selectedItem]
                    when (selectedItem) {
                        0 -> {
                            binding.ivPreview.setImageResource(R.drawable.plot_uncultivated)
                        }
                        in 1..13 -> {
                            val layerDrawable = ResourcesCompat.getDrawable(
                                resources,
                                R.drawable.plot_vegetables,
                                null
                            ) as LayerDrawable
                            if (selectedItem == 13) { // User clicks on "others"
                                layerDrawable.setDrawableByLayerId(
                                    R.id.vegetableImage, ResourcesCompat.getDrawable(
                                        resources,
                                        mapVegetableResource[9999]!!,
                                        null
                                    )
                                )
                            } else {
                                layerDrawable.setDrawableByLayerId(
                                    R.id.vegetableImage, ResourcesCompat.getDrawable(
                                        resources,
                                        mapVegetableResource[selectedItem]!!,
                                        null
                                    )
                                )
                            }
                            binding.ivPreview.setImageDrawable(layerDrawable)
                        }
                    }
                }
                .setSingleChoiceItems(cropsList, selectedItem) { _, which ->
                    selectedItem = which
                }.show()
        }
        binding.llSowingDate.isClickable = true
        binding.llSowingDate.setOnClickListener {
            sowingDatePicker()
        }

        binding.llNumberPlants.isClickable = true
        binding.llNumberPlants.setOnClickListener {
            plantsNumberPicker()
        }
    }

    private fun sowingDatePicker() {
        val myCalendar = Calendar.getInstance()
        val year = myCalendar.get(Calendar.YEAR)
        val month = myCalendar.get(Calendar.MONTH)
        val day = myCalendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                val selectedDate = "$selectedDayOfMonth/${selectedMonth + 1}/$selectedYear"
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ITALY)
                val theDate = sdf.parse(selectedDate)
                val daysSinceSowing =
                    (Calendar.getInstance().time.time - theDate.time).floorDiv(86_400_000)
                binding.tvDate.text = "$selectedDate   ($daysSinceSowing days ago)"
            }, year, month, day
        ).show()

    }

    private fun plantsNumberPicker(maxValue: Int = 50): Dialog? {
        val minValue = 0
        val numberPicker = NumberPicker(this)
        numberPicker.maxValue = maxValue
        numberPicker.minValue = minValue
        val stringLabels = Array(maxValue - minValue + 1) { i -> if (i > 0) "$i" else "Unknown" }
        numberPicker.displayedValues = stringLabels
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Changing the Hue")
        builder.setMessage("Choose a value :")
        builder.setPositiveButton("OK") { _, _ ->
            binding.tvPlants.text =
                if (numberPicker.value == 0) "Unknown" else numberPicker.value.toString()
        }
        builder.setNegativeButton("CANCEL") { _, _ -> }
        builder.setView(numberPicker)
        builder.create();
        return builder.show();
    }
}
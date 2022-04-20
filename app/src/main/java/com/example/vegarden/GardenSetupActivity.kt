package com.example.vegarden

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.example.vegarden.databinding.ActivityGardenSetupBinding

class GardenSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGardenSetupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGardenSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Depending on text it will show/hide elements and compute the area

        binding.etWidth.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (isSizeCorrect(s.toString(), binding.etHeight.text.toString())) {
                    binding.tvarea.text =
                        "${s.toString().toInt() * binding.etHeight.text.toString().toInt()} m²"
                    binding.layoutArea.visibility = View.VISIBLE
                    binding.fabNext.visibility = View.VISIBLE
                } else {
                    binding.layoutArea.visibility = View.INVISIBLE
                    binding.fabNext.visibility = View.INVISIBLE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.etHeight.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (isSizeCorrect(s.toString(), binding.etWidth.text.toString())) {
                    binding.tvarea.text =
                        "${s.toString().toInt() * binding.etWidth.text.toString().toInt()} m²"
                    binding.layoutArea.visibility = View.VISIBLE
                    binding.fabNext.visibility = View.VISIBLE
                } else {
                    binding.layoutArea.visibility = View.INVISIBLE
                    binding.fabNext.visibility = View.INVISIBLE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // If the user unfocus a field with an invalid value it will show an error
        binding.etWidth.setOnFocusChangeListener { _, focus ->
            run {
                val etText = binding.etWidth.text.toString()
                if (!focus && (etText.isBlank() || etText.toInt() <= 0)) {
                    binding.etWidth.error = "Error"
                    // The following line prevent the error icon to be displayed on top of the "m"
                    binding.tvWidthUnit.visibility = View.INVISIBLE
                }
                else {
                    binding.etWidth.error = null
                    binding.tvWidthUnit.visibility = View.VISIBLE
                }
            }
        }

        binding.etHeight.setOnFocusChangeListener { _, focus ->
            run {
                val etText = binding.etHeight.text.toString()
                if (!focus && (etText.isBlank() || etText.toInt() <= 0)) {
                    binding.etHeight.error = "Error"
                    // The following line prevent the error icon to be displayed on top of the "m"
                    binding.tvHeightUnit.visibility = View.INVISIBLE
                }
                else {
                    binding.etHeight.error = null
                    binding.tvHeightUnit.visibility = View.VISIBLE
                }
            }
        }
    }
}

private fun isSizeCorrect(str1: String, str2: String): Boolean =
    str1.isNotBlank() && str2.isNotBlank() && str1.toInt() > 0 && str2.toInt() > 0
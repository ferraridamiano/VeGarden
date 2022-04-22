package com.example.vegarden

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.vegarden.databinding.ActivityAddPostBinding

class AddPostActivity : AppCompatActivity() {

    lateinit var binding: ActivityAddPostBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.fabConfirm.setOnClickListener {
            finish()
        }

    }
}
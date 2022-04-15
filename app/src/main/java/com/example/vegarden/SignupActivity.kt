package com.example.vegarden

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.vegarden.databinding.ActivitySignupBinding

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvSignin.setOnClickListener {
            finish()
        }

        binding.fabSignup.setOnClickListener {
            if (binding.etName.text.toString().isBlank()) {
                binding.tilName.error = "Name field should not be empty"
            } else {
                binding.tilName.error = null
            }
            if (binding.etSurname.text.toString().isBlank()) {
                binding.tilSurname.error = "Surname field should not be empty"
            } else {
                binding.tilSurname.error = null
            }
            if (!binding.etEmail.text.toString().isValidEmail()) {
                binding.tilEmail.error = "Email is not valid"
            } else {
                binding.tilEmail.error = null
            }
            if (!binding.etPassword.text.toString().isValidPassword()) {
                binding.tilPassword.error = "The password should contain at least 8 characters"
            } else if (binding.etPassword.text.toString() != binding.etConfirmPassword.text.toString()) {
                binding.tilPassword.error = "The two passwords are different"
                binding.tilConfirmPassword.error = "The two passwords are different"
            } else {
                binding.tilPassword.error = null
                binding.tilConfirmPassword.error = null
            }
        }

    }
}
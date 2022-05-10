package com.example.vegarden.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.vegarden.R
import com.example.vegarden.databinding.ActivitySigninBinding
import com.example.vegarden.isValidEmail
import com.example.vegarden.isValidPassword
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SigninActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySigninBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySigninBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        // Check if user is signed in (non-null) and update UI accordingly
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
        }

        binding.tvSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        binding.fabSignin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            var error = false

            if (!email.isValidEmail()) {
                binding.tilEmail.error = getString(R.string.invalid_email)
                error = true
            } else {
                binding.tilEmail.error = null
            }
            if (!password.isValidPassword()) {
                binding.tilPassword.error = getString(R.string.invalid_password)
                error = true
            } else {
                binding.tilPassword.error = null
            }
            if (!error) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            // If sign in fails, display the error messages
                            binding.tilEmail.error = getString(R.string.invalid_email_password)
                            binding.tilPassword.error = getString(R.string.invalid_email_password)
                        }
                    }
            }

        }
    }
}
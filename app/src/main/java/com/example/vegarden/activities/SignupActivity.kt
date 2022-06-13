package com.example.vegarden.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.vegarden.R
import com.example.vegarden.models.User
import com.example.vegarden.databinding.ActivitySignupBinding
import com.example.vegarden.isValidEmail
import com.example.vegarden.isValidPassword
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.tvSignin.setOnClickListener {
            finish()
        }

        binding.fabSignup.setOnClickListener {
            val name = binding.etName.text.toString()
            val surname = binding.etSurname.text.toString()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()
            var error = false

            if (name.isBlank()) {
                binding.tilName.error = getString(R.string.empty_field)
                error = true
            } else {
                binding.tilName.error = null
            }
            if (surname.isBlank()) {
                binding.tilSurname.error = getString(R.string.empty_field)
                error = true
            } else {
                binding.tilSurname.error = null
            }
            if (!email.isValidEmail()) {
                binding.tilEmail.error = getString(R.string.empty_field)
                error = true
            } else {
                binding.tilEmail.error = null
            }
            if (!password.isValidPassword()) {
                binding.tilPassword.error = getString(R.string.invalid_password)
                error = true
            } else if (password != confirmPassword) {
                binding.tilPassword.error = getString(R.string.different_passwords)
                binding.tilConfirmPassword.error = getString(R.string.different_passwords)
                error = true
            } else {
                binding.tilPassword.error = null
                binding.tilConfirmPassword.error = null
            }

            // Register the user
            if (!error) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            val user = auth.currentUser
                            val db = Firebase.firestore
                            // Create a new user with a first and last name
                            val newUser = User(name, surname, email, user!!.uid)
                            db.collection("users").document(user.uid).set(newUser)
                                .addOnSuccessListener {
                                    startActivity(Intent(this, GardenSetupActivity::class.java))
                                    finish()
                                }
                                .addOnFailureListener { displayRegistrationError() }
                        } else {
                            // If sign in fails, display a message to the user.
                            displayRegistrationError()
                        }
                    }
            }
        }
    }

    /**
     * Displays a snackbar that says that it is not possible to register at the moment
     */
    private fun displayRegistrationError() {
        Snackbar.make(
            findViewById(android.R.id.content),
            getString(R.string.registration_error),
            Snackbar.LENGTH_LONG
        ).show()
    }
}
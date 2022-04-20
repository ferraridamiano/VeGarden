package com.example.vegarden

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.vegarden.databinding.ActivitySignupBinding
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
                binding.tilName.error = "Name field should not be empty"
                error = true
            } else {
                binding.tilName.error = null
            }
            if (surname.isBlank()) {
                binding.tilSurname.error = "Surname field should not be empty"
                error = true
            } else {
                binding.tilSurname.error = null
            }
            if (!email.isValidEmail()) {
                binding.tilEmail.error = "Email is not valid"
                error = true
            } else {
                binding.tilEmail.error = null
            }
            if (!password.isValidPassword()) {
                binding.tilPassword.error = "The password should contain at least 8 characters"
                error = true
            } else if (password != confirmPassword.toString()) {
                binding.tilPassword.error = "The two passwords are different"
                binding.tilConfirmPassword.error = "The two passwords are different"
                error = true
            } else {
                binding.tilPassword.error = null
                binding.tilConfirmPassword.error = null
            }

            if (!error) {
                Toast.makeText(this, "Creating account: $email $password", Toast.LENGTH_SHORT)
                    .show()
                createAccount(name, surname, email, password)
            }
        }
    }

    private fun createAccount(name: String, surname: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    //TODO updateUI(user)
                    Toast.makeText(
                        baseContext, "Authentication succeded.",
                        Toast.LENGTH_SHORT
                    ).show()
                    val db = Firebase.firestore
                    // Create a new user with a first and last name
                    val newUser = hashMapOf(
                        "name" to name,
                        "surname" to surname,
                    )

                    db.collection("users").document(user!!.uid).set(newUser)
                        .addOnSuccessListener { _ ->
                            Log.d(TAG, "DocumentSnapshot added with ID: ${user.uid}")
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error adding document", e)
                        }
                    startActivity(Intent(this, GardenSetupActivity::class.java))
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext, task.exception.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                    //TODO Error updateUI(null)
                }
            }
    }
}
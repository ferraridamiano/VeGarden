package com.example.vegarden.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.vegarden.R
import com.example.vegarden.databinding.ActivityAddPostBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Calendar

class AddPostActivity : AppCompatActivity() {

    lateinit var binding: ActivityAddPostBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Appbar
        title = getString(R.string.write_a_post)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        //Firebase init
        auth = Firebase.auth
        db = Firebase.firestore

        db.collection("users").document(auth.currentUser!!.uid).get()
            .addOnSuccessListener { document ->
                val name = document.data!!["name"]
                val surname = document.data!!["surname"]
                binding.tvNameSurnameSays.text =
                    getString(R.string.name_surname_says, name, surname)
            }

        binding.fabConfirm.setOnClickListener {
            val text = binding.etPost.text.toString()
            if (text.trim().isBlank()) {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    getString(R.string.write_something_or_exit),
                    Snackbar.LENGTH_LONG
                ).setAnchorView(binding.fabConfirm).show()
            } else {
                val newPost = hashMapOf(
                    "user" to auth.currentUser!!.uid,
                    "type" to "post",
                    "content" to text,
                    "timestamp" to Calendar.getInstance().time
                )
                db.collection("posts").add(newPost)
                    .addOnSuccessListener {
                        finish()
                    }.addOnFailureListener {
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            getString(R.string.connection_error),
                            Snackbar.LENGTH_LONG
                        ).setAnchorView(binding.fabConfirm).show()
                    }
            }
        }
    }
}
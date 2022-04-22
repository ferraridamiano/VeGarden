package com.example.vegarden

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.vegarden.databinding.ActivityAddPostBinding
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

        auth = Firebase.auth
        db = Firebase.firestore

        db.collection("users").document(auth.currentUser!!.uid).get()
            .addOnSuccessListener { document ->
                val name = document.data!!["name"]
                val surname = document.data!!["surname"]
                binding.tvNameSurname.text = "$name $surname"
                binding.tvNameSurname.visibility = View.VISIBLE
                binding.tvSays.visibility = View.VISIBLE
            }.addOnFailureListener {
                Toast.makeText(this, "Error connecting to internet", Toast.LENGTH_SHORT).show()
            }

        binding.fabConfirm.setOnClickListener {
            val text = binding.etPost.text.toString()
            if (text.trim().isBlank()) {
                Toast.makeText(this, "Write something or exit", Toast.LENGTH_SHORT).show()
            } else {
                val newPost = hashMapOf(
                    "user" to auth.currentUser!!.uid,
                    "type" to "post",
                    "content" to text,
                    "timestamp" to Calendar.getInstance().time
                )
                db.collection("posts").add(newPost)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Post added", Toast.LENGTH_SHORT).show()
                        finish()
                    }.addOnFailureListener {
                        Toast.makeText(this, "Error connecting to internet", Toast.LENGTH_SHORT)
                            .show()
                    }
            }
        }

    }
}
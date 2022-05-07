package com.example.vegarden.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.vegarden.R
import com.example.vegarden.models.User
import com.example.vegarden.activities.MyFriendsActivity
import com.example.vegarden.activities.SigninActivity
import com.example.vegarden.databinding.FragmentMyAccountBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso

class MyAccountFragment : Fragment(R.layout.fragment_my_account) {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var _binding: FragmentMyAccountBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        db = Firebase.firestore
        storage = Firebase.storage
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db.collection("users").document(auth.currentUser!!.uid).get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)!!
                binding.tvNameSurname.text = "${user?.name} ${user?.surname}"
                binding.tvEmail.text = user.email

                refreshProfilePhoto(user)

                binding.ivPhoto.setOnClickListener {
                    fileChooser.launch("image/*")
                    refreshProfilePhoto(user)
                }
            }

        binding.llMyFriends.setOnClickListener {
            startActivity(Intent(requireContext(), MyFriendsActivity::class.java))
        }

        binding.llLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(requireContext(), SigninActivity::class.java))
            activity?.finish()
        }

    }

    private fun refreshProfilePhoto(user: User) {
        // If there is a saved photo load it to the imageview, otherwise leave the placeholder
        if (!user.profilePhoto.isNullOrEmpty()) {
            Picasso.get().load(user.profilePhoto).into(binding.ivPhoto)
        }
    }

    private val fileChooser =
        registerForActivityResult(ActivityResultContracts.GetContent()) { imageUri ->
            if (imageUri != null) {
                val ref =
                    storage.reference.child("profilePhotos/${auth.currentUser!!.uid}-${System.currentTimeMillis()}")
                ref.putFile(imageUri).continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    ref.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        val firestoreRef = db.collection("users").document(auth.currentUser!!.uid)
                        firestoreRef.get().addOnSuccessListener { document ->
                            val user = document.toObject(User::class.java)!!
                            user.profilePhoto = downloadUri.toString()
                            firestoreRef.set(user)
                        }
                    }
                }
            }
        }

}
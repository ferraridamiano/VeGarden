package com.example.vegarden.fragments

import android.app.Activity
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.vegarden.R
import com.example.vegarden.activities.GardenSetupActivity
import com.example.vegarden.activities.SigninActivity
import com.example.vegarden.databinding.FragmentMyAccountBinding
import com.example.vegarden.models.User
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import java.util.*


class MyAccountFragment : Fragment(R.layout.fragment_my_account) {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var _binding: FragmentMyAccountBinding? = null

    var selectedTheme = 0

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        db = Firebase.firestore
        storage = Firebase.storage
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Appbar
        activity?.title = resources.getString(R.string.my_account)

        // Get default value of theme
        val prefs = activity?.getSharedPreferences("Settings", MODE_PRIVATE)
        selectedTheme = prefs?.getInt("selectedTheme", 0) ?: 0

        refreshData()

        binding.ivPhoto.setOnClickListener {
            pickImageFromGallery()
        }

        binding.llMyFriends.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.flFragment, MyFriendsFragment())
                ?.addToBackStack(null)?.commit()
        }

        binding.llChangeGardenSize.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(resources.getString(R.string.change_garden_size))
                .setMessage(resources.getString(R.string.change_garden_size_alert))
                .setPositiveButton(R.string.yes) { _, _ ->
                    val gardenRef = db.collection("gardens").document(auth.currentUser!!.uid)
                    gardenRef.collection("plots").get()
                        .addOnSuccessListener { documents ->
                            //delete everything and open GardenSetupActivity
                            db.runBatch { batch ->
                                documents.forEach { document -> batch.delete(document.reference) }
                                batch.delete(gardenRef)
                            }.addOnSuccessListener {
                                startActivity(
                                    Intent(
                                        requireContext(),
                                        GardenSetupActivity::class.java
                                    )
                                )
                                activity?.finish()
                            }
                        }
                }
                .setNegativeButton(R.string.no, null)
                .setIcon(R.drawable.ic_warning)
                .show()
        }

        binding.llTheme.setOnClickListener {

            val themeList = resources.getStringArray(R.array.theme_list).toMutableList()

            // Phones before android 10 can't use system theme
            if (android.os.Build.VERSION.SDK_INT < 29){
                themeList.removeAt(2)
            }

            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.theme))
                .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->

                    when (selectedTheme) {
                        // Light theme
                        0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        // Dark theme
                        1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                        // System default
                        2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    }
                    val prefsEdit = prefs?.edit()
                    prefsEdit?.putInt("selectedTheme", selectedTheme)
                    prefsEdit?.apply()
                }
                .setSingleChoiceItems(themeList.toTypedArray(), selectedTheme) { _, which ->
                    selectedTheme = which
                }.show()
        }

        binding.llLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(requireContext(), SigninActivity::class.java))
            activity?.finish()
        }

    }

    private fun refreshData() {
        db.collection("users").document(auth.currentUser!!.uid).get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)!!
                binding.tvNameSurname.text = "${user.name} ${user.surname}"
                binding.tvEmail.text = user.email

                if (!user.profilePhoto.isNullOrEmpty()) {
                    Picasso.get().load(user.profilePhoto).into(binding.ivPhoto)
                }
            }
    }

    private fun pickImageFromGallery() {
        //Intent to pick image
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    companion object {
        //image pick code
        private val IMAGE_PICK_CODE = 1000
        //Permission code
        private val PERMISSION_CODE = 1001
    }

    //handle requested permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                    pickImageFromGallery()
                }
            }
        }
    }

    //handle result of picked image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE){
            val imageUri = data?.data!!
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
                        refreshData()
                    }
                }
            }
        }
    }
}
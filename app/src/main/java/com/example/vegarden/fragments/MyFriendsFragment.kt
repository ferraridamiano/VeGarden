package com.example.vegarden.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vegarden.R
import com.example.vegarden.adapters.MyFriendsAdapter
import com.example.vegarden.databinding.FragmentMyFriendsBinding
import com.example.vegarden.models.MyFriendsViewModel
import com.example.vegarden.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// WARNING: THIS IS NOT THE FRAGMENT FOR THE FEED OF THE FRIENDS' POSTS BUT JUST THE LIST OF THE
//          FRIENDS IN "MY ACCOUNT"

class MyFriendsFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var _binding: FragmentMyFriendsBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyFriendsBinding.inflate(inflater, container, false)
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
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Appbar
        activity?.title = resources.getString(R.string.my_friends)
    }

    override fun onResume() {
        super.onResume()
        refreshFriends()
    }

    private fun refreshFriends() {
        binding.rvMyFriends.layoutManager = LinearLayoutManager(requireContext())
        val arrayFriends = ArrayList<MyFriendsViewModel>()
        val adapter = MyFriendsAdapter(arrayFriends)
        binding.rvMyFriends.adapter = adapter

        adapter.onItemClick = { friend ->
            val bundle = Bundle()
            bundle.putString("gardenUserUid", friend.uid)
            bundle.putBoolean("isMyGarden", false)
            val gardenFragment = GardenFragment()
            gardenFragment.arguments = bundle
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.flFragment, gardenFragment)
                ?.addToBackStack(null)?.commit()
        }

        db.collection("users").document(auth.currentUser!!.uid).get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)!!
                db.collection("users").whereIn("uid", user.myFriends).get()
                    .addOnSuccessListener { documents ->
                        documents.forEach { document ->
                            val friend = document.toObject(User::class.java)
                            arrayFriends.add(
                                MyFriendsViewModel(
                                    friend.uid!!,
                                    "${friend.name} ${friend.surname}",
                                    friend.profilePhoto
                                )
                            )
                        }
                        adapter.notifyItemRangeChanged(0, documents.size())
                    }
            }
    }

}
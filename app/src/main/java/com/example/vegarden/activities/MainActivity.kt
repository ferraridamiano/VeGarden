package com.example.vegarden.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.vegarden.R
import com.example.vegarden.databinding.ActivityMainBinding
import com.example.vegarden.fragments.FeedFragment
import com.example.vegarden.fragments.GardenFragment
import com.example.vegarden.fragments.MyAccountFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val auth = Firebase.auth

        val myGardenFragment = GardenFragment()
        val bundleMyGarden = Bundle()
        bundleMyGarden.putString("gardenUserUid", auth.currentUser!!.uid)
        bundleMyGarden.putBoolean("isMyGarden", true)
        myGardenFragment.arguments = bundleMyGarden

        val friendsFragment = FeedFragment()
        val friendsExplore = Bundle()
        friendsExplore.putBoolean("isExploreFragment", false)
        friendsFragment.arguments = friendsExplore

        val exploreFragment = FeedFragment()
        val bundleExplore = Bundle()
        bundleExplore.putBoolean("isExploreFragment", true)
        exploreFragment.arguments = bundleExplore

        val myAccountFragment = MyAccountFragment()

        setCurrentFragment(myGardenFragment)

        binding.bottomNavigationBar.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menuMyGarden -> setCurrentFragment(myGardenFragment)
                R.id.menuMyFriends -> setCurrentFragment(friendsFragment)
                R.id.menuExplore -> setCurrentFragment(exploreFragment)
                R.id.menuMyAccount -> setCurrentFragment(myAccountFragment)
            }
            true
        }

    }

    private fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment)
            commit()
        }

    override fun onBackPressed() {
        // Close app if back button is pressed
        finishAffinity()
    }

}

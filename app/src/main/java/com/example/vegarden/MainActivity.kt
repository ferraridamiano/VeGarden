package com.example.vegarden

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.vegarden.databinding.ActivityMainBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val auth = Firebase.auth

        val myGardenFragment = MyGardenFragment()
        val exploreFragment = ExploreFragment()
        val settingsFragment = SettingsFragment()

        val bundle = Bundle()
        bundle.putString("gardenUserUid", auth.currentUser!!.uid)
        myGardenFragment.arguments = bundle

        setCurrentFragment(myGardenFragment)

        binding.bottomNavigationBar.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menuMyGarden -> setCurrentFragment(myGardenFragment)
                R.id.menuExplore -> setCurrentFragment(exploreFragment)
                R.id.menuSettings -> setCurrentFragment(settingsFragment)
            }
            true
        }

    }

    private fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment)
            commit()
        }

}

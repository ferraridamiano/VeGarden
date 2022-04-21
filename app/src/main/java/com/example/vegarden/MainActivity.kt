package com.example.vegarden

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.vegarden.databinding.ActivityMainBinding
import com.example.vegarden.databinding.ActivitySigninBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val myGardenFragment=MyGardenFragment()
        val settingsFragment=SettingsFragment()

        setCurrentFragment(myGardenFragment)

        binding.bottomNavigationBar.setOnItemSelectedListener {
            when(it.itemId){
                R.id.myGarden->setCurrentFragment(myGardenFragment)
                R.id.settings->setCurrentFragment(settingsFragment)
            }
            true
        }

    }

    private fun setCurrentFragment(fragment:Fragment)=
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment,fragment)
            commit()
        }

}

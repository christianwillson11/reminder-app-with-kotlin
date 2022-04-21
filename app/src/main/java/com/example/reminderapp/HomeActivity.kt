package com.example.reminderapp

import android.app.Activity
import android.content.ClipData
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val _fab = findViewById<FloatingActionButton>(R.id.fab)
        bottomNavigationView.background = null
        _fab.setOnClickListener {
            val eIntent = Intent(this@HomeActivity, AddScheduleActivity::class.java)
            startActivityForResult(eIntent, 101)
        }

        val navController = findNavController(R.id.homeFrContainer)

        bottomNavigationView.setupWithNavController(navController)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(applicationContext, "You have successfully upload data", Toast.LENGTH_SHORT).show()

            } else {
                Toast.makeText(applicationContext, "You canceled the action", Toast.LENGTH_LONG).show()
            }
        }
    }
}
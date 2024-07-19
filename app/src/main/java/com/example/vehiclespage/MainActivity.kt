package com.example.vehiclespage

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vehiclespage.databinding.ActivityMainBinding
import vehicleAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val fragmentAddVehicles = addVehicles()
        val fragmentMyVehicles = myVehicles()
        supportFragmentManager.beginTransaction().add(R.id.fragmentContainer, fragmentMyVehicles).commit()
        /*
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentContainer, fragmentMyVehicles)
            addToBackStack(null)
            commit()
        }*/






    }


}
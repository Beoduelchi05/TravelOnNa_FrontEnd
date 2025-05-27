package com.example.travelonna

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.travelonna.api.RetrofitClient
import android.util.Log

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val loginButton = findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        val homeButton = findViewById<Button>(R.id.homeButton)
        homeButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.createScheduleButton)?.setOnClickListener {
            val intent = Intent(this, ScheduleCreateActivity::class.java)
            startActivity(intent)
        }
        
        findViewById<Button>(R.id.planButton)?.setOnClickListener {
            val intent = Intent(this, PlanActivity::class.java)
            startActivity(intent)
        }
        
        findViewById<Button>(R.id.profileButton)?.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
        
        findViewById<Button>(R.id.logButton)?.setOnClickListener {
            val intent = Intent(this, LogActivity::class.java)
            startActivity(intent)
        }
        
        findViewById<Button>(R.id.searchButton)?.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }
    }
}
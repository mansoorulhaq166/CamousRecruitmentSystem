package com.example.campusrecruitmentsystem.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.campusrecruitmentsystem.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        if (auth.currentUser == null) {
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
            return
        }

        val userId = auth.currentUser?.uid

        val userRef: DatabaseReference = database.getReference("users").child(userId!!)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.progressBar.visibility = View.GONE
                binding.textViewGreeting.visibility = View.VISIBLE
                if (snapshot.exists()) {
                    val userName = snapshot.child("name").getValue(String::class.java)

                    val welcomeMessage = if (userName.isNullOrEmpty()) {
                        "Welcome!"
                    } else {
                        "Welcome, $userName!"
                    }

                    binding.textViewGreeting.text = welcomeMessage

                    when (snapshot.child("role").getValue(String::class.java)) {
                        "Recruiter" -> {
                            binding.buttonPostJob.visibility = View.VISIBLE
                            binding.buttonBrowseJobs.visibility = View.GONE
                        }

                        "Student" -> {
                            binding.buttonPostJob.visibility = View.GONE
                            binding.buttonBrowseJobs.visibility = View.VISIBLE
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database read error
            }
        })
        binding.buttonPostJob.setOnClickListener {
            startActivity(Intent(this@MainActivity, JobPostingActivity::class.java))
        }

        binding.buttonBrowseJobs.setOnClickListener {
            startActivity(Intent(this@MainActivity, JobBrowsingActivity::class.java))
        }

        binding.llLogout.setOnClickListener {
            logoutUser()
        }
    }

    private fun logoutUser() {
        auth.signOut()

        val intent = Intent(this, WelcomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}
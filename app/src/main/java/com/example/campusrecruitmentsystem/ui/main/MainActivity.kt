package com.example.campusrecruitmentsystem.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.campusrecruitmentsystem.databinding.ActivityMainBinding
import com.example.campusrecruitmentsystem.ui.recruiter.AppliedApplicationsActivity
import com.example.campusrecruitmentsystem.ui.recruiter.JobPostingActivity
import com.example.campusrecruitmentsystem.ui.recruiter.JobsPostedActivity
import com.example.campusrecruitmentsystem.ui.recruiter.NotificationsActivity
import com.example.campusrecruitmentsystem.ui.recruiter.TestManagementActivity
import com.example.campusrecruitmentsystem.ui.student.JobBrowsingActivity
import com.example.campusrecruitmentsystem.ui.student.JobsAppliedActivity
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
                            binding.buttonJobsPosted.visibility = View.VISIBLE
                            binding.buttonApplications.visibility = View.VISIBLE
                            binding.buttonTestManagement.visibility = View.VISIBLE
                            binding.buttonBrowseJobs.visibility = View.GONE
                            binding.buttonAppliedJobs.visibility = View.GONE
                            binding.imgNotification.visibility = View.VISIBLE
                        }

                        "Student" -> {
                            binding.buttonPostJob.visibility = View.GONE
                            binding.buttonJobsPosted.visibility = View.GONE
                            binding.buttonApplications.visibility = View.GONE
                            binding.imgNotification.visibility = View.GONE
                            binding.buttonBrowseJobs.visibility = View.VISIBLE
                            binding.buttonAppliedJobs.visibility = View.VISIBLE
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

        binding.buttonJobsPosted.setOnClickListener {
            startActivity(Intent(this@MainActivity, JobsPostedActivity::class.java))
        }

        binding.buttonApplications.setOnClickListener {
            startActivity(Intent(this@MainActivity, AppliedApplicationsActivity::class.java))
        }

        binding.buttonTestManagement.setOnClickListener {
            startActivity(Intent(this@MainActivity, TestManagementActivity::class.java))
        }

        binding.buttonBrowseJobs.setOnClickListener {
            startActivity(Intent(this@MainActivity, JobBrowsingActivity::class.java))
        }

        binding.buttonAppliedJobs.setOnClickListener {
            startActivity(Intent(this@MainActivity, JobsAppliedActivity::class.java))
        }

        binding.imgNotification.setOnClickListener {
            startActivity(Intent(this@MainActivity, NotificationsActivity::class.java))
        }

        binding.llProfile.setOnClickListener {
            startActivity(Intent(this@MainActivity, ProfileActivity::class.java))
        }
        binding.llLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishAffinity()
            }
        }

        onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Logout")
        builder.setMessage("Are you sure you want to logout?")

        builder.setPositiveButton("Yes") { dialog, _ ->
            logoutUser()
            dialog.dismiss()
        }

        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun logoutUser() {
        auth.signOut()

        val intent = Intent(this, WelcomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}
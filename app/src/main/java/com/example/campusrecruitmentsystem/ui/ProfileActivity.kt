package com.example.campusrecruitmentsystem.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.example.campusrecruitmentsystem.databinding.ActivityProfileBinding
import com.example.campusrecruitmentsystem.ui.recruiter.JobsPostedActivity
import com.example.campusrecruitmentsystem.ui.student.JobsAppliedActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        fetchUserInfo()

        binding.textViewAppliedJobsLink.setOnClickListener {
            startActivity(Intent(this@ProfileActivity, JobsAppliedActivity::class.java))
        }

        binding.textViewPostedJobsLink.setOnClickListener {
            startActivity(Intent(this@ProfileActivity, JobsPostedActivity::class.java))
        }

        binding.llHome.setOnClickListener {
            startActivity(Intent(this@ProfileActivity, MainActivity::class.java))
        }
        binding.llLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        binding.backImg.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun fetchUserInfo() {
        val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

        currentUser?.let { user ->
            val userId: String = user.uid
            val databaseReference: DatabaseReference =
                FirebaseDatabase.getInstance().getReference("users").child(userId)

            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val name: String? = snapshot.child("name").getValue(String::class.java)
                        val role: String? = snapshot.child("role").getValue(String::class.java)

                        name?.let { binding.textViewName.text = it }
                        role?.let {
                            binding.textViewRole.text = it
                            if (it.equals("student", ignoreCase = true)) {
                                binding.textViewPostedJobsLink.visibility = View.GONE
                                countApplications(userId)
                            } else if (role == "Recruiter") {
                                binding.textViewAppliedJobsLink.visibility = View.GONE
                                countPostedJobs(userId)
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }

    private fun countApplications(studentId: String) {
        val applicationsReference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("applications")
        val query: Query = applicationsReference.orderByChild("studentId").equalTo(studentId)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val applicationCount: Long = snapshot.childrenCount
                binding.textViewJobCount.text = "Applied for $applicationCount Jobs"
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun countPostedJobs(recruiterId: String) {
        val applicationsReference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("applications")
        val query: Query = applicationsReference.orderByChild("recruiterId").equalTo(recruiterId)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val applicationCount: Long = snapshot.childrenCount
                binding.textViewJobCount.text = "$applicationCount Jobs Posted"
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
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
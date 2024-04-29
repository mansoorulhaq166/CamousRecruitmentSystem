package com.example.campusrecruitmentsystem.ui.recruiter

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.campusrecruitmentsystem.databinding.ActivityApplicationDetailsBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ApplicationDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityApplicationDetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApplicationDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val applicationId = intent.getStringExtra("application")

        if (applicationId != null) {
            getApplicationInfoFromFirebase(applicationId)
        }
    }

    private fun getApplicationInfoFromFirebase(applicationId: String) {
        val applicationRef =
            FirebaseDatabase.getInstance().getReference("applications").child(applicationId)
        applicationRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val jobId = snapshot.child("jobId").value.toString()
                    val studentId = snapshot.child("studentId").value.toString()
                    val applicationDate = snapshot.child("applicationDate").value.toString()
                    val status = snapshot.child("status").value.toString()
                    val resumeUrl = snapshot.child("resumeUrl").value.toString()
                    val interViewDate = snapshot.child("resumeUrl").value.toString()
                    val comments = snapshot.child("comments").value.toString()
                    val internalNotes = snapshot.child("notes").value.toString()

                    binding.textViewApplicationDate.text = "Application Date: $applicationDate"

                    val studentRef =
                        FirebaseDatabase.getInstance().getReference("users").child(studentId)
                    studentRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(userSnapshot: DataSnapshot) {
                            if (userSnapshot.exists()) {
                                val studentName = userSnapshot.child("name").value.toString()
                                val contact = userSnapshot.child("contact").value.toString()
                                val email = userSnapshot.child("email").value.toString()

                                if (contact != "") {
                                    binding.textViewContactNumber.visibility = View.VISIBLE
                                }
                                // Populate UI with fetched data
                                binding.textViewName.text = "Name: $studentName"
                                binding.textViewContactEmail.text = "Contact Email: $email"
                                binding.textViewContactNumber.text = "Contact Number: $contact"
                                // Continue populating other UI elements
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Handle error
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}
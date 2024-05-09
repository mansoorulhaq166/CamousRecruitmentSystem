package com.example.campusrecruitmentsystem.ui.recruiter.application

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.campusrecruitmentsystem.R
import com.example.campusrecruitmentsystem.databinding.ActivityApplicationDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class ApplicationDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityApplicationDetailsBinding
    private lateinit var auth: FirebaseAuth
    private val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
    private val requestCode = 1234

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApplicationDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        val applicationId = intent.getStringExtra("application")

        if (applicationId != null) {
            getApplicationInfoFromFirebase(applicationId)
        }

        binding.backAppDetails.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        if (ContextCompat.checkSelfPermission(
                this,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        }

        checkScheduledInterview(applicationId)
        updateApplication(applicationId)
    }

    private fun updateApplication(applicationId: String?) {
        binding.edTextComments.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                checkFirebaseInterviewNode(
                    binding.edTextComments.text.toString(),
                    "comments",
                    applicationId
                )
            }
        })

        binding.edTextInternalNotes.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                checkFirebaseInterviewNode(
                    binding.edTextInternalNotes.text.toString(),
                    "notes",
                    applicationId
                )
            }
        })


        binding.imgCheckApplication.setOnClickListener {
            val notes = binding.edTextInternalNotes.text.toString().trim()
            val comments = binding.edTextComments.text.toString().trim()
            updateNewDetailsToApplication(notes, comments, applicationId)
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
                    val resumeUrl = snapshot.child("resumeUrl").value.toString()
                    val comments = snapshot.child("comments").value.toString()
                    val internalNotes = snapshot.child("notes").value.toString()
                    val status = snapshot.child("status").value.toString()

                    binding.textViewApplicationDate.text = "Application Date: $applicationDate"
                    binding.edTextComments.text =
                        Editable.Factory.getInstance().newEditable(comments)
                    binding.edTextInternalNotes.text =
                        Editable.Factory.getInstance().newEditable(internalNotes)
                    binding.textViewAppStatus.text = "Status: $status"

                    val studentRef =
                        FirebaseDatabase.getInstance().getReference("users").child(studentId)
                    studentRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(userSnapshot: DataSnapshot) {
                            if (userSnapshot.exists()) {
                                val studentName = userSnapshot.child("name").value.toString()
                                val contact = userSnapshot.child("contact").value.toString()
                                val email = userSnapshot.child("email").value.toString()

                                binding.textViewAdditionalDocs.setOnClickListener {
                                    downloadResume(resumeUrl, studentName)
                                }

                                binding.scheduleInterview.setOnClickListener {
                                    val interviewIntent = Intent(
                                        this@ApplicationDetailsActivity,
                                        ScheduleInterviewActivity::class.java
                                    )
                                    interviewIntent.putExtra("studentId", studentId)
                                    interviewIntent.putExtra("jobId", jobId)
                                    interviewIntent.putExtra("applicationId", applicationId)
                                    startActivity(interviewIntent)
                                }
                                if (contact != "") {
                                    binding.textViewContactNumber.visibility = View.VISIBLE
                                }

                                binding.textViewName.text = "Name: $studentName"
                                binding.textViewContactEmail.text = "Contact Email: $email"
                                binding.textViewContactNumber.text = "Contact Number: $contact"
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

    private fun downloadResume(
        resumeUrl: String,
        studentName: String
    ) {
        binding.downloadProgressbar.visibility = View.VISIBLE
        binding.textViewAdditionalDocs.visibility = View.GONE

        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(resumeUrl)

        val fileName = "$studentName - resume.pdf"
//        val fileName = "$studentName - $jobTitle - resume.pdf"
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            fileName
        )

        try {
//            val outputStream = FileOutputStream(file)
            storageRef.getFile(file)
                .addOnSuccessListener {
                    Toast.makeText(
                        this@ApplicationDetailsActivity,
                        "File downloaded successfully",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    binding.downloadProgressbar.visibility = View.GONE
                    binding.textViewAdditionalDocs.visibility = View.VISIBLE
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        this@ApplicationDetailsActivity,
                        "Failed to download resume",
                        Toast.LENGTH_SHORT
                    ).show()
                    exception.printStackTrace()
                    binding.downloadProgressbar.visibility = View.GONE
                    binding.textViewAdditionalDocs.visibility = View.VISIBLE
                }
                .addOnProgressListener { taskSnapshot ->
                    val progress =
                        (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()

                    binding.downloadProgressbar.progress = progress
                }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                this@ApplicationDetailsActivity,
                "Failed to download resume",
                Toast.LENGTH_SHORT
            ).show()
            binding.downloadProgressbar.visibility = View.GONE
            binding.textViewAdditionalDocs.visibility = View.VISIBLE
        }
    }

    private fun checkScheduledInterview(applicationId: String?) {
        val interviewsRef = FirebaseDatabase.getInstance().getReference("interviews")

        interviewsRef.orderByChild("applicationId").equalTo(applicationId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        binding.scheduleInterview.text = "Interview Scheduled"
                        binding.scheduleInterview.isEnabled = false
                    } else {
                        binding.scheduleInterview.text = "Schedule Interview"
                        binding.scheduleInterview.isEnabled = true
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("TAG", "Error retrieving scheduled interviews: ${databaseError.message}")
                }
            })
    }

    private fun checkFirebaseInterviewNode(
        text: String,
        fieldName: String,
        applicationId: String?
    ) {
        val applicationRefs = FirebaseDatabase.getInstance().getReference("applications").child(
            applicationId.toString()
        )

        applicationRefs.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val firebaseValue = snapshot.child(fieldName).getValue(String::class.java)
                    if (text != firebaseValue) {
                        binding.imgCheckApplication.setImageResource(R.drawable.baseline_check_bold)
                    } else {
                        binding.imgCheckApplication.setImageResource(R.drawable.baseline_check_light)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error fetching data: ${error.message}")
            }
        })
    }

    private fun updateNewDetailsToApplication(notes: String, comments: String, applicationId: String?) {
        val applicationRefs = FirebaseDatabase.getInstance().getReference("applications").child(
            applicationId.toString()
        )

        applicationRefs.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val currentComments = snapshot.child("comments").getValue(String::class.java)

                    applicationRefs.child("notes").setValue(notes)
                    applicationRefs.child("comments").setValue(comments)
                        .addOnSuccessListener {
                            if (currentComments != comments) {
                                // If comments have changed, update the status to "reviewed"
                                applicationRefs.child("status").setValue("reviewed")
                                    .addOnSuccessListener {
                                        Toast.makeText(this@ApplicationDetailsActivity, "Status updated to reviewed", Toast.LENGTH_SHORT).show()
                                        finish()
                                    }
                                    .addOnFailureListener { error ->
                                        Log.e("Firebase", "Failed to update status: ${error.message}")
                                    }
                            } else {
                                Toast.makeText(this@ApplicationDetailsActivity, "Details Updated Successfully", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        }
                        .addOnFailureListener { error ->
                            Log.e("Firebase", "Failed to update new details: ${error.message}")
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error fetching data: ${error.message}")
            }
        })
    }
}
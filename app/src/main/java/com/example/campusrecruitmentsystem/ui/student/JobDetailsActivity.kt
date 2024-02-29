package com.example.campusrecruitmentsystem.ui.student

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.campusrecruitmentsystem.R
import com.example.campusrecruitmentsystem.databinding.ActivityJobDetailsBinding
import com.example.campusrecruitmentsystem.models.ApplicationDetails
import com.example.campusrecruitmentsystem.models.Job
import com.example.campusrecruitmentsystem.ui.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.Date

class JobDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityJobDetailsBinding
    private var job: Job? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        @Suppress("DEPRECATION")
        job = intent.getParcelableExtra("job")

        if (job != null) {
            binding.textViewJobTitle.text = job!!.title
            binding.textViewCompany.text = job!!.company
            binding.textViewDeadline.text = job!!.deadline
            binding.textViewLocation.text = job!!.location
            binding.textViewDescription.text = job!!.description
            binding.textViewSalary.text = job!!.salary
            binding.textViewCriteria.text = job!!.criteria
        }

        // Checking if the student has already applied for this job
        checkJobStatus()

        binding.btnApply.setOnClickListener {
            showApplicationSubmissionDialog()
        }
    }

    private fun checkJobStatus() {
        val jobId = job?.id
        val studentId = FirebaseAuth.getInstance().currentUser?.uid

        val applicationReference = FirebaseDatabase.getInstance().getReference("applications")
        val studentQuery = applicationReference.orderByChild("studentId").equalTo(studentId)

        studentQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(studentSnapshot: DataSnapshot) {
                if (studentSnapshot.exists()) {
                    val applied = studentSnapshot.children.any { studentApplication ->
                        val applicationJobId = studentApplication.child("jobId").getValue(String::class.java)
                        applicationJobId == jobId
                    }

                    if (applied) {
                        // Student has applied for this specific job
                        binding.btnApply.text = getString(R.string.applied)
                        binding.btnApply.isEnabled = false
                    } else {
                        // Student has not applied for this specific job
                        binding.btnApply.text = getString(R.string.apply_now)
                        binding.btnApply.isEnabled = true
                    }
                } else {
                    binding.btnApply.text = getString(R.string.apply_now)
                    binding.btnApply.isEnabled = true
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TAG", "Failed to read student application data.", error.toException())
            }
        })
    }

    @Suppress("DEPRECATION")
    private fun chooseResumeFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        startActivityForResult(intent, 123)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 123 && resultCode == Activity.RESULT_OK) {
            val selectedFileUri: Uri? = data?.data
            binding.btnApply.text = getString(R.string.submit_application)
            binding.btnApply.setOnClickListener {
                uploadResumeToFirebaseStorage(selectedFileUri)
            }
        }
    }

    private fun uploadResumeToFirebaseStorage(fileUri: Uri?) {
        binding.btnApply.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
        binding.progressText.visibility = View.VISIBLE
        if (fileUri != null) {
            val storageRef = FirebaseStorage.getInstance().reference
            val resumeRef =
                storageRef.child("resumes/${FirebaseAuth.getInstance().currentUser?.uid}/${System.currentTimeMillis()}_${fileUri.lastPathSegment}")

            val uploadTask = resumeRef.putFile(fileUri)

            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress =
                    (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()

                // Update the progress bar
                binding.progressText.text = getString(R.string.upload_progress, progress)
                binding.progressBar.progress = progress
            }.addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()
                    // Storing the download URL and other details in the Firebase Database
                    storeApplicationDetails(downloadUrl)
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "Upload failed: $exception", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun storeApplicationDetails(downloadUrl: String) {
        val jobId = job?.id
        val studentId = FirebaseAuth.getInstance().currentUser?.uid

        getRecruiterId(jobId) { recruiterId ->
            if (recruiterId != null) {
                val application = ApplicationDetails(
                    jobId = jobId,
                    studentId = studentId,
                    recruiterId = recruiterId,
                    resumeUrl = downloadUrl,
                    applicationDate = getCurrentDate()
                )

                val databaseReference = FirebaseDatabase.getInstance().getReference("applications")
                val applicationId = databaseReference.push().key

                if (applicationId != null) {
                    databaseReference.child(applicationId).setValue(application)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    this,
                                    "Application submitted successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                                binding.btnApply.visibility = View.VISIBLE
                                binding.progressBar.visibility = View.GONE
                                startActivity(
                                    Intent(
                                        this@JobDetailsActivity,
                                        MainActivity::class.java
                                    )
                                )
                                finish()
                            } else {
                                binding.btnApply.visibility = View.VISIBLE
                                binding.progressBar.visibility = View.GONE
                                Toast.makeText(
                                    this,
                                    "Failed to submit application",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        }
                }
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDate = Date()
        return dateFormat.format(currentDate)
    }

    private fun getRecruiterId(jobId: String?, callback: (String?) -> Unit) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("jobs").child(jobId!!)
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val recruiterId = snapshot.child("recruiter_id").getValue(String::class.java)
                callback(recruiterId)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                callback(null)
            }
        })
    }

    private fun showApplicationSubmissionDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Application Submission")
        alertDialogBuilder.setMessage("Before submitting your application, please ensure you've uploaded your latest resume or cover letter.")
        alertDialogBuilder.setPositiveButton("Upload Now") { dialog, _ ->
            chooseResumeFile()
            dialog.dismiss()
        }
        alertDialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}
package com.example.campusrecruitmentsystem.ui.student.jobs

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.campusrecruitmentsystem.R
import com.example.campusrecruitmentsystem.databinding.ActivityAppliedJobsDetailBinding
import com.example.campusrecruitmentsystem.models.main.ApplicationDetails
import com.example.campusrecruitmentsystem.models.main.Job
import com.example.campusrecruitmentsystem.ui.main.MainActivity
import com.example.campusrecruitmentsystem.ui.student.InterviewStatusActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AppliedJobsDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAppliedJobsDetailBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppliedJobsDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        val jobId = intent.getStringExtra("jobId")
        val currentUserId = auth.currentUser?.uid

        binding.backAppliedJobDetails.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        if (jobId != null) {
            getJobDetails(jobId, currentUserId)
        } else {
            Toast.makeText(this, "No Job ID provided", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getApplicationDetails(jobId: String?, currentUserId: String?) {
        binding.progressbar.visibility = View.VISIBLE
        binding.llMain.visibility = View.GONE

        val applicationsRef = FirebaseDatabase.getInstance().getReference("applications")
        val applicationQuery = applicationsRef
            .orderByChild("jobId")
            .equalTo(jobId)

        applicationQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (applicationSnapshot in snapshot.children) {
                    val application = applicationSnapshot.getValue(ApplicationDetails::class.java)
                    if (application != null && application.studentId == currentUserId) {
                        val status = "Status: ${application.status}"
                        binding.textViewApplicationStatus.text = status
                        val comments = "Comments/Notes:  ${application.comments}"
                        if (application.comments != "") {
                            binding.textViewRecruiterComments.text = comments
                        } else {
                            binding.textViewRecruiterComments.visibility = View.GONE
                        }

                        if (status == "Accepted") {
                            binding.textViewCancelApplication.visibility = View.GONE
                        }

                        binding.textViewCancelApplication.setOnClickListener {
                            showCancelApplicationDialog(applicationSnapshot.key)
                        }
                        checkInterviewExists(applicationSnapshot.key)
                        return
                    }
                }
                Toast.makeText(
                    this@AppliedJobsDetailActivity,
                    "Application not found",
                    Toast.LENGTH_SHORT
                ).show()
                binding.progressbar.visibility = View.GONE
                binding.llMain.visibility = View.VISIBLE
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@AppliedJobsDetailActivity,
                    "Failed to load application details",
                    Toast.LENGTH_SHORT
                ).show()
                binding.progressbar.visibility = View.GONE
                binding.llMain.visibility = View.VISIBLE
            }
        })
    }
    private fun checkInterviewExists(applicationId: String?) {
        if (applicationId == null) {
            Toast.makeText(this, "Application ID is null", Toast.LENGTH_SHORT).show()
            return
        }

        val interviewsRef = FirebaseDatabase.getInstance().getReference("interviews")
        val interviewQuery = interviewsRef
            .orderByChild("applicationId")
            .equalTo(applicationId)

        interviewQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (interviewSnapshot in snapshot.children) {
                        val interviewKey = interviewSnapshot.key
                        binding.textViewInterviewDetails.text = getString(R.string.interview_details)
                        Toast.makeText(
                            this@AppliedJobsDetailActivity,
                            "Interview scheduled for this application.",
                            Toast.LENGTH_SHORT
                        ).show()
                        binding.progressbar.visibility = View.GONE
                        binding.llMain.visibility = View.VISIBLE

                        binding.textViewInterviewDetails.setOnClickListener {
                            val intent = Intent(this@AppliedJobsDetailActivity, InterviewStatusActivity::class.java)
                            intent.putExtra("interviewId", interviewKey)
                            startActivity(intent)
                        }
                        break
                    }
                } else {
                    // Interview does not exist
                    binding.textViewInterviewDetails.text = getString(R.string.no_interview)
                    binding.textViewInterviewDetails.isEnabled = false
                    binding.textViewInterviewDetails.background = ContextCompat.getDrawable(
                        this@AppliedJobsDetailActivity,
                        R.drawable.button_disabled
                    )
                    Toast.makeText(
                        this@AppliedJobsDetailActivity,
                        "No interview scheduled for this application.",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.progressbar.visibility = View.GONE
                    binding.llMain.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@AppliedJobsDetailActivity,
                    "Failed to check interview details",
                    Toast.LENGTH_SHORT
                ).show()
                binding.progressbar.visibility = View.GONE
                binding.llMain.visibility = View.VISIBLE
            }
        })
    }

    private fun getJobDetails(jobId: String, currentUserId: String?) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("jobs").child(jobId)
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val job = snapshot.getValue(Job::class.java)
                if (job != null) {
                    updateUI(job)
                    getApplicationDetails(jobId, currentUserId)
                } else {
                    Toast.makeText(
                        this@AppliedJobsDetailActivity,
                        "Job not found",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.progressbar.visibility = View.GONE
                    binding.llMain.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@AppliedJobsDetailActivity,
                    "Failed to load job details",
                    Toast.LENGTH_SHORT
                ).show()
                binding.progressbar.visibility = View.GONE
                binding.llMain.visibility = View.VISIBLE
            }
        })
    }

    private fun updateUI(job: Job) {
        val title = "Job Title: ${job.title}"
        val company = "Company Name: ${job.company}"
        val location = "Job Location: ${job.location}"
        val salary = "Job Salary: ${job.salary}"
        val deadline = "Application Deadline: ${job.deadline}"

        binding.textViewJobTitle.text = title
        binding.textViewCompanyName.text = company
        binding.textViewJobLocation.text = location
        binding.textViewJobSalary.text = salary
        binding.textViewApplicationDate.text = deadline
    }

    private fun showCancelApplicationDialog(applicationId: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Cancel Application")
        builder.setMessage("Are you sure you want to delete Application for this Job?")

        builder.setPositiveButton("Yes") { dialog, _ ->
            deleteApplication(applicationId)
            dialog.dismiss()
        }

        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun deleteApplication(applicationId: String?) {
        if (applicationId == null) {
            Toast.makeText(this, "Application ID is null", Toast.LENGTH_SHORT).show()
            return
        }

        val applicationsRef = FirebaseDatabase.getInstance().getReference("applications")
        val applicationRef = applicationsRef.child(applicationId)

        applicationRef.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Application deleted successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@AppliedJobsDetailActivity, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Failed to delete application", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
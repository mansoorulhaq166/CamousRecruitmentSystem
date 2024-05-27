package com.example.campusrecruitmentsystem.ui.recruiter

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.campusrecruitmentsystem.databinding.ActivityJobPostingBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class JobPostingActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityJobPostingBinding
    private val calendar: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobPostingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        binding.editTextJobDeadline.setOnClickListener {
            showDatePickerDialog()
        }

        binding.backJobPosting.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnPostJob.setOnClickListener {
            val jobTitle = binding.editTextJobTitle.text.toString().trim()
            val jobDescription = binding.editTextJobDescription.text.toString().trim()
            val jobSalary = binding.editTextJobSalary.text.toString().trim()
            val jobLocation = binding.editTextJobLocation.text.toString().trim()
            val criteria = binding.editTextEligibilityCriteria.text.toString().trim()
            val companyName = binding.editTextCompanyName.text.toString().trim()
            val deadline = binding.editTextJobDeadline.text.toString().trim()

            binding.jobTitleLayout.error = null
            binding.companyNameLayout.error = null
            binding.jobDescriptionLayout.error = null
            binding.jobSalaryLayout.error = null
            binding.jobLocationLayout.error = null
            binding.jobCriteriaLayout.error = null
            binding.editTextJobDeadline.error = null

            if (jobTitle.isEmpty()) {
                binding.jobTitleLayout.error = "Job Title cannot be empty"
            } else if (companyName.isEmpty()) {
                binding.companyNameLayout.error = "Company Name cannot be empty"
            } else if (jobDescription.isEmpty()) {
                binding.jobDescriptionLayout.error = "Job Description cannot be empty"
            } else if (jobSalary.isEmpty()) {
                binding.jobSalaryLayout.error = "Job Salary cannot be empty"
            } else if (jobLocation.isEmpty()) {
                binding.jobLocationLayout.error = "Job Location cannot be empty"
            } else if (criteria.isEmpty()) {
                binding.jobCriteriaLayout.error = "Eligibility Criteria cannot be empty"
            } else if (deadline.isEmpty()) {
                binding.jobDeadlineLayout.error = "Deadline cannot be empty"
            } else {
                binding.btnPostJob.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE

                // Save job details to Firebase Database
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    val job = hashMapOf(
                        "title" to jobTitle,
                        "description" to jobDescription,
                        "salary" to jobSalary,
                        "location" to jobLocation,
                        "criteria" to criteria,
                        "company" to companyName,
                        "deadline" to deadline,
                        "recruiter_id" to userId
                    )

                    val jobId = FirebaseDatabase.getInstance().getReference("jobs").push().key
                    if (jobId != null) {
                        val jobsReference =
                            FirebaseDatabase.getInstance().getReference("jobs").child(jobId)
                        job["id"] = jobId
                        jobsReference.setValue(job)
                            .addOnCompleteListener { jobTask ->
                                if (jobTask.isSuccessful) {
                                    Toast.makeText(
                                        this,
                                        "Job posted successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    binding.btnPostJob.visibility = View.VISIBLE
                                    binding.progressBar.visibility = View.GONE
                                    binding.editTextJobTitle.text.clear()
                                    binding.editTextCompanyName.text.clear()
                                    binding.editTextJobDescription.text.clear()
                                    binding.editTextJobSalary.text.clear()
                                    binding.editTextJobLocation.text.clear()
                                    binding.editTextEligibilityCriteria.text.clear()

                                    finish()
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Error posting job: ${jobTask.exception}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    } else {
                        Toast.makeText(this, "Error generating job ID", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showDatePickerDialog() {
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                // Update your UI with the selected date
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                updateDeadline(selectedDate.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Set minimum date (optional)
        datePicker.datePicker.minDate = System.currentTimeMillis() - 1000

        // Show the date picker dialog
        datePicker.show()
    }

    private fun updateDeadline(selectedDate: Date) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(selectedDate)
        binding.editTextJobDeadline.setText(formattedDate)
    }
}
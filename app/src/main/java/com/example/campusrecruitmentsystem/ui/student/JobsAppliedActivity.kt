package com.example.campusrecruitmentsystem.ui.student

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusrecruitmentsystem.adapters.JobAdapter
import com.example.campusrecruitmentsystem.databinding.ActivityJobsAppliedBinding
import com.example.campusrecruitmentsystem.models.main.ApplicationDetails
import com.example.campusrecruitmentsystem.models.main.Job
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class JobsAppliedActivity : AppCompatActivity() {

    private lateinit var jobAdapter: JobAdapter
    private lateinit var jobsList: MutableList<Job>
    private lateinit var binding: ActivityJobsAppliedBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var currentUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobsAppliedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser!!
        database = FirebaseDatabase.getInstance().reference
        jobsList = mutableListOf()

        binding.recyclerViewAppliedJobs.layoutManager = LinearLayoutManager(this)
        jobAdapter = JobAdapter(jobsList, false, this@JobsAppliedActivity)
        binding.recyclerViewAppliedJobs.adapter = jobAdapter

        binding.backJobsApplied.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        fetchAppliedJobs()
    }

    private fun fetchAppliedJobs() {
        val studentId = currentUser.uid
        val applicationReference =
            database.child("applications").orderByChild("studentId").equalTo(studentId)

        applicationReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    jobsList.clear()
                    for (applicationSnapshot in snapshot.children) {
                        val application =
                            applicationSnapshot.getValue(ApplicationDetails::class.java)
                        val jobId = application?.jobId

                        if (jobId != null) {
                            // Fetch the job details using the jobId
                            val jobReference = database.child("jobs").child(jobId)
                            jobReference.addListenerForSingleValueEvent(object :
                                ValueEventListener {
                                override fun onDataChange(jobSnapshot: DataSnapshot) {
                                    val job = jobSnapshot.getValue(Job::class.java)
                                    if (job != null) {
                                        jobsList.add(job)
                                        jobAdapter.notifyDataSetChanged()
                                    }
                                    if (jobsList.isEmpty()) {
                                        binding.textViewNoJobsApplied.visibility = View.VISIBLE
                                    } else {
                                        binding.textViewNoJobsApplied.visibility = View.GONE
                                    }
                                    binding.progressBar.visibility = View.GONE
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e(
                                        "JobsAppliedActivity",
                                        "Error fetching job details: ${error.message}"
                                    )
                                }
                            })
                        }
                    }
                } else {
                    binding.textViewNoJobsApplied.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("JobsAppliedActivity", "Error fetching applied jobs: ${error.message}")
            }
        })
    }
}
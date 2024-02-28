package com.example.campusrecruitmentsystem.ui.recruiter

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusrecruitmentsystem.adapters.JobAdapter
import com.example.campusrecruitmentsystem.databinding.ActivityJobsPostedBinding
import com.example.campusrecruitmentsystem.models.Job
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class JobsPostedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJobsPostedBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var currentUser: FirebaseUser
    private lateinit var postedJobs: MutableList<Job>
    private lateinit var jobAdapter:JobAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobsPostedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser!!
        database = FirebaseDatabase.getInstance().reference
        postedJobs = mutableListOf()

        binding.recyclerViewPostedJobs.layoutManager = LinearLayoutManager(this)
        jobAdapter = JobAdapter(postedJobs)
        binding.recyclerViewPostedJobs.adapter = jobAdapter

        fetchPostedJobs()
    }

    private fun fetchPostedJobs() {
        val recruiterId = currentUser.uid
        val jobsReference = database.child("jobs").orderByChild("recruiter_id").equalTo(recruiterId)

        jobsReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                postedJobs.clear()

                for (jobSnapshot in snapshot.children) {
                    val job = jobSnapshot.getValue(Job::class.java)
                    if (job != null) {
                        postedJobs.add(job)
                    }
                }

                if (postedJobs.isEmpty()) {
                    binding.textViewNoJobsPosted.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                } else {
                    binding.textViewNoJobsPosted.visibility = View.GONE
                    jobAdapter.notifyDataSetChanged()
                    binding.progressBar.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("JobsPostedActivity", "Error fetching posted jobs: ${error.message}")
            }
        })
    }
}
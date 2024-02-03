package com.example.campusrecruitmentsystem.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusrecruitmentsystem.adapters.JobAdapter
import com.example.campusrecruitmentsystem.databinding.ActivityJobBrowsingBinding
import com.example.campusrecruitmentsystem.models.Job
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class JobBrowsingActivity : AppCompatActivity() {

    private lateinit var jobAdapter: JobAdapter
    private lateinit var binding: ActivityJobBrowsingBinding
    private lateinit var jobList: MutableList<Job>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobBrowsingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        jobList = mutableListOf()
        jobAdapter = JobAdapter(jobList)

        binding.recyclerViewJobs.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewJobs.adapter = jobAdapter

        retrieveJobsFromDatabase()
    }

    private fun retrieveJobsFromDatabase() {
        val databaseReference = FirebaseDatabase.getInstance().getReference("jobs")
        databaseReference.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                jobList.clear()
                for (childSnapshot in snapshot.children) {
                    val job = childSnapshot.getValue(Job::class.java)
                    if (job != null) {
                        jobList.add(job)
                        binding.progressBar.visibility = View.GONE
                    }
                }
                jobList.reverse()
                jobAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("TAG", "Failed to read value.", error.toException())
            }
        })
    }
}
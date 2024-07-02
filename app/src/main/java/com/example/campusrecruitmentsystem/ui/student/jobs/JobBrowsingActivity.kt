package com.example.campusrecruitmentsystem.ui.student.jobs

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusrecruitmentsystem.adapters.JobAdapter
import com.example.campusrecruitmentsystem.databinding.ActivityJobBrowsingBinding
import com.example.campusrecruitmentsystem.listeners.OnItemClickListener
import com.example.campusrecruitmentsystem.models.main.Job
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class JobBrowsingActivity : AppCompatActivity(), OnItemClickListener {

    private lateinit var jobAdapter: JobAdapter
    private lateinit var binding: ActivityJobBrowsingBinding
    private lateinit var jobList: MutableList<Job>
    private lateinit var filteredJobList: MutableList<Job>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobBrowsingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        jobList = mutableListOf()
        filteredJobList = mutableListOf()
        jobAdapter = JobAdapter(
            filteredJobList, false,
            fromMain = false,
            false,
            context = this@JobBrowsingActivity
        )

        binding.recyclerViewJobs.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewJobs.adapter = jobAdapter

        jobAdapter.setOnItemClickListener(this)
        retrieveJobsFromDatabase()

        binding.backBrowseJobs.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.searchImg.setOnClickListener {
            toggleSearchBar()
        }

        binding.editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilters(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
                // Not needed
            }
        })
    }

    private fun retrieveJobsFromDatabase() {
        val databaseReference = FirebaseDatabase.getInstance().getReference("jobs")
        databaseReference.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                jobList.clear()
                filteredJobList.clear()
                for (childSnapshot in snapshot.children) {
                    val job = childSnapshot.getValue(Job::class.java)
                    job?.let {
                        jobList.add(it)
                    }
                }
                if (jobList.isEmpty()) {
                    binding.textViewNoJobs.visibility = View.VISIBLE
                } else {
                    binding.textViewNoJobs.visibility = View.GONE
                }
                jobList.reverse()
                applyFilters(binding.editTextSearch.text.toString())
                binding.progressBar.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TAG", "Failed to read value.", error.toException())
            }
        })
    }

    private fun applyFilters(query: String) {
        filteredJobList.clear()
        if (query.isNotEmpty()) {
            for (job in jobList) {
                if (job.title.contains(query, ignoreCase = true) || job.company.contains(
                        query,
                        ignoreCase = true
                    ) || (job.location.contains(query, true))
                ) {
                    filteredJobList.add(job)
                }
            }
        } else {
            filteredJobList.addAll(jobList)
        }
        jobAdapter.notifyDataSetChanged()
    }

    override fun onItemClick(position: Int) {
        val selectedJob = jobList[position]
        val intent = Intent(this, JobDetailsActivity::class.java)
        intent.putExtra("job", selectedJob)
        startActivity(intent)
    }

    private fun toggleSearchBar() {
        if (binding.editTextSearch.visibility == View.GONE) {
            binding.editTextSearch.alpha = 0f
            binding.editTextSearch.visibility = View.VISIBLE
            binding.headerBrowseJobs.animate()
                .alpha(0f)
                .setDuration(300)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        binding.headerBrowseJobs.visibility = View.GONE
                    }
                })
            binding.backBrowseJobs.animate()
                .alpha(0f)
                .setDuration(300)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        binding.backBrowseJobs.visibility = View.GONE
                    }
                })
            binding.editTextSearch.animate()
                .alpha(1f)
                .setDuration(300)
                .setListener(null)
            binding.editTextSearch.requestFocus()
            // Show soft keyboard
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.editTextSearch, InputMethodManager.SHOW_IMPLICIT)
        } else {
            binding.editTextSearch.animate()
                .alpha(0f)
                .setDuration(300)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        binding.editTextSearch.visibility = View.GONE
                        binding.editTextSearch.setText("")
                    }
                })
            binding.headerBrowseJobs.alpha = 0f
            binding.headerBrowseJobs.visibility = View.VISIBLE
            binding.backBrowseJobs.alpha = 0f
            binding.backBrowseJobs.visibility = View.VISIBLE
            binding.headerBrowseJobs.animate()
                .alpha(1f)
                .setDuration(300)
                .setListener(null)
            binding.backBrowseJobs.animate()
                .alpha(1f)
                .setDuration(300)
                .setListener(null)
            // Hide soft keyboard
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.editTextSearch.windowToken, 0)
        }
    }
}
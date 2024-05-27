package com.example.campusrecruitmentsystem.ui.recruiter.application

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusrecruitmentsystem.R
import com.example.campusrecruitmentsystem.adapters.ApplicationsAdapter
import com.example.campusrecruitmentsystem.databinding.ActivityAppliedApplicationsBinding
import com.example.campusrecruitmentsystem.models.main.ApplicationDetails
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class AppliedApplicationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppliedApplicationsBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: ApplicationsAdapter
    private lateinit var filteredApplications: List<ApplicationDetails>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppliedApplicationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backApplication.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        auth = FirebaseAuth.getInstance()
        val currentUserId = auth.currentUser?.uid

        database = FirebaseDatabase.getInstance().getReference("applications")

        binding.btnPending.isActivated = true
        val allApplicationsQuery: Query =
            database.orderByChild("recruiterId").equalTo(currentUserId)

        allApplicationsQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(allApplicationsSnapshot: DataSnapshot) {
                val allApplications = mutableListOf<ApplicationDetails>()

                // Iterate through all applications
                for (applicationSnapshot in allApplicationsSnapshot.children) {
                    val application = applicationSnapshot.getValue(ApplicationDetails::class.java)
                    if (application != null) {
                        allApplications.add(application)
                    }
                }

                filteredApplications = allApplications.filter { it.recruiterId == currentUserId }
                updateRecyclerView()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("TAG", "onCancelled: " + error.message)
            }
        })

        binding.btnPending.setOnClickListener {
            binding.apply {
                btnPending.isActivated = true
                btnReviewed.isActivated = false
                btnAccepted.isActivated = false
                btnRejected.isActivated = false
            }
            updateRecyclerView()
        }

        binding.btnReviewed.setOnClickListener {
            // Update button states
            binding.apply {
                btnPending.isActivated = false
                btnReviewed.isActivated = true
                btnAccepted.isActivated = false
                btnRejected.isActivated = false
            }
            updateRecyclerView()
        }

        binding.btnAccepted.setOnClickListener {
            binding.apply {
                btnPending.isActivated = false
                btnReviewed.isActivated = false
                btnAccepted.isActivated = true
                btnRejected.isActivated = false
            }
            updateRecyclerView()
        }

        binding.btnRejected.setOnClickListener {
            binding.apply {
                btnPending.isActivated = false
                btnReviewed.isActivated = false
                btnAccepted.isActivated = false
                btnRejected.isActivated = true
            }
            updateRecyclerView()
        }
    }

    private fun updateRecyclerView() {
        val layoutManager = LinearLayoutManager(this@AppliedApplicationsActivity)
        binding.recyclerview.layoutManager = layoutManager

        val status = when {
            binding.btnPending.isActivated -> "Pending"
            binding.btnReviewed.isActivated -> "Reviewed"
            binding.btnAccepted.isActivated -> "Accepted"
            binding.btnRejected.isActivated -> "Rejected"
            else -> return
        }

        val filteredList = filteredApplications.filter { application ->
            application.status == status
        }

        adapter = ApplicationsAdapter(filteredList)
        binding.recyclerview.adapter = adapter
        binding.progressBar.visibility = View.GONE

        binding.textViewNoApplications.visibility =
            if (filteredList.isEmpty()) View.VISIBLE else View.GONE

        binding.textViewNoApplications.text = "No Applications $status"

        if (binding.btnPending.isActivated) {
            binding.btnPending.setBackgroundResource(R.drawable.button_activated_background)
            binding.btnPending.elevation = 16F
        } else {
            binding.btnPending.setBackgroundResource(R.drawable.login_bg)
            binding.btnPending.elevation = 0F
        }

        if (binding.btnReviewed.isActivated) {
            binding.btnReviewed.setBackgroundResource(R.drawable.button_activated_background)
            binding.btnReviewed.elevation = 16F
        } else {
            binding.btnReviewed.setBackgroundResource(R.drawable.login_bg)
            binding.btnReviewed.elevation = 0F
        }

        if (binding.btnAccepted.isActivated) {
            binding.btnAccepted.setBackgroundResource(R.drawable.button_activated_background)
            binding.btnAccepted.elevation = 16F
        } else {
            binding.btnAccepted.setBackgroundResource(R.drawable.login_bg)
            binding.btnAccepted.elevation = 0F
        }

        if (binding.btnRejected.isActivated) {
            binding.btnRejected.setBackgroundResource(R.drawable.button_activated_background)
            binding.btnRejected.elevation = 16F
        } else {
            binding.btnRejected.setBackgroundResource(R.drawable.login_bg)
            binding.btnRejected.elevation = 0F
        }
    }
}
package com.example.campusrecruitmentsystem.ui.recruiter

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
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
    private val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
    private val requestCode = 1234

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppliedApplicationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (ContextCompat.checkSelfPermission(
                this,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        }

        binding.backApplication.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        auth = FirebaseAuth.getInstance()
        val currentUserId = auth.currentUser?.uid

        database = FirebaseDatabase.getInstance().getReference("applications")

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

                // Filter applications based on the job ID and recruiter ID
                val filteredApplications =
                    allApplications.filter { it.recruiterId == currentUserId }
                val layoutManager = LinearLayoutManager(this@AppliedApplicationsActivity)
                binding.recyclerview.layoutManager = layoutManager
                val adapter = ApplicationsAdapter(filteredApplications)
                binding.recyclerview.adapter = adapter

                binding.progressBar.visibility = View.GONE
                binding.textViewNoApplications.visibility = View.GONE

                if (filteredApplications.isEmpty()) {
                    binding.textViewNoApplications.visibility = View.VISIBLE
                } else {
                    binding.textViewNoApplications.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}
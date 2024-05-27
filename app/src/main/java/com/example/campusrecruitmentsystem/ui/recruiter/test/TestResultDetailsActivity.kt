package com.example.campusrecruitmentsystem.ui.recruiter.test

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusrecruitmentsystem.adapters.recruiter.ResponseAdapter
import com.example.campusrecruitmentsystem.databinding.ActivityTestResultDetailsBinding
import com.example.campusrecruitmentsystem.models.main.User
import com.example.campusrecruitmentsystem.models.recruiter.Test
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TestResultDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTestResultDetailsBinding
    private lateinit var databaseReference: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestResultDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val testId = intent.getStringExtra("testId")
        val userId = intent.getStringExtra("userId")
        val responses = intent.getStringArrayListExtra("responses")

        if (testId != null && userId != null && responses != null) {
            setupRecyclerView(responses)
            fetchTestDetails(testId)
            fetchUserDetails(userId)
        } else {
            Toast.makeText(this, "Missing test details", Toast.LENGTH_SHORT).show()
        }
    }
    private fun fetchUserDetails(userId: String) {
        databaseReference = FirebaseDatabase.getInstance().getReference("users/$userId")
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    binding.tvUserName.text = "Student Name: ${user.name}"
                    // Populate other views with user details if needed
                } else {
                    Toast.makeText(
                        this@TestResultDetailsActivity,
                        "Student not found",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TestResultDetailsActivity, error.message, Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }


    private fun fetchTestDetails(testId: String) {
        databaseReference = FirebaseDatabase.getInstance().getReference("tests/$testId")
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val test = snapshot.getValue(Test::class.java)
                if (test != null) {
                    binding.tvTestName.text = "Test Name: ${test.testName}"
                    // Populate other views with test details if needed
                } else {
                    Toast.makeText(
                        this@TestResultDetailsActivity,
                        "Test not found",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TestResultDetailsActivity, error.message, Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun setupRecyclerView(responses: List<String>) {
        binding.rvResponses.layoutManager = LinearLayoutManager(this)
        binding.rvResponses.adapter = ResponseAdapter(responses)
    }
}
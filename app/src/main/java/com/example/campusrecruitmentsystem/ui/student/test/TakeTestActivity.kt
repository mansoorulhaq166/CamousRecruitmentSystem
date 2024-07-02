package com.example.campusrecruitmentsystem.ui.student.test

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusrecruitmentsystem.adapters.test.TakeTestAdapter
import com.example.campusrecruitmentsystem.databinding.ActivityTakeTestBinding
import com.example.campusrecruitmentsystem.models.recruiter.Test
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TakeTestActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTakeTestBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: TakeTestAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTakeTestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        adapter = TakeTestAdapter(this@TakeTestActivity, mutableListOf())
        binding.recyclerViewTests.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewTests.adapter = adapter

        val testsRef = FirebaseDatabase.getInstance().reference.child("tests")
        fetchAvailableTests(testsRef)

        binding.backTakeTests.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun fetchAvailableTests(databaseReference: DatabaseReference) {
        val currentUserId = auth.currentUser?.uid
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val tests = mutableListOf<Test>()
                    for (snapshot in dataSnapshot.children) {
                        val recruiterId =
                            snapshot.child("userId").getValue(String::class.java) ?: ""
                        val testId = snapshot.key
                        val testName =
                            snapshot.child("testName").getValue(String::class.java) ?: ""
                        val creationTime =
                            snapshot.child("creationTime").getValue(Long::class.java) ?: 0
                        val testTime =
                            snapshot.child("testTime").getValue(String::class.java) ?: ""
                        val testType =
                            snapshot.child("testType").getValue(String::class.java) ?: ""
                        val jobId =
                            snapshot.child("jobId").getValue(String::class.java) ?: ""

                        tests.add(Test(testId.toString(), testName, testTime, creationTime, testType, recruiterId, jobId))
                    }

                    adapter.clearData()
                    adapter.addAllData(tests)

                    if (tests.isEmpty()) {
                        binding.textViewEmpty.visibility = View.VISIBLE
                        binding.progressBar.visibility = View.GONE
                        binding.recyclerViewTests.visibility = View.GONE
                    } else {
                        binding.textViewEmpty.visibility = View.GONE
                        binding.progressBar.visibility = View.GONE
                        binding.recyclerViewTests.visibility = View.VISIBLE
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@TakeTestActivity, databaseError.message, Toast.LENGTH_SHORT)
                    .show()
                binding.progressBar.visibility = View.GONE
                binding.textViewEmpty.visibility = View.VISIBLE
            }
        })
    }
}
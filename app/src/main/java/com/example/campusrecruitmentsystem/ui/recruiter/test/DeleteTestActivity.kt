package com.example.campusrecruitmentsystem.ui.recruiter.test

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusrecruitmentsystem.adapters.test.DeleteTestAdapter
import com.example.campusrecruitmentsystem.databinding.ActivityDeleteTestBinding
import com.example.campusrecruitmentsystem.listeners.TestItemClickListener
import com.example.campusrecruitmentsystem.models.recruiter.Test
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DeleteTestActivity : AppCompatActivity(), TestItemClickListener {
    private lateinit var binding: ActivityDeleteTestBinding
    private lateinit var deleteTestAdapter: DeleteTestAdapter
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeleteTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        deleteTestAdapter = DeleteTestAdapter(mutableListOf())
        binding.recyclerViewTests.adapter = deleteTestAdapter
        binding.recyclerViewTests.layoutManager = LinearLayoutManager(this)

        deleteTestAdapter.setOnItemClickListener(this)

        val testsRef = FirebaseDatabase.getInstance().reference.child("tests")
        fetchDataFromFirebase(testsRef)
    }

    private fun fetchDataFromFirebase(databaseReference: DatabaseReference) {
        val userId = auth.currentUser?.uid
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    var hasData = false
                    for (snapshot in dataSnapshot.children) {
                        val recruiterId =
                            snapshot.child("userId").getValue(String::class.java) ?: ""
                        if (userId == recruiterId) {
                            val testId = snapshot.key
                            val testName =
                                snapshot.child("testName").getValue(String::class.java) ?: ""
                            val creationTime =
                                snapshot.child("creationTime").getValue(Long::class.java) ?: 0
                            val testTime =
                                snapshot.child("testTime").getValue(String::class.java) ?: ""
                            val testType =
                                snapshot.child("testType").getValue(String::class.java) ?: ""

                            val type: Int = when (testType) {
                                "Multiple Choice" -> {
                                    1
                                }

                                "True/False" -> {
                                    2
                                }

                                "Short Answers" -> {
                                    3
                                }

                                else -> {
                                    1
                                }
                            }

                            deleteTestAdapter.addData(
                                Test(
                                    testId.toString(),
                                    testName,
                                    testTime,
                                    creationTime,
                                    type
                                )
                            )
                            hasData = true
                        }
                    }
                    if (hasData) {
                        binding.progressBar.visibility = View.GONE
                        binding.recyclerViewTests.visibility = View.VISIBLE
                        binding.textViewEmpty.visibility = View.GONE
                    } else {
                        binding.progressBar.visibility = View.GONE
                        binding.recyclerViewTests.visibility = View.GONE
                        binding.textViewEmpty.visibility = View.VISIBLE
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@DeleteTestActivity, databaseError.message, Toast.LENGTH_SHORT)
                    .show()
                binding.progressBar.visibility = View.GONE
            }
        })
    }

    override fun onDeleteClicked(position: Int, testId: String, testType: Int) {
        val userId = auth.currentUser?.uid

        val testsRef = FirebaseDatabase.getInstance().reference.child("tests")
        var reference = testsRef.child("MultipleChoiceTests")

        when (testType) {
            1 -> {
                reference = testsRef.child("MultipleChoiceTests")
            }

            2 -> {
                reference = testsRef.child("TrueFalseTests")
            }

            3 -> {
                reference = testsRef.child("ShortAnswerTests")
            }
        }

        reference.child(testId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val testUserId = dataSnapshot.child("userId").getValue(String::class.java)
                if (testUserId == userId) {
                    dataSnapshot.ref.removeValue()
                        .addOnSuccessListener {
                            Log.d("TAG", "Test deleted successfully")
                        }
                        .addOnFailureListener { e ->
                            Log.e("TAG", "Failed to delete test: ${e.message}")
                        }
                } else {
                    Log.e("TAG", "User does not have permission to delete this test")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
                Log.e("TAG", "Database error: ${databaseError.message}")
            }
        })
    }
}
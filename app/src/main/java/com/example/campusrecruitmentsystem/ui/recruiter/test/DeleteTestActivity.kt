package com.example.campusrecruitmentsystem.ui.recruiter.test

import android.os.Bundle
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

        binding.backTestDelete.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun fetchDataFromFirebase(databaseReference: DatabaseReference) {
        val userId = auth.currentUser?.uid
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
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

                            deleteTestAdapter.addData(
                                Test(
                                    testId.toString(),
                                    testName,
                                    testTime,
                                    creationTime,
                                    testType
                                )
                            )
                        }
                    }

                    binding.progressBar.visibility = View.GONE
                    binding.recyclerViewTests.visibility = View.VISIBLE
                    binding.textViewEmpty.visibility = View.GONE

                } else {
                    binding.progressBar.visibility = View.GONE
                    binding.recyclerViewTests.visibility = View.GONE
                    binding.textViewEmpty.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@DeleteTestActivity, databaseError.message, Toast.LENGTH_SHORT)
                    .show()
                binding.progressBar.visibility = View.GONE
            }
        })
    }

    override fun onDeleteClicked(position: Int, testId: String) {
        val userId = auth.currentUser?.uid
        val testsRef = FirebaseDatabase.getInstance().reference.child("tests")
        testsRef.child(testId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val testUserId = dataSnapshot.child("userId").getValue(String::class.java)
                if (testUserId == userId) {
                    dataSnapshot.ref.removeValue()
                        .addOnSuccessListener {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.recyclerViewTests.visibility = View.GONE
                            Toast.makeText(
                                this@DeleteTestActivity,
                                "Test Deleted Successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this@DeleteTestActivity,
                                "${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    Toast.makeText(
                        this@DeleteTestActivity,
                        "User does not have permission to delete this test",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@DeleteTestActivity, databaseError.message, Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }
}
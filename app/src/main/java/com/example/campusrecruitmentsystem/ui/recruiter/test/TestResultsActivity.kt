package com.example.campusrecruitmentsystem.ui.recruiter.test

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusrecruitmentsystem.R
import com.example.campusrecruitmentsystem.adapters.recruiter.TestResultsAdapter
import com.example.campusrecruitmentsystem.databinding.ActivityTestResultsBinding
import com.example.campusrecruitmentsystem.models.recruiter.Test
import com.example.campusrecruitmentsystem.models.recruiter.TestResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TestResultsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTestResultsBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private val testResults = mutableListOf<TestResult>()
    private lateinit var testResultsAdapter: TestResultsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestResultsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("test_submissions")

        binding.backTestResults.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.resultsRecyclerview.layoutManager = LinearLayoutManager(this)
        testResultsAdapter = TestResultsAdapter(testResults) { result ->
            val intent = Intent(this@TestResultsActivity, TestResultDetailsActivity::class.java).apply {
                putExtra("testId", result.testId)
                putExtra("userId", result.userId)
                putExtra("testName", result.testName)
                putExtra("testDate", result.testDate)
                putExtra("testDuration", result.testDuration)
                putStringArrayListExtra("responses", ArrayList(result.responses))
            }
            startActivity(intent)
        }
        binding.resultsRecyclerview.adapter = testResultsAdapter

        fetchTestResults()
    }

    private fun fetchTestResults() {
        val currentUserId = auth.currentUser?.uid ?: return

        binding.progressBar.visibility = View.VISIBLE
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                testResults.clear()
                if (!snapshot.exists()) {
                    binding.progressBar.visibility = View.GONE
                    binding.textNoResults.visibility = View.VISIBLE
                    return
                }

                var remaining = snapshot.childrenCount
                for (dataSnapshot in snapshot.children) {
                    val testResult = dataSnapshot.getValue(TestResult::class.java)
                    testResult?.let {
                        val testRef = FirebaseDatabase.getInstance().getReference("tests/${it.testId}")
                        testRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(testSnapshot: DataSnapshot) {
                                val test = testSnapshot.getValue(Test::class.java)
                                if (test?.userId == currentUserId) {
                                    testResult.testName = test.testName
                                    testResult.testDate = test.creationTime
                                    testResult.testDuration = test.testTime
                                    testResults.add(testResult)
                                }

                                remaining -= 1
                                if (remaining == 0L) {
                                    testResultsAdapter.notifyDataSetChanged()
                                    binding.progressBar.visibility = View.GONE
                                    binding.textNoResults.visibility = if (testResults.isEmpty()) View.VISIBLE else View.GONE
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(this@TestResultsActivity, error.message, Toast.LENGTH_SHORT).show()
                                binding.progressBar.visibility = View.GONE
                            }
                        })
                    } ?: run {
                        remaining -= 1
                        if (remaining == 0L) {
                            testResultsAdapter.notifyDataSetChanged()
                            binding.progressBar.visibility = View.GONE
                            binding.textNoResults.visibility = if (testResults.isEmpty()) View.VISIBLE else View.GONE
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TestResultsActivity, error.message, Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
            }
        })
    }

}
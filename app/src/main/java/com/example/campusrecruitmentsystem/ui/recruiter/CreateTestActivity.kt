package com.example.campusrecruitmentsystem.ui.recruiter

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusrecruitmentsystem.adapters.QuestionAdapter
import com.example.campusrecruitmentsystem.databinding.ActivityCreateTestBinding
import com.example.campusrecruitmentsystem.models.Question
import com.example.campusrecruitmentsystem.models.Test
import com.google.firebase.database.FirebaseDatabase

class CreateTestActivity : AppCompatActivity() {

    private val questionsList = mutableListOf<Question>()
    private lateinit var questionAdapter: QuestionAdapter
    private lateinit var binding: ActivityCreateTestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        questionAdapter = QuestionAdapter(questionsList)
        binding.recyclerViewQuestions.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewQuestions.adapter = questionAdapter

        binding.btnAddQuestion.setOnClickListener {
            val newQuestion = Question()
            questionsList.add(newQuestion)
            questionAdapter.notifyItemInserted(questionsList.size - 1)
            questionAdapter.notifyDataSetChanged()
        }

        binding.btnSaveTest.setOnClickListener {
            if (questionsList.isNotEmpty()) {
                saveTestToFirebase()
            } else {
                Toast.makeText(this, "Please add at least one question", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveTestToFirebase() {
        // Get other test details (title, description, time limit)
        Log.d("TAG", "saveTestToFirebase:\n${questionsList.joinToString("\n")}")

        val testsRef = FirebaseDatabase.getInstance().getReference("tests")
        val testId = testsRef.push().key // Generate a unique key for the test
        testId?.let {
            val test = Test(
                title = "Test Title",
                description = "Test Description",
                timeLimit = 30, // 30 minutes (example)
                questions = ArrayList(questionsList)
            )

            testsRef.child(it).setValue(test)
                .addOnSuccessListener {
                    // Test saved successfully
                    Toast.makeText(this, "Test created successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    // Handle failure
                    Toast.makeText(this, "Failed to create test: $e", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
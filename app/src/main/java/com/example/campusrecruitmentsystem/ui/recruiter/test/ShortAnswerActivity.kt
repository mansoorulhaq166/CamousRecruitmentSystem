package com.example.campusrecruitmentsystem.ui.recruiter.test

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusrecruitmentsystem.adapters.test.TrueFalseAdapter
import com.example.campusrecruitmentsystem.databinding.ActivityShortAnswerBinding
import com.example.campusrecruitmentsystem.ui.main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ShortAnswerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityShortAnswerBinding
    private val questionsList = mutableListOf<String>()
    private lateinit var trueFalseAdapter: TrueFalseAdapter
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShortAnswerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val testName = intent.getStringExtra("testName").toString()
        val testTime = intent.getStringExtra("testTime").toString()
        val testJob = intent.getStringExtra("testJob").toString()

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference.child("tests")

        binding.rvQuestions.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        trueFalseAdapter = TrueFalseAdapter(questionsList)
        binding.rvQuestions.adapter = trueFalseAdapter

        addQuestion()

        binding.btnAddQuestion.setOnClickListener {
            addQuestion()
        }

        binding.btnSaveTest.setOnClickListener {
            saveTestToFirebase(testName, testTime, testJob)
        }
    }

    private fun addQuestion() {
        val questionNumber = "Question ${questionsList.size + 1}"
        trueFalseAdapter.addQuestion(questionNumber)
    }

    private fun saveTestToFirebase(testName: String, testTime: String, testJob: String) {
        val questions = trueFalseAdapter.getQuestions()
        val testId = database.push().key
        val userId = auth.currentUser?.uid
        val creationTime = System.currentTimeMillis()
        val testType = "Short Answers"
        val test = mapOf(
            "userId" to userId,
            "creationTime" to creationTime,
            "jobId" to testJob,
            "testName" to testName,
            "testTime" to testTime,
            "testType" to testType,
            "questions" to trueFalseAdapter.getQuestions()
        )

        if (questions.isEmpty()) {
            Toast.makeText(this, "Please Enter Test Questions", Toast.LENGTH_SHORT).show()
            return
        }
        testId?.let {
            database.child(it).setValue(test).addOnSuccessListener {
                Toast.makeText(this, "Test Created Successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@ShortAnswerActivity, MainActivity::class.java))
                finish()
            }.addOnFailureListener { exc ->
                Toast.makeText(this, exc.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
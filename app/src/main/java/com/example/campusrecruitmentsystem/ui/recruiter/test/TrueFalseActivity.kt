package com.example.campusrecruitmentsystem.ui.recruiter.test

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusrecruitmentsystem.adapters.TrueFalseAdapter
import com.example.campusrecruitmentsystem.databinding.ActivityTrueFalseBinding
import com.example.campusrecruitmentsystem.ui.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class TrueFalseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTrueFalseBinding
    private val questionsList = mutableListOf<String>()
    private lateinit var trueFalseAdapter: TrueFalseAdapter
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrueFalseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference.child("tests").child("TrueFalseTests")

        binding.rvQuestions.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        trueFalseAdapter = TrueFalseAdapter(questionsList)
        binding.rvQuestions.adapter = trueFalseAdapter

        addQuestion()

        binding.btnAddQuestion.setOnClickListener {
            addQuestion()
        }

        binding.btnSaveTest.setOnClickListener {
            saveTestToFirebase()
        }
    }

    private fun addQuestion() {
        val questionNumber = "Question ${questionsList.size + 1}"
        trueFalseAdapter.addQuestion(questionNumber)
    }

    private fun saveTestToFirebase() {
        val testName = binding.etTestName.text.toString().trim()
        val questions = trueFalseAdapter.getQuestions()
        val testId = database.push().key
        val userId = auth.currentUser?.uid
        val test = mapOf(
            "userId" to userId,
            "testName" to testName,
            "questions" to trueFalseAdapter.getQuestions()
        )

        if (testName.isEmpty()) {
            return
        }

        if (questions.isEmpty()) {
            return
        }
        testId?.let {
            database.child(it).setValue(test).addOnSuccessListener {
                Toast.makeText(this, "Test Created Successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@TrueFalseActivity, MainActivity::class.java))
                finish()
            }.addOnFailureListener { exc ->
                Toast.makeText(this, exc.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
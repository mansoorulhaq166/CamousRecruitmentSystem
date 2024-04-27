package com.example.campusrecruitmentsystem.ui.recruiter.test

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusrecruitmentsystem.adapters.test.MultipleChoiceAdapter
import com.example.campusrecruitmentsystem.databinding.ActivityMultipleChoiceBinding
import com.example.campusrecruitmentsystem.models.MultipleChoiceQuestion
import com.example.campusrecruitmentsystem.ui.main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MultipleChoiceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMultipleChoiceBinding
    private val questionsList = mutableListOf<MultipleChoiceQuestion>()
    private lateinit var multipleChoiceAdapter: MultipleChoiceAdapter
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMultipleChoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val testName = intent.getStringExtra("testName").toString()
        val testTime = intent.getStringExtra("testTime").toString()

        auth = FirebaseAuth.getInstance()
        database =
            FirebaseDatabase.getInstance().reference.child("tests").child("MultipleChoiceTests")

        binding.rvQuestions.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        multipleChoiceAdapter = MultipleChoiceAdapter(questionsList)
        binding.rvQuestions.adapter = multipleChoiceAdapter

        addQuestion()

        binding.btnAddQuestion.setOnClickListener {
            addQuestion()
        }

        binding.btnSaveTest.setOnClickListener {
            saveTestToFirebase(testName, testTime)
        }
    }

    private fun addQuestion() {
        val questionNumber = MultipleChoiceQuestion("${questionsList.size + 1}", "", "", "", "", 1)
        multipleChoiceAdapter.addQuestion(questionNumber)
    }

    private fun saveTestToFirebase(testName: String, testTime: String) {
        val questions = multipleChoiceAdapter.getQuestions()
        for (question in questions) {
            if (question.question.isEmpty()) {
                showToast(this, "Question cannot be empty")
                return
            } else if (question.choiceA.isEmpty()) {
                showToast(this, "Choice A cannot be empty")
                return
            } else if (question.choiceB.isEmpty()) {
                showToast(this, "Choice B cannot be empty")
                return
            } else if (question.choiceC.isEmpty()) {
                showToast(this, "Choice C cannot be empty")
                return
            } else if (question.choiceD.isEmpty()) {
                showToast(this, "Choice D cannot be empty")
                return
            }
        }
        val testId = database.push().key
        val userId = auth.currentUser?.uid
        val creationTime = System.currentTimeMillis()
        val test = mapOf(
            "userId" to userId,
            "creationTime" to creationTime,
            "testName" to testName,
            "testTime" to testTime,
            "questions" to questions
        )

        testId?.let {
            database.child(it).setValue(test).addOnSuccessListener {
                Toast.makeText(this, "Test Created Successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@MultipleChoiceActivity, MainActivity::class.java))
                finish()
            }.addOnFailureListener { exc ->
                Toast.makeText(this, exc.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
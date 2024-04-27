package com.example.campusrecruitmentsystem.ui.recruiter.test

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.campusrecruitmentsystem.databinding.ActivityTestCreationBinding

class TestCreationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTestCreationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestCreationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val questionTypes = arrayOf("Multiple Choice", "True/False", "Short Answer")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, questionTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerQuestionType.adapter = adapter

        binding.btnNext.setOnClickListener {
            val selectedType = binding.spinnerQuestionType.selectedItem.toString()
            val manualTimeInput = binding.etManualTime.text.toString().trim()
            val testName = binding.etTestName.text.toString().trim()

            if (testName.isEmpty()) {
                Toast.makeText(
                    this@TestCreationActivity,
                    "Test Name cannot be empty",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (manualTimeInput.isEmpty()) {
                Toast.makeText(
                    this@TestCreationActivity,
                    "Test Time cannot be empty",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            val intent = when (selectedType) {
                "Multiple Choice" -> Intent(
                    this@TestCreationActivity,
                    MultipleChoiceActivity::class.java
                )

                "True/False" -> Intent(this@TestCreationActivity, TrueFalseActivity::class.java)
                "Short Answer" -> Intent(this@TestCreationActivity, ShortAnswerActivity::class.java)
                else -> null
            }

            intent?.apply {
                putExtra("testTime", manualTimeInput)
                putExtra("testName", testName)
                startActivity(this)
            }

        }
    }
}
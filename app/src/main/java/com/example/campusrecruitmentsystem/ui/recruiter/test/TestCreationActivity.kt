package com.example.campusrecruitmentsystem.ui.recruiter.test

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
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
            val intent = when (selectedType) {
                "Multiple Choice" -> Intent(
                    this@TestCreationActivity,
                    MultipleChoiceActivity::class.java
                )

                "True/False" -> Intent(this@TestCreationActivity, TrueFalseActivity::class.java)
                "Short Answer" -> Intent(this@TestCreationActivity, ShortAnswerActivity::class.java)
                else -> null
            }
            intent?.let {
                startActivity(it)
            }
        }
    }
}
package com.example.campusrecruitmentsystem.ui.student.test

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.campusrecruitmentsystem.databinding.ActivityStudentTestBinding

class StudentTestActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStudentTestBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentTestBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
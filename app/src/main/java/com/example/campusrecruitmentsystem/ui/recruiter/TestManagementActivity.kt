package com.example.campusrecruitmentsystem.ui.recruiter

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.campusrecruitmentsystem.databinding.ActivityTestManagementBinding
import com.example.campusrecruitmentsystem.ui.recruiter.test.DeleteTestActivity
import com.example.campusrecruitmentsystem.ui.recruiter.test.TestCreationActivity

class TestManagementActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTestManagementBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCreateTest.setOnClickListener {
            startActivity(Intent(this@TestManagementActivity, TestCreationActivity::class.java))
        }

        binding.btnDeleteTest.setOnClickListener {
            startActivity(Intent(this@TestManagementActivity, DeleteTestActivity::class.java))
        }

        binding.btnViewResults.setOnClickListener {
        }
    }
}
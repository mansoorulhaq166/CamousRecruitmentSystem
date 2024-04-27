package com.example.campusrecruitmentsystem.ui.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.campusrecruitmentsystem.R
import com.example.campusrecruitmentsystem.databinding.ActivitySignUpBinding
import com.example.campusrecruitmentsystem.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val rolesAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.roles,
            android.R.layout.simple_spinner_item
        )
        rolesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerRole.adapter = rolesAdapter

        binding.btnSignUp.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnSignUp.visibility = View.GONE

            val userName = binding.editTextUserName.text.toString().trim()
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()
            val confirmPassword = binding.editTextConfirmPassword.text.toString().trim()
            val role = binding.spinnerRole.selectedItem.toString()

            val user = User(userName, email, role)

            if (userName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_SHORT).show()
                if (userName.isEmpty()) {
                    binding.editTextUserName.requestFocus()
                } else if (email.isEmpty()) {
                    binding.editTextEmail.requestFocus()
                } else if (password.isEmpty()) {
                    binding.editTextPassword.requestFocus()
                } else {
                    binding.editTextConfirmPassword.requestFocus()
                }
                binding.progressBar.visibility = View.GONE
                binding.btnSignUp.visibility = View.VISIBLE
            } else if (password.length < 6) {
                Toast.makeText(this, "Password should be at least 6 characters", Toast.LENGTH_SHORT)
                    .show()
                binding.editTextPassword.requestFocus()
                binding.progressBar.visibility = View.GONE
                binding.btnSignUp.visibility = View.VISIBLE
            } else if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                binding.editTextConfirmPassword.requestFocus()
                binding.progressBar.visibility = View.GONE
                binding.btnSignUp.visibility = View.VISIBLE
            } else if (role == "Select a Role") {
                Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
                binding.btnSignUp.visibility = View.VISIBLE
            } else {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid
                            if (userId != null) {
                                val userReference =
                                    FirebaseDatabase.getInstance().getReference("users")
                                        .child(userId)
                                userReference.setValue(user)
                                    .addOnCompleteListener { userTask ->
                                        if (userTask.isSuccessful) {
                                            Toast.makeText(
                                                this,
                                                "Sign up successful",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            startActivity(
                                                Intent(
                                                    this@SignUpActivity,
                                                    LoginActivity::class.java
                                                )
                                            )
                                            finish()
                                        } else {
                                            Toast.makeText(
                                                this,
                                                "Could Not Create Credentials",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            binding.progressBar.visibility = View.GONE
                                            binding.btnSignUp.visibility = View.VISIBLE
                                        }
                                    }
                            }
                        } else {
                            Toast.makeText(this, "Sign up failed. Try again.", Toast.LENGTH_SHORT)
                                .show()
                            binding.progressBar.visibility = View.GONE
                            binding.btnSignUp.visibility = View.VISIBLE
                        }
                    }
            }
        }

        binding.textViewLoginLink.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
package com.example.campusrecruitmentsystem.ui.main

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.campusrecruitmentsystem.databinding.ActivityChangePasswordBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ChangePasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangePasswordBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSavePassword.setOnClickListener {
            changePassword()
        }

        binding.backImg.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun changePassword() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSavePassword.visibility = View.GONE

        val oldPassword: String = binding.textOldPassword.getText().toString().trim()
        val newPassword: String = binding.textPassword.getText().toString().trim()
        val confirmNewPassword: String = binding.textConfirmNew.getText().toString().trim()

        // Validate inputs
        if (TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(
                confirmNewPassword
            )
        ) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }
        if (newPassword != confirmNewPassword) {
            Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        val credential = EmailAuthProvider.getCredential(currentUser!!.email!!, oldPassword)

        currentUser.reauthenticate(credential)
            .addOnSuccessListener {
                currentUser.updatePassword(newPassword)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this@ChangePasswordActivity,
                            "Password changed successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this@ChangePasswordActivity,
                            "Failed to change password: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        binding.textOldPassword.requestFocus()
                        binding.progressBar.visibility = View.GONE
                        binding.btnSavePassword.visibility = View.VISIBLE
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this@ChangePasswordActivity,
                    "Failed to authenticate: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                binding.progressBar.visibility = View.GONE
                binding.btnSavePassword.visibility = View.VISIBLE
            }
    }
}
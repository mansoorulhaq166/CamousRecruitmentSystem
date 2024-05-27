package com.example.campusrecruitmentsystem.ui.main

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import com.example.campusrecruitmentsystem.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val text = "Don't have an account? Sign Up"
        val spannableString = SpannableString(text)
        spannableString.setSpan(UnderlineSpan(), 0, text.length, 0)
        spannableString.setSpan(ForegroundColorSpan(Color.BLUE), 0, text.length, 0)
        binding.textViewSignUpLink.text = spannableString

        binding.btnLogin.setOnClickListener {

            binding.progressBar.visibility = VISIBLE
            binding.btnLogin.visibility = GONE

            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_SHORT).show()
                if (email.isEmpty()) {
                    binding.editTextEmail.requestFocus()
                } else if (password.isEmpty()) {
                    binding.editTextPassword.requestFocus()
                }
                binding.progressBar.visibility = GONE
                binding.btnLogin.visibility = VISIBLE
            } else {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(
                                this,
                                "Login failed. Check your credentials.",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.progressBar.visibility = GONE
                            binding.btnLogin.visibility = VISIBLE
                        }
                    }
            }
        }

        binding.textViewSignUpLink.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
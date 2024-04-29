package com.example.campusrecruitmentsystem.ui.main

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.campusrecruitmentsystem.R
import com.example.campusrecruitmentsystem.Utils.isFieldEmpty
import com.example.campusrecruitmentsystem.databinding.ActivityProfileBinding
import com.example.campusrecruitmentsystem.ui.recruiter.JobsPostedActivity
import com.example.campusrecruitmentsystem.ui.student.JobsAppliedActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private var initialName: String? = null
    private var initialEmail: String? = null
    private var initialContact: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        fetchUserInfo()

        binding.textViewAppliedJobsLink.setOnClickListener {
            startActivity(Intent(this@ProfileActivity, JobsAppliedActivity::class.java))
        }

        binding.textViewPostedJobsLink.setOnClickListener {
            startActivity(Intent(this@ProfileActivity, JobsPostedActivity::class.java))
        }

        binding.llHome.setOnClickListener {
            startActivity(Intent(this@ProfileActivity, MainActivity::class.java))
        }
        binding.llLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        binding.backImg.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.textViewName.addTextChangedListener(nameTextWatcher)
        binding.textViewEmail.addTextChangedListener(nameTextWatcher)
        binding.textViewContact.addTextChangedListener(nameTextWatcher)

        binding.confirmSave.setOnClickListener {
            showConfirmationDialog()
        }

        binding.btnChangePassword.setOnClickListener {
            startActivity(Intent(this@ProfileActivity, ChangePasswordActivity::class.java))
        }

        updateSaveButton()
    }

    private fun showConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm Save")
        builder.setMessage("Are you sure you want to save the changes?")
        builder.setPositiveButton("Yes") { _, _ ->
            saveDataToFirebase()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun saveDataToFirebase() {
        val newName = binding.textViewName.text.toString()
        val newEmail = binding.textViewEmail.text.toString()
        val newContact = binding.textViewContact.text.toString()

        if (isFieldEmpty(this, binding.textViewName, "Name") || isFieldEmpty(
                this,
                binding.textViewEmail,
                "Email"
            ) || isFieldEmpty(this, binding.textViewContact, "Contact")
        ) {
            return
        }

        // Get the current user
        val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

        currentUser?.let { user ->
            val userId: String = user.uid
            val databaseReference: DatabaseReference =
                FirebaseDatabase.getInstance().getReference("users").child(userId)

            // Create a map to update multiple fields in Firebase
            val updates = hashMapOf<String, Any>(
                "name" to newName,
                "email" to newEmail,
                "contact" to newContact
            )

            // Update the values in Firebase
            databaseReference.updateChildren(updates)
                .addOnSuccessListener {
                    Toast.makeText(this, "Data updated successfully", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@ProfileActivity, MainActivity::class.java))
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error updating data: $e", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private val nameTextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            updateSaveButton()
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    private fun updateSaveButton() {
        val resId =
            if (isDataChanged()) R.drawable.baseline_check_bold else R.drawable.baseline_check_light
        binding.confirmSave.setImageResource(resId)
        binding.confirmSave.isClickable = isDataChanged()
    }

    private fun isDataChanged(): Boolean {
        val newName = binding.textViewName.text.toString()
        val newEmail = binding.textViewEmail.text.toString()
        val newContact = binding.textViewContact.text.toString()

        return newName != initialName || newEmail != initialEmail || newContact != initialContact
    }

    private fun fetchUserInfo() {
        val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

        currentUser?.let { user ->
            val userId: String = user.uid
            val databaseReference: DatabaseReference =
                FirebaseDatabase.getInstance().getReference("users").child(userId)

            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        initialName = snapshot.child("name").getValue(String::class.java)
                        initialEmail = snapshot.child("email").getValue(String::class.java)
                        val role: String? = snapshot.child("role").getValue(String::class.java)
                        initialContact = snapshot.child("contact").getValue(String::class.java)

                        initialName?.let {
                            binding.textViewName.text =
                                Editable.Factory.getInstance().newEditable(it)
                        }
                        initialEmail?.let {
                            binding.textViewEmail.text =
                                Editable.Factory.getInstance().newEditable(it)
                        }
                        initialContact?.let {
                            binding.textViewContact.text =
                                Editable.Factory.getInstance().newEditable(it)
                        }

                        role?.let {
                            binding.textViewRole.text = it
                            if (it.equals("student", ignoreCase = true)) {
                                binding.textViewPostedJobsLink.visibility = View.GONE
                                countApplications(userId)
                            } else if (role == "Recruiter") {
                                binding.textViewAppliedJobsLink.visibility = View.GONE
                                countPostedJobs(userId)
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }

    private fun countApplications(studentId: String) {
        val applicationsReference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("applications")
        val query: Query = applicationsReference.orderByChild("studentId").equalTo(studentId)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val applicationCount: Long = snapshot.childrenCount
                binding.textViewJobCount.text = "Applied for $applicationCount Jobs"
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun countPostedJobs(recruiterId: String) {
        val applicationsReference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("jobs")
        val query: Query = applicationsReference.orderByChild("recruiter_id").equalTo(recruiterId)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val applicationCount: Long = snapshot.childrenCount
                binding.textViewJobCount.text = "$applicationCount Jobs Posted"
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Logout")
        builder.setMessage("Are you sure you want to logout?")

        builder.setPositiveButton("Yes") { dialog, _ ->
            logoutUser()
            dialog.dismiss()
        }

        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun logoutUser() {
        auth.signOut()

        val intent = Intent(this, WelcomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}
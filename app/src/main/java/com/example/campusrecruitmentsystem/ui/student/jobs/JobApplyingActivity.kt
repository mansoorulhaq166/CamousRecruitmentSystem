package com.example.campusrecruitmentsystem.ui.student.jobs

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Editable
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.campusrecruitmentsystem.R
import com.example.campusrecruitmentsystem.databinding.ActivityJobApplyingBinding
import com.example.campusrecruitmentsystem.models.main.ApplicationDetails
import com.example.campusrecruitmentsystem.models.main.Job
import com.example.campusrecruitmentsystem.ui.main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.util.Date

class JobApplyingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityJobApplyingBinding
    private var job: Job? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var userName: String
    private lateinit var userEmail: String
    private lateinit var userPhone: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobApplyingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        @Suppress("DEPRECATION")
        job = intent.getParcelableExtra("job")
        auth = FirebaseAuth.getInstance()

        if (job != null) {
            binding.applyingJobTitle.text = job!!.title
            binding.applyingJobCompany.text = job!!.company

            val userId = auth.currentUser?.uid
            getStudentInformation(userId)
        }

        binding.buttonCancel.setOnClickListener {
            showCancelApplyingDialog()
        }

        binding.selectResumeButton.setOnClickListener {
            chooseResumeFile()
        }
    }

    private fun getStudentInformation(studentId: String?) {
        if (studentId == null) {
            Toast.makeText(this, "Student ID is null", Toast.LENGTH_SHORT).show()
            return
        }

        // Reference to the user node in Firebase
        val usersRef = FirebaseDatabase.getInstance().getReference("users").child(studentId)

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Retrieve the user information
                    val name = snapshot.child("name").getValue(String::class.java)
                    val email = snapshot.child("email").getValue(String::class.java)
                    val contactNumber = snapshot.child("contact").getValue(String::class.java)
                    val location = snapshot.child("location").getValue(String::class.java)

                    userName = name.toString()
                    userName = email.toString()
                    userName = contactNumber.toString()

                    // Populate the fields in the layout
                    binding.editTextName.text = Editable.Factory.getInstance().newEditable(name)
                    binding.editTextEmail.text = Editable.Factory.getInstance().newEditable(email)
                    binding.editTextPhoneNumber.text =
                        Editable.Factory.getInstance().newEditable(contactNumber)
                    if (location != null) {
                        binding.editTextCity.text =
                            Editable.Factory.getInstance().newEditable(location)
                    }
                } else {
                    Toast.makeText(
                        this@JobApplyingActivity,
                        "User data not found",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@JobApplyingActivity,
                    "Failed to retrieve user data: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun showCancelApplyingDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Cancel")
        builder.setMessage("Are you sure you want to Cancel?")

        builder.setPositiveButton("Yes") { dialog, _ ->
            val intent = Intent(this@JobApplyingActivity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            dialog.dismiss()
        }

        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    @Suppress("DEPRECATION")
    private fun chooseResumeFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        startActivityForResult(intent, 123)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 123 && resultCode == Activity.RESULT_OK) {
            val selectedFileUri: Uri? = data?.data
            if (selectedFileUri != null) {
                val fileName = getFileName(selectedFileUri)
                binding.selectResumeButton.text = fileName
            }
            binding.submitApplicationButton.setOnClickListener {
                if (selectedFileUri == null) {
                    Toast.makeText(this, "Please select a resume", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val name = binding.editTextName.text.toString().trim()
                val email = binding.editTextEmail.text.toString().trim()
                val city = binding.editTextCity.text.toString().trim()
                val phoneNo = binding.editTextPhoneNumber.text.toString().trim()

                if (name.isEmpty()) {
                    binding.editTextName.error = "Name is required"
                    binding.editTextName.requestFocus()
                    return@setOnClickListener
                }

                if (email.isEmpty()) {
                    binding.editTextEmail.error = "Email is required"
                    binding.editTextEmail.requestFocus()
                    return@setOnClickListener
                }

                if (city.isEmpty()) {
                    binding.editTextCity.error = "City is required"
                    binding.editTextCity.requestFocus()
                    return@setOnClickListener
                }

                if (phoneNo.isEmpty()) {
                    binding.editTextPhoneNumber.error = "Phone number is required"
                    binding.editTextPhoneNumber.requestFocus()
                    return@setOnClickListener
                }

                uploadResumeToFirebaseStorage(selectedFileUri)
            }
        }
    }

    @SuppressLint("Range")
    private fun getFileName(uri: Uri): String? {
        var fileName: String? = null
        if (uri.scheme.equals("content")) {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor.use {
                if (cursor != null && cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (fileName == null) {
            fileName = uri.path
            val cut = fileName?.lastIndexOf('/')
            if (cut != -1) {
                fileName = cut?.let { fileName?.substring(it + 1) }
            }
        }
        return fileName
    }

    private fun uploadResumeToFirebaseStorage(fileUri: Uri?) {
        binding.submitApplicationButton.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
        binding.progressText.visibility = View.VISIBLE
        if (fileUri != null) {
            val storageRef = FirebaseStorage.getInstance().reference

            val resumeRef =
                storageRef.child("resumes/${FirebaseAuth.getInstance().currentUser?.uid}/${System.currentTimeMillis()}_${fileUri.lastPathSegment}")
            val uploadTask: UploadTask = resumeRef.putFile(fileUri)

            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress =
                    (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()

                // Update the progress bar
                binding.progressText.text = getString(R.string.upload_progress, progress)
                binding.progressBar.progress = progress
            }.addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()
                    // Storing the download URL and other details in the Firebase Database
                    storeApplicationDetails(downloadUrl)
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "Upload failed: $exception", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun storeApplicationDetails(downloadUrl: String) {
        val jobId = job?.id
        val studentId = FirebaseAuth.getInstance().currentUser?.uid

        getRecruiterId(jobId) { recruiterId ->
            if (recruiterId != null) {
                val databaseReference = FirebaseDatabase.getInstance().getReference("applications")
                val applicationId = databaseReference.push().key

                val name = binding.editTextName.text.toString().trim()
                val email = binding.editTextEmail.text.toString().trim()
                val city = binding.editTextCity.text.toString().trim()
                val phoneNo = binding.editTextPhoneNumber.text.toString().trim()
                val experience = binding.editTextRelevantExperience.text.toString().trim()

                if (applicationId != null) {
                    val application = ApplicationDetails(
                        applicationId = applicationId,
                        jobId = jobId,
                        studentId = studentId,
                        studentName = name,
                        studentEmail = email,
                        studentPhone = phoneNo,
                        recruiterId = recruiterId,
                        resumeUrl = downloadUrl,
                        applicationDate = getCurrentDate(),
                        status = "Pending",
                        studentLocation = city,
                        relevantExperience = experience,
                        notes = "",
                        comments = ""
                    )

                    databaseReference.child(applicationId).setValue(application)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    this,
                                    "Application submitted successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                                startActivity(
                                    Intent(
                                        this@JobApplyingActivity,
                                        MainActivity::class.java
                                    )
                                )
                                finish()
                            } else {
                                binding.submitApplicationButton.visibility = View.VISIBLE
                                binding.progressBar.visibility = View.GONE
                                Toast.makeText(
                                    this,
                                    "Failed to submit application",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        }
                }
            }
        }
    }

    private fun getRecruiterId(jobId: String?, callback: (String?) -> Unit) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("jobs").child(jobId!!)
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val recruiterId = snapshot.child("recruiter_id").getValue(String::class.java)
                callback(recruiterId)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                callback(null)
            }
        })
    }

    @SuppressLint("SimpleDateFormat")
    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDate = Date()
        return dateFormat.format(currentDate)
    }
}
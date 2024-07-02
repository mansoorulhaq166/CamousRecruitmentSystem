package com.example.campusrecruitmentsystem.ui.recruiter.application

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.campusrecruitmentsystem.R
import com.example.campusrecruitmentsystem.databinding.ActivityApplicationDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFParagraph
import org.apache.poi.xwpf.usermodel.XWPFRun
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ApplicationDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityApplicationDetailsBinding
    private lateinit var auth: FirebaseAuth
    private val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
    private val requestCode = 1234
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApplicationDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        val applicationId = intent.getStringExtra("application")

        if (applicationId != null) {
            checkScheduledInterview(applicationId)
            getApplicationInfoFromFirebase(applicationId)
            updateApplication(applicationId)
        }

        binding.backAppDetails.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        if (ContextCompat.checkSelfPermission(
                this,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        }

        binding.btnAccept.setOnClickListener {
            showConfirmationDialog(
                applicationId.toString(),
                "accept"
            )
        }
        binding.btnReject.setOnClickListener {
            showConfirmationDialog(
                applicationId.toString(),
                "reject"
            )
        }

        binding.generateOfferLetter.setOnClickListener {
            generateOfferLetter(applicationId.toString())
        }

        binding.sendOfferLetter.setOnClickListener {
            sendOfferLetter(applicationId.toString())
        }

        binding.downloadOfferLetter.setOnClickListener {
            downloadOfferLetter(applicationId.toString())
        }

        binding.generateOfferLetterAgain.setOnClickListener {
            binding.llOfferLetter.visibility = View.GONE
            generateOfferLetter(applicationId.toString())
        }

        val text = "Generate Offer Letter Again"
        val spannableString = SpannableString(text)
        spannableString.setSpan(UnderlineSpan(), 0, text.length, 0)
        spannableString.setSpan(ForegroundColorSpan(Color.BLUE), 0, text.length, 0)
        binding.generateOfferLetterAgain.text = spannableString
    }

    private fun generateOfferLetter(applicationId: String) {
        binding.offerLetterProgressbar.visibility = View.VISIBLE
        binding.generateOfferLetter.visibility = View.GONE
        binding.statusTextRejected.visibility = View.GONE

        // Inflate the custom dialog layout

        // Inflate the custom dialog layout
        val inflater = LayoutInflater.from(this)
        val dialogView: View = inflater.inflate(R.layout.dialog_offer_letter_details, null)

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
            .setTitle("Enter Offer Letter Details")
            .setPositiveButton("Generate", null)
            .setNegativeButton("Cancel") { dialog: DialogInterface, which: Int ->
                binding.offerLetterProgressbar.visibility = View.GONE
                binding.generateOfferLetter.visibility = View.VISIBLE
                binding.statusTextRejected.visibility = View.VISIBLE
                dialog.dismiss()
            }

        val dialog = builder.create()
        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val departmentNameEditText = dialogView.findViewById<EditText>(R.id.department_name)
            val companyLocationEditText = dialogView.findViewById<EditText>(R.id.company_location)
            val managerNameEditText = dialogView.findViewById<EditText>(R.id.manager_name)
            val bonusDetailsEditText = dialogView.findViewById<EditText>(R.id.bonus_details)
            val benefitsEditText = dialogView.findViewById<EditText>(R.id.benefits)
            val paidTimeOffEditText = dialogView.findViewById<EditText>(R.id.paid_time_off)
            val startDateEditText = dialogView.findViewById<EditText>(R.id.start_date)
            val acceptanceDeadlineEditText =
                dialogView.findViewById<EditText>(R.id.acceptance_deadline)
            val contactPersonNameEditText =
                dialogView.findViewById<EditText>(R.id.contact_person_name)
            val contactPersonEmailEditText =
                dialogView.findViewById<EditText>(R.id.contact_person_email)

            val departmentName = departmentNameEditText.getText().toString().trim { it <= ' ' }
            val companyLocation = companyLocationEditText.getText().toString().trim { it <= ' ' }
            val managerName = managerNameEditText.getText().toString().trim { it <= ' ' }
            val bonusDetails = bonusDetailsEditText.getText().toString().trim { it <= ' ' }
            val benefits = benefitsEditText.getText().toString().trim { it <= ' ' }
            val paidTimeOff = paidTimeOffEditText.getText().toString().trim { it <= ' ' }
            val startDate = startDateEditText.getText().toString().trim { it <= ' ' }
            val acceptanceDeadline =
                acceptanceDeadlineEditText.getText().toString().trim { it <= ' ' }
            val contactPersonName =
                contactPersonNameEditText.getText().toString().trim { it <= ' ' }
            val contactPersonEmail =
                contactPersonEmailEditText.getText().toString().trim { it <= ' ' }

            var hasError = false

            if (departmentName.isEmpty()) {
                departmentNameEditText.error = "Department Name is required"
                hasError = true
            }

            if (companyLocation.isEmpty()) {
                companyLocationEditText.error = "Company Location is required"
                hasError = true
            }

            if (startDate.isEmpty()) {
                startDateEditText.error = "Start Date is required"
                hasError = true
            }

            if (acceptanceDeadline.isEmpty()) {
                acceptanceDeadlineEditText.error = "Acceptance Deadline is required"
                hasError = true
            }

            if (contactPersonName.isEmpty()) {
                contactPersonNameEditText.error = "Contact Person's Name is required"
                hasError = true
            }

            if (contactPersonEmail.isEmpty()) {
                contactPersonEmailEditText.error = "Contact Person's Email/Phone Number is required"
                hasError = true
            }

            if (hasError) {
                return@setOnClickListener
            }

            val applicationRef =
                FirebaseDatabase.getInstance().getReference("applications").child(applicationId)
            applicationRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(applicationSnapshot: DataSnapshot) {
                    // Retrieve student ID and job ID from the application
                    val studentId =
                        applicationSnapshot.child("studentId").getValue(String::class.java)
                            .toString()
                    val jobId =
                        applicationSnapshot.child("jobId").getValue(String::class.java).toString()

                    // Retrieve student details
                    val studentRef =
                        FirebaseDatabase.getInstance().getReference("users").child(studentId)
                    studentRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(studentSnapshot: DataSnapshot) {
                            val studentName =
                                studentSnapshot.child("name").getValue(String::class.java)
                                    .toString()

                            // Retrieve job details
                            val jobRef =
                                FirebaseDatabase.getInstance().getReference("jobs").child(jobId)
                            jobRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(jobSnapshot: DataSnapshot) {
                                    val jobRole =
                                        jobSnapshot.child("title").getValue(String::class.java)
                                    val salary =
                                        jobSnapshot.child("salary").getValue(String::class.java)

                                    val offerLetterContent = StringBuilder(
                                        """Dear $studentName,

We are delighted to extend this offer of employment for the position of $jobRole at our esteemed company. We are confident that your skills and experience will be a valuable asset to our team.

Position Details:
----------------------------------------
Job Title: $jobRole
"""
                                    )

                                    offerLetterContent.append("Department: ").append(departmentName)
                                        .append("\n")
                                    offerLetterContent.append("Location: ").append(companyLocation)
                                        .append("\n")
                                    if (!managerName.isEmpty()) {
                                        offerLetterContent.append("Reporting To: ")
                                            .append(managerName).append("\n")
                                    }

                                    offerLetterContent.append(
                                        """

                                                    Compensation and Benefits:
                                                    ----------------------------------------
                                                    Salary: 
                                                    """.trimIndent()
                                    ).append(salary).append(" per annum\n")

                                    if (!bonusDetails.isEmpty()) {
                                        offerLetterContent.append("Bonus: ").append(bonusDetails)
                                            .append("\n")
                                    }
                                    if (!benefits.isEmpty()) {
                                        offerLetterContent.append("Benefits: ").append(benefits)
                                            .append("\n")
                                    }
                                    if (!paidTimeOff.isEmpty()) {
                                        offerLetterContent.append("Paid Time Off (in days): ")
                                            .append(paidTimeOff).append("\n")
                                    }

                                    offerLetterContent.append(
                                        """

                                                    Start Date:
                                                    ----------------------------------------
                                                    Your start date will be 
                                                    """.trimIndent()
                                    ).append(startDate)
                                        .append(". Please confirm your acceptance of this offer by signing and returning the attached offer letter by ")
                                        .append(acceptanceDeadline).append(
                                            """
                                                    .

                                                    ----------------------------------------
                                                    We believe that you will find working with our company to be a challenging and rewarding experience. We look forward to having you on our team and contributing to our mutual success.

                                                    

                                                    """.trimIndent()
                                        )

                                    offerLetterContent.append("If you have any questions or need additional information, please feel free to contact ")
                                    offerLetterContent.append(contactPersonName)
                                    offerLetterContent.append(" at ").append(contactPersonEmail)
                                    offerLetterContent.append(".\n\n")
                                    offerLetterContent.append("Thank You.\n\n")

                                    dialog.dismiss()
                                    createOfferLetterFile(
                                        studentName,
                                        offerLetterContent.toString(),
                                        applicationSnapshot
                                    )
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e(
                                        "Firebase",
                                        "Error retrieving job details: ${error.message}"
                                    )
                                    binding.offerLetterProgressbar.visibility = View.GONE
                                    binding.generateOfferLetter.visibility = View.VISIBLE
                                    binding.statusTextRejected.visibility = View.VISIBLE
                                }
                            })
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("Firebase", "Error retrieving student details: ${error.message}")
                            binding.offerLetterProgressbar.visibility = View.GONE
                            binding.generateOfferLetter.visibility = View.VISIBLE
                            binding.statusTextRejected.visibility = View.VISIBLE
                        }
                    })
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error retrieving application details: ${error.message}")
                    binding.offerLetterProgressbar.visibility = View.GONE
                    binding.generateOfferLetter.visibility = View.VISIBLE
                    binding.statusTextRejected.visibility = View.VISIBLE
                }
            })
        }
    }

    private fun createOfferLetterFile(
        name: String,
        content: String,
        applicationSnapshot: DataSnapshot
    ) {
        val fileName = "$name - offer letter.docx"
        val file = File(getExternalFilesDir(null), fileName)

        // Create a new document
        val doc = XWPFDocument()

        // Split the content into lines
        val lines = content.split("\n")

        // Create a paragraph for each line
        lines.forEach { line ->
            val paragraph: XWPFParagraph = doc.createParagraph()
            val run: XWPFRun = paragraph.createRun()

            // Set the content to the run
            run.setText(line.trim())
        }

        val outputStream = FileOutputStream(file)
        doc.write(outputStream)
        outputStream.close()

        // Upload file to Firebase Storage
        val storageRef = FirebaseStorage.getInstance().reference
        val fileRef = storageRef.child("offer_letters/${UUID.randomUUID()}.docx")
        val fileInputStream = FileInputStream(file)
        fileRef.putStream(fileInputStream)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()
                    applicationSnapshot.ref.child("offerLetterUrl").setValue(downloadUrl)
                    binding.offerLetterProgressbar.visibility = View.GONE
                    binding.llOfferLetter.visibility = View.VISIBLE
                    showDownloadOrSendDialog(name, applicationSnapshot)
                }.addOnFailureListener { exception ->
                    Log.e("Firebase", "Failed to get download URL: $exception")
                    binding.offerLetterProgressbar.visibility = View.GONE
                    binding.generateOfferLetter.visibility = View.VISIBLE
                    binding.statusTextRejected.visibility = View.VISIBLE
                }
            }.addOnFailureListener { exception ->
                Log.e("Firebase", "Failed to upload file: $exception")
                binding.offerLetterProgressbar.visibility = View.GONE
                binding.generateOfferLetter.visibility = View.VISIBLE
                binding.statusTextRejected.visibility = View.VISIBLE
            }
    }

    private fun showDownloadOrSendDialog(name: String, applicationSnapshot: DataSnapshot) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Offer Letter Options").setCancelable(false)
        dialogBuilder.setMessage("Do you want to download or send the offer letter?")
        dialogBuilder.setPositiveButton("Download") { _, _ ->
            val offerLetterUrl =
                applicationSnapshot.child("offerLetterUrl").getValue(String::class.java)
            if (offerLetterUrl != null) {
                val applicationId = applicationSnapshot.key
                if (applicationId != null) {
                    val applicationRef =
                        FirebaseDatabase.getInstance().getReference("applications")
                            .child(applicationId)
                    applicationRef.child("offerLetterSent").setValue(false)
                }
                downloadOfferLetter(offerLetterUrl, name)
            } else {
                Toast.makeText(this, "Offer letter URL not found", Toast.LENGTH_SHORT).show()
            }
        }
        dialogBuilder.setNegativeButton("Send") { _, _ ->
            val applicationId = applicationSnapshot.key
            sendOfferLetter(applicationId.toString())
        }
        dialogBuilder.setNeutralButton("Cancel") { dialog, _ ->
            val applicationId = applicationSnapshot.key
            if (applicationId != null) {
                val applicationRef =
                    FirebaseDatabase.getInstance().getReference("applications").child(applicationId)
                applicationRef.child("offerLetterSent").setValue(false)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Offer Letter Generated",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Error in Generating Offer Letter ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            dialog.dismiss()
        }
        dialogBuilder.create().show()
    }

    private fun sendOfferLetter(applicationId: String) {
        val applicationRef =
            FirebaseDatabase.getInstance().getReference("applications").child(applicationId)
        applicationRef.child("offerLetterSent").setValue(true)
            .addOnSuccessListener {
                binding.statusTextRejected.text = getString(R.string.offer_letter_sent)
                binding.statusTextRejected.visibility = View.VISIBLE
                binding.llOfferLetter.visibility = View.GONE
                Toast.makeText(this, "Offer Letter Sent to Applicant", Toast.LENGTH_SHORT)
                    .show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Failed to set offer letter sent: $e",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun downloadResume(
        resumeUrl: String,
        studentName: String
    ) {
        binding.downloadProgressbar.visibility = View.VISIBLE
        binding.textViewAdditionalDocs.visibility = View.GONE

        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(resumeUrl)

        val fileName = "$studentName - resume.pdf"
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "Recruitment Resume/" +
                    fileName
        )

        try {
            storageRef.getFile(file)
                .addOnSuccessListener {
                    Toast.makeText(
                        this@ApplicationDetailsActivity,
                        "File downloaded successfully",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    binding.downloadProgressbar.visibility = View.GONE
                    binding.textViewAdditionalDocs.visibility = View.VISIBLE
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        this@ApplicationDetailsActivity,
                        "Failed to download resume",
                        Toast.LENGTH_SHORT
                    ).show()
                    exception.printStackTrace()
                    binding.downloadProgressbar.visibility = View.GONE
                    binding.textViewAdditionalDocs.visibility = View.VISIBLE
                }
                .addOnProgressListener { taskSnapshot ->
                    val progress =
                        (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()

                    binding.downloadProgressbar.progress = progress
                }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                this@ApplicationDetailsActivity,
                "Failed to download resume",
                Toast.LENGTH_SHORT
            ).show()
            binding.downloadProgressbar.visibility = View.GONE
            binding.textViewAdditionalDocs.visibility = View.VISIBLE
        }
    }

    private fun downloadOfferLetter(offerLetterUrl: String, name: String) {
        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(offerLetterUrl)

        val fileName = "$name - Offer Letter.docx"
        val fileDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "Recruitment Offer Letters"
        )

        if (!fileDir.exists()) {
            fileDir.mkdirs()
        }
        val file = File(fileDir, fileName)

        try {
            storageRef.getFile(file)
                .addOnSuccessListener {
                    Toast.makeText(
                        this@ApplicationDetailsActivity,
                        "Offer Letter Downloaded Successfully",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        this@ApplicationDetailsActivity,
                        "Failed to Download Offer Letter",
                        Toast.LENGTH_SHORT
                    ).show()
                    exception.printStackTrace()
                }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                this@ApplicationDetailsActivity,
                "Failed to download Offer Letter",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun downloadOfferLetter(applicationId: String) {
        val applicationRef =
            FirebaseDatabase.getInstance().getReference("applications").child(applicationId)
        applicationRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val offerLetterUrl = snapshot.child("offerLetterUrl").value.toString()
                    val studentId = snapshot.child("studentId").value.toString()
                    getStudentName(studentId, offerLetterUrl)
                } else {
                    // Handle case where application data doesn't exist
                    Toast.makeText(
                        this@ApplicationDetailsActivity,
                        "Application data doesn't exist",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
                Toast.makeText(
                    this@ApplicationDetailsActivity,
                    "Database error: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun getStudentName(studentId: String, offerLetterUrl: String) {
        val studentRef = FirebaseDatabase.getInstance().getReference("users").child(studentId)
        studentRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val studentName = snapshot.child("name").value.toString()
                    if (offerLetterUrl.isNotEmpty() && offerLetterUrl != "null") {
                        downloadOfferLetter(offerLetterUrl, studentName)
                    } else {
                        Toast.makeText(
                            this@ApplicationDetailsActivity,
                            "Offer letter URL is empty or null",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@ApplicationDetailsActivity,
                        "Student data doesn't exist",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
                Toast.makeText(
                    this@ApplicationDetailsActivity,
                    "Database error: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun updateApplication(applicationId: String?) {
        binding.edTextComments.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                checkFirebaseInterviewNode(
                    binding.edTextComments.text.toString(),
                    "comments",
                    applicationId
                )
            }
        })

        binding.edTextInternalNotes.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                checkFirebaseInterviewNode(
                    binding.edTextInternalNotes.text.toString(),
                    "notes",
                    applicationId
                )
            }
        })


        binding.imgCheckApplication.setOnClickListener {
            val notes = binding.edTextInternalNotes.text.toString().trim()
            val comments = binding.edTextComments.text.toString().trim()
            updateNewDetailsToApplication(notes, comments, applicationId)
        }
    }

    private fun getApplicationInfoFromFirebase(applicationId: String) {
        val applicationRef =
            FirebaseDatabase.getInstance().getReference("applications").child(applicationId)
        applicationRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val jobId = snapshot.child("jobId").value.toString()
                    val studentId = snapshot.child("studentId").value.toString()
                    val applicationDate = snapshot.child("applicationDate").value.toString()
                    val resumeUrl = snapshot.child("resumeUrl").value.toString()
                    val comments = snapshot.child("comments").value.toString()
                    val internalNotes = snapshot.child("notes").value.toString()
                    val status = snapshot.child("status").value.toString()
                    val offerLetterUrl = snapshot.child("offerLetterUrl").value.toString()
                    val offerLetterSent = snapshot.child("offerLetterSent").value

                    layoutByStatus(status)
                    if (status == "Accepted") {
                        if (offerLetterUrl.isNotEmpty() && offerLetterUrl != "null") {
                            checkOfferLetterStatus(offerLetterSent)
                        }
                    }
                    binding.textViewApplicationDate.text = "Application Date: $applicationDate"
                    binding.edTextComments.text =
                        Editable.Factory.getInstance().newEditable(comments)
                    binding.edTextInternalNotes.text =
                        Editable.Factory.getInstance().newEditable(internalNotes)
                    binding.textViewAppStatus.text = "Status: $status"

                    val studentRef =
                        FirebaseDatabase.getInstance().getReference("users").child(studentId)
                    studentRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(userSnapshot: DataSnapshot) {
                            if (userSnapshot.exists()) {
                                val studentName = userSnapshot.child("name").value.toString()
                                val contact = userSnapshot.child("contact").value.toString()
                                val email = userSnapshot.child("email").value.toString()

                                binding.textViewAdditionalDocs.setOnClickListener {
                                    downloadResume(resumeUrl, studentName)
                                }

                                binding.scheduleInterview.setOnClickListener {
                                    if (binding.scheduleInterview.text == "Schedule Interview Again") {
                                        val interviewsRef = FirebaseDatabase.getInstance()
                                            .getReference("interviews")
                                        val query = interviewsRef.orderByChild("applicationId")
                                            .equalTo(applicationId)
                                        query.addListenerForSingleValueEvent(object :
                                            ValueEventListener {
                                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                for (interviewSnapshot in dataSnapshot.children) {
                                                    interviewSnapshot.ref.removeValue()
                                                }
                                                val interviewIntent = Intent(
                                                    this@ApplicationDetailsActivity,
                                                    ScheduleInterviewActivity::class.java
                                                )
                                                interviewIntent.putExtra("studentId", studentId)
                                                interviewIntent.putExtra("jobId", jobId)
                                                interviewIntent.putExtra(
                                                    "applicationId",
                                                    applicationId
                                                )
                                                startActivity(interviewIntent)
                                                finish()
                                            }

                                            override fun onCancelled(databaseError: DatabaseError) {
                                                Log.e(
                                                    "Firebase",
                                                    "Error deleting previous interview: ${databaseError.message}"
                                                )
                                            }
                                        })
                                    } else {
                                        val interviewIntent = Intent(
                                            this@ApplicationDetailsActivity,
                                            ScheduleInterviewActivity::class.java
                                        )
                                        interviewIntent.putExtra("studentId", studentId)
                                        interviewIntent.putExtra("jobId", jobId)
                                        interviewIntent.putExtra("applicationId", applicationId)
                                        startActivity(interviewIntent)
                                        finish()
                                    }
                                }
                                if (contact != "") {
                                    binding.textViewContactNumber.visibility = View.VISIBLE
                                }

                                binding.textViewName.text = "Name: $studentName"
                                binding.textViewContactEmail.text = "Contact Email: $email"
                                binding.textViewContactNumber.text = "Contact Number: $contact"
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Handle error
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun checkOfferLetterStatus(
        offerLetterSent: Any?
    ) {
        binding.generateOfferLetter.visibility = View.GONE
        binding.statusTextRejected.visibility = View.GONE
        binding.llOfferLetter.visibility = View.VISIBLE

        if (offerLetterSent == true) {
            binding.statusTextRejected.text = "Offer Letter Sent to Applicant"
            binding.statusTextRejected.visibility = View.VISIBLE
            binding.llOfferLetter.visibility = View.GONE
            binding.scheduleInterview.visibility = View.GONE
            binding.interviewDate.visibility = View.GONE
        }
    }

    private fun checkScheduledInterview(applicationId: String?) {
        val interviewsRef = FirebaseDatabase.getInstance().getReference("interviews")

        interviewsRef.orderByChild("applicationId").equalTo(applicationId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (interviewSnapshot in dataSnapshot.children) {
                            val interviewDateTime = interviewSnapshot.child("interviewDateTime")
                                .getValue(String::class.java)
                            if (!interviewDateTime.isNullOrEmpty()) {
                                val currentTime = System.currentTimeMillis()
                                binding.interviewDate.text = "Interview Date: $interviewDateTime"
                                binding.interviewDate.visibility = View.VISIBLE

                                val dateFormat =
                                    SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault())
                                val interviewDate: Date? = dateFormat.parse(interviewDateTime)
                                val interviewTimeInMillis = interviewDate?.time ?: 0

                                if (currentTime > interviewTimeInMillis) {
                                    interviewConductStatus(interviewSnapshot)
                                    binding.radioGroupInterviewConducted.setOnCheckedChangeListener { _, checkedId ->
                                        if (checkedId == R.id.radioButtonNo) {
                                            interviewSnapshot.ref.child("interviewConducted")
                                                .setValue(false)
                                                .addOnSuccessListener {
                                                    binding.scheduleInterview.text =
                                                        "Schedule Interview Again"
                                                    binding.scheduleInterview.isEnabled = true
                                                    binding.radioGroupInterviewConducted.visibility =
                                                        View.GONE
                                                }
                                                .addOnFailureListener { e ->
                                                    Log.e(
                                                        "Firebase",
                                                        "Failed to update interview conducted status: $e"
                                                    )
                                                }

                                        } else if (checkedId == R.id.radioButtonYes) {
                                            interviewSnapshot.ref.child("interviewConducted")
                                                .setValue(true)
                                                .addOnSuccessListener {
                                                    showFeedbackDialog(interviewSnapshot)
                                                }
                                                .addOnFailureListener { e ->
                                                    Log.e(
                                                        "Firebase",
                                                        "Failed to update interview conducted status: $e"
                                                    )
                                                }
                                        }
                                    }
                                } else {
                                    binding.radioGroupInterviewConducted.visibility = View.GONE
                                    binding.scheduleInterview.text = "Interview Scheduled"
                                    binding.scheduleInterview.isEnabled = false
                                }
                            } else {
                                binding.interviewDate.visibility = View.GONE
                                binding.radioGroupInterviewConducted.visibility = View.GONE
                            }
                        }
                    } else {
                        binding.scheduleInterview.text = "Schedule Interview"
                        binding.scheduleInterview.isEnabled = true
                        binding.interviewDate.visibility = View.GONE
                        binding.radioGroupInterviewConducted.visibility = View.GONE
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("TAG", "Error retrieving scheduled interviews: ${databaseError.message}")
                }
            })
    }

    private fun checkFirebaseInterviewNode(
        text: String,
        fieldName: String,
        applicationId: String?
    ) {
        val applicationRefs = FirebaseDatabase.getInstance().getReference("applications").child(
            applicationId.toString()
        )

        applicationRefs.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val firebaseValue = snapshot.child(fieldName).getValue(String::class.java)
                    if (text != firebaseValue) {
                        binding.imgCheckApplication.setImageResource(R.drawable.baseline_check_bold)
                    } else {
                        binding.imgCheckApplication.setImageResource(R.drawable.baseline_check_light)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error fetching data: ${error.message}")
            }
        })
    }

    private fun updateNewDetailsToApplication(
        notes: String,
        comments: String,
        applicationId: String?
    ) {
        val applicationRefs = FirebaseDatabase.getInstance().getReference("applications").child(
            applicationId.toString()
        )

        applicationRefs.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val currentComments = snapshot.child("comments").getValue(String::class.java)

                    applicationRefs.child("notes").setValue(notes)
                    applicationRefs.child("comments").setValue(comments)
                        .addOnSuccessListener {
                            if (currentComments != comments) {
                                applicationRefs.child("status").setValue("Reviewed")
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            this@ApplicationDetailsActivity,
                                            "Status updated to reviewed",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        finish()
                                    }
                                    .addOnFailureListener { error ->
                                        Log.e(
                                            "Firebase",
                                            "Failed to update status: ${error.message}"
                                        )
                                    }
                            } else {
                                Toast.makeText(
                                    this@ApplicationDetailsActivity,
                                    "Details Updated Successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }
                        }
                        .addOnFailureListener { error ->
                            Log.e("Firebase", "Failed to update new details: ${error.message}")
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error fetching data: ${error.message}")
            }
        })
    }

    private fun acceptApplication(applicationId: String?) {
        val applicationRefs = FirebaseDatabase.getInstance().getReference("applications").child(
            applicationId.toString()
        )

        applicationRefs.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    applicationRefs.child("status").setValue("Accepted")
                        .addOnSuccessListener {
                            Toast.makeText(
                                this@ApplicationDetailsActivity,
                                "Application Accepted",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                        .addOnFailureListener { error ->
                            Log.e("Firebase", "Failed to update status: ${error.message}")
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error fetching data: ${error.message}")
            }
        })
    }

    private fun rejectApplication(applicationId: String?) {
        val applicationRefs = FirebaseDatabase.getInstance().getReference("applications").child(
            applicationId.toString()
        )

        applicationRefs.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    applicationRefs.child("status").setValue("Rejected")
                        .addOnSuccessListener {
                            Toast.makeText(
                                this@ApplicationDetailsActivity,
                                "Application Rejected",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                        .addOnFailureListener { error ->
                            Log.e("Firebase", "Failed to update status: ${error.message}")
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error fetching data: ${error.message}")
            }
        })
    }

    private fun showConfirmationDialog(applicationId: String, action: String) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.apply {
            setTitle("Confirmation")
            setMessage("Are you sure you want to $action this application?")
            setPositiveButton("Yes") { _, _ ->
                if (action == "accept") {
                    acceptApplication(applicationId)
                } else {
                    rejectApplication(applicationId)
                }
            }
            setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    // Show Accepted/Rejected layout based on application status
    private fun layoutByStatus(status: String) {
        if (status == "Rejected") {
            binding.statusTextRejected.visibility = View.VISIBLE
            binding.llActionButtons.visibility = View.GONE
            binding.scheduleInterview.visibility = View.GONE
            binding.generateOfferLetter.visibility = View.GONE
        } else if (status == "Accepted") {
            binding.statusTextRejected.visibility = View.VISIBLE
            binding.llActionButtons.visibility = View.GONE
            binding.statusTextRejected.text = getString(R.string.application_accepted)
            binding.generateOfferLetter.visibility = View.VISIBLE
        }
    }

    private fun showFeedbackDialog(interviewSnapshot: DataSnapshot) {
        binding.radioGroupInterviewConducted.visibility = View.GONE
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_feedback, null)
        val editTextFeedback = dialogView.findViewById<EditText>(R.id.editTextFeedback)

        builder.setView(dialogView)
        builder.setPositiveButton("Yes") { dialog, _ ->
            val feedback = editTextFeedback.text.toString().trim()
            interviewSnapshot.ref.child("interviewFeedback").setValue(feedback)
                .addOnSuccessListener {
                    binding.scheduleInterview.text = "Interview Conducted"
                    binding.scheduleInterview.isEnabled = false
                    binding.radioGroupInterviewConducted.visibility = View.GONE
                    dialog.dismiss()
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase", "Failed to update interview conducted status: $e")
                    dialog.dismiss()
                }
        }
        builder.setNegativeButton("Not Now") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun interviewConductStatus(interviewSnapshot: DataSnapshot) {
        val interviewConducted =
            interviewSnapshot.child("interviewConducted").getValue(Boolean::class.java)
        if (interviewConducted != null && !interviewConducted) {
            binding.scheduleInterview.text = "Schedule Interview Again"
            binding.scheduleInterview.isEnabled = true
            binding.radioGroupInterviewConducted.visibility = View.GONE
        } else if (interviewConducted != null && interviewConducted) {
            binding.scheduleInterview.text = "Interview Conducted"
            binding.scheduleInterview.isEnabled = false
            binding.radioGroupInterviewConducted.visibility = View.GONE
        } else {
            binding.scheduleInterview.text = "Interview Time Passed"
            binding.scheduleInterview.isEnabled = false
            binding.radioGroupInterviewConducted.visibility = View.VISIBLE
        }
        binding.interviewDate.visibility = View.GONE
    }
}
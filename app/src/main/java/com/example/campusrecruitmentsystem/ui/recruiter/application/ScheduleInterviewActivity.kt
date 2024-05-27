package com.example.campusrecruitmentsystem.ui.recruiter.application

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.campusrecruitmentsystem.R
import com.example.campusrecruitmentsystem.databinding.ActivityScheduleInterviewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ScheduleInterviewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScheduleInterviewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleInterviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val jobId = intent.getStringExtra("jobId").toString()
        val applicationId = intent.getStringExtra("applicationId").toString()
        val studentId = intent.getStringExtra("studentId").toString()
        binding.backScheduleInterview.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        binding.btnSelectDateTime.setOnClickListener {
            showDateTimePicker()
        }

        binding.radioGroupInterviewType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioButtonPhone -> {
                    binding.layoutPhoneInterview.visibility = View.VISIBLE
                    binding.layoutInPersonInterview.visibility = View.GONE
                }

                R.id.radioButtonInPerson -> {
                    binding.layoutPhoneInterview.visibility = View.GONE
                    binding.layoutInPersonInterview.visibility = View.VISIBLE
                }
            }
        }

        binding.btnScheduleInterview.setOnClickListener {
            scheduleInterview(
                applicationId,
                jobId,
                studentId
            )
        }
    }

    private fun scheduleInterview(applicationId: String, jobId: String, studentId: String) {
        val selectedInterviewType = when (binding.radioGroupInterviewType.checkedRadioButtonId) {
            R.id.radioButtonPhone -> "Phone"
            R.id.radioButtonInPerson -> "In-Person"
            else -> ""
        }

        if (selectedInterviewType.isNotEmpty()) {
            if (selectedInterviewType == "In-Person") {
                val location = binding.edtInterviewLocation.text.toString().trim()
                if (location.isEmpty()) {
                    binding.edtInterviewLocation.error = "Please enter location details"
                    return
                }
            }

            val interviewerName = binding.edTextInterviewerName.text.toString().trim()
            val interviewerEmail = binding.edTextEmail.text.toString().trim()
            val additionalInfo = binding.edTextInfo.text.toString().trim()
            val interviewDateTime = binding.btnSelectDateTime.text.toString().trim()

            if (interviewerName.isEmpty()) {
                binding.edTextInterviewerName.error = "Please enter interviewer name"
                return
            }

            if (interviewerEmail.isEmpty()) {
                binding.edTextEmail.error = "Please enter interviewer email"
                return
            }

            if (interviewDateTime == "Select Date and Time") {
                showToast("Please select interview date and time")
                return
            }

            val interviewsRef = FirebaseDatabase.getInstance().getReference("interviews")
            val interviewId = interviewsRef.push().key
            if (interviewId != null) {
                val interviewDetails = hashMapOf(
                    "interviewId" to interviewId,
                    "studentId" to studentId,
                    "applicationId" to applicationId,
                    "interviewDateTime" to interviewDateTime,
                    "interviewType" to selectedInterviewType,
                    "interviewerName" to interviewerName,
                    "interviewerEmail" to interviewerEmail,
                    "additionalInformation" to additionalInfo,
                    "jobId" to jobId,
                    "recruiterId" to FirebaseAuth.getInstance().currentUser?.uid,
                )

                if (selectedInterviewType == "In-Person") {
                    interviewDetails["location"] = binding.edtInterviewLocation.text.toString().trim()
                } else if (selectedInterviewType == "Phone") {
                    val callType = when (binding.radioGroupMedium.checkedRadioButtonId) {
                        R.id.radioButtonAudioCall -> "Audio Call"
                        R.id.radioButtonVideoCall -> "Video Call"
                        else -> ""
                    }

                    val callMedium = binding.editTextCommunicationPlatform.text.toString().trim()
                    val callJoinDetails = binding.editTextJoiningDetails.text.toString().trim()


                    if (callType.isEmpty()) {
                        showToast("Please select call type")
                        return
                    }

                    if (callMedium.isEmpty()) {
                        showToast("Please enter communication platform")
                        return
                    }

                    if (callJoinDetails.isEmpty()) {
                        showToast("Please enter joining details")
                        return
                    }

                    interviewDetails["callType"] = callType
                    interviewDetails["callMedium"] = callMedium
                    interviewDetails["callJoinDetails"] = callJoinDetails
                }

                interviewsRef.child(interviewId).setValue(interviewDetails)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            showToast("Interview scheduled successfully")
                            finish()
                        } else {
                            showToast("Failed to schedule interview: " + task.exception)
                        }
                    }
            } else {
                showToast("Failed to generate interview ID")
            }
        } else {
            showToast("Please select interview type")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showDateTimePicker() {
        val calendar: Calendar = Calendar.getInstance()

        // Date Picker Dialog
        val datePickerDialog = DatePickerDialog(
            this, { _: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val timePickerDialog = TimePickerDialog(
                    this, { _: TimePicker, hourOfDay: Int, minute: Int ->
                        // Set selected time to calendar
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)

                        // Format selected date and time
                        val dateFormat = SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault())
                        val formattedDateTime = dateFormat.format(calendar.time)

                        // Set formatted date and time to button text
                        binding.btnSelectDateTime.text = formattedDateTime
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
                )
                // Show Time Picker Dialog
                timePickerDialog.show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        // Show Date Picker Dialog
        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        datePickerDialog.show()
    }
}
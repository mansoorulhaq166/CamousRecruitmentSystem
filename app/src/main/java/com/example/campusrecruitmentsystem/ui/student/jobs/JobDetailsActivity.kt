package com.example.campusrecruitmentsystem.ui.student.jobs

import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.campusrecruitmentsystem.R
import com.example.campusrecruitmentsystem.databinding.ActivityJobDetailsBinding
import com.example.campusrecruitmentsystem.models.main.Job
import com.example.campusrecruitmentsystem.ui.student.test.ChoiceTestActivity
import com.example.campusrecruitmentsystem.ui.student.test.ShortTestActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar
import java.util.Locale

class JobDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityJobDetailsBinding
    private var job: Job? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        @Suppress("DEPRECATION")
        job = intent.getParcelableExtra("job")

        if (job != null) {
            binding.textViewJobTitle.text = job!!.title
            binding.textViewCompany.text = job!!.company
            binding.textViewLocation.text = job!!.location
            binding.textViewDescription.text = "Description: ${job!!.description}"
            binding.textViewSalary.text = job!!.salary
            binding.textViewCriteria.text = "Eligibility Criteria: ${job!!.criteria}"

            val deadlineDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val deadlineDate = deadlineDateFormat.parse(job?.deadline ?: "")
            val currentTime = Calendar.getInstance().time

            val timeDifference = deadlineDate?.time?.minus(currentTime.time) ?: 0

            object : CountDownTimer(timeDifference, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val days = millisUntilFinished / (1000 * 60 * 60 * 24)
                    val hours = (millisUntilFinished % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
                    val minutes = (millisUntilFinished % (1000 * 60 * 60)) / (1000 * 60)
                    val seconds = (millisUntilFinished % (1000 * 60)) / 1000

                    val countdownText = "$days d, $hours h, $minutes m, $seconds s"
                    binding.textViewDeadlineCount.text = countdownText
                }

                override fun onFinish() {
                    binding.textViewDeadlineCount.text = getString(R.string.deadline_reached)
                }
            }.start()
        }

        binding.btnApply.setOnClickListener {
            val intent = Intent(this@JobDetailsActivity, JobApplyingActivity::class.java)
            intent.putExtra("job", job)
            startActivity(intent)
        }

        binding.backJobDetails.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnTrackApplication.setOnClickListener {
            val intent = Intent(this@JobDetailsActivity, AppliedJobsDetailActivity::class.java)
            intent.putExtra("jobId", job?.id)
            startActivity(intent)
        }

        // Checking if the student has already applied for this job
        checkJobStatus()
    }

    private fun checkAvailableTests() {
        val jobId = job?.id
        val testRefs = FirebaseDatabase.getInstance().getReference("tests")
        val studentQuery = testRefs.orderByChild("jobId").equalTo(jobId)

        studentQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(studentSnapshot: DataSnapshot) {
                if (studentSnapshot.exists()) {
                    binding.btnTakeTest.visibility = View.VISIBLE
                    binding.bottomProgressbar.visibility = View.GONE
                    binding.rlBottom.visibility = View.VISIBLE

                    val (testId, testType) = getTestIdAndTypeFromSnapshot(studentSnapshot)
                    binding.btnTakeTest.setOnClickListener {
                        if (testType == "Multiple Choice") {
                            val intent =
                                Intent(this@JobDetailsActivity, ChoiceTestActivity::class.java)
                            intent.putExtra("testId", testId)
                            startActivity(intent)
                        } else {
                            val intent =
                                Intent(this@JobDetailsActivity, ShortTestActivity::class.java)
                            intent.putExtra("testId", testId)
                            startActivity(intent)
                        }
                    }
                } else {
                    binding.btnTakeTest.visibility = View.GONE
                    binding.bottomProgressbar.visibility = View.GONE
                    binding.rlBottom.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TAG", "Failed To read Test Data.", error.toException())
                binding.bottomProgressbar.visibility = View.GONE
                binding.rlBottom.visibility = View.VISIBLE
            }
        })
    }

    private fun getTestIdAndTypeFromSnapshot(snapshot: DataSnapshot): Pair<String?, String?> {
        for (childSnapshot in snapshot.children) {
            val testId = childSnapshot.key
            val testType = childSnapshot.child("testType").getValue(String::class.java)
            // Return the first test ID and test type found
            return Pair(testId, testType)
        }
        return Pair(null, null)
    }

    private fun checkJobStatus() {
        binding.progressBar.visibility = View.VISIBLE
        val jobId = job?.id
        val studentId = FirebaseAuth.getInstance().currentUser?.uid

        val applicationReference = FirebaseDatabase.getInstance().getReference("applications")
        val studentQuery = applicationReference.orderByChild("studentId").equalTo(studentId)

        studentQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(studentSnapshot: DataSnapshot) {
                if (studentSnapshot.exists()) {
                    val applied = studentSnapshot.children.any { studentApplication ->
                        val applicationJobId =
                            studentApplication.child("jobId").getValue(String::class.java)
                        applicationJobId == jobId
                    }

                    if (applied) {
                        // Student has applied for this specific job
                        binding.btnApply.text = getString(R.string.applied)
                        binding.btnApply.isEnabled = false
                        binding.btnTrackApplication.visibility = View.VISIBLE
                        binding.btnApply.visibility = View.VISIBLE
                        binding.btnApply.background = ContextCompat.getDrawable(
                            this@JobDetailsActivity,
                            R.drawable.button_disabled
                        )

                        checkAvailableTests()
                    } else {
                        // Student has not applied for this specific job
                        binding.btnApply.text = getString(R.string.apply_now)
                        binding.btnApply.isEnabled = true
                        binding.btnTrackApplication.visibility = View.GONE
                        binding.btnApply.visibility = View.VISIBLE

                        checkAvailableTests()
                    }
                } else {
                    binding.btnApply.text = getString(R.string.apply_now)
                    binding.btnApply.isEnabled = true
                    binding.btnApply.visibility = View.VISIBLE

                    checkAvailableTests()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                checkAvailableTests()
                Log.e("TAG", "Failed to read student application data.", error.toException())
            }
        })
    }
}
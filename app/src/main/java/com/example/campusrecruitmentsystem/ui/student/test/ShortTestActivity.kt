package com.example.campusrecruitmentsystem.ui.student.test

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.campusrecruitmentsystem.R
import com.example.campusrecruitmentsystem.databinding.ActivityShortTestBinding
import com.example.campusrecruitmentsystem.models.recruiter.TestTrueFalse
import com.example.campusrecruitmentsystem.ui.main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale

class ShortTestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShortTestBinding
    private lateinit var databaseReference: DatabaseReference
    private var testId: String? = null
    private var currentQuestionNumber = 1
    private lateinit var auth: FirebaseAuth
    private var userResponses: MutableList<String> = mutableListOf()
    private var isActivityRunning = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShortTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Retrieve the test ID from the intent
        testId = intent.getStringExtra("testId")

        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().reference.child("tests")
        fetchTestData(testId)

        binding.answer1.setTextColor(
            ContextCompat.getColor(
                this@ShortTestActivity,
                R.color.white
            )
        )
        binding.answer2.setTextColor(
            ContextCompat.getColor(
                this@ShortTestActivity,
                R.color.white
            )
        )
        binding.answer1.setBackgroundResource(R.drawable.b)
        binding.answer2.setBackgroundResource(R.drawable.b)

        binding.radioGroupTrueFalse.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                -1 -> {
                    binding.answer1.setTextColor(
                        ContextCompat.getColor(
                            this@ShortTestActivity,
                            R.color.white
                        )
                    )
                    binding.answer2.setTextColor(
                        ContextCompat.getColor(
                            this@ShortTestActivity,
                            R.color.white
                        )
                    )

                    binding.answer1.setBackgroundResource(R.drawable.b)
                    binding.answer2.setBackgroundResource(R.drawable.b)
                }

                R.id.answer1 -> {
                    binding.answer1.setTextColor(
                        ContextCompat.getColor(
                            this@ShortTestActivity,
                            R.color.black
                        )
                    )
                    binding.answer2.setTextColor(
                        ContextCompat.getColor(
                            this@ShortTestActivity,
                            R.color.white
                        )
                    )
                    binding.answer1.setBackgroundResource(R.drawable.a)
                    binding.answer2.setBackgroundResource(R.drawable.b)
                }

                R.id.answer2 -> {
                    binding.answer1.setTextColor(
                        ContextCompat.getColor(
                            this@ShortTestActivity,
                            R.color.white
                        )
                    )
                    binding.answer2.setTextColor(
                        ContextCompat.getColor(
                            this@ShortTestActivity,
                            R.color.black
                        )
                    )
                    binding.answer1.setBackgroundResource(R.drawable.b)
                    binding.answer2.setBackgroundResource(R.drawable.a)
                }
            }
        }
        
    }

    private fun fetchTestData(testId: String?) {
        testId?.let {
            databaseReference.child(testId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            val test: TestTrueFalse? = dataSnapshot.getValue(TestTrueFalse::class.java)
                            // Process the fetched test data
                            test?.let {
                                val totalQuestions = test.questions?.size ?: 0
                                binding.numberOfQuestion.text = "Total Questions: $totalQuestions"

                                val testDurationSeconds = test.testTime?.toLongOrNull() ?: 0
                                startTimer(testDurationSeconds)

                                binding.questionNumber.text = "$currentQuestionNumber. "

                                displayQuestion(test, currentQuestionNumber)

                                binding.submit.setOnClickListener {
                                    val currentResponse = getUserResponse(test)
                                    if (currentResponse != null) {
                                        userResponses.add(currentResponse)
                                        if (currentQuestionNumber >= totalQuestions - 1) {
                                            binding.submit.text = "Submit"
                                        } else {
                                            binding.submit.text = "Next"
                                        }

                                        binding.editTextShortAnswer.text = Editable.Factory.getInstance().newEditable("")
                                        binding.radioGroupTrueFalse.check(-1)
                                        binding.answer1.setTextColor(
                                            ContextCompat.getColor(
                                                this@ShortTestActivity,
                                                R.color.white
                                            )
                                        )
                                        binding.answer2.setTextColor(
                                            ContextCompat.getColor(
                                                this@ShortTestActivity,
                                                R.color.white
                                            )
                                        )
                                        binding.answer1.setBackgroundResource(R.drawable.b)
                                        binding.answer2.setBackgroundResource(R.drawable.b)
                                    } else {
                                        Toast.makeText(
                                            this@ShortTestActivity,
                                            "Please Select an Option",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@setOnClickListener
                                    }

                                    if (currentQuestionNumber < totalQuestions) {
                                        // Increment current question number
                                        currentQuestionNumber++
                                        displayQuestion(test, currentQuestionNumber)
                                        binding.questionNumber.text = "$currentQuestionNumber. "
                                    } else {
                                        // Last question reached, submit the test
                                        submitTest(testId, userResponses)
                                    }
                                }

                                binding.finish.setOnClickListener {
                                    val intent =
                                        Intent(this@ShortTestActivity, MainActivity::class.java)
                                    intent.flags =
                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                }
                            }
                        } else {
                            Toast.makeText(
                                this@ShortTestActivity,
                                "Error: Test Not Found",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(
                            this@ShortTestActivity,
                            "Error: ${databaseError.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }

    private fun getUserResponse(test: TestTrueFalse): String? {
        val type = test.testType
        return if (type == "True/False") {
            val selectedRadioButtonId = binding.radioGroupTrueFalse.checkedRadioButtonId
            when (selectedRadioButtonId) {
                R.id.answer1 -> binding.answer1.text.toString()
                R.id.answer2 -> binding.answer2.text.toString()
                else -> null // No option selected
            }
        } else {
            binding.editTextShortAnswer.text.toString().trim()
        }
    }

    private fun displayQuestion(test: TestTrueFalse, questionNumber: Int) {
        val type = test.testType
        if (type == "True/False") {
            binding.radioGroupTrueFalse.visibility = View.VISIBLE
            binding.shortAnswerLayout.visibility = View.GONE

            val question = test.questions?.getOrNull(questionNumber - 1) // Adjust index
            question?.let {
                binding.question.text = it.question
                binding.answer1.text = "True"
                binding.answer2.text = "False"
            }
        } else {
            binding.radioGroupTrueFalse.visibility = View.GONE
            binding.shortAnswerLayout.visibility = View.VISIBLE
            val question = test.questions?.getOrNull(questionNumber - 1) // Adjust index
            question?.let {
                binding.question.text = it.question
            }
        }
    }

    private fun submitTest(testId: String, responses: MutableList<String>) {
        val userId = auth.currentUser?.uid
        val testSubmissionsRef = FirebaseDatabase.getInstance().getReference("test_submissions")
        val submissionId = testSubmissionsRef.push().key

        // Create a map to store the test submission data
        val submissionData = HashMap<String, Any>()
        submissionData["testId"] = testId
        submissionData["userId"] = userId.toString()
        submissionData["responses"] = responses

        // Store the test submission data in the database under the unique submissionId
        submissionId?.let {
            testSubmissionsRef.child(it).setValue(submissionData)
                .addOnSuccessListener {
                    Toast.makeText(
                        this@ShortTestActivity,
                        "Test Submitted Successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(this@ShortTestActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this@ShortTestActivity,
                        "Test Submission Failed: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun startTimer(durationInSeconds: Long) {
        object : CountDownTimer(durationInSeconds * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                val minutes = secondsRemaining / 60
                val seconds = secondsRemaining % 60
                binding.timer.text =
                    String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                if (!isActivityRunning) return
                binding.timer.text = "00:00"

                if (userResponses.isNotEmpty()) {
                    // Show a dialog informing the user that the time is over
                    AlertDialog.Builder(this@ShortTestActivity)
                        .setTitle("Test Time Over")
                        .setMessage("Your test time is over. The test will be submitted now.")
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                            submitTest(testId.toString(), userResponses)
                        }
                        .setCancelable(false)
                        .show()
                } else {
                    // Handle the case where no responses have been recorded
                    AlertDialog.Builder(this@ShortTestActivity)
                        .setTitle("No Responses")
                        .setMessage("Your test time is over and no responses were recorded.")
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                            val intent = Intent(this@ShortTestActivity, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                        .setCancelable(false)
                        .show()
                }
            }
        }.start()
    }
    override fun onDestroy() {
        super.onDestroy()
        isActivityRunning = false
    }
}
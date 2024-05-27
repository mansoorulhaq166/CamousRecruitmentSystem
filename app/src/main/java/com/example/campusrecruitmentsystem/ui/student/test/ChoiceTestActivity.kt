package com.example.campusrecruitmentsystem.ui.student.test

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.campusrecruitmentsystem.R
import com.example.campusrecruitmentsystem.databinding.ActivityStudentTestBinding
import com.example.campusrecruitmentsystem.models.recruiter.Test
import com.example.campusrecruitmentsystem.ui.main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale

class ChoiceTestActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStudentTestBinding
    private lateinit var databaseReference: DatabaseReference
    private var testId: String? = null
    private var currentQuestionNumber = 1
    private lateinit var auth: FirebaseAuth
    private var userResponses: MutableList<String> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentTestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        // Retrieve the test ID from the intent
        testId = intent.getStringExtra("testId")

        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().reference.child("tests")
        fetchTestData(testId)

        binding.answer1.setTextColor(
            ContextCompat.getColor(
                this@ChoiceTestActivity,
                R.color.white
            )
        )
        binding.answer2.setTextColor(
            ContextCompat.getColor(
                this@ChoiceTestActivity,
                R.color.white
            )
        )
        binding.answer3.setTextColor(
            ContextCompat.getColor(
                this@ChoiceTestActivity,
                R.color.white
            )
        )
        binding.answer4.setTextColor(
            ContextCompat.getColor(
                this@ChoiceTestActivity,
                R.color.white
            )
        )
        binding.answer1.setBackgroundResource(R.drawable.b)
        binding.answer2.setBackgroundResource(R.drawable.b)
        binding.answer3.setBackgroundResource(R.drawable.b)
        binding.answer4.setBackgroundResource(R.drawable.b)

    binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                -1 -> {
                    binding.answer1.setTextColor(
                        ContextCompat.getColor(
                            this@ChoiceTestActivity,
                            R.color.white
                        )
                    )
                    binding.answer2.setTextColor(
                        ContextCompat.getColor(
                            this@ChoiceTestActivity,
                            R.color.white
                        )
                    )
                    binding.answer3.setTextColor(
                        ContextCompat.getColor(
                            this@ChoiceTestActivity,
                            R.color.white
                        )
                    )
                    binding.answer4.setTextColor(
                        ContextCompat.getColor(
                            this@ChoiceTestActivity,
                            R.color.white
                        )
                    )
                    binding.answer1.setBackgroundResource(R.drawable.b)
                    binding.answer2.setBackgroundResource(R.drawable.b)
                    binding.answer3.setBackgroundResource(R.drawable.b)
                    binding.answer4.setBackgroundResource(R.drawable.b)
                }

                R.id.answer1 -> {
                    binding.answer1.setTextColor(
                        ContextCompat.getColor(
                            this@ChoiceTestActivity,
                            R.color.black
                        )
                    )
                    binding.answer2.setTextColor(
                        ContextCompat.getColor(
                            this@ChoiceTestActivity,
                            R.color.white
                        )
                    )
                    binding.answer3.setTextColor(
                        ContextCompat.getColor(
                            this@ChoiceTestActivity,
                            R.color.white
                        )
                    )
                    binding.answer4.setTextColor(
                        ContextCompat.getColor(
                            this@ChoiceTestActivity,
                            R.color.white
                        )
                    )
                    binding.answer1.setBackgroundResource(R.drawable.a)
                    binding.answer2.setBackgroundResource(R.drawable.b)
                    binding.answer3.setBackgroundResource(R.drawable.b)
                    binding.answer4.setBackgroundResource(R.drawable.b)
                }

                R.id.answer2 -> {
                    binding.answer1.setTextColor(
                        ContextCompat.getColor(
                            this@ChoiceTestActivity,
                            R.color.white
                        )
                    )
                    binding.answer2.setTextColor(
                        ContextCompat.getColor(
                            this@ChoiceTestActivity,
                            R.color.black
                        )
                    )
                    binding.answer3.setTextColor(
                        ContextCompat.getColor(
                            this@ChoiceTestActivity,
                            R.color.white
                        )
                    )
                    binding.answer4.setTextColor(
                        ContextCompat.getColor(
                            this@ChoiceTestActivity,
                            R.color.white
                        )
                    )
                    binding.answer1.setBackgroundResource(R.drawable.b)
                    binding.answer2.setBackgroundResource(R.drawable.a)
                    binding.answer3.setBackgroundResource(R.drawable.b)
                    binding.answer4.setBackgroundResource(R.drawable.b)
                }

                R.id.answer3 -> {
                    binding.answer1.setTextColor(
                        ContextCompat.getColor(
                            this@ChoiceTestActivity,
                            R.color.white
                        )
                    )
                    binding.answer2.setTextColor(
                        ContextCompat.getColor(
                            this@ChoiceTestActivity,
                            R.color.white
                        )
                    )
                    binding.answer3.setTextColor(
                        ContextCompat.getColor(
                            this@ChoiceTestActivity,
                            R.color.black
                        )
                    )
                    binding.answer4.setTextColor(
                        ContextCompat.getColor(
                            this@ChoiceTestActivity,
                            R.color.white
                        )
                    )
                    binding.answer1.setBackgroundResource(R.drawable.b)
                    binding.answer2.setBackgroundResource(R.drawable.b)
                    binding.answer3.setBackgroundResource(R.drawable.a)
                    binding.answer4.setBackgroundResource(R.drawable.b)
                }

                R.id.answer4 -> {
                    binding.answer1.setTextColor(
                        ContextCompat.getColor(
                            this@ChoiceTestActivity,
                            R.color.white
                        )
                    )
                    binding.answer2.setTextColor(
                        ContextCompat.getColor(
                            this@ChoiceTestActivity,
                            R.color.white
                        )
                    )
                    binding.answer3.setTextColor(
                        ContextCompat.getColor(
                            this@ChoiceTestActivity,
                            R.color.white
                        )
                    )
                    binding.answer4.setTextColor(
                        ContextCompat.getColor(
                            this@ChoiceTestActivity,
                            R.color.black
                        )
                    )
                    binding.answer1.setBackgroundResource(R.drawable.b)
                    binding.answer2.setBackgroundResource(R.drawable.b)
                    binding.answer3.setBackgroundResource(R.drawable.b)
                    binding.answer4.setBackgroundResource(R.drawable.a)
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
                            val test = dataSnapshot.getValue(Test::class.java)
                            // Process the fetched test data
                            test?.let {
                                val totalQuestions = test.questions?.size ?: 0
                                binding.numberOfQuestion.text = "Total Questions: $totalQuestions"

                                val testDurationSeconds = test.testTime?.toLongOrNull() ?: 0
                                startTimer(testDurationSeconds)

                                binding.questionNumber.text = "$currentQuestionNumber. "

                                displayQuestion(test, currentQuestionNumber)

                                if (currentQuestionNumber == totalQuestions) {
                                    binding.submit.text = "Submit"
                                } else {
                                    binding.submit.text = "Next"
                                }

                                binding.submit.setOnClickListener {
                                    val currentResponse = getUserResponse()
                                    if (currentResponse != null) {
                                        userResponses.add(currentResponse)
                                    } else {
                                        Toast.makeText(
                                            this@ChoiceTestActivity,
                                            "Please Select an Option",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@setOnClickListener
                                    }

                                    if (currentQuestionNumber < totalQuestions) {
                                        // Increment current question number
                                        currentQuestionNumber++
                                        displayQuestion(test, currentQuestionNumber)
                                        binding.questionNumber.text =
                                            currentQuestionNumber.toString()
                                    } else {
                                        // Last question reached, submit the test
                                        submitTest(testId, userResponses)
                                    }
                                }

                                binding.finish.setOnClickListener {
                                    val intent =
                                        Intent(this@ChoiceTestActivity, MainActivity::class.java)
                                    intent.flags =
                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                }
                            }
                        } else {
                            Toast.makeText(
                                this@ChoiceTestActivity,
                                "Error: Test Not Found",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(
                            this@ChoiceTestActivity,
                            "Error: ${databaseError.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }

    private fun getUserResponse(): String? {
        val selectedRadioButtonId = binding.radioGroup.checkedRadioButtonId
        return when (selectedRadioButtonId) {
            R.id.answer1 -> binding.answer1.text.toString()
            R.id.answer2 -> binding.answer2.text.toString()
            R.id.answer3 -> binding.answer3.text.toString()
            R.id.answer4 -> binding.answer4.text.toString()
            else -> null // No option selected
        }
    }

    private fun displayQuestion(test: Test, questionNumber: Int) {
        val question = test.questions?.getOrNull(questionNumber - 1) // Adjust index
        question?.let {
            binding.question.text = it.question
            binding.answer1.text = it.choiceA
            binding.answer2.text = it.choiceB
            binding.answer3.text = it.choiceC
            binding.answer4.text = it.choiceD
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
                        this@ChoiceTestActivity,
                        "Test Submitted Successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(this@ChoiceTestActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this@ChoiceTestActivity,
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
                // Timer finished, handle accordingly
                binding.timer.text = "00:00"

                if (userResponses.isNotEmpty()) {
                    // Show a dialog informing the user that the time is over
                    AlertDialog.Builder(this@ChoiceTestActivity)
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
                    AlertDialog.Builder(this@ChoiceTestActivity)
                        .setTitle("No Responses")
                        .setMessage("Your test time is over and no responses were recorded.")
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                            val intent = Intent(this@ChoiceTestActivity, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                        .setCancelable(false)
                        .show()
                }
            }
        }.start()
    }
}
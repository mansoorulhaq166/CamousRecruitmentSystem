package com.example.campusrecruitmentsystem.ui.student

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.campusrecruitmentsystem.databinding.ActivityInterviewStatusBinding
import com.example.campusrecruitmentsystem.models.main.Interview
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class InterviewStatusActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInterviewStatusBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInterviewStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val interviewId = intent.getStringExtra("interviewId").toString()
        binding.backDetailsInterview.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        geInterviewDetails(interviewId)
    }

    private fun geInterviewDetails(interviewId: String) {

        val interviewsRef = FirebaseDatabase.getInstance().getReference("interviews")
        val interviewQuery = interviewsRef.orderByChild("interviewId")
            .equalTo(interviewId)

        interviewQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (interviewSnapshot in snapshot.children) {
                    val interviewDetails = interviewSnapshot.getValue(Interview::class.java)
                    if (interviewDetails != null) {
                        val name = interviewDetails.interviewerName
                        val email = interviewDetails.interviewerEmail
                        val date = interviewDetails.interviewDateTime
                        val location = interviewDetails.location
                        val additionalInformation = interviewDetails.additionalInformation
                        val interviewType = interviewDetails.interviewType
                        val callType = interviewDetails.callType
                        val callMedium = interviewDetails.callMedium
                        val callJoinDetails = interviewDetails.callJoinDetails
                        val invitationStatus = interviewDetails.invitationStatus

                        when (invitationStatus) {
                            "accepted" -> {
                                binding.invitationStatus.text = "Interview Invitation Accepted"
                                binding.invitationStatus.visibility = View.VISIBLE
                                binding.llInvitationButtons.visibility = View.GONE
                            }
                            "rejected" -> {
                                binding.invitationStatus.text = "Interview Invitation Rejected"
                                binding.invitationStatus.visibility = View.VISIBLE
                                binding.llInvitationButtons.visibility = View.GONE
                            }
                            else -> {
                                binding.invitationStatus.visibility = View.GONE
                                binding.llInvitationButtons.visibility = View.VISIBLE
                            }
                        }

                        // Update UI with the fetched details
                        binding.textViewInterviewerName.text = name
                        binding.textViewInterviewerEmail.text = email
                        binding.textInterviewDateAndTime.text = "Interview Date: $date"
                        binding.textViewInterviewNotes.text = additionalInformation
                        binding.textViewInterviewType.text = interviewType

                        if (interviewType == "In-Person") {
                            binding.llPhoneType.visibility = View.GONE
                            binding.textViewInterviewLocation.text = "Location: $location"
                        } else if (interviewType == "Phone") {
                            binding.llPhoneType.visibility = View.VISIBLE
                            binding.textViewInterviewLocation.visibility = View.GONE

                            binding.textViewInterviewCallType.text = "Call Type: $callType"
                            binding.textViewInterviewCallMedium.text = "Call Medium: $callMedium"
                            binding.textViewInterviewCallJoinDetails.text =
                                "Joining Details: $callJoinDetails"
                        }

                        binding.btnRejectInterview.setOnClickListener {
                            val interviewRef =
                                FirebaseDatabase.getInstance().getReference("interviews")
                                    .child(interviewId)
                            interviewRef.child("invitationStatus").setValue("rejected")
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this@InterviewStatusActivity,
                                        "Interview rejected.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    binding.invitationStatus.text = "Interview Invitation Rejected"
                                    binding.invitationStatus.visibility = View.VISIBLE
                                    binding.llInvitationButtons.visibility = View.GONE
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        this@InterviewStatusActivity,
                                        "Failed to reject interview: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }

                        binding.btnAcceptInterview.setOnClickListener {
                            val interviewRef =
                                FirebaseDatabase.getInstance().getReference("interviews")
                                    .child(interviewId)
                            interviewRef.child("invitationStatus").setValue("accepted")
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this@InterviewStatusActivity,
                                        "Interview Accepted.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    binding.invitationStatus.text = "Interview Invitation Accepted"
                                    binding.invitationStatus.visibility = View.VISIBLE
                                    binding.llInvitationButtons.visibility = View.GONE
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        this@InterviewStatusActivity,
                                        "Failed to accepted interview: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                        binding.progressBar.visibility = View.GONE
                        binding.llInterviewStatus.visibility = View.VISIBLE
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@InterviewStatusActivity,
                    "Failed to load Interview Details: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
                binding.progressBar.visibility = View.GONE
                binding.llInterviewStatus.visibility = View.VISIBLE
            }

        })
    }
}
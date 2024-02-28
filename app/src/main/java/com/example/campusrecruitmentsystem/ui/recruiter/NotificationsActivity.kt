package com.example.campusrecruitmentsystem.ui.recruiter

import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusrecruitmentsystem.adapters.NotificationAdapter
import com.example.campusrecruitmentsystem.databinding.ActivityNotificationsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NotificationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationsBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var notificationAdapter: NotificationAdapter
    private val notificationsList: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        databaseReference =
            FirebaseDatabase.getInstance().getReference("applications")
        databaseReference.orderByChild("applicationDate")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (applicationSnapshot in snapshot.children) {
                            val jobId =
                                applicationSnapshot.child("jobId").getValue(String::class.java)
                            val studentId =
                                applicationSnapshot.child("studentId").getValue(String::class.java)

                            fetchJobTitle(jobId, studentId)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("NotificationsActivity", "Error fetching applications: ${error.message}")
                    binding.progressBar.visibility = View.GONE
                    binding.textViewNoNotifications.visibility = View.VISIBLE
                }
            })

        notificationAdapter = NotificationAdapter(notificationsList)
        binding.recyclerViewNotifications.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewNotifications.adapter = notificationAdapter
    }

    private fun fetchJobTitle(jobId: String?, studentId: String?) {
        val recruiterId = auth.currentUser?.uid

        FirebaseDatabase.getInstance().getReference("jobs").child(jobId!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val jobRecruiterId =
                            snapshot.child("recruiter_id").getValue(String::class.java)
                        val jobTitle = snapshot.child("title").getValue(String::class.java)

                        if (jobRecruiterId == recruiterId && jobTitle != null) {
                           displayNotificationMessage(jobTitle)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("NotificationsActivity", "Error fetching job title: ${error.message}")
                    binding.progressBar.visibility = View.GONE
                    binding.textViewNoNotifications.visibility = View.VISIBLE
                }
            })
    }

    private fun displayNotificationMessage(jobTitle: String?) {
        if (jobTitle != null) {
            binding.progressBar.visibility = View.GONE
            binding.textViewNoNotifications.visibility = View.GONE

            val boldJobTitle = SpannableString(jobTitle)
            boldJobTitle.setSpan(StyleSpan(Typeface.BOLD_ITALIC), 0, jobTitle.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            val notificationMessage = "New job application received for $boldJobTitle"
            notificationsList.add(notificationMessage)
            notificationAdapter.notifyDataSetChanged()
        } else {
            binding.progressBar.visibility = View.GONE
            binding.textViewNoNotifications.visibility = View.VISIBLE
        }
    }
}
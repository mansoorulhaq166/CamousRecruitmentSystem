package com.example.campusrecruitmentsystem.adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.campusrecruitmentsystem.R
import com.example.campusrecruitmentsystem.models.main.ApplicationDetails
import com.example.campusrecruitmentsystem.ui.recruiter.application.ApplicationDetailsActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ApplicationsAdapter(private val appliedApplications: List<ApplicationDetails>) :
    RecyclerView.Adapter<ApplicationsAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewStudentName: TextView = itemView.findViewById(R.id.textViewStudentName)
        val textViewJobTitle: TextView = itemView.findViewById(R.id.textViewJobTitle)
        val textViewApplicationDate: TextView = itemView.findViewById(R.id.textViewApplicationDate)
        val textApplicationStatus: TextView = itemView.findViewById(R.id.itemApplicationStatus)
        val btnViewDetails: TextView = itemView.findViewById(R.id.btnViewDetails)
        var userId: String? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_applied_application, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appliedApplication = appliedApplications[position]

        holder.userId = appliedApplication.studentId!!
        val status = appliedApplication.status
        if (status != null) {
            if (status.isNotEmpty()) {
                val textStatus = "Status: $status"
                holder.textApplicationStatus.text = textStatus
            }
        }
        fetchStudentName(appliedApplication.studentId) { studentName ->
            holder.textViewStudentName.text = studentName

        }

        fetchJobTitle(appliedApplication.jobId!!) { jobTitle ->
            holder.textViewJobTitle.text = jobTitle
        }


        holder.textViewApplicationDate.text = appliedApplication.applicationDate

        holder.btnViewDetails.setOnClickListener {
            val intent = Intent(holder.itemView.context, ApplicationDetailsActivity::class.java)
            intent.putExtra("application", appliedApplication.applicationId)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return appliedApplications.size
    }

    private fun fetchStudentName(studentId: String, callback: (String) -> Unit) {
        FirebaseDatabase.getInstance().getReference("users").child(studentId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val studentName = snapshot.child("name").getValue(String::class.java)
                        callback(studentName.orEmpty())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ApplicationsAdapter", "Error fetching student name: ${error.message}")
                }
            })
    }

    private fun fetchJobTitle(jobId: String, callback: (String) -> Unit) {
        FirebaseDatabase.getInstance().getReference("jobs").child(jobId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val jobTitle = snapshot.child("title").getValue(String::class.java)
                        callback(jobTitle.orEmpty())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ApplicationsAdapter", "Error fetching job title: ${error.message}")
                }
            })
    }
}